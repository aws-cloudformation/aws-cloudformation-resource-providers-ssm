package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DocumentDescription;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_DOCUMENT_CONTENT = "sampleDocumentContent";
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final UpdateDocumentRequest SAMPLE_UPDATE_DOCUMENT_REQUEST = UpdateDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT)
            .build();
    private static final UpdateDocumentResponse SAMPLE_UPDATE_DOCUMENT_ACTIVE_RESPONSE = UpdateDocumentResponse.builder()
            .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.ACTIVE).build())
            .build();

    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT)
            .build();
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .systemTags(SAMPLE_SYSTEM_TAGS)
            .clientRequestToken(SAMPLE_REQUEST_TOKEN)
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .build();
    private static final GetDocumentRequest SAMPLE_GET_DOCUMENT_REQUEST = GetDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    private static final int CALLBACK_DELAY_SECONDS = 30;
    private static final int NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES = 20;
    private static final String FAILED_MESSAGE = "failed";

    private static final String RESOURCE_MODEL_ACTIVE_STATE = "Active";
    private static final String RESOURCE_MODEL_UPDATING_STATE = "Updating";
    private static final String RESOURCE_MODEL_FAILED_STATE = "Failed";
    private static final String SAMPLE_STATUS_INFO = "resource status info";
    private static final String OPERATION_NAME = "AWS::SSM::UpdateDocument";

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
    private SsmException ssmException;

    @Mock
    private CfnGeneralServiceException cfnException;

    private UpdateHandler unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new UpdateHandler(documentModelTranslator, progressUpdater, exceptionTranslator, ssmClient);
    }

    @Test
    public void testHandleRequest_DocumentUpdateSuccess_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(documentModelTranslator.generateUpdateDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_UPDATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_UPDATE_DOCUMENT_REQUEST), any())).thenReturn(SAMPLE_UPDATE_DOCUMENT_ACTIVE_RESPONSE);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_DocumentUpdateFailed_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.FAILED)
                .message(FAILED_MESSAGE)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        final UpdateDocumentResponse updateDocumentResponse = UpdateDocumentResponse.builder()
                .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.FAILED).statusInformation(FAILED_MESSAGE).build())
                .build();

        when(documentModelTranslator.generateUpdateDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_UPDATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_UPDATE_DOCUMENT_REQUEST), any())).thenReturn(updateDocumentResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_DocumentUpdateApiReturnsUpdatingStatus_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .message(SAMPLE_STATUS_INFO)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        final UpdateDocumentResponse updateDocumentResponse = UpdateDocumentResponse.builder()
                .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.UPDATING).statusInformation(SAMPLE_STATUS_INFO).build())
                .build();

        when(documentModelTranslator.generateUpdateDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_UPDATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_UPDATE_DOCUMENT_REQUEST), any())).thenReturn(updateDocumentResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void handleRequest_StabilizationRetrieverReturnsUpdatingStatus_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .status(RESOURCE_MODEL_UPDATING_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceModel(expectedModel)
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
    }

    @Test
    public void handleRequest_StabilizationRetrieverReturnsActiveState_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceModel(expectedModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .message(SAMPLE_STATUS_INFO)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void handleRequest_StabilizationRetrieverReturnsFailedState_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .status(RESOURCE_MODEL_FAILED_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceModel(expectedModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.FAILED)
                .message(SAMPLE_STATUS_INFO)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void handleRequest_StabilizationRetrieverThrowsSsmException_VerifyExpectedException() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenThrow(SsmException.class);
        when(exceptionTranslator.getCfnException(any(SsmException.class), eq(SAMPLE_DOCUMENT_NAME), eq(OPERATION_NAME))).thenThrow(ResourceNotFoundException.class);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger));
    }

    @Test
    public void handleRequest_DocumentUpdate_SsmClientThrowsSsmException_VerifyExpectedException() {
        when(documentModelTranslator.generateUpdateDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_UPDATE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_UPDATE_DOCUMENT_REQUEST), any())).thenThrow(ssmException);

        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
    }
}
