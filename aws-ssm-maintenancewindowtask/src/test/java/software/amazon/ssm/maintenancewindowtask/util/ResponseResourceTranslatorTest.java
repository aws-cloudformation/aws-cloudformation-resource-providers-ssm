package software.amazon.ssm.maintenancewindowtask.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.ssm.maintenancewindowtask.LoggingInfo;
import software.amazon.ssm.maintenancewindowtask.Target;
import software.amazon.ssm.maintenancewindowtask.TaskInvocationParameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_AUTOMATION_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.REQUEST_TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_AUTOMATION_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_PARAMETERS;

public class ResponseResourceTranslatorTest {
    private ResponseResourceTranslator responseResourceTranslator;

    @BeforeEach
    void setUp() {
        responseResourceTranslator = new ResponseResourceTranslator();
    }

    @Test
    void translateToResourceModelTargetsReturnsEmptyWithNullInput() {
        final Optional<List<Target>> resourceModelTargets =
                responseResourceTranslator.translateToResourceModelTargets(null);

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void translateToResourceModelTargetsReturnsEmptyWithEmptyInput() {
        final Optional<List<Target>> resourceModelTargets =
                responseResourceTranslator.translateToResourceModelTargets(Collections.emptyList());

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void translateToResourceModelTargets() {
        final Optional<List<Target>> resourceModelTargets =
                responseResourceTranslator.translateToResourceModelTargets(REQUEST_TASK_TARGETS);

        assertThat(resourceModelTargets.get()).isEqualTo(TASK_TARGETS);
    }

    @Test
    void translateToResourceModelLoggingInfoWithNullInput() {
        final Optional<LoggingInfo> resourceModelLoggingInfo =
                responseResourceTranslator.translateToResourceModelLoggingInfo(null);

        assertThat(resourceModelLoggingInfo).isEqualTo(Optional.empty());
    }

    @Test
    void translateToResourceModelLoggingInfo() {
        final Optional<LoggingInfo> resourceModelLoggingInfo =
                responseResourceTranslator.translateToResourceModelLoggingInfo(REQUEST_LOGGING_INFO);

        assertThat(resourceModelLoggingInfo.get()).isEqualTo(LOGGING_INFO);
    }

    @Test
    void translateToResourceModelTaskParametersWithNull() {
        final Optional<Map<String, List<String>>> resourceModelTaskParameters =
                responseResourceTranslator.translateToResourceModelTaskParameters(null);

        assertThat(resourceModelTaskParameters).isEqualTo(Optional.empty());
    }

    @Test
    void translateToResourceModelTaskParameters() {

        final Optional<Map<String, List<String>>> resourceModelTaskParameters =
                responseResourceTranslator.translateToResourceModelTaskParameters(TASK_TASK_PARAMETERS);

        assertThat(resourceModelTaskParameters.get()).isEqualTo(RESOURCE_TASK_TASK_PARAMETERS);
    }

    @Test
    void translateToResourceModelTaskInvocationParametersWithNull() {
        final Optional<TaskInvocationParameters>  resourceModelTaskInvocationParameters =
                responseResourceTranslator.translateToResourceModelTaskInvocationParameters(null);

        assertThat(resourceModelTaskInvocationParameters).isEqualTo(Optional.empty());
    }

    @Test
    void translateToResourceModelTaskInvocationParameterWithLambda() {
        final Optional<TaskInvocationParameters>  resourceModelLambdaTaskInvocationParameters =
                responseResourceTranslator.translateToResourceModelTaskInvocationParameters(REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS);

        assertThat(resourceModelLambdaTaskInvocationParameters.get()).isEqualTo(RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToResourceModelTaskInvocationParameterWithStepFunctions() {
        final Optional<TaskInvocationParameters>  resourceModelTaskInvocationParameters =
                responseResourceTranslator.translateToResourceModelTaskInvocationParameters(REQUEST_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS);

        assertThat(resourceModelTaskInvocationParameters.get()).isEqualTo(RESOURCE_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToResourceModelTaskInvocationParameterWithAutomation() {
        final Optional<TaskInvocationParameters>  resourceModelTaskInvocationParameters =
                responseResourceTranslator.translateToResourceModelTaskInvocationParameters(REQUEST_AUTOMATION_TASK_INVOCATION_PARAMETERS);

        assertThat(resourceModelTaskInvocationParameters.get()).isEqualTo(RESOURCE_AUTOMATION_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToResourceModelTaskInvocationParameterWithRunCommand() {
        final Optional<TaskInvocationParameters>  resourceModelTaskInvocationParameters =
                responseResourceTranslator.translateToResourceModelTaskInvocationParameters(REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS);

        assertThat(resourceModelTaskInvocationParameters.get()).isEqualTo(RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS);
    }
}
