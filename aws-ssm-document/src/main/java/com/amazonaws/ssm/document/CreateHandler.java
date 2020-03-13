package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionVersionNotFoundException;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.CreateDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentContentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentSchemaVersionException;
import software.amazon.awssdk.services.ssm.model.MaxDocumentSizeExceededException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static com.amazonaws.ssm.document.ResourceModel.TYPE_NAME;

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

    private static final String CREATE_DOCUMENT_OPERATION_NAME = "CreateDocument";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    public CreateHandler() {
        this(new DocumentModelTranslator(), ClientBuilder.getClient());
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
                    .status(getOperationStatus(response.documentDescription().status()))
                    .message(response.documentDescription().statusInformation())
                    .callbackContext(context)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (final DocumentLimitExceededException e) {
            throw new CfnServiceLimitExceededException(TYPE_NAME, e.getMessage(), e);
        } catch (final DocumentAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(TYPE_NAME, model.getName());
        } catch (final MaxDocumentSizeExceededException | InvalidDocumentContentException | InvalidDocumentSchemaVersionException
                | AutomationDefinitionNotFoundException | AutomationDefinitionVersionNotFoundException e) {
            throw new CfnInvalidRequestException(e.getMessage(), e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(CREATE_DOCUMENT_OPERATION_NAME, e);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProgress(final ResourceModel model, final CallbackContext context,
                                                                         final SsmClient ssmClient,
                                                                         final AmazonWebServicesClientProxy proxy,
                                                                         final Logger logger) {
        if (context.getStabilizationRetriesRemaining() == 0) {
            logger.log(String.format(
                    "Maximum stabilization retries reached for %s [%s]. Resource not stabilized",
                    TYPE_NAME,
                    model.getName()));
            throw new CfnNotStabilizedException(TYPE_NAME, model.getName());
        }

        final GetDocumentRequest describeDocumentRequest = documentModelTranslator.generateGetDocumentRequest(model);
        context.decrementStabilizationRetriesRemaining();
        try {
            final GetDocumentResponse response =
                    proxy.injectCredentialsAndInvokeV2(describeDocumentRequest, ssmClient::getDocument);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(getOperationStatus(response.status()))
                    .message(response.statusInformation())
                    .callbackContext(context)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(e);
        }
    }

    private OperationStatus getOperationStatus(@NonNull final DocumentStatus status) {
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
