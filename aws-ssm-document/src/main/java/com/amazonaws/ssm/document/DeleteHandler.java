package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteDocumentRequest;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@RequiredArgsConstructor
public class DeleteHandler extends BaseHandler<CallbackContext> {

    /**
     * Time period after which the Handler should be called again to check the status of the request.
     */
    private static final int CALLBACK_DELAY_SECONDS = 30;

    private static final int NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES = 10 * 60 / CALLBACK_DELAY_SECONDS;

    private static final String OPERATION_NAME = "AWS::SSM::DeleteDocument";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final StabilizationProgressRetriever stabilizationProgressRetriever;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    DeleteHandler() {
        this(DocumentModelTranslator.getInstance(), StabilizationProgressRetriever.getInstance(),
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

        final DeleteDocumentRequest deleteDocumentRequest = documentModelTranslator.generateDeleteDocumentRequest(model);

        try {
            proxy.injectCredentialsAndInvokeV2(deleteDocumentRequest, ssmClient::deleteDocument);
            context.setEventStarted(true);
            context.setStabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(context)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(@NonNull final ResourceModel model,
                                                                         @NonNull final CallbackContext context,
                                                                         @NonNull final AmazonWebServicesClientProxy proxy,
                                                                         @NonNull final Logger logger) {
        final GetProgressResponse progressResponse;

        try {
            progressResponse = stabilizationProgressRetriever.getEventProgress(model, context, ssmClient, proxy, logger);
        } catch (final InvalidDocumentException ex) {
            // If GetDocument call fails with InvalidDocument, it means document is not found and has been deleted successfully.
            return getDeleteSuccessEvent(model);
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME);
        }

        final ResourceInformation resourceInformation = progressResponse.getResourceInformation();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(resourceInformation.getResourceModel())
                .status(getOperationStatus(resourceInformation.getStatus()))
                .message(resourceInformation.getStatusInformation())
                .callbackContext(progressResponse.getCallbackContext())
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> getDeleteSuccessEvent(final ResourceModel model) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private OperationStatus getOperationStatus(@NonNull final ResourceStatus status) {
        if (status == ResourceStatus.DELETING) {
            return OperationStatus.IN_PROGRESS;
        }

        return OperationStatus.FAILED;
    }
}
