package com.amazonaws.ssm.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StabilizationProgressRetrieverTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_DOCUMENT_CONTENT = "sampleDocumentContent";
    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).build();
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .build();
    private static final GetDocumentRequest SAMPLE_GET_DOCUMENT_REQUEST = GetDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    final GetDocumentResponse SAMPLE_GET_DOCUMENT_RESPONSE = GetDocumentResponse.builder()
            .name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.ACTIVE)
            .build();
    private static final int CALLBACK_DELAY_SECONDS = 30;
    private static final int NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES = 30;
    private static final String FAILED_MESSAGE = "failed";
    private static final String SAMPLE_STATUS_INFO = "sample status info";
    private static final ResourceStatus SAMPLE_RESOURCE_STATE = ResourceStatus.ACTIVE;

    @Mock
    private DocumentModelTranslator documentModelTranslator;

    @Mock
    private DocumentResponseModelTranslator responseModelTranslator;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private SsmClient ssmClient;

    private StabilizationProgressRetriever unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new StabilizationProgressRetriever(documentModelTranslator, responseModelTranslator);
    }

    @Test
    public void testGetEventProgress_GetDocumentReturnsResponse_VerifyExpectedResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(SAMPLE_RESOURCE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES-1)
                .build();

        final GetProgressResponse expectedResponse = GetProgressResponse.builder()
                .callbackContext(expectedCallbackContext)
                .resourceInformation(expectedResourceInformation)
                .build();

        final GetDocumentResponse getDocumentResponse = GetDocumentResponse.builder()
                .name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.ACTIVE)
                .build();

        when(documentModelTranslator.generateGetDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_GET_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_GET_DOCUMENT_REQUEST), any())).thenReturn(getDocumentResponse);
        when(responseModelTranslator.generateResourceModel(getDocumentResponse)).thenReturn(expectedResourceInformation);

        final GetProgressResponse response
                = unitUnderTest.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetEventProgress_StabilizationRetriesExhausted_VerifyExpectedException() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .createDocumentStarted(true)
                .stabilizationRetriesRemaining(0)
                .build();

        Assertions.assertThrows(CfnNotStabilizedException.class, () -> unitUnderTest.getEventProgress(SAMPLE_RESOURCE_MODEL, inProgressCallbackContext, ssmClient, proxy, logger));
    }
}
