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
import software.amazon.awssdk.services.ssm.model.InvalidDocumentSchemaVersionException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
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

    @NonNull
    private final SafeLogger safeLogger;

    @VisibleForTesting
    UpdateHandler() {
        this(DocumentModelTranslator.getInstance(), StabilizationProgressRetriever.getInstance(),
            TagUpdater.getInstance(),
            DocumentExceptionTranslator.getInstance(), ClientBuilder.getClient(), SafeLogger.getInstance());
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        final ResourceModel model = request.getDesiredResourceState();

        safeLogger.safeLogDocumentInformation(model, callbackContext, request.getAwsAccountId(), request.getSystemTags(), logger);

        // Only Tags are handled in Update Handler. Other properties of the Document resource are CreateOnly.
        try {
            logger.log("update tags request for document name: " + model.getName());
            tagUpdater.updateTags(model.getName(), request.getDesiredResourceTags(), ssmClient, proxy);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .callbackContext(context)
                .callbackDelaySeconds(0)
                .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
        }
    }
}
