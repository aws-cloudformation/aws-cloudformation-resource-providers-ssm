package com.amazonaws.ssm.opsmetadata;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ssm.opsmetadata.translator.property.MetadataTranslator;
import com.amazonaws.ssm.opsmetadata.translator.request.RequestTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.DeleteOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private static final String OPERATION = "DeleteOpsMetadata";
    private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private Logger logger;

    private final RequestTranslator requestTranslator;


    public DeleteHandler() {
        this.requestTranslator = new RequestTranslator();
    }

    public DeleteHandler(final RequestTranslator requestTranslator, final MetadataTranslator metadataTranslator) {
        this.requestTranslator = requestTranslator;
    }

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("aws-ssm-opsmetadata::resource-delete", proxyClient, model, callbackContext)
                .translateToServiceRequest((resourceModel) -> requestTranslator.deleteOpsMetadataRequest(resourceModel))
                .makeServiceCall(this::deleteResource)
                .done((deleteOpsMetadataRequest, deleteParameterResponse, _client, _model, _callbackContext) -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteOpsMetadataResponse deleteResource(final DeleteOpsMetadataRequest deleteOpsMetadataRequest,
                                                     final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(deleteOpsMetadataRequest, proxyClient.client()::deleteOpsMetadata);
        } catch (final OpsMetadataNotFoundException exception) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteOpsMetadataRequest.opsMetadataArn());
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
}
