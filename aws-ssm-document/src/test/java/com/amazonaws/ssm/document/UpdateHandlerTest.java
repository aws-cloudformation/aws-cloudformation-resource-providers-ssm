package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagUpdater;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_ACCOUNT_ID = "123456";
    private static final String SAMPLE_DOCUMENT_CONTENT_STRING = "sampleDocumentContent";
    private static final Map<String, Object> SAMPLE_DOCUMENT_CONTENT = ImmutableMap.of(
            "schemaVersion", "1.2",
            "description", "Join instances to an AWS Directory Service domain."
    );
    private static final Map<String, Object> SAMPLE_PREVIOUS_DOCUMENT_CONTENT = ImmutableMap.of(
            "schemaVersion", "1.1",
            "description", "Join instances to an AWS Directory Service domain."
    );
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final Map<String, String> SAMPLE_DESIRED_RESOURCE_TAGS = ImmutableMap.of("tagKey1", "tagValue1");
    private static final Map<String, String> SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS = ImmutableMap.of("tagKey2", "tagValue2");
    private static final List<Tag> SAMPLE_PREVIOUS_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey1").value("tagModelValue1").build(),
            Tag.builder().key("tagModelKey2").value("tagModelValue2").build()
    );
    private static final List<Tag> SAMPLE_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey3").value("tagModelValue3").build(),
            Tag.builder().key("tagModelKey4").value("tagModelValue4").build()
    );
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final UpdateDocumentRequest SAMPLE_UPDATE_DOCUMENT_REQUEST = UpdateDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT_STRING)
            .build();

    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
            .tags(SAMPLE_MODEL_TAGS)
            .build();
    private static final ResourceModel SAMPLE_INVALID_RESOURCE_MODEL = ResourceModel.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT)
            .tags(SAMPLE_MODEL_TAGS)
            .build();
    private static final ResourceModel SAMPLE_PREVIOUS_RESOURCE_MODEL = ResourceModel.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
            .tags(SAMPLE_PREVIOUS_MODEL_TAGS)
            .build();
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .systemTags(SAMPLE_SYSTEM_TAGS)
            .desiredResourceTags(SAMPLE_DESIRED_RESOURCE_TAGS)
            .previousResourceTags(SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS)
            .clientRequestToken(SAMPLE_REQUEST_TOKEN)
            .desiredResourceState(SAMPLE_RESOURCE_MODEL)
            .previousResourceState(SAMPLE_PREVIOUS_RESOURCE_MODEL)
            .awsAccountId(SAMPLE_ACCOUNT_ID)
            .build();
    private static final ResourceHandlerRequest<ResourceModel> SAMPLE_RESOURCE_HANDLER_INVALID_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .systemTags(SAMPLE_SYSTEM_TAGS)
            .desiredResourceTags(SAMPLE_DESIRED_RESOURCE_TAGS)
            .previousResourceTags(SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS)
            .clientRequestToken(SAMPLE_REQUEST_TOKEN)
            .desiredResourceState(SAMPLE_INVALID_RESOURCE_MODEL)
            .previousResourceState(SAMPLE_PREVIOUS_RESOURCE_MODEL)
            .awsAccountId(SAMPLE_ACCOUNT_ID)
            .build();
    private static final String UPDATING_MESSAGE = "Updating";

    private static final int CALLBACK_DELAY_SECONDS = 30;
    private static final int NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES = 20;

    private static final ResourceStatus RESOURCE_MODEL_ACTIVE_STATE = ResourceStatus.ACTIVE;
    private static final ResourceStatus RESOURCE_MODEL_UPDATING_STATE = ResourceStatus.UPDATING;
    private static final ResourceStatus RESOURCE_MODEL_FAILED_STATE = ResourceStatus.FAILED;
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
    private TagUpdater tagUpdater;

    @Mock
    private SsmClient ssmClient;

    @Mock
    private SafeLogger safeLogger;

    @Mock
    private SsmException ssmException;

    @Mock
    private CfnGeneralServiceException cfnException;

    private UpdateHandler unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new UpdateHandler(documentModelTranslator, progressUpdater, tagUpdater, exceptionTranslator, ssmClient, safeLogger);
    }

    @Test
    public void testHandleRequest_DocumentUpdateTagsSuccess_VerifyResponse() {

        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .tags(SAMPLE_MODEL_TAGS)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder().build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(0)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        Mockito.verify(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_DocumentUpdateTagsThrowsException_VerifyResponse() {
        doThrow(ssmException).when(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS,
                SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_DocumentUpdateCreateOnlyPropertyFails_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder().build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.failed(
                expectedModel,
                expectedCallbackContext,
                HandlerErrorCode.NotUpdatable, "Create-Only Property cannot be updated");

        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_INVALID_REQUEST, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_INVALID_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }
}
