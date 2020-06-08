package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.AssociationOverview;
import software.amazon.awssdk.services.ssm.model.AssociationStatusName;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InProgressCreateHandlerTest {

    private static final int CALLBACK_DELAY_SECONDS = 15;

    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";
    private static final String ASSOCIATION_ID = "test-12345-associationId";

    private InProgressCreateHandler handler;
    private ResourceModel model;
    @Mock
    private SsmClient ssmClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private AssociationDescriptionTranslator associationDescriptionTranslator;
    @Mock
    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    void setUp() {
        model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .waitForSuccessTimeoutSeconds(90)
            .build();
        handler = new InProgressCreateHandler(CALLBACK_DELAY_SECONDS, ssmClient, associationDescriptionTranslator, exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestWithTimeoutRemainingAndAssociationSuccessStatus() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .overview(AssociationOverview.builder()
                    .status(AssociationStatusName.SUCCESS.name())
                    .build())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ResourceModel expectedModel = request.getDesiredResourceState();

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final int remainingTimeoutSeconds = 60;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(expectedModel.getAssociationId())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestWithTimeoutLargerThanCallbackDelayAndAssociationPendingStatus() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .overview(AssociationOverview.builder()
                    .status(AssociationStatusName.PENDING.name())
                    .build())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ResourceModel expectedModel = request.getDesiredResourceState();

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final int remainingTimeoutSeconds = 60;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(expectedModel.getAssociationId())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds - CALLBACK_DELAY_SECONDS)
            .associationId(expectedModel.getAssociationId())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(expectedCallbackContext, CALLBACK_DELAY_SECONDS, expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestWithTimeoutSmallerThanCallbackDelayAndAssociationPendingStatus() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .overview(AssociationOverview.builder()
                    .status(AssociationStatusName.PENDING.name())
                    .build())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ResourceModel expectedModel = request.getDesiredResourceState();

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final int remainingTimeoutSeconds = 10;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(expectedModel.getAssociationId())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        final CallbackContext expectedCallbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(0)
            .associationId(expectedModel.getAssociationId())
            .build();

        // expecting the callback delay period to be the same as the remaining timeout, because it is less than the
        // default callback delay period.
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(expectedCallbackContext, remainingTimeoutSeconds, expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestWithTimeoutRemainingAndAssociationFailedStatus() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .overview(AssociationOverview.builder()
                    .status(AssociationStatusName.FAILED.name())
                    .build())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final int remainingTimeoutSeconds = 60;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(associationDescription.associationId())
            .build();

        Assertions.assertThrows(CfnNotStabilizedException.class, () -> {
            handler.handleRequest(proxy, request, callbackContext, logger);
        });
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestWithNoTimeoutRemainingAndAssociationPendingStatus() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .overview(AssociationOverview.builder()
                    .status(AssociationStatusName.PENDING.name())
                    .build())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final int remainingTimeoutSeconds = 0;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(associationDescription.associationId())
            .build();

        // since there is only one retry left, and the association status will still be PENDING, we should expect failure
        Assertions.assertThrows(CfnNotStabilizedException.class, () -> {
            handler.handleRequest(proxy, request, callbackContext, logger);
        });
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleInProgressRequestThrowsTranslatedServiceException() {
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .build();

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();
        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(describeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                describeAssociationRequest,
                model))
            .thenReturn(new CfnServiceInternalErrorException("DescribeAssociation", serviceException));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final int remainingTimeoutSeconds = 60;
        final CallbackContext callbackContext = CallbackContext.builder()
            .remainingTimeoutSeconds(remainingTimeoutSeconds)
            .associationId(model.getAssociationId())
            .build();

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, callbackContext, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, describeAssociationRequest, model);
    }
}
