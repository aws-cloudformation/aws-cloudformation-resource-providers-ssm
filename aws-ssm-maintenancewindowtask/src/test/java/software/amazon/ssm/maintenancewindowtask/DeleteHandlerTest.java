package software.amazon.ssm.maintenancewindowtask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DeregisterTaskFromMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterTaskFromMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.DeregisterTaskFromMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private DeleteHandler handler;

    private static final DeregisterTaskFromMaintenanceWindowRequest deregisterTaskFromMaintenanceWindowRequest =
            DeregisterTaskFromMaintenanceWindowRequest.builder()
                    .windowId(WINDOW_ID)
                    .windowTaskId(WINDOW_TASK_ID)
                    .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private DeregisterTaskFromMaintenanceWindowTranslator deregisterTaskFromMaintenanceWindowTranslator;

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        deregisterTaskFromMaintenanceWindowTranslator = mock(DeregisterTaskFromMaintenanceWindowTranslator.class);
        handler = new DeleteHandler(deregisterTaskFromMaintenanceWindowTranslator,exceptionTranslator, requestToStringConverter);
    }

    @Test
    public void handleDeleteRequestForSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        final DeregisterTaskFromMaintenanceWindowRequest expectedDeregisterTaskFromMaintenanceWindowRequest =
                DeregisterTaskFromMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTaskId(model.getWindowTaskId())
                        .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(deregisterTaskFromMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(deregisterTaskFromMaintenanceWindowRequest);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(null);

        assertThat(response).isEqualTo(expectedProgressEvent);

        verify(proxy)
                .injectCredentialsAndInvokeV2(eq(expectedDeregisterTaskFromMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<DeregisterTaskFromMaintenanceWindowRequest, DeregisterTaskFromMaintenanceWindowResponse>>any());
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleRequestWithoutWindowId() {
        final ResourceModel model = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
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
                        .message("WindowId and WindowTaskId must be specified to deregister a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleRequestWithoutWindowTaskId() {
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
                        .message("WindowId and WindowTaskId must be specified to deregister a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleDeleteRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        when(deregisterTaskFromMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(deregisterTaskFromMaintenanceWindowRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(deregisterTaskFromMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<DeregisterTaskFromMaintenanceWindowRequest, DeregisterTaskFromMaintenanceWindowResponse>>any()))
                .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, deregisterTaskFromMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceInternalErrorException(deregisterTaskFromMaintenanceWindowRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, deregisterTaskFromMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleDeleteRequestThrowsDoesNotExistsException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(deregisterTaskFromMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(deregisterTaskFromMaintenanceWindowRequest);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(deregisterTaskFromMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<DeregisterTaskFromMaintenanceWindowRequest, DeregisterTaskFromMaintenanceWindowResponse>>any()))
                .thenThrow(doesNotExistsException);

        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, deregisterTaskFromMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, deregisterTaskFromMaintenanceWindowRequest,request.getDesiredResourceState());
    }
}
