package com.amazonaws.ssm.opsmetadata;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ssm.opsmetadata.translator.request.RequestTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashMap;
import java.util.Map;

public class CreateHandler extends BaseHandlerStd {
    private static final String OPERATION = "CreateOpsMetadata";
    private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private Logger logger;
    private final RequestTranslator requestTranslator;

    public CreateHandler() {
        this.requestTranslator = new RequestTranslator();
    }

    public CreateHandler(final RequestTranslator requestTranslator) {
        this.requestTranslator = requestTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        Map<String, String> consolidatedTagList = new HashMap<>();
        if (request.getDesiredResourceTags() != null) {
            consolidatedTagList.putAll(request.getDesiredResourceTags());
        }
        if (request.getSystemTags() != null) {
            consolidatedTagList.putAll(request.getSystemTags());
        }

        return proxy.initiate("aws-ssm-opsmetadata::resource-create", proxyClient, model, callbackContext)
                .translateToServiceRequest((resourceModel) -> requestTranslator.createOpsMetadataRequest(resourceModel, consolidatedTagList))
                .makeServiceCall(this::createResource)
                .done((createOpsMetadataRequest, createOpsMetadataResponse, _client, _model, _callbackContext) -> ProgressEvent.defaultSuccessHandler(toResourceModel(createOpsMetadataResponse)));
    }

    private CreateOpsMetadataResponse createResource(final CreateOpsMetadataRequest createOpsMetadataRequest,
                                                     final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(createOpsMetadataRequest, proxyClient.client()::createOpsMetadata);
        } catch (final OpsMetadataAlreadyExistsException exception) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createOpsMetadataRequest.resourceId());
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        } catch (final AmazonServiceException exception) {
            final Integer errorStatus = exception.getStatusCode();
            final String errorCode = exception.getErrorCode();
            if (errorStatus >= Constants.ERROR_STATUS_CODE_400 && errorStatus < Constants.ERROR_STATUS_CODE_500) {
                if (THROTTLING_ERROR_CODES.contains(errorCode)) {
                    logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
                    throw new CfnThrottlingException(OPERATION, exception);
                }
            }
            throw new CfnGeneralServiceException(OPERATION, exception);
        }
    }

    private ResourceModel toResourceModel(final CreateOpsMetadataResponse createOpsMetadataResponse) {
        return ResourceModel.builder()
                .opsMetadataArn(createOpsMetadataResponse.opsMetadataArn())
                .build();
    }
}
