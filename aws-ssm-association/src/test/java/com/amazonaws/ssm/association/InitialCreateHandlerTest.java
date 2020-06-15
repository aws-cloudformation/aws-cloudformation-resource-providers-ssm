package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.translator.request.CreateAssociationTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.CreateAssociationResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
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
class InitialCreateHandlerTest {

    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";
    private static final String ASSOCIATION_ID = "test-12345-associationId";

    private InitialCreateHandler handler;
    @Mock
    private SsmClient ssmClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private CreateAssociationTranslator createAssociationTranslator;
    @Mock
    private AssociationDescriptionTranslator associationDescriptionTranslator;
    @Mock
    private ExceptionTranslator exceptionTranslator;
    @Mock
    private InProgressEventCreator inProgressEventCreator;

    @BeforeEach
    void setUp() {
        handler = new InitialCreateHandler(ssmClient,
            createAssociationTranslator,
            associationDescriptionTranslator,
            exceptionTranslator,
            inProgressEventCreator);
    }

    @Test
    void handleInitialCreateRequestWithNoWaitForSuccess() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final CreateAssociationRequest createAssociationRequest =
            CreateAssociationRequest.builder()
                .name(model.getName())
                .scheduleExpression(model.getScheduleExpression())
                .build();

        when(createAssociationTranslator.resourceModelToRequest(model))
            .thenReturn(createAssociationRequest);

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        final CreateAssociationResponse result =
            CreateAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(createAssociationRequest),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleInitialCreateRequestWithWaitForSuccessSet() {
        final int waitForSuccessTimeoutSeconds = 35;
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .waitForSuccessTimeoutSeconds(waitForSuccessTimeoutSeconds)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final CreateAssociationRequest createAssociationRequest =
            CreateAssociationRequest.builder()
                .name(model.getName())
                .scheduleExpression(model.getScheduleExpression())
                .build();

        when(createAssociationTranslator.resourceModelToRequest(model))
            .thenReturn(createAssociationRequest);

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        final CreateAssociationResponse result =
            CreateAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(createAssociationRequest),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setAssociationId(ASSOCIATION_ID);

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final int callbackDelaySeconds = 15;
        final CallbackContext expectedCallbackContext =
            CallbackContext.builder()
                .remainingTimeoutSeconds(waitForSuccessTimeoutSeconds - callbackDelaySeconds)
                .associationId(expectedModel.getAssociationId())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(expectedCallbackContext, callbackDelaySeconds, expectedModel);

        when(inProgressEventCreator.nextInProgressEvent(waitForSuccessTimeoutSeconds, expectedModel))
            .thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
        verify(inProgressEventCreator).nextInProgressEvent(waitForSuccessTimeoutSeconds, expectedModel);
    }

    @Test
    void handleInitialCreateRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final CreateAssociationRequest createAssociationRequest =
            CreateAssociationRequest.builder()
                .name(model.getName())
                .scheduleExpression(model.getScheduleExpression())
                .build();

        when(createAssociationTranslator.resourceModelToRequest(model))
            .thenReturn(createAssociationRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();
        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(createAssociationRequest),
                ArgumentMatchers.<Function<CreateAssociationRequest, CreateAssociationResponse>>any()))
            .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, createAssociationRequest, model))
            .thenReturn(new CfnServiceInternalErrorException("CreateAssociation", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, createAssociationRequest, model);
    }
}
