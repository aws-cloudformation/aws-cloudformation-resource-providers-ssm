package com.amazonaws.ssm.parameter;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        if(model.getDataType() != null && model.getDataType() == Constants.AWS_EC2_IMAGE_DATATYPE) {
            return ProgressEvent.progress(model, callbackContext)
                    .then(progress ->
                            proxy.initiate("aws-ssm-parameter::resource-update", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(Translator::updatePutParameterRequest)
                                    .backoffDelay(
                                            Constant.of()
                                                    .timeout(Duration.ofMinutes(5))
                                                    .delay(Duration.ofSeconds(30))
                                                    .build())
                                    .makeServiceCall((createPutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createPutParameterRequest, proxyInvocation.client()::putParameter))
                                    .stabilize(BaseHandlerStd::stabilize)
                                    .handleError((putParameterRequest, exception, _proxyClient, _model, _callbackContext) -> handleError("aws-ssm-parameter::resource-update", exception, _model, _callbackContext, logger))
                                    .progress())
                    .then(progress -> tagResources(proxy, proxyClient, progress, request.getDesiredResourceTags(), callbackContext, logger))
                    .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-update", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::updatePutParameterRequest)
                                .makeServiceCall((createPutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createPutParameterRequest, proxyInvocation.client()::putParameter))
                                .handleError((putParameterRequest, exception, _proxyClient, _model, _callbackContext) -> handleError("aws-ssm-parameter::resource-update", exception, _model, _callbackContext, logger))
                                .progress())
                .then(progress -> tagResources(proxy, proxyClient, progress, request.getDesiredResourceTags(), callbackContext, logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel,CallbackContext> tagResources(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<SsmClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Map<String, String> desiredResourceTags,
            final CallbackContext callbackContext,
            final Logger logger) {
        return proxy.initiate("aws-ssm-parameter::resource-update-tag-key", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::listTagsForResourceRequest)
                .makeServiceCall((listResourceTagsRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(listResourceTagsRequest, proxyInvocation.client()::listTagsForResource))
                .done((listResourceTagsRequest, listResourceTagsResponse, proxyInvocation, resourceModel, context) -> {
                    final Set<Tag> currentTags = new HashSet<>(Translator.translateTagsToSdk(desiredResourceTags));
                    logger.log("Current Tags are: " + currentTags.toString());

                    final Set<Tag> existingTags = new HashSet<>(listResourceTagsResponse.tagList());
                    existingTags.removeIf(tag -> tag.key().startsWith("aws"));
                    logger.log("Existing Tags are: " + existingTags.toString());


                    final Set<Tag> setTagsToRemove = Sets.difference(existingTags, currentTags);
                    final Set<Tag> setTagsToAdd = Sets.difference(currentTags, existingTags);

                    final List<Tag> tagsToRemove = setTagsToRemove.stream().collect(Collectors.toList());
                    final List<Tag> tagsToAdd = setTagsToAdd.stream().collect(Collectors.toList());

                    proxyInvocation.injectCredentialsAndInvokeV2(Translator.removeTagsFromResourceRequest(resourceModel.getName(),tagsToRemove), proxyInvocation.client()::removeTagsFromResource);
                    proxyInvocation.injectCredentialsAndInvokeV2(Translator.addTagsToResourceRequest(resourceModel.getName(), tagsToAdd), proxyInvocation.client()::addTagsToResource);
                    return ProgressEvent.progress(resourceModel, callbackContext);
                });
    }
}
