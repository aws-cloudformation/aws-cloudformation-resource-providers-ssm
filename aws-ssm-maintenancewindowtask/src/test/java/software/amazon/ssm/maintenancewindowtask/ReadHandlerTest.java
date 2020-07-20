package software.amazon.ssm.maintenancewindowtask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskResponse;
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
import software.amazon.ssm.maintenancewindowtask.translator.request.GetMaintenanceWindowTaskTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.NAME;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private ReadHandler handler;

    private static final GetMaintenanceWindowTaskRequest getMaintenanceWindowTaskRequest =
            GetMaintenanceWindowTaskRequest.builder()
                    .windowId(WINDOW_ID)
                    .windowTaskId(WINDOW_TASK_ID)
                    .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private GetMaintenanceWindowTaskTranslator getMaintenanceWindowTaskTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        getMaintenanceWindowTaskTranslator = mock(GetMaintenanceWindowTaskTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new ReadHandler(getMaintenanceWindowTaskTranslator, exceptionTranslator, requestToStringConverter);
    }

    @Test
    public void handleReadRequestForSuccess() {

        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetMaintenanceWindowTaskRequest expectedGetMaintenanceWindowRequest =
                GetMaintenanceWindowTaskRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTaskId(model.getWindowTaskId())
                        .build();

        final GetMaintenanceWindowTaskResponse result = GetMaintenanceWindowTaskResponse.builder()
                .windowId(model.getWindowId())
                .windowTaskId(model.getWindowTaskId())
                .maxErrors(model.getMaxErrors())
                .maxConcurrency(model.getMaxConcurrency())
                .priority(model.getPriority())
                .taskType(model.getTaskType())
                .targets(REQUEST_TASK_TARGETS)
                .taskArn(model.getTaskArn())
                .build();

        when(getMaintenanceWindowTaskTranslator.resourceModelToRequest(model))
                .thenReturn(getMaintenanceWindowTaskRequest);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedGetMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<GetMaintenanceWindowTaskRequest, GetMaintenanceWindowTaskResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setName(NAME);

        when(getMaintenanceWindowTaskTranslator.responseToResourceModel(result))
                .thenReturn(expectedModel);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);

    }

    @Test
    public void handleReadRequestWithoutWindowId(){
        final ResourceModel model = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.InvalidRequest)
                        .message("WindowId and WindowTaskId must be specified to get a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(getMaintenanceWindowTaskTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleReadRequestWithoutWindowTaskId(){
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.InvalidRequest)
                        .message("WindowId and WindowTaskId must be specified to get a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(getMaintenanceWindowTaskTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleReadRequestThrowsDoesNotExistsException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(getMaintenanceWindowTaskTranslator.resourceModelToRequest(model))
                .thenReturn(getMaintenanceWindowTaskRequest);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(getMaintenanceWindowTaskRequest),
                        ArgumentMatchers.<Function<GetMaintenanceWindowTaskRequest, GetMaintenanceWindowTaskResponse>>any()))
                .thenThrow(doesNotExistsException);


        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, getMaintenanceWindowTaskRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, getMaintenanceWindowTaskRequest,request.getDesiredResourceState());
    }

    @Test
    void handleReadRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTaskId(WINDOW_TASK_ID)
                .build();

        when(getMaintenanceWindowTaskTranslator.resourceModelToRequest(model))
                .thenReturn(getMaintenanceWindowTaskRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(getMaintenanceWindowTaskRequest),
                        ArgumentMatchers.<Function<GetMaintenanceWindowTaskRequest, GetMaintenanceWindowTaskResponse>>any()))
                .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, getMaintenanceWindowTaskRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceInternalErrorException(getMaintenanceWindowTaskRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, getMaintenanceWindowTaskRequest, request.getDesiredResourceState());
    }

}
