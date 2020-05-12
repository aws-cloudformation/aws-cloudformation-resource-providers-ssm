package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        // initiate the call context.
        return proxy.initiate("aws-ssm-parameter::resource-list", proxyClient, model, callbackContext)
                // transform Resource model properties to ListFindingsFilters API
                .translateToServiceRequest((resourceModel) -> Translator.describeParametersRequest(request.getNextToken()))
                // Make a service call. Handler does not worry about credentials, they are auto injected
                .makeServiceCall((describeParametersRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(describeParametersRequest, proxyInvocation.client()::describeParameters))
                .handleError((_request, _exception, _client, _model, _context) -> handleError("aws-ssm-parameter::resource-list", _exception, _model, _context, logger))
                // build model from successful response
                .done((describeParametersRequest, describeParametersResponse, proxyInvocation, resourceModel, context) -> {
                    final List<ResourceModel> models = describeParametersResponse
                            .parameters()
                            .stream().map(parameterMetadata -> ResourceModel.builder().name(parameterMetadata.name()).build()).collect(Collectors.toList());

                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(models)
                            .nextToken(describeParametersResponse.nextToken())
                            .status(OperationStatus.SUCCESS)
                            .build();
                });
    }
}
