package com.amazonaws.ssm.parameter;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
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
    private static final String OPERATION = "PutParameter";
    private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-update", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::updatePutParameterRequest)
                                .backoffDelay(getBackOffDelay(model))
                                .makeServiceCall(this::updateResource)
                                .stabilize(BaseHandlerStd::stabilize)
                                .progress())
                .then(progress -> tagResources(proxy, proxyClient, progress, request.getDesiredResourceTags(), callbackContext, logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private PutParameterResponse updateResource(final PutParameterRequest putParameterRequest,
                                                final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(putParameterRequest, proxyClient.client()::putParameter);
        } catch (final TooManyUpdatesException exception) {
            logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
            throw new CfnThrottlingException(OPERATION, exception);
        } catch (final ParameterNotFoundException exception) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, putParameterRequest.name());
        }
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
