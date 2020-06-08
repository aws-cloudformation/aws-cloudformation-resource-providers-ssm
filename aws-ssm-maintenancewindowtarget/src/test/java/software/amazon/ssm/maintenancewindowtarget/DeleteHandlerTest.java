package software.amazon.ssm.maintenancewindowtarget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DeregisterTargetFromMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterTargetFromMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import java.util.function.Function;

import org.mockito.ArgumentMatchers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new DeleteHandler(exceptionTranslator);
    }

    @Test
    public void handleRequestWithWindowTargetId() {
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
    void handleRequestWithNoRequiredParametersPresent() {
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
    void handleRequestThrowsTranslatedServiceException() {
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

        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDeregisterTargetFromMaintenanceWindowRequest),
                ArgumentMatchers.<Function<DeregisterTargetFromMaintenanceWindowRequest, DeregisterTargetFromMaintenanceWindowResponse>>any()))
            .thenThrow(serviceException);
        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedDeregisterTargetFromMaintenanceWindowRequest))
            .thenReturn(new CfnThrottlingException("DeregisterTargetFromMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, expectedDeregisterTargetFromMaintenanceWindowRequest);
    }
}
