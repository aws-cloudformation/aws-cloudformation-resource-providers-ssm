package software.amazon.ssm.maintenancewindow.translator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.IdempotentParameterMismatchException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.ssm.maintenancewindow.ResourceModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.ssm.maintenancewindow.TestConstants.ALLOWED_UNASSOCIATED_TARGETS;
import static software.amazon.ssm.maintenancewindow.TestConstants.CUTOFF;
import static software.amazon.ssm.maintenancewindow.TestConstants.DURATION;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_NAME;
import static software.amazon.ssm.maintenancewindow.TestConstants.SCHEDULE;

@ExtendWith(MockitoExtension.class)
public class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;
    private ResourceModel model;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
        model = ResourceModel.builder()
                .name(WINDOW_NAME)
                .windowId(WINDOW_ID)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
    }

    @Test
    void translateFromAlreadyExistsException() {
        final AlreadyExistsException serviceException = AlreadyExistsException.builder().build();

        final CreateMaintenanceWindowRequest request = CreateMaintenanceWindowRequest.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnAlreadyExistsException);
        final String expectedMessage =
                String.format("Resource of type 'AWS::SSM::MaintenanceWindow' with identifier '%s' already exists.",
                        model.getWindowId());
        //assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromResourceLimitExceededException() {
        final ResourceLimitExceededException serviceException = ResourceLimitExceededException.builder().build();

        final CreateMaintenanceWindowRequest request = CreateMaintenanceWindowRequest.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
        final String expectedMessage = "Limit exceeded for resource of type 'AWS::SSM::MaintenanceWindowTask'. Reason: null";
        //assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromDoesNotExistException() {
        final DoesNotExistException serviceException = DoesNotExistException.builder().build();

        final GetMaintenanceWindowRequest request = GetMaintenanceWindowRequest.builder()
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnNotFoundException);
//        final String expectedMessage =
//                String.format("Resource of type 'AWS::SSM::MaintenanceWindowTask' with identifier '%s' was not found.",
//                        model.getWindowTaskId());
//        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromInternalServerErrorException() {
        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final CreateMaintenanceWindowRequest request = CreateMaintenanceWindowRequest.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnServiceInternalErrorException);
        final String expectedMessage =
                String.format("Internal error reported from downstream service during operation 'CreateMaintenanceWindow'.");
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromIdempotentParameterMismatchException() {
        final IdempotentParameterMismatchException serviceException = IdempotentParameterMismatchException.builder().build();

        final CreateMaintenanceWindowRequest request = CreateMaintenanceWindowRequest.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage =
                String.format("Invalid request provided: %s", serviceException.getMessage());
    }

    @Test
    void translateFromUnknownServiceException() {
        final IllegalArgumentException serviceException = new IllegalArgumentException();
        final CreateMaintenanceWindowRequest request = CreateMaintenanceWindowRequest.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request);
        assertTrue(cfnException instanceof CfnGeneralServiceException);
        final String expectedMessage = "Error occurred during operation 'CreateMaintenanceWindow'.";
        assertEquals(expectedMessage, cfnException.getMessage());
    }
}
