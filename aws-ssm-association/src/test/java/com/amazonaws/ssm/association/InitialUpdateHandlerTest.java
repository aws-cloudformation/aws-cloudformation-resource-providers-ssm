package com.amazonaws.ssm.association;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.translator.request.UpdateAssociationTranslator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class InitialUpdateHandlerTest {

    private static final String DOCUMENT_NAME = "NewTestDocument";
    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String ASSOCIATION_NAME = "TestAssociation";
    private static final String NEW_ASSOCIATION_NAME = "NewTestAssociation";

    private InitialUpdateHandler handler;
    @Mock
    private SsmClient ssmClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private UpdateAssociationTranslator updateAssociationTranslator;
    @Mock
    private AssociationDescriptionTranslator associationDescriptionTranslator;
    @Mock
    private ExceptionTranslator exceptionTranslator;
    @Mock
    private InProgressEventCreator inProgressEventCreator;

    @BeforeEach
    void setup() {
        handler = new InitialUpdateHandler(ssmClient,
            updateAssociationTranslator,
            associationDescriptionTranslator,
            exceptionTranslator,
            inProgressEventCreator);
    }

    @Test
    void defaultConstructorWorks() {
        new InitialUpdateHandler(ssmClient);
    }

    @Test
    void handleRequestWithAssociationIdPresentWithNoWaitForSuccess() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(desiredModel.getAssociationId())
                .name(desiredModel.getName())
                .associationName(desiredModel.getAssociationName())
                .build();

        final UpdateAssociationRequest expectedUpdateAssociationRequest =
            UpdateAssociationRequest.builder()
                .associationId(desiredModel.getAssociationId())
                .name(desiredModel.getName())
                .associationName(desiredModel.getAssociationName())
                .build();

        when(updateAssociationTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateAssociationRequest);

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(desiredModel);

        final UpdateAssociationResponse result =
            UpdateAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateAssociationRequest),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResponse>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(desiredModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithNoAssociationId() {
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

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(previousModel)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.InvalidRequest)
                .message("AssociationId must be present to update the existing association.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(associationDescriptionTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final UpdateAssociationRequest expectedUpdateAssociationRequest =
            UpdateAssociationRequest.builder()
                .associationId(desiredModel.getAssociationId())
                .name(desiredModel.getName())
                .associationName(desiredModel.getAssociationName())
                .build();

        when(updateAssociationTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateAssociationRequest);

        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateAssociationRequest),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedUpdateAssociationRequest,
                desiredModel))
            .thenReturn(new CfnThrottlingException("UpdateAssociation", serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(
                serviceException,
                expectedUpdateAssociationRequest,
                desiredModel);
    }

    @Test
    void handleRequestWithWaitForSuccessPreviouslySet() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .waitForSuccessTimeoutSeconds(30);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .associationName(NEW_ASSOCIATION_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(desiredModel.getAssociationId())
                .name(desiredModel.getName())
                .associationName(desiredModel.getAssociationName())
                .build();

        final UpdateAssociationRequest expectedUpdateAssociationRequest =
            UpdateAssociationRequest.builder()
                .associationId(desiredModel.getAssociationId())
                .name(desiredModel.getName())
                .associationName(desiredModel.getAssociationName())
                .build();

        when(updateAssociationTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateAssociationRequest);

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(desiredModel);

        final UpdateAssociationResponse result =
            UpdateAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateAssociationRequest),
                ArgumentMatchers.<Function<UpdateAssociationRequest, UpdateAssociationResponse>>any()))
            .thenReturn(result);

        final CallbackContext inProgressContext =
            CallbackContext.builder().associationId(ASSOCIATION_ID).remainingTimeoutSeconds(15).build();
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(inProgressContext, 15, desiredModel);

        when(inProgressEventCreator.nextInProgressEvent(desiredModel.getWaitForSuccessTimeoutSeconds(), desiredModel))
            .thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }
}
