package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagUpdater;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentDefaultVersionRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    private static final String NEW_VERSION = "NewVersion";

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
        final ResourceModel previousModel = request.getPreviousResourceState();

        boolean isTrueUpdate = model.getUpdateMethod() != null && model.getUpdateMethod().equalsIgnoreCase(NEW_VERSION);

        if (isTrueUpdate && model.getName() == null) {
            model.setName(previousModel.getName()); // use the previously used documentName for true update
        }

        safeLogger.safeLogDocumentInformation(model, callbackContext, request.getAwsAccountId(), request.getSystemTags(), logger);

        if (context.getEventStarted() != null) {
            return updateProgress(model, context, ssmClient, proxy, logger);
        }

        if(isCreateOnlyModified(model, previousModel, isTrueUpdate)) {
            if (isTrueUpdate) {
                // Name and DocumentType cannot be updated with True Update
                throw new CfnInvalidRequestException("Create-Only Property cannot be updated with true update.");
            } else {
                throw new CfnNotUpdatableException(new Exception("Create-Only Property cannot be updated."));
            }
        }

        if (!Objects.equals(previousModel.getTags(), model.getTags())) {
            try {
                logger.log("update tags request for document name: " + model.getName());
                tagUpdater.updateTags(model.getName(), request.getPreviousResourceTags(), request.getDesiredResourceTags(),
                        previousModel.getTags(), model.getTags(),
                        ssmClient, proxy, logger);
            } catch (final SsmException e) {
                throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
            }
        }

        if (isTrueUpdate && isUpdatableModified(model, previousModel)) { // remove
            final UpdateDocumentRequest updateDocumentRequest;
            try {
                updateDocumentRequest = documentModelTranslator.generateUpdateDocumentRequest(model);
            } catch (final InvalidDocumentContentException e) {
                throw new CfnInvalidRequestException(e.getMessage(), e);
            }

            try {
                final UpdateDocumentResponse response = proxy.injectCredentialsAndInvokeV2(updateDocumentRequest, ssmClient::updateDocument);
                setInProgressContext(context);

                return getInProgressEvent(model, context, UPDATING_MESSAGE);
            } catch (final SsmException e) {
                throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
            }
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .callbackContext(context)
                    .callbackDelaySeconds(0)
                    .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(final ResourceModel model, final CallbackContext context,
                                                                         final SsmClient ssmClient,
                                                                         final AmazonWebServicesClientProxy proxy,
                                                                         final Logger logger) {
        final GetProgressResponse progressResponse;

        try {
            progressResponse = stabilizationProgressRetriever.getEventProgress(model, context, ssmClient, proxy, logger);
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
        }

        final ResourceInformation resourceInformation = progressResponse.getResourceInformation();

        final OperationStatus operationStatus = getOperationStatus(resourceInformation.getStatus());

        if (operationStatus == OperationStatus.SUCCESS) {
            // Update document default version after updateDocument completes
            String latestVersion = resourceInformation.getLatestVersion();
            String defaultVersion = resourceInformation.getDefaultVersion();

            if (latestVersion == null || defaultVersion == null) {
                final DescribeDocumentRequest describeDocumentRequest =
                    documentModelTranslator.generateDescribeDocumentRequest(model);
                try {
                    final DescribeDocumentResponse describeResponse =
                        proxy.injectCredentialsAndInvokeV2(describeDocumentRequest, ssmClient::describeDocument);
                    latestVersion = describeResponse.document().latestVersion();
                    defaultVersion = describeResponse.document().defaultVersion();
                } catch(SsmException e) {
                    throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
                }
            }

            if (!latestVersion.equalsIgnoreCase(defaultVersion)) {
                final UpdateDocumentDefaultVersionRequest request =
                    documentModelTranslator.generateUpdateDocumentDefaultVersionRequest(model.getName(), latestVersion);

                try {
                    proxy.injectCredentialsAndInvokeV2(request, ssmClient::updateDocumentDefaultVersion);
                } catch (SsmException e) {
                    throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
                }
            }
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(operationStatus)
                .message(resourceInformation.getStatusInformation())
                .callbackContext(progressResponse.getCallbackContext())
                .callbackDelaySeconds(setCallbackDelay(operationStatus))
                .build();
    }

    private int setCallbackDelay(final OperationStatus operationStatus) {
        return operationStatus == OperationStatus.SUCCESS ? 0 : CALLBACK_DELAY_SECONDS;
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

    private boolean isCreateOnlyModified(final ResourceModel model, final ResourceModel previousModel, boolean isTrueUpdate) {
        final ResourceModel comparator = ResourceModel.builder()
                //CreatOnly Properties
                .name(model.getName())
                .documentType(model.getDocumentType())
                //Modifiable Properties
                .tags(previousModel.getTags())
                .updateMethod(previousModel.getUpdateMethod())
                //Properties that depend on UpdateMethod
                .content(isTrueUpdate ? previousModel.getContent() : model.getContent())
                .attachments(isTrueUpdate ? previousModel.getAttachments() : model.getAttachments())
                .versionName(isTrueUpdate ? previousModel.getVersionName() : model.getVersionName())
                .documentFormat(isTrueUpdate ? previousModel.getDocumentFormat() : model.getDocumentFormat())
                .targetType(isTrueUpdate ? previousModel.getTargetType() : model.getTargetType())
                .requires(isTrueUpdate ? previousModel.getRequires() : model.getRequires())
                .build();
        return !comparator.equals(previousModel);
    }

    private boolean isUpdatableModified(final ResourceModel model, final ResourceModel previousModel) {
        final ResourceModel comparator = ResourceModel.builder()
                .name(previousModel.getName())
                .documentType(previousModel.getDocumentType())
                .tags(previousModel.getTags())
                .updateMethod(previousModel.getUpdateMethod())
                //Requires is not updatable through SDK
                .requires(previousModel.getRequires())
                //Properties allowed to be updated using UpdateDocument
                .content(model.getContent())
                .attachments(model.getAttachments())
                .versionName(model.getVersionName())
                .documentFormat(model.getDocumentFormat())
                .targetType(model.getTargetType())
                .build();
        return !comparator.equals(previousModel);
    }
}
