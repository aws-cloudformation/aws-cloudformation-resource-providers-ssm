package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
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
        // if invalid parameters list is not empty return false as the validation for
        // DataType has not been completed and the parameter has not been created yet.
        if(response.invalidParameters().size() != 0) {
            return false;
        }
        return (response != null &&
                response.parameters() != null &&
                response.parameters().get(0).version() == putParameterResponse.version());
    }
}
