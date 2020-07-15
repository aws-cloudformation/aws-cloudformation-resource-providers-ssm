package software.amazon.ssm.maintenancewindowtask.translator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.IdempotentParameterMismatchException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskRequest;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;

import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;
    private ResourceModel model;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
        model = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .windowId(WINDOW_ID)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .targets(TASK_TARGETS)
                .taskArn(TASK_TASK_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
    }

    @Test
    void translateFromAlreadyExistsException() {
        final AlreadyExistsException serviceException = AlreadyExistsException.builder().build();

        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnAlreadyExistsException);
        final String expectedMessage =
                String.format("Resource of type 'AWS::SSM::MaintenanceWindowTask' with identifier '%s' already exists.",
                        model.getWindowTaskId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromResourceLimitExceededException() {
        final ResourceLimitExceededException serviceException = ResourceLimitExceededException.builder().build();

        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
        final String expectedMessage = "Limit exceeded for resource of type 'AWS::SSM::MaintenanceWindowTask'. Reason: null";
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromDoesNotExistException() {
        final DoesNotExistException serviceException = DoesNotExistException.builder().build();

        final UpdateMaintenanceWindowTaskRequest request = UpdateMaintenanceWindowTaskRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnNotFoundException);
        final String expectedMessage =
                String.format("Resource of type 'AWS::SSM::MaintenanceWindowTask' with identifier '%s' was not found.",
                        model.getWindowTaskId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromInternalServerErrorException() {
        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnServiceInternalErrorException);
        final String expectedMessage =
                String.format("Internal error reported from downstream service during operation 'RegisterTaskWithMaintenanceWindow'.");
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromFeatureNotAvailableException() {
        final FeatureNotAvailableException serviceException = FeatureNotAvailableException.builder().build();

        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage =
                String.format("Invalid request provided: %s", serviceException.getMessage());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromIdempotentParameterMismatchException() {
        final IdempotentParameterMismatchException serviceException = IdempotentParameterMismatchException.builder().build();

        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage =
                String.format("Invalid request provided: %s", serviceException.getMessage());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromUnknownServiceException() {
        final IllegalArgumentException serviceException = new IllegalArgumentException();
        final RegisterTaskWithMaintenanceWindowRequest request = RegisterTaskWithMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .targets(REQUEST_TASK_TARGETS)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnGeneralServiceException);
        final String expectedMessage = "Error occurred during operation 'RegisterTaskWithMaintenanceWindow'.";
        assertEquals(expectedMessage, cfnException.getMessage());
    }
}
