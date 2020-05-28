package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DeleteParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private static final String OPERATION = "DeleteParameter";
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

        return proxy.initiate("aws-ssm-parameter::resource-delete", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::deleteParameterRequest)
                .makeServiceCall(this::deleteResource)
                .success();
    }

    private DeleteParameterResponse deleteResource(final DeleteParameterRequest deleteParameterRequest,
                                                   final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(deleteParameterRequest, proxyClient.client()::deleteParameter);
        } catch (final TooManyUpdatesException exception) {
            logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
            throw new CfnThrottlingException(OPERATION, exception);
        } catch (final ParameterNotFoundException exception) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteParameterRequest.name());
        }
    }
}
