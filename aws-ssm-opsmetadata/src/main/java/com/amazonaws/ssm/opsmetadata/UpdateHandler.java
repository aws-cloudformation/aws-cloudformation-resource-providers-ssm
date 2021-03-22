package com.amazonaws.ssm.opsmetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ssm.opsmetadata.translator.request.RequestTranslator;
import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataNotFoundException;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private static final String OPERATION = "UpdateOpsMetadata";
    private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private Logger logger;
    private final RequestTranslator requestTranslator;

    public UpdateHandler() {
        this.requestTranslator = new RequestTranslator();
    }

    public UpdateHandler(final RequestTranslator requestTranslator) {
        this.requestTranslator = requestTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                // First validate the resource actually exists per the contract requirements
                // https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
                .then(progress ->
                        proxy.initiate("aws-ssm-opsmetadata::validate-resource-exists", proxyClient, model, callbackContext)
                                .translateToServiceRequest((resourceModel) -> requestTranslator.getOpsMetadataRequest(resourceModel))
                                .makeServiceCall(this::validateResourceExists)
                                .progress())

                .then(progress ->
                        proxy.initiate("aws-ssm-opsmetadata::resource-update", proxyClient, model, callbackContext)
                                .translateToServiceRequest((resourceModel) -> requestTranslator.updateOpsMetadataRequest(resourceModel))
                                .makeServiceCall(this::updateResource)
                                .progress())
                .then(progress -> handleTagging(proxy, proxyClient, progress, model, request.getDesiredResourceTags(), request.getPreviousResourceTags()))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private GetOpsMetadataResponse validateResourceExists(GetOpsMetadataRequest getOpsMetadataRequest,
                                                          ProxyClient<SsmClient> proxyClient) {
        GetOpsMetadataResponse getOpsMetadataResponse;

        try {
            getOpsMetadataResponse = proxyClient.injectCredentialsAndInvokeV2(getOpsMetadataRequest,
                    proxyClient.client()::getOpsMetadata);
        } catch (OpsMetadataNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, getOpsMetadataRequest.opsMetadataArn());
        }
        return getOpsMetadataResponse;
    }

    private UpdateOpsMetadataResponse updateResource(final UpdateOpsMetadataRequest updateOpsMetadataRequest,
                                                     final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(updateOpsMetadataRequest, proxyClient.client()::updateOpsMetadata);
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

    private ProgressEvent<ResourceModel,CallbackContext> handleTagging(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<SsmClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final ResourceModel resourceModel,
            final Map<String, String> desiredResourceTags,
            final Map<String, String> previousResourceTags) {

        final Set<Tag> currentTags = new HashSet<>(requestTranslator.translateTagsToSdk(desiredResourceTags));
        final Set<Tag> existingTags = new HashSet<>(requestTranslator.translateTagsToSdk(previousResourceTags));
        // Remove tags with aws prefix as they should not be modified once attached
        existingTags.removeIf(tag -> tag.key().startsWith("aws"));

        final Set<Tag> setTagsToRemove = Sets.difference(existingTags, currentTags);
        final Set<Tag> setTagsToAdd = Sets.difference(currentTags, existingTags);

        final List<Tag> tagsToRemove = setTagsToRemove.stream().collect(Collectors.toList());
        final List<Tag> tagsToAdd = setTagsToAdd.stream().collect(Collectors.toList());

        // Deletes tags only if tagsToRemove is not empty.
        if (!CollectionUtils.isNullOrEmpty(tagsToRemove)) proxy.injectCredentialsAndInvokeV2(
                requestTranslator.removeTagsFromResourceRequest(resourceModel, tagsToRemove), proxyClient.client()::removeTagsFromResource);

        // Adds tags only if tagsToAdd is not empty.
        if (!CollectionUtils.isNullOrEmpty(tagsToAdd)) proxy.injectCredentialsAndInvokeV2(
                requestTranslator.addTagsToResourceRequest(resourceModel, tagsToAdd), proxyClient.client()::addTagsToResource);

        return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
    }
}
