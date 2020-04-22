package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SsmClient> proxyClient,
            Logger logger) {
        return proxy.initiate("ssm::read-parameter-group", proxyClient, request.getDesiredResourceState(), callbackContext)
                .request(Translator::getParametersRequest)
                .call((getParametersRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(getParametersRequest, proxyInvocation.client()::getParameters))
                .done((getParametersRequest, getParametersResponse, proxyInvocation, resourceModel, context) -> {
                    if(getParametersResponse.parameters().size() == 0) {
                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getName());
                    }
                    final Parameter parameter = getParametersResponse.parameters().stream().findFirst().get();

                    return ProgressEvent.success(ResourceModel.builder()
                            .name(parameter.name())
                            .type(parameter.typeAsString())
                            .value(parameter.value()).build(), context);
                });
    }
}
