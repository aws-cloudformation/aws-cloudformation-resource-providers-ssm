package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagUpdater;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentDescription;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentContentException;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentVersionNameException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentDefaultVersionRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
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
    private static final List<Tag> SAMPLE_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey1").value("tagModelValue1").build(),
            Tag.builder().key("tagModelKey2").value("tagModelValue2").build()
    );
    private static final List<Tag> SAMPLE_PREVIOUS_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey3").value("tagModelValue3").build(),
            Tag.builder().key("tagModelKey4").value("tagModelValue4").build()
    );
    private static final Map<String, String> SAMPLE_CONSOLIDATED_TAGS = ImmutableMap.of(
            "tagKey1", "tagValue1",
            "tagModelKey1", "tagModelValue1",
            "tagModelKey2", "tagModelValue2",
            "aws:cloudformation:stack-name", "testStack"
    );
    private static final Map<String, String> SAMPLE_PREVIOUS_CONSOLIDATED_TAGS = ImmutableMap.of(
            "tagKey2", "tagValue2",
            "tagModelKey3", "tagModelValue3",
            "tagModelKey4", "tagModelValue4"
    );
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final UpdateDocumentRequest SAMPLE_UPDATE_DOCUMENT_REQUEST = UpdateDocumentRequest.builder()
            .name(SAMPLE_DOCUMENT_NAME)
            .content(SAMPLE_DOCUMENT_CONTENT_STRING)
            .build();

    private static final AwsErrorDetails ACCESS_DENIED_ERROR_DETAILS = AwsErrorDetails.builder()
            .errorCode("AccessDeniedException")
            .errorMessage("errorMessage")
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
    private static final ResourceStatus RESOURCE_MODEL_CREATING_STATE = ResourceStatus.CREATING;
    private static final ResourceStatus RESOURCE_MODEL_FAILED_STATE = ResourceStatus.FAILED;
    private static final String SAMPLE_STATUS_INFO = "resource status info";
    private static final String OPERATION_NAME = "AWS::SSM::UpdateDocument";
    private static final String NEW_VERSION = "NewVersion";
    private static final String IN_PROGRESS_MESSAGE = "Updating";

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
        documentModelTranslator = DocumentModelTranslator.getInstance();
        unitUnderTest = new UpdateHandler(documentModelTranslator, progressUpdater, tagUpdater, exceptionTranslator, ssmClient, safeLogger);
    }

    // Test Update for Replacement
    @Test
    public void testHandleRequest_withReplacement_DocumentUpdateTagsSuccess_VerifyResponse() {
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
        Mockito.verify(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME,
                SAMPLE_PREVIOUS_CONSOLIDATED_TAGS, SAMPLE_CONSOLIDATED_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS,
                ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withReplacement_DocumentUpdateTagsThrowsException_VerifyResponse() {
        doThrow(ssmException).when(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_CONSOLIDATED_TAGS,
                SAMPLE_CONSOLIDATED_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, SAMPLE_RESOURCE_HANDLER_REQUEST, null, logger));
        verify(safeLogger).safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withReplacement_DocumentUpdateContentNotUpdatableException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder().name(SAMPLE_DOCUMENT_NAME).content(SAMPLE_DOCUMENT_CONTENT)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(SAMPLE_PREVIOUS_RESOURCE_MODEL, expectedModel);
        Assertions.assertThrows(CfnNotUpdatableException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));

        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withReplacement_DocumentUpdateNameNotUpdatableException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder().name("NewName").content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(SAMPLE_PREVIOUS_RESOURCE_MODEL, expectedModel);
        Assertions.assertThrows(CfnNotUpdatableException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));

        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withReplacement_DocumentUpdateDocumentTypeNotUpdatableException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .documentType("Automation")
                .tags(SAMPLE_MODEL_TAGS)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .documentType("Command")
                .tags(SAMPLE_PREVIOUS_MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        Assertions.assertThrows(CfnNotUpdatableException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));

        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    // Test Update for True Update
    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateTagsSuccess_VerifyResponse() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_PREVIOUS_MODEL_TAGS)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder().build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.SUCCESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(0)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response = unitUnderTest.handleRequest(proxy, request, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        Mockito.verify(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME,
                SAMPLE_PREVIOUS_CONSOLIDATED_TAGS,
                SAMPLE_CONSOLIDATED_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS,
                ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateTagsThrowsException_VerifyResponse() {
        doThrow(ssmException).when(tagUpdater).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_CONSOLIDATED_TAGS,
                SAMPLE_CONSOLIDATED_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenReturn(cfnException);

        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_PREVIOUS_MODEL_TAGS)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);

        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateInProgress_VerifyResponse() {
        // the name of the previousModel should be used automatically
        final ResourceModel expectedModel = ResourceModel.builder()
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .message(IN_PROGRESS_MESSAGE)
                .build();

        final UpdateDocumentResponse expectedUpdateDocumentResponse = UpdateDocumentResponse.builder()
                .documentDescription(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).status(DocumentStatus.UPDATING).build())
                .build();
        when(proxy.injectCredentialsAndInvokeV2(any(UpdateDocumentRequest.class), any())).thenReturn(expectedUpdateDocumentResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response = unitUnderTest.handleRequest(proxy, request, null, logger);

        Assertions.assertEquals(expectedResponse, response);
        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateContentThrowsDuplicatedContentException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .versionName("v2")
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .versionName("v1")
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> expectedResponse = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(expectedModel)
                .status(OperationStatus.IN_PROGRESS)
                .message(IN_PROGRESS_MESSAGE)
                .callbackContext(expectedCallbackContext)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(UpdateDocumentRequest.class), any())).thenThrow(DuplicateDocumentContentException.class);
        when(exceptionTranslator.getCfnException(any(DuplicateDocumentContentException.class), eq(SAMPLE_DOCUMENT_NAME), eq(OPERATION_NAME), eq(logger))).thenThrow(CfnInvalidRequestException.class);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));
        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateNameThrowsException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name("newName")
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));
        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_DocumentUpdateDocumentTypeThrowsException_VerifyException() {
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .documentType("Automation")
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .tags(SAMPLE_MODEL_TAGS)
                .documentType("Command")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> unitUnderTest.handleRequest(proxy, request, null, logger));
        Mockito.verify(tagUpdater, never()).updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);
        verify(safeLogger).safeLogDocumentInformation(expectedModel, null, SAMPLE_ACCOUNT_ID, SAMPLE_SYSTEM_TAGS, logger);
    }

    // tests for True Update API call stablization
    @Test
    public void testHandleRequest_withTrueUpdate_StabilizationRetrieverReturnsUpdatingStatus_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_UPDATING_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES-1)
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

        when(progressUpdater.getEventProgress(expectedModel, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, request, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_StabilizationRetrieverReturnsCreatingStatus_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_CREATING_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES-1)
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

        when(progressUpdater.getEventProgress(expectedModel, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, request, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_StabilizationRetrieverReturnsActiveStatus_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES-1)
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

        final String defaultVersion = "1";
        final String latestVersion = "2";
        final DescribeDocumentResponse expectedDescribeDocumentResponse = DescribeDocumentResponse.builder()
                .document(DocumentDescription.builder().name(SAMPLE_DOCUMENT_NAME).latestVersion(latestVersion).defaultVersion(defaultVersion).build())
                .build();
        final UpdateDocumentDefaultVersionRequest expectedUpdateDocumentDefaultVersionRequest = UpdateDocumentDefaultVersionRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .documentVersion(latestVersion)
                .build();

        when(progressUpdater.getEventProgress(expectedModel, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);
        when(proxy.injectCredentialsAndInvokeV2(any(DescribeDocumentRequest.class), any()))
                .thenReturn(expectedDescribeDocumentResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, request, inProgressCallbackContext, logger);

        Mockito.verify(proxy).injectCredentialsAndInvokeV2(eq(expectedUpdateDocumentDefaultVersionRequest), any());
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_StabilizationRetrieverReturnsFailedStatus_VerifyResponse() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();

        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder().resourceModel(expectedModel)
                .status(RESOURCE_MODEL_FAILED_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();
        final CallbackContext expectedCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES-1)
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

        when(progressUpdater.getEventProgress(expectedModel, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenReturn(getProgressResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        final ProgressEvent<ResourceModel, CallbackContext> response
                = unitUnderTest.handleRequest(proxy, request, inProgressCallbackContext, logger);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    public void testHandleRequest_withTrueUpdate_StabilizationRetrieverThrowsException_VerifyException() {
        final CallbackContext inProgressCallbackContext = CallbackContext.builder()
                .eventStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_DOCUMENT_UPDATE_POLL_RETRIES)
                .build();
        final ResourceModel expectedModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_PREVIOUS_DOCUMENT_CONTENT)
                .updateMethod(NEW_VERSION)
                .build();

        when(progressUpdater.getEventProgress(expectedModel, inProgressCallbackContext, ssmClient, proxy, logger))
                .thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, OPERATION_NAME, logger)).thenThrow(CfnGeneralServiceException.class);

        final ResourceHandlerRequest<ResourceModel> request = buildRequest(previousModel, expectedModel);
        Assertions.assertThrows(CfnGeneralServiceException.class, () -> unitUnderTest.handleRequest(proxy, request, inProgressCallbackContext, logger));
    }

    private ResourceHandlerRequest<ResourceModel> buildRequest(ResourceModel previousModel, ResourceModel model) {
        return buildRequest(previousModel, model, SAMPLE_PREVIOUS_DESIRED_RESOURCE_TAGS, SAMPLE_DESIRED_RESOURCE_TAGS);
    }

    private ResourceHandlerRequest<ResourceModel> buildRequest(ResourceModel previousModel, ResourceModel model, Map<String, String> previousTags, Map<String, String> tags) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .systemTags(SAMPLE_SYSTEM_TAGS)
                .desiredResourceTags(tags)
                .previousResourceTags(previousTags)
                .clientRequestToken(SAMPLE_REQUEST_TOKEN)
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .awsAccountId(SAMPLE_ACCOUNT_ID)
                .build();
    }
}
