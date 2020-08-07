package software.amazon.ssm.maintenancewindowtask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.UpdateMaintenanceWindowTaskTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_NAME;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_TASK_NAME;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATED_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.UPDATE_RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private UpdateMaintenanceWindowTaskTranslator updateMaintenanceWindowTaskTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        updateMaintenanceWindowTaskTranslator = mock(UpdateMaintenanceWindowTaskTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new UpdateHandler(updateMaintenanceWindowTaskTranslator, exceptionTranslator, requestToStringConverter);
    }

    @Test
    public void handleRequestForSuccess() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .maxErrors(TASK_MAX_ERRORS)
                        .priority(TASK_PRIORITY)
                        .name(TASK_NAME)
                        .targets(TASK_TARGETS)
                        .taskType(TASK_TASK_TYPE)
                        .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                        .taskArn(TASK_TASK_ARN)
                        .taskInvocationParameters(RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS)
                        .taskParameters(RESOURCE_TASK_TASK_PARAMETERS);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .maxConcurrency(UPDATED_MAX_CONCURRENCY)
                .maxErrors(UPDATED_MAX_ERRORS)
                .priority(UPDATED_PRIORITY)
                .name(UPDATED_TASK_NAME)
                .targets(UPDATED_TASK_TARGETS)
                .taskType(UPDATED_TASK_TYPE)
                .serviceRoleArn(UPDATED_SERVICE_ROLE_ARN)
                .taskArn(UPDATED_TASK_ARN)
                .taskParameters(UPDATE_RESOURCE_TASK_TASK_PARAMETERS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowTaskRequest expectedUpdateMaintenanceWindowTaskRequest =
                UpdateMaintenanceWindowTaskRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .windowTaskId(desiredModel.getWindowTaskId())
                        .maxConcurrency(desiredModel.getMaxConcurrency())
                        .maxErrors(desiredModel.getMaxErrors())
                        .priority(desiredModel.getPriority())
                        .name(desiredModel.getName())
                        .targets(UPDATED_REQUEST_TASK_TARGETS)
                        .serviceRoleArn(desiredModel.getServiceRoleArn())
                        .taskArn(desiredModel.getTaskArn())
                        .taskParameters(UPDATED_TASK_PARAMETERS)
                        .build();

        final UpdateMaintenanceWindowTaskResponse result =
                UpdateMaintenanceWindowTaskResponse.builder()
                        .windowId(desiredModel.getWindowId())
                        .windowTaskId(desiredModel.getWindowTaskId())
                        .maxConcurrency(desiredModel.getMaxConcurrency())
                        .maxErrors(desiredModel.getMaxErrors())
                        .priority(desiredModel.getPriority())
                        .name(desiredModel.getName())
                        .targets(UPDATED_REQUEST_TASK_TARGETS)
                        .serviceRoleArn(desiredModel.getServiceRoleArn())
                        .taskArn(desiredModel.getTaskArn())
                        .taskParameters(UPDATED_TASK_PARAMETERS)
                        .build();

        when(updateMaintenanceWindowTaskTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowTaskRequest);

        when(updateMaintenanceWindowTaskTranslator.responseToResourceModel(result))
                .thenReturn(desiredModel);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowTaskRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowTaskRequest, UpdateMaintenanceWindowTaskResponse>>any()))
                .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(desiredModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleUpdateRequestWithoutWindowId(){
        final ResourceModel previousModel = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .maxErrors(TASK_MAX_ERRORS)
                .priority(TASK_PRIORITY)
                .build();
        final ResourceModel desiredModel = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .maxConcurrency(UPDATED_MAX_CONCURRENCY)
                .maxErrors(UPDATED_MAX_ERRORS)
                .priority(UPDATED_PRIORITY)
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
                        .message("WindowId and WindowTaskId must be specified to update a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(updateMaintenanceWindowTaskTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleUpdateRequestWithoutWindowTaskId(){
        final ResourceModel previousModel = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .maxErrors(TASK_MAX_ERRORS)
                .priority(TASK_PRIORITY)
                .build();
        final ResourceModel desiredModel = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .maxConcurrency(UPDATED_MAX_CONCURRENCY)
                .maxErrors(UPDATED_MAX_ERRORS)
                .priority(UPDATED_PRIORITY)
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
                        .message("WindowId and WindowTaskId must be specified to update a maintenance window task.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(updateMaintenanceWindowTaskTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleUpdateRequestThrowsDoesNotExistsException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .maxErrors(TASK_MAX_ERRORS)
                        .priority(TASK_PRIORITY);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .maxConcurrency(UPDATED_MAX_CONCURRENCY)
                .maxErrors(UPDATED_MAX_ERRORS)
                .priority(UPDATED_PRIORITY)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowTaskRequest expectedUpdateMaintenanceWindowTaskRequest =
                UpdateMaintenanceWindowTaskRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .windowTaskId(desiredModel.getWindowTaskId())
                        .maxConcurrency(desiredModel.getMaxConcurrency())
                        .maxErrors(desiredModel.getMaxErrors())
                        .priority(desiredModel.getPriority())
                        .build();


        when(updateMaintenanceWindowTaskTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowTaskRequest);

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowTaskRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowTaskRequest, UpdateMaintenanceWindowTaskResponse>>any()))
                .thenThrow(doesNotExistsException);


        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, expectedUpdateMaintenanceWindowTaskRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, expectedUpdateMaintenanceWindowTaskRequest,request.getDesiredResourceState());
    }

    @Test
    void handleUpdateRequestThrowsTranslatedServiceException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .maxErrors(TASK_MAX_ERRORS)
                        .priority(TASK_PRIORITY);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .maxConcurrency(UPDATED_MAX_CONCURRENCY)
                .maxErrors(UPDATED_MAX_ERRORS)
                .priority(UPDATED_PRIORITY)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowTaskRequest expectedUpdateMaintenanceWindowTaskRequest =
                UpdateMaintenanceWindowTaskRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .windowTaskId(desiredModel.getWindowTaskId())
                        .maxConcurrency(desiredModel.getMaxConcurrency())
                        .maxErrors(desiredModel.getMaxErrors())
                        .priority(desiredModel.getPriority())
                        .build();


        when(updateMaintenanceWindowTaskTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowTaskRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowTaskRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowTaskRequest, UpdateMaintenanceWindowTaskResponse>>any()))
                .thenThrow(serviceException);


        when(exceptionTranslator.translateFromServiceException(serviceException, expectedUpdateMaintenanceWindowTaskRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceInternalErrorException(expectedUpdateMaintenanceWindowTaskRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, expectedUpdateMaintenanceWindowTaskRequest,request.getDesiredResourceState());
    }
}
