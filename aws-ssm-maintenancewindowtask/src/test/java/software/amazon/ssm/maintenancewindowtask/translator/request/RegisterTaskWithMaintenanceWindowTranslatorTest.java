package software.amazon.ssm.maintenancewindowtask.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;
import software.amazon.ssm.maintenancewindowtask.util.SimpleTypeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_NAME;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;

@ExtendWith(MockitoExtension.class)
public class RegisterTaskWithMaintenanceWindowTranslatorTest {

    @Mock
    private SimpleTypeValidator simpleTypeValidator;

    private RegisterTaskWithMaintenanceWindowTranslator registerTaskWithMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        registerTaskWithMaintenanceWindowTranslator = new RegisterTaskWithMaintenanceWindowTranslator(simpleTypeValidator);
    }

    @Test
    void resourceModelToRequestWithRequiredFieldsTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .targets(TASK_TARGETS)
                .build();

        final RegisterTaskWithMaintenanceWindowRequest registerTaskWithMaintenanceWindowRequest =
                registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final RegisterTaskWithMaintenanceWindowRequest expectedRequest =
                RegisterTaskWithMaintenanceWindowRequest.builder()
                        .windowId(WINDOW_ID)
                        .taskArn(LAMBDA_TASK_ARN)
                        .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                        .taskType(LAMBDA_TASK_TYPE)
                        .targets(REQUEST_TASK_TARGETS)
                        .build();
        assertThat(registerTaskWithMaintenanceWindowRequest).isEqualTo(expectedRequest);
    }

    @Test
    void resourceModelToRequestWithAllFieldsTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .taskArn(LAMBDA_TASK_ARN)
                .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .targets(TASK_TARGETS)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .description(DESCRIPTION)
                .name(TASK_NAME)
                .taskParameters(RESOURCE_TASK_TASK_PARAMETERS)
                .taskInvocationParameters(RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS)
                .loggingInfo(LOGGING_INFO)
                .build();

        final RegisterTaskWithMaintenanceWindowRequest registerTaskWithMaintenanceWindowRequest =
                registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final RegisterTaskWithMaintenanceWindowRequest expectedRequest =
                RegisterTaskWithMaintenanceWindowRequest.builder()
                        .windowId(WINDOW_ID)
                        .taskArn(LAMBDA_TASK_ARN)
                        .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                        .taskType(LAMBDA_TASK_TYPE)
                        .targets(REQUEST_TASK_TARGETS)
                        .maxErrors(TASK_MAX_ERRORS)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .priority(TASK_PRIORITY)
                        .description(DESCRIPTION)
                        .name(TASK_NAME)
                        .taskParameters(TASK_TASK_PARAMETERS)
                        .taskInvocationParameters(REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS)
                        .loggingInfo(REQUEST_LOGGING_INFO)
                        .build();
        assertThat(registerTaskWithMaintenanceWindowRequest).isEqualTo(expectedRequest);
    }
}
