package software.amazon.ssm.maintenancewindowtask;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.RegisterTaskWithMaintenanceWindowTranslator;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.*;


@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final ResourceModel model = ResourceModel.builder()
            .windowId(WINDOW_ID)
            .taskArn(TASK_TASK_ARN)
            .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
            .taskType(LAMBDA_TASK_TYPE)
            .targets(TASK_TARGETS)
            .taskParameters(RESOURCE_TASK_TASK_PARAMETERS)
            .priority(TASK_PRIORITY)
            .maxConcurrency(TASK_MAX_CONCURRENCY)
            .maxErrors(TASK_MAX_ERRORS)
            .build();

    private static final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

    private static final RegisterTaskWithMaintenanceWindowRequest registerTaskWithMaintenanceWindowRequest =
            RegisterTaskWithMaintenanceWindowRequest.builder()
                    .windowId(WINDOW_ID)
                    .taskArn(TASK_TASK_ARN)
                    .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                    .taskType(LAMBDA_TASK_TYPE)
                    .targets(REQUEST_TASK_TARGETS)
                    .taskParameters(TASK_TASK_PARAMETERS)
                    .priority(TASK_PRIORITY)
                    .maxConcurrency(TASK_MAX_CONCURRENCY)
                    .maxErrors(TASK_MAX_ERRORS)
                    .build();

    private CreateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private RegisterTaskWithMaintenanceWindowTranslator registerTaskWithMaintenanceWindowTranslator;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        registerTaskWithMaintenanceWindowTranslator = mock(RegisterTaskWithMaintenanceWindowTranslator.class);
        handler = new CreateHandler(registerTaskWithMaintenanceWindowTranslator, exceptionTranslator);
    }

    @Test
    public void handleCreateRequestForSuccess() {

        when(registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTaskWithMaintenanceWindowRequest);

        final RegisterTaskWithMaintenanceWindowResponse result =
                RegisterTaskWithMaintenanceWindowResponse.builder()
                        .windowTaskId(WINDOW_TASK_ID)
                        .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTaskWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTaskWithMaintenanceWindowRequest, RegisterTaskWithMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();

        assertEquals(request.getDesiredResourceState().getWindowTaskId(),null);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertEquals(request.getDesiredResourceState().getWindowTaskId(),WINDOW_TASK_ID);
        assertEquals(response.getStatus(),expectedProgressEvent.getStatus());
        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleCreateRequestThrowsTranslatedServiceException() {
        when(registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTaskWithMaintenanceWindowRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTaskWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTaskWithMaintenanceWindowRequest, RegisterTaskWithMaintenanceWindowResponse>>any()))
                .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, registerTaskWithMaintenanceWindowRequest,request.getDesiredResourceState()))
                .thenReturn(new CfnServiceInternalErrorException(registerTaskWithMaintenanceWindowRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState());

    }

    @Test
    public void handleCreateRequestThrowsDoesNotExistsException() {
        when(registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTaskWithMaintenanceWindowRequest);

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTaskWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTaskWithMaintenanceWindowRequest, RegisterTaskWithMaintenanceWindowResponse>>any()))
                .thenThrow(doesNotExistsException);

        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleCreateRequestThrowsInvalidRequestException(){
        when(registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTaskWithMaintenanceWindowRequest);

        final FeatureNotAvailableException featureNotAvailableException = FeatureNotAvailableException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTaskWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTaskWithMaintenanceWindowRequest, RegisterTaskWithMaintenanceWindowResponse>>any()))
                .thenThrow(featureNotAvailableException);

        when(exceptionTranslator.translateFromServiceException(featureNotAvailableException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnInvalidRequestException(registerTaskWithMaintenanceWindowRequest.toString(),featureNotAvailableException));

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(featureNotAvailableException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleCreateRequestThrowsResourceLimitExceedException(){
        when(registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTaskWithMaintenanceWindowRequest);

        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTaskWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTaskWithMaintenanceWindowRequest, RegisterTaskWithMaintenanceWindowResponse>>any()))
                .thenThrow(resourceLimitExceededException);

        when(exceptionTranslator.translateFromServiceException(resourceLimitExceededException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceLimitExceededException(resourceLimitExceededException));

        Assertions.assertThrows(CfnServiceLimitExceededException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(resourceLimitExceededException, registerTaskWithMaintenanceWindowRequest, request.getDesiredResourceState());

    }
}
