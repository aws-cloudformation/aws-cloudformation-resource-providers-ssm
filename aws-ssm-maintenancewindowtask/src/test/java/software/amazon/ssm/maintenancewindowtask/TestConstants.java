package software.amazon.ssm.maintenancewindowtask;

import com.amazonaws.util.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskInvocationParameters;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskParameterValueExpression;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds constants for the tests.
 * None of the values actually matter since the SSM endpoint is mocked.
 */
public class TestConstants {

    public static final String DESCRIPTION = "This is a dummy description.";

    public static final String NAME = "DummyName";

    public static final String WINDOW_ID = "mw-12345678901234567";

    public static final String TASK_NAME = "myTestMWTask";

    public static final String WINDOW_TASK_ID = "1892870897asdf098109284hjaxdh";

    public static final String TASK_TASK_ARN = "AWS-RunPowerShellScript";

    public static final String LAMBDA_TASK_ARN = "arn:aws:lambda:us-east-1:872023530462:function:SSMCFNMaintenanceWindowTest";

    public static final Map<String, MaintenanceWindowTaskParameterValueExpression> TASK_TASK_PARAMETERS =
            Collections.singletonMap("RunCommand", MaintenanceWindowTaskParameterValueExpression.builder().values("ipconfig").build());

    public static final List<String> RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION =
            Collections.singletonList("ipconfig");

    public static final Map<String, List<String>> RESOURCE_TASK_TASK_PARAMETERS =
            Collections.singletonMap("RunCommand", RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION);

    private static final String LAMBDA_PAYLOAD = "eyJrZXkzIjogInZhbHVlMyIsImtleTIiOiAidmFsdWUyIiwia2V5MSI6ICJ2YWx1ZTEifQ==";

    public static final MaintenanceWindowTaskInvocationParameters REQUEST_LAMBDA_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .lambda(software.amazon.awssdk.services.ssm.model.MaintenanceWindowLambdaParameters.builder()
                            .clientContext("eyJ0ZXN0Q29udGV4dCI6Ik5vdGhpbmcifQ==")
                            .payload(SdkBytes.fromByteArray(Base64.decode(LAMBDA_PAYLOAD)))
                            .qualifier("$LATEST")
                            .build())
                    .build();

    public static final TaskInvocationParameters RESOURCE_LAMBDA_TASK_INVOCATION_PARAMETERS =
            TaskInvocationParameters.builder()
                    .maintenanceWindowLambdaParameters(MaintenanceWindowLambdaParameters.builder()
                            .clientContext("eyJ0ZXN0Q29udGV4dCI6Ik5vdGhpbmcifQ==")
                            .payload(LAMBDA_PAYLOAD)
                            .qualifier("$LATEST")
                            .build())
                    .build();

    public static final MaintenanceWindowTaskInvocationParameters REQUEST_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .stepFunctions(software.amazon.awssdk.services.ssm.model.MaintenanceWindowStepFunctionsParameters.builder()
                            .input("{\"Input\": {\"Comment\": \"Insert Your Json Here\"}}")
                            .name("{{INVOCATION_ID}}")
                            .build())
                    .build();

    public static final TaskInvocationParameters RESOURCE_STEP_FUNCTIONS_TASK_INVOCATION_PARAMETERS =
            TaskInvocationParameters.builder()
                    .maintenanceWindowStepFunctionsParameters(MaintenanceWindowStepFunctionsParameters.builder()
                            .input("{\"Input\": {\"Comment\": \"Insert Your Json Here\"}}")
                            .name("{{INVOCATION_ID}}")
                            .build())
                    .build();

    public static final MaintenanceWindowTaskInvocationParameters REQUEST_AUTOMATION_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .automation(software.amazon.awssdk.services.ssm.model.MaintenanceWindowAutomationParameters.builder()
                            .documentVersion("1")
                            .parameters(Collections.singletonMap("single", Collections.singletonList("single")))
                            .build())
                    .build();

    public static final TaskInvocationParameters RESOURCE_AUTOMATION_TASK_INVOCATION_PARAMETERS =
            TaskInvocationParameters.builder()
                    .maintenanceWindowAutomationParameters(MaintenanceWindowAutomationParameters.builder()
                            .documentVersion("1")
                            .parameters(Collections.singletonMap("single", Collections.singletonList("single")))
                            .build())
                    .build();

    public static final MaintenanceWindowTaskInvocationParameters REQUEST_RUN_COMMAND_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .runCommand(software.amazon.awssdk.services.ssm.model.MaintenanceWindowRunCommandParameters.builder()
                            .parameters(Collections.singletonMap("commands", Collections.singletonList("ls")))
                            .serviceRoleArn("AWS-RunPowerShellScript")
                            .build())
                    .build();

    public static final TaskInvocationParameters RESOURCE_RUN_COMMAND_TASK_INVOCATION_PARAMETERS =
            TaskInvocationParameters.builder()
                    .maintenanceWindowRunCommandParameters(MaintenanceWindowRunCommandParameters.builder()
                            .parameters(Collections.singletonMap("commands", Collections.singletonList("ls")))
                            .serviceRoleArn("AWS-RunPowerShellScript")
                            .build())
                    .build();

    public static final MaintenanceWindowTaskInvocationParameters UPDATED_TASK_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .runCommand(software.amazon.awssdk.services.ssm.model.MaintenanceWindowRunCommandParameters.builder()
                            .parameters(Collections.singletonMap("commands", Collections.singletonList("dir")))
                            .serviceRoleArn("AWS-RunPowerShellScript")
                            .build())
                    .build();

    public static final LoggingInfo LOGGING_INFO = LoggingInfo.builder()
            .s3Bucket("")
            .s3Prefix("")
            .region("")
            .build();

    public static final software.amazon.awssdk.services.ssm.model.LoggingInfo REQUEST_LOGGING_INFO =
            software.amazon.awssdk.services.ssm.model.LoggingInfo.builder()
                    .s3BucketName("")
                    .s3KeyPrefix("")
                    .s3Region("")
                    .build();

    public static final String TASK_TASK_TYPE = "RUN_COMMAND";

    public static final String LAMBDA_TASK_TYPE = "LAMBDA";

    public static final String TASK_MAX_CONCURRENCY = "5";

    public static final String TASK_MAX_ERRORS = "5";

    public static final Integer TASK_PRIORITY = 1;

    public static final String TASK_SERVICE_ROLE_ARN = "arn:aws:iam::872023530462:role/MaintenanceWindows";

    public static final List<String> TASK_TARGET_VALUES = Collections.singletonList("124908132095871092840971230897");

    public static final List<Target> TASK_TARGETS = Collections.singletonList(Target.builder().key("windowtargetids").values(TASK_TARGET_VALUES).build());

    public static final List<software.amazon.awssdk.services.ssm.model.Target> REQUEST_TASK_TARGETS =
            Collections.singletonList(
                    software.amazon.awssdk.services.ssm.model.Target.builder()
                            .key("windowtargetids")
                            .values("124908132095871092840971230897")
                            .build());

    public static final String UPDATED_TASK_NAME = "myUpdatedMWTask";

    public static final String UPDATED_TASK_ARN = "AWS-RunPowerShellScript";

    public static final String UPDATED_TASK_TYPE = "RUN_COMMAND";

    public static final String UPDATED_MAX_CONCURRENCY = "1";

    public static final String UPDATED_MAX_ERRORS = "1";

    public static final Integer UPDATED_PRIORITY = 1;

    public static final String UPDATED_SERVICE_ROLE_ARN = "arn:aws:iam::872023530462:role/MaintenanceWindows";

    public static final List<String> UPDATE_TASK_TARGET_VALUES =
            Collections.singletonList("i-011facdf17c5f13ac");

    public static final List<Target> UPDATED_TASK_TARGETS = Collections.singletonList(
            Target.builder().key("instanceids").values(UPDATE_TASK_TARGET_VALUES).build());

    public static final Map<String, MaintenanceWindowTaskParameterValueExpression> UPDATED_TASK_PARAMETERS =
            Collections.singletonMap("RunCommand", MaintenanceWindowTaskParameterValueExpression.builder().values("tree").build());

    public static final List<String> UPDATE_RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION =
            Collections.singletonList("tree");

    public static final Map<String, List<String>> UPDATE_RESOURCE_TASK_TASK_PARAMETERS =
            Collections.singletonMap("RunCommand", UPDATE_RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION);

    public static final List<software.amazon.awssdk.services.ssm.model.Target> UPDATED_REQUEST_TASK_TARGETS =
            Collections.singletonList(
                    software.amazon.awssdk.services.ssm.model.Target.builder()
                            .key("instanceids")
                            .values(UPDATE_TASK_TARGET_VALUES)
                            .build());

}
