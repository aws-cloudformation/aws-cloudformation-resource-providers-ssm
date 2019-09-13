package com.aws.ssm.association;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationVersionLimitExceededException;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidScheduleException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidUpdateException;
import com.amazonaws.services.simplesystemsmanagement.model.TooManyUpdatesException;
import com.amazonaws.services.simplesystemsmanagement.model.UpdateAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.UpdateAssociationResult;
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
public class UpdateHandlerTest {

    private UpdateHandler handler = new UpdateHandler();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private static final String DOCUMENT_NAME = "NewTestDocument";
    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String ASSOCIATION_NAME = "TestAssociation";
    private static final String NEW_ASSOCIATION_NAME = "NewTestAssociation";
    private static final String DOCUMENT_VERSION = "2";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";
    private static final String COMPLIANCE_SEVERITY = "CRITICAL";
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

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequestWithAllParametersNonAutomationNonLegacy() {

        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .parameters(PARAMETERS)
                .targets(TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(OUTPUT_LOCATION);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(NEW_ASSOCIATION_NAME)
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

        final UpdateAssociationResult result =
            new UpdateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithAllParametersLegacy() {

        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .parameters(PARAMETERS)
                .instanceId(INSTANCE_ID)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(OUTPUT_LOCATION);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(NEW_ASSOCIATION_NAME)
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

        final UpdateAssociationResult result =
            new UpdateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithAllParametersAutomationAssociation() {

        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
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
                .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(NEW_ASSOCIATION_NAME)
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

        final UpdateAssociationResult result =
            new UpdateAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithNoAssociationId() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("AssociationId must be present to update the existing association.");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenAssociationDoesNotExist() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDoesNotExistException serviceException =
            new AssociationDoesNotExistException("This association already exists.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequestWhenInvalidScheduleProvided() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .scheduleExpression("Every 5 minutes")
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final InvalidScheduleException serviceException =
            new InvalidScheduleException("This schedule expression is invalid.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenUpdateNotSupported() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .instanceId(INSTANCE_ID)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .targets(TARGETS)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final InvalidUpdateException serviceException =
            new InvalidUpdateException("This update is unsupported.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }

    @Test
    public void handleRequestWhenTooManyUpdatesHappened() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final TooManyUpdatesException serviceException =
            new TooManyUpdatesException("Too many updates happened at the same time; try again later.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.Throttling);
    }

    @Test
    public void handleRequestWhenAssociationVersionLimitReached() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationVersionLimitExceededException serviceException =
            new AssociationVersionLimitExceededException("This association has reached the maximum number of versions.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
    }

    @Test
    public void handleRequestWhenInternalServerErrorHappened() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final InternalServerErrorException serviceException =
            new InternalServerErrorException("Internal server error occurred.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequestWhenUnknownExceptionHappened() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .associationName(ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final IllegalArgumentException serviceException =
            new IllegalArgumentException("Unknown failure occurred.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(UpdateAssociationRequest.class),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        verify(logger).log(eq(serviceException.getMessage()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(previousModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
}
