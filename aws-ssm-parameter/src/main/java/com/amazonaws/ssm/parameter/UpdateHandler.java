package com.amazonaws.ssm.parameter;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SsmClient> proxyClient,
            Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("ssm::update-parameter-group", proxyClient, model, callbackContext)
                .request(Translator::updatePutParameterRequest)
                .call((updatePutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(updatePutParameterRequest, proxyInvocation.client()::putParameter))
                .progress()
                .then(progress -> tagResources(proxy, proxyClient, progress, request.getDesiredResourceTags(), logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel,CallbackContext> tagResources(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<SsmClient> proxyClient,
            ProgressEvent<ResourceModel, CallbackContext> progress,
            Map<String, String> desiredResourceTags, Logger logger) {
        return proxy.initiate("ssm::update-parameter-tag-key", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .request(Translator::listTagsForResourceRequest)
                .call((listResourceTagsRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(listResourceTagsRequest, proxyInvocation.client()::listTagsForResource))
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
                    return ProgressEvent.defaultSuccessHandler(resourceModel);
                });
    }
}
