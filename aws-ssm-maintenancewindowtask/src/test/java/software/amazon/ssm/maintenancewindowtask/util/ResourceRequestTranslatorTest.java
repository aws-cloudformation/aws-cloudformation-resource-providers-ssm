package software.amazon.ssm.maintenancewindowtask.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.LoggingInfo;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskInvocationParameters;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskParameterValueExpression;
import software.amazon.awssdk.services.ssm.model.Target;

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

public class ResourceRequestTranslatorTest {
    private ResourceRequestTranslator resourceRequestTranslator;

    @BeforeEach
    void setUp(){
        resourceRequestTranslator = new ResourceRequestTranslator();
    }

    @Test
    void translateToServiceModelTargetsReturnsEmptyWithNullInput() {
        final Optional<List<Target>> serviceModelTargets =
                resourceRequestTranslator.translateToRequestTargets(null);

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void translateToServiceModelTargetsReturnsEmptyWithEmptyInput() {
        final Optional<List<Target>> serviceModelTargets =
                resourceRequestTranslator.translateToRequestTargets(Collections.emptyList());

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void translateToServiceModelTargets() {
        final Optional<List<Target>> serviceModelTargets =
                resourceRequestTranslator.translateToRequestTargets(TASK_TARGETS);

        assertThat(serviceModelTargets.get()).isEqualTo(REQUEST_TASK_TARGETS);
    }

    @Test
    void translateToServiceModelLoggingInfoWithNullInput() {
        final Optional<LoggingInfo> serviceModelLoggingInfo =
                resourceRequestTranslator.translateToRequestLoggingInfo(null);

        assertThat(serviceModelLoggingInfo).isEqualTo(Optional.empty());
    }

    @Test
    void translateToServiceModelLoggingInfo() {
        final Optional<LoggingInfo> serviceModelLoggingInfo =
                resourceRequestTranslator.translateToRequestLoggingInfo(LOGGING_INFO);

        assertThat(serviceModelLoggingInfo.get()).isEqualTo(REQUEST_LOGGING_INFO);
    }

    @Test
    void translateToServiceModelTaskParametersWithNull() {
        final Optional<Map<String, MaintenanceWindowTaskParameterValueExpression>> serviceModelTaskParameters =
                resourceRequestTranslator.translateToRequestTaskParameters(null);

        assertThat(serviceModelTaskParameters).isEqualTo(Optional.empty());
    }

    @Test
    void translateToServiceModelTaskParameters() {

        final Optional<Map<String, MaintenanceWindowTaskParameterValueExpression>> serviceModelTaskParameters =
                resourceRequestTranslator.translateToRequestTaskParameters(RESOURCE_TASK_TASK_PARAMETERS);

        assertThat(serviceModelTaskParameters.get()).isEqualTo(TASK_TASK_PARAMETERS);
    }

    @Test
    void translateToServiceModelTaskInvocationParametersWithNull() {
        final Optional<MaintenanceWindowTaskInvocationParameters>  serviceModelTaskInvocationParameters =
                resourceRequestTranslator.translateToRequestTaskInvocationParameters(null);

        assertThat(serviceModelTaskInvocationParameters).isEqualTo(Optional.empty());
    }

    @Test
    void translateToServiceModelTaskInvocationParameterWithLambda() {
        final Optional<MaintenanceWindowTaskInvocationParameters>  serviceModelLambdaTaskInvocationParameters =
                resourceRequestTranslator.translateToRequestTaskInvocationParameters(RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS);

        assertThat(serviceModelLambdaTaskInvocationParameters.get()).isEqualTo(REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToServiceModelTaskInvocationParameterWithStepFunctions() {
        final Optional<MaintenanceWindowTaskInvocationParameters>  serviceModelTaskInvocationParameters =
                resourceRequestTranslator.translateToRequestTaskInvocationParameters(RESOURCE_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS);

        assertThat(serviceModelTaskInvocationParameters.get()).isEqualTo(REQUEST_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToServiceModelTaskInvocationParameterWithAutomation() {
        final Optional<MaintenanceWindowTaskInvocationParameters>  serviceModelTaskInvocationParameters =
                resourceRequestTranslator.translateToRequestTaskInvocationParameters(RESOURCE_AUTOMATION_TASK_INVOCATION_PARAMETERS);

        assertThat(serviceModelTaskInvocationParameters.get()).isEqualTo(REQUEST_AUTOMATION_TASK_INVOCATION_PARAMETERS);
    }

    @Test
    void translateToServiceModelTaskInvocationParameterWithRunCommand() {
        final Optional<MaintenanceWindowTaskInvocationParameters>  serviceModelTaskInvocationParameters =
                resourceRequestTranslator.translateToRequestTaskInvocationParameters(RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS);

        assertThat(serviceModelTaskInvocationParameters.get()).isEqualTo(REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS);
    }
}
