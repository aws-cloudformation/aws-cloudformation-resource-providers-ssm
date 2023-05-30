package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.TerminalException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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
        TagHelper tagHelper = new TagHelper();

        if(model.getType().equalsIgnoreCase(ParameterType.SECURE_STRING.toString())) {
            String message = String.format("SSM Parameters of type %s cannot be updated using CloudFormation", ParameterType.SECURE_STRING);
            return ProgressEvent.defaultFailureHandler(new TerminalException(message),
                    HandlerErrorCode.InvalidRequest);
        }

        ProgressEvent<ResourceModel, CallbackContext> progressEvent = ProgressEvent.progress(model, callbackContext);

        if (TagHelper.shouldUpdateTags(request)) {
            Map<String, String> previousTag = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> newTag = TagHelper.getNewDesiredTags(request);
            Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTag, newTag);
            Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTag, newTag);
            if (tagsToAdd != null && tagsToAdd.size() > 0) {
                progressEvent = progressEvent
                        .then(progress -> tagHelper.tagResource(proxy, proxyClient, model, request, callbackContext, tagsToAdd, logger));
            }
            if (tagsToRemove != null && tagsToRemove.size() > 0) {
                progressEvent = progressEvent
                        .then(progress -> tagHelper.untagResource(proxy, proxyClient, model, request, callbackContext, tagsToRemove, logger));
            }
        }

        // Call PutParameter only if previousResourceProperties is not the same as currentResourceProperties
        // Reference ticket - https://t.corp.amazon.com/D61282592/communication
        // First validate the resource actually exists per the contract requirements
        // https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
        if (!areResourceModelSame(request.getDesiredResourceState(), request.getPreviousResourceState())) {
            progressEvent = progressEvent
                    .then(progress ->
                            proxy.initiate("aws-ssm-parameter::validate-resource-exists", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(Translator::getParametersRequest)
                                    .makeServiceCall(this::validateResourceExists)
                                    .progress())

                    .then(progress ->
                            proxy.initiate("aws-ssm-parameter::resource-update", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(Translator::updatePutParameterRequest)
                                    .backoffDelay(getBackOffDelay(model))
                                    .makeServiceCall(this::updateResource)
                                    .stabilize(BaseHandlerStd::stabilize)
                                    .progress());
        }

        return progressEvent.then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Helper method to check if the previous and current resource model are the same or not except tags.
     * @param currentResourceModel currentResourceModel
     * @param previousResourceModel previousResourceModel
     * @return boolean indicating if previous and current resource model are the same or not
     */
    private boolean areResourceModelSame(final ResourceModel currentResourceModel,
                                         final ResourceModel previousResourceModel) {
        if (previousResourceModel == null) {
            return false;
        }
        if (!currentResourceModel.getType().equals(previousResourceModel.getType())) {
            return false;
        }
        if (!currentResourceModel.getValue().equals(previousResourceModel.getValue())) {
            return false;
        }
        if ((currentResourceModel.getDescription() == null && previousResourceModel.getDescription() != null) ||
                (currentResourceModel.getDescription() != null && !currentResourceModel.getDescription().equals(previousResourceModel.getDescription()))) {
            return false;
        }
        if ((currentResourceModel.getPolicies() == null && previousResourceModel.getPolicies() != null) ||
                (currentResourceModel.getPolicies() != null && !currentResourceModel.getPolicies().equals(previousResourceModel.getPolicies()))) {
            return false;
        }
        if ((currentResourceModel.getAllowedPattern() == null && previousResourceModel.getAllowedPattern() != null) ||
                (currentResourceModel.getAllowedPattern() != null && !currentResourceModel.getAllowedPattern().equals(previousResourceModel.getAllowedPattern()))) {
            return false;
        }
        if ((currentResourceModel.getTier() == null && previousResourceModel.getTier() != null) ||
                (currentResourceModel.getTier() != null && !currentResourceModel.getTier().equals(previousResourceModel.getTier()))) {
            return false;
        }
        if ((currentResourceModel.getDataType() == null && previousResourceModel.getDataType() != null) ||
                (currentResourceModel.getDataType() != null && !currentResourceModel.getDataType().equals(previousResourceModel.getDataType()))) {
            return false;
        }
        return true;
    }

    private GetParametersResponse validateResourceExists(GetParametersRequest getParametersRequest, ProxyClient<SsmClient> proxyClient) {
        GetParametersResponse getParametersResponse;

        getParametersResponse = proxyClient.injectCredentialsAndInvokeV2(getParametersRequest,proxyClient.client()::getParameters);
        if (getParametersResponse.invalidParameters().size() != 0) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, getParametersRequest.names().get(0));
        }

        return getParametersResponse;
    }

    private PutParameterResponse updateResource(final PutParameterRequest putParameterRequest,
                                                final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(putParameterRequest, proxyClient.client()::putParameter);
        } catch (final ParameterAlreadyExistsException exception) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, putParameterRequest.name());
        } catch (final IllegalArgumentException exception) {
            throw new CfnInvalidRequestException(OPERATION, exception);
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

}
