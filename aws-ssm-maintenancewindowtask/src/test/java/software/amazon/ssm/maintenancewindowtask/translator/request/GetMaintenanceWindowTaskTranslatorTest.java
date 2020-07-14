package software.amazon.ssm.maintenancewindowtask.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskResponse;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;
import software.amazon.ssm.maintenancewindowtask.util.SimpleTypeValidator;

import static software.amazon.ssm.maintenancewindowtask.TestConstants.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_NAME;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GetMaintenanceWindowTaskTranslatorTest {

    @Mock
    private SimpleTypeValidator simpleTypeValidator;

    private GetMaintenanceWindowTaskTranslator getMaintenanceWindowTaskTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        getMaintenanceWindowTaskTranslator = new GetMaintenanceWindowTaskTranslator(simpleTypeValidator);
    }

    @Test
    void resourceModelToRequestTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .windowId(WINDOW_ID)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .targets(TASK_TARGETS)
                .taskArn(TASK_TASK_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .build();

        final GetMaintenanceWindowTaskRequest getMaintenanceWindowTaskRequest =
                getMaintenanceWindowTaskTranslator.resourceModelToRequest(modelToTranslate);

        final GetMaintenanceWindowTaskRequest expectedRequest =
                GetMaintenanceWindowTaskRequest.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .build();
        assertThat(getMaintenanceWindowTaskRequest).isEqualTo(expectedRequest);
    }

    @Test
    void responseToResourceModelTest() {
        final GetMaintenanceWindowTaskResponse responseToTranslate =
                GetMaintenanceWindowTaskResponse.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .maxErrors(TASK_MAX_ERRORS)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .priority(TASK_PRIORITY)
                        .targets(REQUEST_TASK_TARGETS)
                        .taskArn(LAMBDA_TASK_ARN)
                        .taskType(LAMBDA_TASK_TYPE)
                        .build();

        final ResourceModel resourceModel =
                getMaintenanceWindowTaskTranslator.responseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .windowId(WINDOW_ID)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .targets(TASK_TARGETS)
                .taskArn(LAMBDA_TASK_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .build();
        assertThat(resourceModel).isEqualTo(expectedModel);
    }

    @Test
    void responseToResourceModelWithAllParametersTest() {
        final GetMaintenanceWindowTaskResponse responseToTranslate =
                GetMaintenanceWindowTaskResponse.builder()
                        .windowId(WINDOW_ID)
                        .windowTaskId(WINDOW_TASK_ID)
                        .maxErrors(TASK_MAX_ERRORS)
                        .maxConcurrency(TASK_MAX_CONCURRENCY)
                        .priority(TASK_PRIORITY)
                        .targets(REQUEST_TASK_TARGETS)
                        .taskArn(TASK_TASK_ARN)
                        .taskType(TASK_TASK_TYPE)
                        .description(DESCRIPTION)
                        .name(TASK_NAME)
                        .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                        .taskParameters(TASK_TASK_PARAMETERS)
                        .taskInvocationParameters(REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS)
                        .loggingInfo(REQUEST_LOGGING_INFO)
                        .build();

        final ResourceModel resourceModel =
                getMaintenanceWindowTaskTranslator.responseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .windowId(WINDOW_ID)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .targets(TASK_TARGETS)
                .taskArn(TASK_TASK_ARN)
                .taskType(TASK_TASK_TYPE)
                .description(DESCRIPTION)
                .name(TASK_NAME)
                .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
                .taskParameters(RESOURCE_TASK_TASK_PARAMETERS)
                .taskInvocationParameters(RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS)
                .loggingInfo(LOGGING_INFO)
                .build();
        assertThat(resourceModel).isEqualTo(expectedModel);
    }

}
