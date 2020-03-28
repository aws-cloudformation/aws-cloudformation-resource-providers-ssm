package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DeleteDocumentResponse;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
public class DeleteHandlerTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_DOCUMENT_CONTENT = "sampleDocumentContent";
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final DeleteDocumentRequest SAMPLE_DELETE_DOCUMENT_REQUEST = DeleteDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    private static final DeleteDocumentResponse SAMPLE_DELETE_DOCUMENT_RESPONSE = DeleteDocumentResponse.builder().build();

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
    private static final int NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES = 20;
    private static final String FAILED_MESSAGE = "failed";

    private static final ResourceStatus RESOURCE_MODEL_DELETING_STATE = ResourceStatus.DELETING;
    private static final ResourceStatus RESOURCE_MODEL_FAILED_STATE = ResourceStatus.FAILED;
    private static final String SAMPLE_STATUS_INFO = "resource status info";
    private static final String OPERATION_NAME = "AWS::SSM::DeleteDocument";

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

    private DeleteHandler unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new DeleteHandler(documentModelTranslator, progressUpdater, exceptionTranslator, ssmClient);
    }

    @Test
    public void testHandleRequest_DeleteDocumentApiSucceeds_verifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(documentModelTranslator.generateDeleteDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DELETE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DELETE_DOCUMENT_REQUEST), any())).thenReturn(SAMPLE_DELETE_DOCUMENT_RESPONSE);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_DeleteDocumentApiFails_verifyResponse() {
        when(documentModelTranslator.generateDeleteDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DELETE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DELETE_DOCUMENT_REQUEST), any())).thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
    }

    @Test
    public void testHandleRequest_StabilizationRetrieverReturnsDeletingState_verifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_DELETING_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES-1)
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
    }

    @Test
    public void testHandleRequest_StabilizationRetrieverReturnsFailedState_verifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_FAILED_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES-1)
                .build();
        final GetProgressResponse getProgressResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceInformation(expectedResourceInformation)
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
    public void testHandleRequest_StabilizationRetrieverThrowsInvalidDocumentException_verifySuccessResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(SAMPLE_RESOURCE_MODEL)
                .status(OperationStatus.SUCCESS)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenThrow(InvalidDocumentException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_StabilizationRetrieverThrowsSsmException_verifySuccessResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_DELETE_POLL_RETRIES)
                .build();

        when(progressUpdater.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class,
                () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, inProgressCallbackContext, logger));
    }
}
