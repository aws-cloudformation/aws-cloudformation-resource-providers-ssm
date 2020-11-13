package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionVersionNotFoundException;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.CreateDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentDescription;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
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
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_ACCOUNT_ID = "123456";
    private static final String SAMPLE_DOCUMENT_CONTENT_STRING = "sampleDocumentContent";
    private static final Map<String, Object> SAMPLE_DOCUMENT_CONTENT = ImmutableMap.of(
            "schemaVersion", "1.2",
            "description", "Join instances to an AWS Directory Service domain."
    );
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final Map<String, String> SAMPLE_RESOURCE_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack",
        "tag1", "tagValue1");
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final CreateDocumentRequest SAMPLE_CREATE_DOCUMENT_REQUEST = CreateDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT_STRING)
            .build();
    private static final CreateDocumentResponse SAMPLE_CREATE_DOCUMENT_ACTIVE_RESPONSE = CreateDocumentResponse.builder()
            .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.ACTIVE).build())
            .build();

    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT)
            .build();
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .systemTags(SAMPLE_SYSTEM_TAGS)
            .desiredResourceTags(SAMPLE_RESOURCE_TAGS)
            .clientRequestToken(SAMPLE_REQUEST_TOKEN)
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .awsAccountId(SAMPLE_ACCOUNT_ID)
            .build();
    private static final GetDocumentRequest SAMPLE_GET_DOCUMENT_REQUEST = GetDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    private static final int CALLBACK_DELAY_SECONDS = 30;
    private static final int NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES = 20;
    private static final String FAILED_MESSAGE = "failed";
    private static final String OPERATION_NAME = "CreateDocument";
    private static final ResourceStatus RESOURCE_MODEL_ACTIVE_STATE = ResourceStatus.ACTIVE;
    private static final ResourceStatus RESOURCE_MODEL_CREATING_STATE = ResourceStatus.CREATING;
    private static final ResourceStatus RESOURCE_MODEL_FAILED_STATE = ResourceStatus.FAILED;
    private static final String SAMPLE_STATUS_INFO = "resource status info";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private DocumentModelTranslator documentModelTranslator;

    @Mock
    private StabilizationProgressRetriever progressUpdater;

    @Mock
    private DocumentExceptionTranslator exceptionTranslator;

    @Mock
    private SsmClient ssmClient;

    @Mock
    private SafeLogger safeLogger;

    @Mock
    private SsmException ssmException;

    @Mock
    private CfnGeneralServiceException cfnException;

    private CreateHandler unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new CreateHandler(documentModelTranslator, progressUpdater, exceptionTranslator, ssmClient, safeLogger);
    }

    @Test
    public void handleRequest_DocumentCreationInProgress_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        final CreateDocumentResponse createDocumentResponse = CreateDocumentResponse.builder()
                .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.CREATING).build())
                .build();

        when(documentModelTranslator.generateCreateDocumentRequest(SAMPLE_RESOURCE_MODEL, SAMPLE_SYSTEM_TAGS, SAMPLE_RESOURCE_TAGS, SAMPLE_REQUEST_TOKEN)).thenReturn(SAMPLE_CREATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_CREATE_DOCUMENT_REQUEST), any())).thenReturn(createDocumentResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void handleRequest_NewDocumentCreation_documentTranslatorThrowsInvalidContent_VerifyExpectedException() {
        when(documentModelTranslator.generateCreateDocumentRequest(SAMPLE_RESOURCE_MODEL, SAMPLE_SYSTEM_TAGS, SAMPLE_RESOURCE_TAGS, SAMPLE_REQUEST_TOKEN)).thenThrow(InvalidDocumentContentException.class);

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID,SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void handleRequest_NewDocumentCreation_ssmServiceThrowsException_VerifyExpectedException() {
        when(documentModelTranslator.generateCreateDocumentRequest(SAMPLE_RESOURCE_MODEL, SAMPLE_SYSTEM_TAGS, SAMPLE_RESOURCE_TAGS, SAMPLE_REQUEST_TOKEN)).thenReturn(SAMPLE_CREATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_CREATE_DOCUMENT_REQUEST), any())).thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void handleRequest_CallbackContextStabilizationInProgress_StabilizationRetrieverThrowsException_VerifyExpectedException() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void handleRequest_CallbackContextStabilizationInProgress_StabilizationRetrieverReturnsActiveState_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES-1)
                .build();

        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceInformation(expectedResourceInformation)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .message(SAMPLE_STATUS_INFO)
                .callbackContext(expectedCallbackContext)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void handleRequest_CallbackContextStabilizationInProgress_StabilizationRetrieverReturnsInProgress_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_CREATING_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES-1)
                .build();
        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceInformation(expectedResourceInformation)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .message(SAMPLE_STATUS_INFO)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void handleRequest_CallbackContextStabilizationInProgress_GetDocumentReturnsFailure_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_FAILED_STATE)
                .statusInformation(FAILED_MESSAGE)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES-1)
                .build();
        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceInformation(expectedResourceInformation)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.FAILED)
                .message(FAILED_MESSAGE)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }
}
