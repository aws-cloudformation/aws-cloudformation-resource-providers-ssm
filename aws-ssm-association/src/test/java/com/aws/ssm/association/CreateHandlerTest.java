package com.aws.ssm.association;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationAlreadyExistsException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationLimitExceededException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationOverview;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationStatusName;
import com.amazonaws.services.simplesystemsmanagement.model.CreateAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.CreateAssociationResult;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationResult;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String ASSOCIATION_NAME = "TestAssociation";
    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String DOCUMENT_VERSION = "2";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";
    private static final String COMPLIANCE_SEVERITY = "CRITICAL";
    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String MAX_CONCURRENCY = "50%";
    private static final String MAX_ERRORS = "10%";
    private static final String INSTANCE_ID = "i-1234abcd";
    private static final String AUTOMATION_TARGET_PARAMETER_NAME = "InstanceId";

    private static final String S3_BUCKET_REGION = "us-east-1";
    private static final String S3_BUCKET_NAME = "test-bucket";
    private static final String S3_KEY_PREFIX = "test-association-output-location";
    private static final InstanceAssociationOutputLocation OUTPUT_LOCATION =
        new InstanceAssociationOutputLocation(
            new S3OutputLocation(S3_BUCKET_REGION, S3_BUCKET_NAME, S3_KEY_PREFIX));

    private static final String TARGET_KEY = "tag:domain";
    private static final String TARGET_VALUE = "test";
    private static final List<Target> TARGETS =
        Collections.singletonList(
            new Target(TARGET_KEY, Collections.singletonList(TARGET_VALUE)));

    private static final Map<String, List<String>> PARAMETERS =
        ImmutableMap.<String, List<String>>builder()
            .put("command", Collections.singletonList("echo 'hello world'"))
            .build();

    private CreateHandler handler = new CreateHandler();
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequestWithAllParametersNonAutomationNonLegacy() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .name(DOCUMENT_NAME)
            .documentVersion(DOCUMENT_VERSION)
            .parameters(PARAMETERS)
            .targets(TARGETS)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .complianceSeverity(COMPLIANCE_SEVERITY)
            .maxConcurrency(MAX_CONCURRENCY)
            .maxErrors(MAX_ERRORS)
            .outputLocation(OUTPUT_LOCATION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withDocumentVersion(DOCUMENT_VERSION)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withComplianceSeverity(COMPLIANCE_SEVERITY)
                .withMaxConcurrency(MAX_CONCURRENCY)
                .withMaxErrors(MAX_ERRORS)
                .withOutputLocation(new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(
                        new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                            .withOutputS3Region(S3_BUCKET_REGION)
                            .withOutputS3BucketName(S3_BUCKET_NAME)
                            .withOutputS3KeyPrefix(S3_KEY_PREFIX)));

        final CreateAssociationResult result =
            new CreateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithAllParametersLegacyAssociation() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .name(DOCUMENT_NAME)
            .documentVersion(DOCUMENT_VERSION)
            .parameters(PARAMETERS)
            .instanceId(INSTANCE_ID)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .complianceSeverity(COMPLIANCE_SEVERITY)
            .maxConcurrency(MAX_CONCURRENCY)
            .maxErrors(MAX_ERRORS)
            .outputLocation(OUTPUT_LOCATION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withDocumentVersion(DOCUMENT_VERSION)
                .withParameters(PARAMETERS)
                .withInstanceId(INSTANCE_ID)
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withComplianceSeverity(COMPLIANCE_SEVERITY)
                .withMaxConcurrency(MAX_CONCURRENCY)
                .withMaxErrors(MAX_ERRORS)
                .withOutputLocation(new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(
                        new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                            .withOutputS3Region(S3_BUCKET_REGION)
                            .withOutputS3BucketName(S3_BUCKET_NAME)
                            .withOutputS3KeyPrefix(S3_KEY_PREFIX)));

        final CreateAssociationResult result =
            new CreateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithAllParametersAutomationAssociation() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .name(DOCUMENT_NAME)
            .documentVersion(DOCUMENT_VERSION)
            .parameters(PARAMETERS)
            .targets(TARGETS)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .complianceSeverity(COMPLIANCE_SEVERITY)
            .maxConcurrency(MAX_CONCURRENCY)
            .maxErrors(MAX_ERRORS)
            .outputLocation(OUTPUT_LOCATION)
            .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withDocumentVersion(DOCUMENT_VERSION)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withComplianceSeverity(COMPLIANCE_SEVERITY)
                .withMaxConcurrency(MAX_CONCURRENCY)
                .withMaxErrors(MAX_ERRORS)
                .withOutputLocation(new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(
                        new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                            .withOutputS3Region(S3_BUCKET_REGION)
                            .withOutputS3BucketName(S3_BUCKET_NAME)
                            .withOutputS3KeyPrefix(S3_KEY_PREFIX)))
                .withAutomationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME);

        final CreateAssociationResult result =
            new CreateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithNoDocumentName() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .parameters(PARAMETERS)
            .targets(TARGETS)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Document name must be specified to create an association.");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenAssociationAlreadyExists() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .name(DOCUMENT_NAME)
            .documentVersion(DOCUMENT_VERSION)
            .parameters(PARAMETERS)
            .targets(TARGETS)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final String errorMessage = "This association already exists.";

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenThrow(new AssociationAlreadyExistsException(errorMessage));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        // actual error message will have service/request and other details appended
        assertThat(response.getMessage()).contains(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
    }

    @Test
    public void handleRequestWhenServiceLimitExceeded() {
        final ResourceModel model = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .name(DOCUMENT_NAME)
            .documentVersion(DOCUMENT_VERSION)
            .parameters(PARAMETERS)
            .targets(TARGETS)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final String errorMessage = "Association limit exceeded for your account.";

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenThrow(new AssociationLimitExceededException(errorMessage));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        // actual error message will have service/request and other details appended
        assertThat(response.getMessage()).contains(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
    }

    @Test
    public void handleRequestWhenInvalidRequestProvided() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final String errorMessage = "Invalid request.";

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenThrow(new InvalidDocumentException(errorMessage));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        // actual error message will have service/request and other details appended
        assertThat(response.getMessage()).contains(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenInternalServerErrorHappened() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final String errorMessage = "Internal server error.";

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenThrow(new InternalServerErrorException(errorMessage));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        // actual error message will have service/request and other details appended
        assertThat(response.getMessage()).contains(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequestWhenUnknownExceptionHappened() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final String errorMessage = "Unknown failure occurred.";

        final IllegalArgumentException unknownServiceException = new IllegalArgumentException(errorMessage);
        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenThrow(unknownServiceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        verify(logger).log(eq(unknownServiceException.getMessage()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }

    @Test
    public void handleRequestWhenWaitForAssociationSuccessIsSetToFalse() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .waitForAssociationSuccess(Boolean.FALSE)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)));

        final CreateAssociationResult result =
            new CreateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);
        // this flag is only used to determine how to start Create operation
        // it is not needed to understand the model of the Association resource being created
        expectedModel.setWaitForAssociationSuccess(null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWhenWaitForAssociationSuccessIsSetToTrue() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .waitForAssociationSuccess(Boolean.TRUE)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)));

        final CreateAssociationResult result =
            new CreateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(CreateAssociationRequest.class),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResult>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(10)
            .build();

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);
        // this flag is only used to determine how to start Create operation
        // it is not needed to understand the model of the Association resource being created
        expectedModel.setWaitForAssociationSuccess(null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isEqualTo(expectedCallbackContext);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(15);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWhenCallbackContextIsPresentWithRetriesRemainingAndAssociationSuccess() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withOverview(new AssociationOverview()
                    .withStatus(AssociationStatusName.Success.name()));

        final DescribeAssociationResult result =
            new DescribeAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenReturn(result);

        final CallbackContext callbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(10)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        final ResourceModel expectedModel = request.getDesiredResourceState();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWhenCallbackContextIsPresentWithRetriesRemainingAndAssociationPending() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withOverview(new AssociationOverview()
                    .withStatus(AssociationStatusName.Pending.name()));

        final DescribeAssociationResult result =
            new DescribeAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenReturn(result);

        final CallbackContext callbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(10)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        final ResourceModel expectedModel = request.getDesiredResourceState();

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(9)
            .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isEqualTo(expectedCallbackContext);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(15);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWhenCallbackContextIsPresentWithRetriesRemainingAndAssociationFailed() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withOverview(new AssociationOverview()
                    .withStatus(AssociationStatusName.Failed.name())
                .withDetailedStatus("Execution failed"));

        final DescribeAssociationResult result =
            new DescribeAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenReturn(result);

        final CallbackContext callbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(10)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Association failed; detailed status: Execution failed");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @Test
    public void handleRequestWhenCallbackContextIsPresentWithLastRetryRemaining() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .targets(TARGETS)
            .parameters(PARAMETERS)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withOverview(new AssociationOverview()
                    .withStatus(AssociationStatusName.Pending.name()));

        final DescribeAssociationResult result =
            new DescribeAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenReturn(result);

        final CallbackContext callbackContext = CallbackContext.builder()
            .associationDescription(associationDescription)
            .stabilizationRetriesRemaining(1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        // since there is only one retry left, and the association status will still be Pending, we should expect failure
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Timed out waiting for association to succeed.");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }
}
