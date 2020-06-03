package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
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
        } catch (final ParameterAlreadyExistsException exception) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, putParameterRequest.name());
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        } catch (final AmazonServiceException exception) {
            final Integer errorStatus = exception.getStatusCode();
            final String errorCode = exception.getErrorCode();
            if (errorStatus >= Constants.ERROR_STATUS_CODE_400 && errorStatus < Constants.ERROR_STATUS_CODE_500) {
                if (THROTTLING_ERROR_CODES.contains(errorCode)) {
                    logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
                    throw new CfnThrottlingException(OPERATION, exception);
                }
            }
            throw new CfnGeneralServiceException(OPERATION, exception);
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
                    final Set<Tag> existingTags = new HashSet<>(listResourceTagsResponse.tagList());
                    // Remove tags with aws prefix as they should not be modified once attached
                    existingTags.removeIf(tag -> tag.key().startsWith("aws"));

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
