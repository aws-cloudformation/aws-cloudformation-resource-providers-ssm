package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Update AWS::SSM::Document resource.
 */
@RequiredArgsConstructor
public class UpdateHandler extends BaseHandler<CallbackContext> {

    /**
     * Time period after which the Handler should be called again to check the status of the request.
     */
    private static final int CALLBACK_DELAY_SECONDS = 30;

    private static final int NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES = 15 * 60 / CALLBACK_DELAY_SECONDS;

    private static final String RESOURCE_MODEL_ACTIVE_STATE = "Active";
    private static final String RESOURCE_MODEL_UPDATING_STATE = "Updating";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final StabilizationProgressRetriever stabilizationProgressRetriever;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    UpdateHandler() {
        this(new DocumentModelTranslator(), new StabilizationProgressRetriever(), new DocumentExceptionTranslator(), SsmClient.create());
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        final ResourceModel model = request.getDesiredResourceState();

        if (context.getEventStarted() != null) {
            return updateProgress(model, context, proxy, logger);
        }

        final UpdateDocumentRequest updateDocumentRequest = documentModelTranslator.generateUpdateDocumentRequest(model);

        try {
            logger.log("sending update request for document name: " + model.getName());
            final UpdateDocumentResponse response = proxy.injectCredentialsAndInvokeV2(updateDocumentRequest, ssmClient::updateDocument);
            context.setEventStarted(true);
            context.setStabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES);

            logger.log("update response: " + response);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(getOperationStatus(response.documentDescription().statusAsString()))
                    .message(response.documentDescription().statusInformation())
                    .callbackContext(context)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName());
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(@NonNull final ResourceModel model,
                                                                         @NonNull final CallbackContext context,
                                                                         @NonNull final AmazonWebServicesClientProxy proxy,
                                                                         @NonNull final Logger logger) {

       final GetProgressResponse progressResponse;

       try {
           progressResponse = stabilizationProgressRetriever.getEventProgress(model, context, ssmClient, proxy, logger);
       } catch (final SsmException e) {
           throw exceptionTranslator.getCfnException(e, model.getName());
       } catch (final Exception e) {
           throw new CfnGeneralServiceException(e);
       }

       final ResourceModel responseModel = progressResponse.getResourceModel();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(responseModel)
                .status(getOperationStatus(responseModel.getStatus()))
                .message(responseModel.getStatusInformation())
                .callbackContext(context)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();
    }

    private OperationStatus getOperationStatus(@NonNull final String status) {
        switch (status) {
            case RESOURCE_MODEL_ACTIVE_STATE:
                return OperationStatus.SUCCESS;
            case RESOURCE_MODEL_UPDATING_STATE:
                return OperationStatus.IN_PROGRESS;
            default:
                return OperationStatus.FAILED;
        }
    }
}
