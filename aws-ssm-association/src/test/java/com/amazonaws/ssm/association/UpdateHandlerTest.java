package com.amazonaws.ssm.association;

import static com.amazonaws.ssm.association.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static com.amazonaws.ssm.association.TestsInputs.SCHEDULE_EXPRESSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
class UpdateHandlerTest {

    private static final String DOCUMENT_NAME = "NewTestDocument";

    private UpdateHandler handler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private BaseHandler<CallbackContext> initialUpdateHandler;
    @Mock
    private BaseHandler<CallbackContext> inProgressHandler;
    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    void setup() {
        handler = new UpdateHandler(initialUpdateHandler,
            inProgressHandler,
            requestToStringConverter);
    }

    @Test
    void defaultConstructorWorks() {
        new UpdateHandler();
    }

    @Test
    void handleRequestWithNoCallbackContextInvokesInitialHandler() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(model);

        final CallbackContext callbackContext = null;

        when(initialUpdateHandler.handleRequest(proxy, request, callbackContext, logger)).thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verify(initialUpdateHandler).handleRequest(proxy, request, callbackContext, logger);
        verifyZeroInteractions(inProgressHandler);
    }

    @Test
    void handleRequestWithCallbackContextInvokesInProgressHandler() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        final ResourceModel model = ResourceModel.builder()
            .name(DOCUMENT_NAME)
            .scheduleExpression(SCHEDULE_EXPRESSION)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final CallbackContext callbackContext = CallbackContext.builder().build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.progress(model, callbackContext);

        when(inProgressHandler.handleRequest(proxy, request, callbackContext, logger)).thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verify(inProgressHandler).handleRequest(proxy, request, callbackContext, logger);
        verifyZeroInteractions(initialUpdateHandler);
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
        assertTrue(loggedStringCaptor.getValue().contains(LOGGED_RESOURCE_HANDLER_REQUEST));
    }
}
