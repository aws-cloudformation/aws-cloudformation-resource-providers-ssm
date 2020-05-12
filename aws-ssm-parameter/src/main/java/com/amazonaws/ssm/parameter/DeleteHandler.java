package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::deleteParameterRequest)
                                .makeServiceCall((deleteParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(deleteParameterRequest, proxyInvocation.client()::deleteParameter))
                                .handleError((putParameterRequest, exception, _proxyClient, _model, _callbackContext) -> handleError("aws-ssm-parameter::resource-delete", exception, _model, _callbackContext, logger))
                                .success());
    }
}
