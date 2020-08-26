package com.amazonaws.ssm.document;

import java.util.logging.LogManager;
import com.amazonaws.ssm.document.tags.TagUpdater;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentContentException;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentVersionNameException;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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

    private static final String UPDATING_MESSAGE = "Updating";

    private static final int NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES = 10 * 60 / CALLBACK_DELAY_SECONDS;

    private static final String OPERATION_NAME = "AWS::SSM::UpdateDocument";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final StabilizationProgressRetriever stabilizationProgressRetriever;

    @NonNull
    private final TagUpdater tagUpdater;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    UpdateHandler() {
        this(DocumentModelTranslator.getInstance(), StabilizationProgressRetriever.getInstance(),
            TagUpdater.getInstance(),
            DocumentExceptionTranslator.getInstance(), ClientBuilder.getClient());
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

        final UpdateDocumentRequest updateDocumentRequest;
        try {
            updateDocumentRequest = documentModelTranslator.generateUpdateDocumentRequest(model);
        } catch (final InvalidDocumentContentException e) {
            throw new CfnInvalidRequestException(e.getMessage(), e);
        }

        try {
            logger.log("update tags request for document name: " + model.getName());
            tagUpdater.updateTags(model.getName(), request.getDesiredResourceTags(), ssmClient, proxy);

            logger.log("sending update request for document name: " + model.getName());
            final UpdateDocumentResponse response = proxy.injectCredentialsAndInvokeV2(updateDocumentRequest, ssmClient::updateDocument);

            setInProgressContext(context);
            logger.log("update InProgress response: " + response);

            return getInProgressEvent(model, context, response.documentDescription().statusInformation());
        } catch (final DuplicateDocumentContentException | DuplicateDocumentVersionNameException e) {
            logger.log("no changes to document were made for update in cloudformation" +
                " stack, sending for stabilization" + model.getName());
            setInProgressContext(context);

            return getInProgressEvent(model, context, UPDATING_MESSAGE);
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> getNotUpdatableProgressEvent() {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED)
            .errorCode(HandlerErrorCode.NotUpdatable)
            .build();
    }

    private boolean isCreateOnlyPropertiesModified(final ResourceModel previousModel, final ResourceModel model) {
        return !previousModel.getName().equals(model.getName()) || !previousModel.getDocumentType().equals(model.getDocumentType());
    }



    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(@NonNull final ResourceModel model,
                                                                         @NonNull final CallbackContext context,
                                                                         @NonNull final AmazonWebServicesClientProxy proxy,
                                                                         @NonNull final Logger logger) {
       final GetProgressResponse progressResponse;

       try {
           progressResponse = stabilizationProgressRetriever.getEventProgress(model, context, ssmClient, proxy, logger);
       } catch (final SsmException e) {
           throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME);
       }

       final ResourceInformation resourceInformation = progressResponse.getResourceInformation();

       final OperationStatus operationStatus = getOperationStatus(resourceInformation.getStatus());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(resourceInformation.getResourceModel())
                .status(operationStatus)
                .message(resourceInformation.getStatusInformation())
                .callbackContext(progressResponse.getCallbackContext())
                .callbackDelaySeconds(setCallbackDelay(operationStatus))
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> getInProgressEvent(final ResourceModel model,
                                                                             final CallbackContext context,
                                                                             final String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.IN_PROGRESS)
            .message(message)
            .callbackContext(context)
            .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
            .build();
    }

    private void setInProgressContext(CallbackContext context) {
        context.setEventStarted(true);
        context.setStabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES);
    }

    private OperationStatus getOperationStatus(@NonNull final ResourceStatus status) {
        switch (status) {
            case ACTIVE:
                return OperationStatus.SUCCESS;
            case UPDATING:
                return OperationStatus.IN_PROGRESS;
            default:
                return OperationStatus.FAILED;
        }
    }

    private int setCallbackDelay(final OperationStatus operationStatus) {
        return operationStatus == OperationStatus.SUCCESS ? 0 : CALLBACK_DELAY_SECONDS;
    }
}
