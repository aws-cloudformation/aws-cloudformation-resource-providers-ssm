package software.amazon.ssm.maintenancewindowtarget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DeregisterTargetFromMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterTargetFromMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;

import java.util.function.Function;

import org.mockito.ArgumentMatchers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

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
    public void setup() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        handler = new DeleteHandler(exceptionTranslator, requestToStringConverter);
    }

    @Test
    public void handleDeleteRequestWithWindowTargetId() {
        final ResourceModel model = ResourceModel.builder()
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final DeregisterTargetFromMaintenanceWindowRequest expectedDeregisterTargetFromMaintenanceWindowRequest =
            DeregisterTargetFromMaintenanceWindowRequest.builder()
                .windowId(model.getWindowId())
                .windowTargetId(model.getWindowTargetId())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(null);

        assertThat(response).isEqualTo(expectedProgressEvent);

        verify(proxy)
            .injectCredentialsAndInvokeV2(eq(expectedDeregisterTargetFromMaintenanceWindowRequest), ArgumentMatchers.<Function<DeregisterTargetFromMaintenanceWindowRequest, DeregisterTargetFromMaintenanceWindowResponse>>any());
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleDeleteRequestWithoutWindowTargetId() {
        final ResourceModel model = ResourceModel.builder()
            .windowId(WINDOW_ID)
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
                .message("Both WindowId and WindowTargetId must be specified to delete a maintenance window target.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleDeleteRequestWithoutWindowId() {
        final ResourceModel model = ResourceModel.builder()
            .windowTargetId(WINDOW_TARGET_ID)
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
                .message("Both WindowId and WindowTargetId must be specified to delete a maintenance window target.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleDeleteRequestWithNoRequiredParametersPresent() {
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
                .message("Both WindowId and WindowTargetId must be specified to delete a maintenance window target.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleDeleteRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final DeregisterTargetFromMaintenanceWindowRequest expectedDeregisterTargetFromMaintenanceWindowRequest =
            DeregisterTargetFromMaintenanceWindowRequest.builder()
                .windowId(model.getWindowId())
                .windowTargetId(model.getWindowTargetId())
                .build();

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDeregisterTargetFromMaintenanceWindowRequest),
                ArgumentMatchers.<Function<DeregisterTargetFromMaintenanceWindowRequest, DeregisterTargetFromMaintenanceWindowResponse>>any()))
            .thenThrow(serviceException);
        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedDeregisterTargetFromMaintenanceWindowRequest,
                request.getDesiredResourceState()))
            .thenReturn(new CfnThrottlingException(expectedDeregisterTargetFromMaintenanceWindowRequest.toString(), serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, expectedDeregisterTargetFromMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleDeleteRequestThrowsDoesNotExistsException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        final DeregisterTargetFromMaintenanceWindowRequest expectedDeregisterTargetFromMaintenanceWindowRequest =
                DeregisterTargetFromMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTargetId(model.getWindowTargetId())
                        .build();

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedDeregisterTargetFromMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<DeregisterTargetFromMaintenanceWindowRequest, DeregisterTargetFromMaintenanceWindowResponse>>any()))
                .thenThrow(doesNotExistsException);

        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, expectedDeregisterTargetFromMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, expectedDeregisterTargetFromMaintenanceWindowRequest,request.getDesiredResourceState());
    }
}
