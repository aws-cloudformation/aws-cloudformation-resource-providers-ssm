package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

public class ReadHandler extends BaseHandlerStd {
    private static final String OPERATION = "ReadParameter";

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        return proxy.initiate("aws-ssm-parameter::resource-read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::getParametersRequest)
                .makeServiceCall(this::ReadResource)
                .done((getParametersRequest, getParametersResponse, proxyInvocation, resourceModel, context) -> {
                    if(getParametersResponse.parameters().size() == 0) {
                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getName());
                    }
                    final Parameter parameter = getParametersResponse.parameters().stream().findFirst().get();

                    return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                            .name(parameter.name())
                            .type(parameter.typeAsString())
                            .value(parameter.value()).build());
                });
    }

    private GetParametersResponse ReadResource(final GetParametersRequest getParametersRequest,
                                               final ProxyClient<SsmClient> proxyClient) {
        try{
            return proxyClient.injectCredentialsAndInvokeV2(getParametersRequest, proxyClient.client()::getParameters);
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }
    }
}
