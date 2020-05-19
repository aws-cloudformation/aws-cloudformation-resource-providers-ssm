package com.amazonaws.ssm.association;

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

import static com.amazonaws.ssm.association.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateHandlerTest {

    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";

    private CreateHandler handler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private BaseHandler<CallbackContext> initialCreateHandler;
    @Mock
    private BaseHandler<CallbackContext> inProgressCreateHandler;
    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    void setUp() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        handler = new CreateHandler(initialCreateHandler, inProgressCreateHandler, requestToStringConverter);
    }

    @Test
    void handleRequestWithNoCallbackContextInvokesInitialHandler() {
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

        when(initialCreateHandler.handleRequest(proxy, request, callbackContext, logger)).thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verify(initialCreateHandler).handleRequest(proxy, request, callbackContext, logger);
        verifyZeroInteractions(inProgressCreateHandler);
    }

    @Test
    void handleRequestWithCallbackContextInvokesInProgressHandler() {
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

        when(inProgressCreateHandler.handleRequest(proxy, request, callbackContext, logger)).thenReturn(expectedProgressEvent);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verify(inProgressCreateHandler).handleRequest(proxy, request, callbackContext, logger);
        verifyZeroInteractions(initialCreateHandler);
    }

    @Test
    void handleRequestLogsWithRequestConverter() {
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
