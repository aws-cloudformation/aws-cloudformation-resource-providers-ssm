package com.amazonaws.ssm.opsmetadata;

import com.amazonaws.ssm.opsmetadata.translator.request.RequestTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ListOpsMetadataResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {
    private final RequestTranslator requestTranslator;

    public ListHandler() {
        this.requestTranslator = new RequestTranslator();
    }

    public ListHandler(final RequestTranslator requestTranslator) {
        this.requestTranslator = requestTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<SsmClient> proxyClient,
        final Logger logger) {

        final ListOpsMetadataResponse listOpsMetadataResponse = proxy.injectCredentialsAndInvokeV2(requestTranslator.listOpsMetadataRequest(request.getNextToken()), proxyClient.client()::listOpsMetadata);

        final List<ResourceModel> models = listOpsMetadataResponse
                .opsMetadataList()
                .stream().map(opsMetadata -> ResourceModel.builder().opsMetadataArn(opsMetadata.opsMetadataArn()).build()).collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(listOpsMetadataResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
