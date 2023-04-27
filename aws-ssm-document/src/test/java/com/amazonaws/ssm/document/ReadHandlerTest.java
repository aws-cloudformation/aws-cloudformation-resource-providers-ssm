package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagReader;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentDescription;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String OPERATION_NAME = "AWS::SSM::GetDocument";
    private static final String SAMPLE_ACCOUNT_ID = "123456";
    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_TARGET_TYPE = "/AWS::EC2::Volume";
    private static final Map<String, Object> SAMPLE_DOCUMENT_CONTENT = ImmutableMap.of(
            "schemaVersion", "1.2",
            "description", "Join instances to an AWS Directory Service domain."
    );    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).build();
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .awsAccountId(SAMPLE_ACCOUNT_ID)
            .systemTags(SAMPLE_SYSTEM_TAGS)
            .build();
    private static final Map<String, String> SAMPLE_TAG_MAP = ImmutableMap.of(
        "tagKey1", "tagValue1",
        "tagKey2", "tagValue2"
    );
    private static final GetDocumentRequest SAMPLE_GET_DOCUMENT_REQUEST = GetDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    final GetDocumentResponse SAMPLE_GET_DOCUMENT_RESPONSE = GetDocumentResponse.builder()
            .name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.ACTIVE)
            .build();
    private static final DescribeDocumentRequest SAMPLE_DESCRIBE_DOCUMENT_REQUEST = DescribeDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .build();
    final DescribeDocumentResponse SAMPLE_DESCRIBE_DOCUMENT_RESPONSE = DescribeDocumentResponse.builder()
            .document(DocumentDescription.builder()
                    .name(SAMPLE_DOCUMENT_NAME)
                    .targetType(SAMPLE_TARGET_TYPE)
                    .status(DocumentStatus.ACTIVE)
                    .build())
            .build();
    private static final ResourceStatus SAMPLE_RESOURCE_STATE = ResourceStatus.ACTIVE;
    private static final String SAMPLE_STATUS_INFO = "resource status info";

    private static final SsmException ACCESS_DENIED_EXCEPTION = (SsmException) SsmException.builder()
            .awsErrorDetails(AwsErrorDetails.builder()
                    .errorCode("AccessDeniedException")
                    .build())
            .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private DocumentModelTranslator documentModelTranslator;

    @Mock
    private DocumentResponseModelTranslator documentResponseModelTranslator;

    @Mock
    private SsmClient ssmClient;

    @Mock
    private TagReader tagReader;

    @Mock
    private SafeLogger safeLogger;

    @Mock
    private DocumentExceptionTranslator exceptionTranslator;

    @Mock
    private SsmException ssmException;

    @Mock
    private CfnGeneralServiceException cfnException;

    private ReadHandler unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new ReadHandler(documentModelTranslator, documentResponseModelTranslator,
            ssmClient, tagReader, exceptionTranslator, safeLogger);
    }

    @Test
    public void testHandleRequest_ReadSuccess_verifyResult() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .targetType(SAMPLE_TARGET_TYPE).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(SAMPLE_RESOURCE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .build();

        when(documentModelTranslator.generateGetDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_GET_DOCUMENT_REQUEST);
        when(documentModelTranslator.generateDescribeDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DESCRIBE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_GET_DOCUMENT_REQUEST), any())).thenReturn(SAMPLE_GET_DOCUMENT_RESPONSE);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DESCRIBE_DOCUMENT_REQUEST), any())).thenReturn(SAMPLE_DESCRIBE_DOCUMENT_RESPONSE);
        when(tagReader.getDocumentTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_TAG_MAP);
        when(documentResponseModelTranslator.generateResourceInformation(SAMPLE_GET_DOCUMENT_RESPONSE, SAMPLE_TAG_MAP))
            .thenReturn(expectedResourceInformation);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_DescribeDocumentAccessDeniedSoftFail_verifyResult() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT).build();
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(SAMPLE_RESOURCE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .build();

        when(documentModelTranslator.generateGetDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_GET_DOCUMENT_REQUEST);
        when(documentModelTranslator.generateDescribeDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DESCRIBE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_GET_DOCUMENT_REQUEST), any())).thenReturn(SAMPLE_GET_DOCUMENT_RESPONSE);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DESCRIBE_DOCUMENT_REQUEST), any())).thenThrow(ssmException);
        when(tagReader.getDocumentTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_TAG_MAP);
        when(documentResponseModelTranslator.generateResourceInformation(SAMPLE_GET_DOCUMENT_RESPONSE, SAMPLE_TAG_MAP))
                .thenReturn(expectedResourceInformation);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_GetDocumentThrowsSsmException_verifyExceptionReturned() {
        when(documentModelTranslator.generateGetDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_GET_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_GET_DOCUMENT_REQUEST), any())).thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_DescribeThrowsSsmException_verifyExceptionReturned() {
        when(documentModelTranslator.generateGetDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_GET_DOCUMENT_REQUEST);
        when(documentModelTranslator.generateDescribeDocumentRequest(SAMPLE_RESOURCE_MODEL)).thenReturn(SAMPLE_DESCRIBE_DOCUMENT_REQUEST);
        when(proxy.injectCredentialsAndInvokeV2(eq(SAMPLE_DESCRIBE_DOCUMENT_REQUEST), any())).thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }
}
