package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {
    private static final String OPERATION = "ReadParameter";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        // read model only contains primary key
        ResourceModel model = request.getDesiredResourceState();

        try {
            // Get info from getParameters
            GetParametersResponse getParametersResponse = proxyClient
                    .injectCredentialsAndInvokeV2(Translator.getParametersRequest(model), proxyClient.client()::getParameters);
            if (getParametersResponse.parameters().size() == 0) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getName());
            }
            Parameter parameter = getParametersResponse.parameters().stream().findFirst().get();
            model.setName(parameter.name());
            model.setType(parameter.typeAsString());
            model.setValue(parameter.value());
            model.setDataType(parameter.dataType());
        } catch (InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
