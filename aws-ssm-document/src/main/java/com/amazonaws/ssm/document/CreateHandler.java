package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.CreateDocumentResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Create a new AWS::SSM::Document resource.
 */
@RequiredArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {
    /**
     * Time period after which the Handler should be called again to check the status of the request.
     */
    private static final int CALLBACK_DELAY_SECONDS = 30;

    private static final int NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES = 10 * 60 / CALLBACK_DELAY_SECONDS;

    private static final String RESOURCE_MODEL_ACTIVE_STATE = "Active";
    private static final String RESOURCE_MODEL_CREATING_STATE = "Creating";
    private static final String OPERATION_NAME = "CreateDocument";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final StabilizationProgressRetriever stabilizationProgressRetriever;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    public CreateHandler() {
        this(DocumentModelTranslator.getInstance(), StabilizationProgressRetriever.getInstance(),
                DocumentExceptionTranslator.getInstance(), ClientBuilder.getClient());
    }

    /**
     * Handles the new Create request for the resource.
     */
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        final ResourceModel model = request.getDesiredResourceState();

        if (context.getCreateDocumentStarted() != null) {
            return updateProgress(model, context, ssmClient, proxy, logger);
        }

        final CreateDocumentRequest createDocumentRequest =
                documentModelTranslator.generateCreateDocumentRequest(model, request.getSystemTags(), request.getClientRequestToken());

        try {
            final CreateDocumentResponse response = proxy.injectCredentialsAndInvokeV2(createDocumentRequest, ssmClient::createDocument);
            model.setName(response.documentDescription().name());
            context.setCreateDocumentStarted(true);
            context.setStabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .message(response.documentDescription().statusInformation())
                    .callbackContext(context)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(final ResourceModel model, final CallbackContext context,
                                                                         final SsmClient ssmClient,
                                                                         final AmazonWebServicesClientProxy proxy,
                                                                         final Logger logger) {
        final GetProgressResponse progressResponse;

        try {
            progressResponse = stabilizationProgressRetriever.getEventProgress(model, context, ssmClient, proxy, logger);
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

    private OperationStatus getOperationStatus(@NonNull final ResourceStatus status) {
        switch (status) {
            case ACTIVE:
                return OperationStatus.SUCCESS;
            case CREATING:
                return OperationStatus.IN_PROGRESS;
            default:
                return OperationStatus.FAILED;
        }
    }
}
