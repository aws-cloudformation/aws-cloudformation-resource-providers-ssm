package software.amazon.ssm.maintenancewindowtarget.translator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.IdempotentParameterMismatchException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;
    private ResourceModel model;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
        model = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .resourceType(RESOURCE_TYPE)
                .targets(MODEL_TARGETS)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();
    }

    @Test
    void translateFromAlreadyExistsException() {
        final AlreadyExistsException serviceException = AlreadyExistsException.builder().build();

        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnAlreadyExistsException);
        final String expectedMessage =
                String.format("Resource of type 'AWS::SSM::MaintenanceWindowTarget' with identifier '%s' already exists.",
                        model.getWindowTargetId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromResourceLimitExceededException() {
        final ResourceLimitExceededException serviceException = ResourceLimitExceededException.builder().build();

        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
        final String expectedMessage = "Limit exceeded for resource of type 'AWS::SSM::MaintenanceWindowTarget'. Reason: null";
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromDoesNotExistException() {
        final DoesNotExistException serviceException = DoesNotExistException.builder().build();

        final UpdateMaintenanceWindowTargetRequest request = UpdateMaintenanceWindowTargetRequest.builder()
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnNotFoundException);
        final String expectedMessage =
                String.format("Resource of type 'AWS::SSM::MaintenanceWindowTarget' with identifier '%s' was not found.",
                        model.getWindowTargetId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromInternalServerErrorException() {
        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnServiceInternalErrorException);
        final String expectedMessage =
                String.format("Internal error reported from downstream service during operation 'RegisterTargetWithMaintenanceWindow'.");
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromFeatureNotAvailableException() {
        final FeatureNotAvailableException serviceException = FeatureNotAvailableException.builder().build();

        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
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

        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
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
        final RegisterTargetWithMaintenanceWindowRequest request = RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();
        final Exception cfnException = exceptionTranslator
                .translateFromServiceException(serviceException,
                        request,
                        model);
        assertTrue(cfnException instanceof CfnGeneralServiceException);
        final String expectedMessage = "Error occurred during operation 'RegisterTargetWithMaintenanceWindow'.";
        assertEquals(expectedMessage, cfnException.getMessage());
    }
}