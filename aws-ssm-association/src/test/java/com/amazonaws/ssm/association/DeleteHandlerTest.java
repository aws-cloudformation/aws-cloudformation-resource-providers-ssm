package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AssociationDoesNotExistException;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationResponse;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

import static com.amazonaws.ssm.association.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteHandlerTest {

    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String INSTANCE_ID = "i-1234abcd";
    private static final String DOCUMENT_NAME = "TestDocument";

    private DeleteHandler handler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private ExceptionTranslator exceptionTranslator;
    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    void setup() {
        handler = new DeleteHandler(exceptionTranslator, requestToStringConverter);
    }

    @Test
    void defaultConstructorWorks() {
        new DeleteHandler();
    }

    @Test
    void handleRequestWithAssociationId() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final DeleteAssociationRequest expectedDeleteAssociationRequest =
            DeleteAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .build();

        // no need to mock proxy response as we do not do anything with it unless there are exceptions

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        // delete handler returns null model if delete is successful
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(null);

        assertThat(response).isEqualTo(expectedProgressEvent);
        // need to check that the DeleteAssociation was invoked with the correct request made from the model
        verify(proxy)
            .injectCredentialsAndInvokeV2(
                eq(expectedDeleteAssociationRequest),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResponse>>any());
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithInstanceIdAndDocumentName() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder()
            .instanceId(INSTANCE_ID)
            .name(DOCUMENT_NAME)
            .build();

        final DeleteAssociationRequest expectedDeleteAssociationRequest =
            DeleteAssociationRequest.builder()
                .instanceId(model.getInstanceId())
                .name(model.getName())
                .build();

        // no need to mock proxy response as we do not do anything with it unless there are exceptions

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        // delete handler returns null model if delete is successful
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(null);

        assertThat(response).isEqualTo(expectedProgressEvent);
        // need to check that the DeleteAssociation was invoked with the correct request made from the model
        verify(proxy)
            .injectCredentialsAndInvokeV2(
                eq(expectedDeleteAssociationRequest),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResponse>>any());
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithNoRequiredParametersPresent() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder()
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.InvalidRequest)
                .message("AssociationId, or InstanceId and Document Name must be specified to delete an association.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWhenAssociationDoesNotExist() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final DeleteAssociationRequest expectedDeleteAssociationRequest =
            DeleteAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .build();

        final AssociationDoesNotExistException serviceException =
            AssociationDoesNotExistException.builder()
                .message("Requested association does not exist.")
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDeleteAssociationRequest),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResponse>>any()))
            .thenThrow(serviceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedDeleteAssociationRequest,
                model))
            .thenReturn(new CfnNotFoundException(
                ResourceModel.TYPE_NAME,
                model.getAssociationId(),
                serviceException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, expectedDeleteAssociationRequest, model);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final DeleteAssociationRequest expectedDeleteAssociationRequest =
            DeleteAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDeleteAssociationRequest),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedDeleteAssociationRequest,
                model))
            .thenReturn(new CfnThrottlingException("DeleteAssociation", serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, expectedDeleteAssociationRequest, model);
    }

    @Test
    void handleRequestLogsWithRequestConverter() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);
        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        handler.handleRequest(proxy, request, null, logger);

        final ArgumentCaptor<String> loggedStringCaptor = ArgumentCaptor.forClass(String.class);

        // verifying logger was invoked with the safe-to-print request converter
        verify(requestToStringConverter).convert(request);
        verify(logger).log(loggedStringCaptor.capture());
        System.out.println(loggedStringCaptor.getValue());
        assertTrue(loggedStringCaptor.getValue().contains(LOGGED_RESOURCE_HANDLER_REQUEST));
    }
}
