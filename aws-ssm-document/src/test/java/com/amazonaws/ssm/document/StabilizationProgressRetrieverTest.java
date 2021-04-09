package com.amazonaws.ssm.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentDescription;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.amazonaws.ssm.document.tags.TagReader;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class StabilizationProgressRetrieverTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).build();
    private static final Map<String, String> SAMPLE_TAG_MAP = ImmutableMap.of(
        "tagKey1", "tagValue1",
        "tagKey2", "tagValue2"
    );
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .build();
    private static final DescribeDocumentRequest SAMPLE_DESCRIBE_DOCUMENT_REQUEST = DescribeDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    final DescribeDocumentResponse SAMPLE_DESCRIBE_DOCUMENT_RESPONSE = DescribeDocumentResponse.builder()
            .document(DocumentDescription.builder()
                    .name(SAMPLE_DOCUMENT_NAME)
                    .status(DocumentStatus.ACTIVE)
                    .build())
            .build();
    private static final int CALLBACK_DELAY_SECONDS = 30;
    private static final int NUMBER_OF_DOCUMENT_CREATE_POLL_RETRIES = 30;
    private static final String FAILED_MESSAGE = "failed";
    private static final String SAMPLE_STATUS_INFO = "sample status info";
    private static final ResourceStatus SAMPLE_RESOURCE_STATE = ResourceStatus.ACTIVE;

    @Mock
    private TagReader tagReader;

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
        tagReader = Mockito.mock(TagReader.class);
        unitUnderTest = new StabilizationProgressRetriever(tagReader, documentModelTranslator, responseModelTranslator);
    }

    @Test
    public void testGetEventProgress_DescribeDocumentReturnsResponse_VerifyExpectedResponse() {
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

        final DescribeDocumentResponse describeDocumentResponse = DescribeDocumentResponse.builder()
                .document(DocumentDescription.builder()
                        .name(SAMPLE_DOCUMENT_NAME)
                        .status(DocumentStatus.ACTIVE)
                        .build())
                .build();

        when(documentModelTranslator.generateDescribeDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DESCRIBE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DESCRIBE_DOCUMENT_REQUEST), any())).thenReturn(describeDocumentResponse);
        when(tagReader.getDocumentTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_TAG_MAP);
        when(responseModelTranslator.generateResourceInformation(describeDocumentResponse, SAMPLE_TAG_MAP)).thenReturn(expectedResourceInformation);

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
