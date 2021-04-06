package com.amazonaws.ssm.opsmetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.ssm.opsmetadata.translator.property.MetadataTranslator;
import com.amazonaws.ssm.opsmetadata.translator.request.RequestTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

public class ReadHandler extends BaseHandlerStd {
    private static final String OPERATION = "ReadOpsMetadata";
    private final RequestTranslator requestTranslator;
    private final MetadataTranslator metadataTranslator;

    public ReadHandler() {
        this.requestTranslator = new RequestTranslator();
        this. metadataTranslator = new MetadataTranslator();
    }

    public ReadHandler(final RequestTranslator requestTranslator, final MetadataTranslator metadataTranslator) {
        this.requestTranslator = requestTranslator;
        this.metadataTranslator = metadataTranslator;
    }

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        return proxy.initiate("aws-ssm-opsmetadata::resource-read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest((resourceModel) -> requestTranslator.getOpsMetadataRequest(resourceModel))
                .makeServiceCall(this::ReadResource)
                .done((getOpsMetadataRequest, getOpsMetadataResponse, proxyInvocation, resourceModel, context) -> {
                    Optional<Map<String, MetadataValue>> metadataMap = metadataTranslator.serviceModelPropertyToResourceModel(
                            getOpsMetadataResponse.metadata());
                    ResourceModel.ResourceModelBuilder resourceModelBuilder = ResourceModel.builder();
                    resourceModelBuilder.resourceId(getOpsMetadataResponse.resourceId());
                    resourceModelBuilder.opsMetadataArn(getOpsMetadataRequest.opsMetadataArn());
                    if (metadataMap.isPresent()) {
                        resourceModelBuilder.metadata(metadataMap.get());
                    }

                    return ProgressEvent.defaultSuccessHandler(resourceModelBuilder.build());
                });
    }

    private GetOpsMetadataResponse ReadResource(final GetOpsMetadataRequest getOpsMetadataRequest,
                                                final ProxyClient<SsmClient> proxyClient) {
        try{
            return proxyClient.injectCredentialsAndInvokeV2(getOpsMetadataRequest, proxyClient.client()::getOpsMetadata);
        } catch (final OpsMetadataNotFoundException exception) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, getOpsMetadataRequest.opsMetadataArn());
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }
    }
}
