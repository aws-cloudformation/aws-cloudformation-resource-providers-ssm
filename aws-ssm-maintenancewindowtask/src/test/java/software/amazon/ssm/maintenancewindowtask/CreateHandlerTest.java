package software.amazon.ssm.maintenancewindowtask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.RegisterTaskWithMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;


@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final ResourceModel model = ResourceModel.builder()
            .windowId(WINDOW_ID)
            .taskArn(LAMBDA_TASK_ARN)
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
                    .taskArn(LAMBDA_TASK_ARN)
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

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        registerTaskWithMaintenanceWindowTranslator = mock(RegisterTaskWithMaintenanceWindowTranslator.class);
        handler = new CreateHandler(registerTaskWithMaintenanceWindowTranslator, exceptionTranslator, requestToStringConverter);
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
