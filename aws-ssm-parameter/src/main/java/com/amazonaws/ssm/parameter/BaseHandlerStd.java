package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private static final String RESOURCE_MISSING_CFN_MESSAGE = "Resource of type '%s' with identifier '%s' was not found.";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                       final ResourceHandlerRequest<ResourceModel> request,
                                                                       final CallbackContext callbackContext,
                                                                       final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(SSMClientBuilder::getClient),
                logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SsmClient> client,
            Logger logger);

    protected ProgressEvent<ResourceModel, CallbackContext> handleError(
            final String operation,
            final Exception exception,
            final ResourceModel model,
            final CallbackContext callbackContext,
            final Logger logger) {
        // All InternalServerExceptions are retryable
        if (exception instanceof TooManyUpdatesException) {
            logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
            throw new CfnThrottlingException(operation, exception);
        }

        if (exception instanceof ParameterNotFoundException) {
            // If operation is for DeleteHandler throwing CfnNotFoundException would not work,
            // so throw a failed status with an error code for NotFound.
            // Reference: https://t.corp.amazon.com/P35512867/communication
            if (operation.equalsIgnoreCase("aws-ssm-parameter::resource-delete")) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.NotFound)
                        .message(String.format(RESOURCE_MISSING_CFN_MESSAGE, ResourceModel.TYPE_NAME, model.getName()))
                        .build();
            } else {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName());
            }
        }

        throw new CfnGeneralServiceException(operation, exception);
    }

    /**
     * If your resource requires some form of stabilization (e.g. service does not provide strong
     * consistency), you will need to ensure that your code accounts for any potential issues, so that
     * a subsequent read/update requests will not cause any conflicts (e.g.
     * NotFoundException/InvalidRequestException) for more information ->
     * https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
     *
     * @param putParameterRequest the aws service request to create a resource
     * @param proxyClient the aws service response to create a resource
     * @param proxyClient the aws service client to make the call
     * @param resourceModel resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    protected static boolean stabilize(
            final PutParameterRequest putParameterRequest,
            final PutParameterResponse putParameterResponse,
            final ProxyClient<SsmClient> proxyClient,
            final ResourceModel resourceModel,
            final CallbackContext callbackContext
    ) {
        final GetParametersResponse response =  proxyClient.injectCredentialsAndInvokeV2(Translator.getParametersRequest(resourceModel), proxyClient.client()::getParameters);
        if (response.parameters() != null && response.parameters().get(0).version() == putParameterResponse.version()) {
            return true;
        }

        return false;
    }
}
