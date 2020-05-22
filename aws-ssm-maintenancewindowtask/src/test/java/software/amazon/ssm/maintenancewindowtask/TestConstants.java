package software.amazon.ssm.maintenancewindowtask;

import com.amazonaws.util.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskInvocationParameters;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskParameterValueExpression;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Holds constants for the tests.
 * None of the values actually matter since the SSM endpoint is mocked.
 */
public class TestConstants {

    public static final String ACCOUNT = "123456789012";

    public static final String DEFAULT_REGION = "us-east-1";

    public static final String ACCESS_KEY_ID = "dummyAccessKey";

    public static final String SECRET_ACCESS_KEY = "dummySecretKey";

    public static final String DESCRIPTION = "This is a dummy description.";

    public static final String NAME = "DummyName";

    public static final String CLIENT_REQUEST_TOKEN = "FakeClientRequestToken";

    public static final String WINDOW_ID = "mw-12345678901234567";

    public static final String WINDOW_NAME = "myTestMW";

    public static final String TASK_NAME = "myTestMWTask";

    public static final String TARGET_NAME = "myTestMWTarget";

    public static final String WINDOW_SCHEDULE = "cron(0 0 23 ? * SUN *)";

    public static final String WINDOW_START_DATE = "2018-01-01T00:00:00Z";

    public static final String WINDOW_END_DATE = "2018-02-01T00:00:00Z";

    public static final String WINDOW_TIMEZONE = "Etc/UTC";

    public static final Integer WINDOW_DURATION = 2;

    public static final Integer WINDOW_CUTOFF = 1;

    public static final Boolean WINDOW_ALLOW_UNASSOCIATED_TARGETS = true;

    public static final String WINDOW_TARGET_ID = "124908132095871092840971230897";

    public static final String WINDOW_TASK_ID = "1892870897asdf098109284hjaxdh";

    public static final String TARGET_OWNER_INFORMATION = "blah";

    public static final String TARGET_RESOURCE_TYPE = "INSTANCE";

    public static final ArrayList<Target> TARGET_TARGETS = new ArrayList<Target>();

    public static final String TASK_TASK_ARN = "AWS-RunPowerShellScript";

    public static final String LAMBDA_TASK_ARN = "arn:aws:lambda:us-east-1:872023530462:function:SSMCFNMaintenanceWindowTest";

    public static final Map<String, MaintenanceWindowTaskParameterValueExpression> TASK_TASK_PARAMETERS =
            new HashMap<String, MaintenanceWindowTaskParameterValueExpression>();

    public static final Map<String, List<String>> RESOURCE_TASK_TASK_PARAMETERS =
            new HashMap<String, List<String>>();

    private static final String LAMBDA_PAYLOAD = "eyJrZXkzIjogInZhbHVlMyIsImtleTIiOiAidmFsdWUyIiwia2V5MSI6ICJ2YWx1ZTEifQ==";

    public static final MaintenanceWindowTaskInvocationParameters TASK_TASK_INVOCATION_PARAMETERS =
            MaintenanceWindowTaskInvocationParameters.builder()
                    .lambda(software.amazon.awssdk.services.ssm.model.MaintenanceWindowLambdaParameters.builder()
                            .clientContext("eyJ0ZXN0Q29udGV4dCI6Ik5vdGhpbmcifQ==")
                            .payload(SdkBytes.fromByteBuffer(ByteBuffer.wrap(Base64.decode(LAMBDA_PAYLOAD))))
                            .qualifier("$LATEST")
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

    public static final String TASK_TASK_TYPE = "RUN_COMMAND";

    public static final String LAMBDA_TASK_TYPE = "LAMBDA";

    public static final String TASK_MAX_CONCURRENCY = "5";

    public static final String TASK_MAX_ERRORS = "5";

    public static final Integer TASK_PRIORITY = 1;

    public static final String TASK_SERVICE_ROLE_ARN = "arn:aws:iam::872023530462:role/MaintenanceWindows";

    public static final ArrayList<Target> TASK_TARGETS = new ArrayList<>();

    public static final ArrayList<software.amazon.awssdk.services.ssm.model.Target> REQUEST_TASK_TARGETS = new ArrayList<>();

    public static final String UPDATED_NAME = "myUpdatedMW";

    public static final String UPDATED_TASK_NAME = "myUpdatedMWTask";

    public static final String UPDATED_TARGET_NAME = "myUpdatedMWTarget";

    public static final Boolean UPDATED_ALLOW_UNASSOCIATED_TARGETS = true;

    public static final Integer UPDATED_CUTOFF = 2;

    public static final Integer UPDATED_DURATION = 3;

    public static final String UPDATED_SCHEDULE = "cron(0 0 23 ? * SUN *)";

    public static final String UPDATED_WINDOW_START_DATE = "2018-10-01T00:00:00Z";

    public static final String UPDATED_OWNER_INFORMATION = "blah";

    public static final String UPDATED_RESOURCE_TYPE = "INSTANCE";

    public static final ArrayList<Target> UPDATED_TARGETS = new ArrayList<Target>();

    public static final String UPDATED_TASK_ARN = "AWS-RunPowerShellScript";

    public static final Map<String, MaintenanceWindowTaskParameterValueExpression> UPDATED_TASK_PARAMETERS =
            new HashMap<String, MaintenanceWindowTaskParameterValueExpression>();

    public static final String UPDATED_TASK_TYPE = "RUN_COMMAND";

    public static final String UPDATED_MAX_CONCURRENCY = "1";

    public static final String UPDATED_MAX_ERRORS = "1";

    public static final Integer UPDATED_PRIORITY = 1;

    public static final String UPDATED_SERVICE_ROLE_ARN = "arn:aws:iam::872023530462:role/MaintenanceWindows";

    public static final ArrayList<Target> UPDATED_TASK_TARGETS = new ArrayList<Target>();

    public static final ArrayList<String> TARGET_TARGET_VALUES =new ArrayList<String>();

    public static final ArrayList<String> TASK_TARGET_VALUES =new ArrayList<String>();

    public static final ArrayList<String> UPDATE_TARGET_TARGET_VALUES =new ArrayList<String>();

    public static final ArrayList<String> UPDATE_TASK_TARGET__VALUES =new ArrayList<String>();

    public static final ArrayList<String> RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION =new ArrayList<String>();



    static {
        TARGET_TARGET_VALUES.add("i-08b5ecef9758c071b");
        TARGET_TARGETS.add(Target.builder().key("instanceids").values(TARGET_TARGET_VALUES).build());
        TASK_TASK_PARAMETERS.put("RunCommand", MaintenanceWindowTaskParameterValueExpression.builder().values("ipconfig").build());
        RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION.add("ipconfig");
        RESOURCE_TASK_TASK_PARAMETERS.put("RunCommand",RESOURCE_TASK_TASK_PARAMETERS_VALUE_EXPRESSION);
        TASK_TARGET_VALUES.add("124908132095871092840971230897");
        TASK_TARGETS.add(Target.builder().key("windowtargetids").values(TASK_TARGET_VALUES).build());
        REQUEST_TASK_TARGETS.add(software.amazon.awssdk.services.ssm.model.Target.builder().key("windowtargetids").values(TASK_TARGET_VALUES).build());
        UPDATE_TARGET_TARGET_VALUES.add("5678");
        UPDATED_TARGETS.add(Target.builder().key("tag:1234").values(UPDATE_TARGET_TARGET_VALUES).build());
        UPDATED_TASK_PARAMETERS.put("RunCommand", MaintenanceWindowTaskParameterValueExpression.builder().values("tree").build());
        UPDATE_TASK_TARGET__VALUES.add("i-011facdf17c5f13ac");
        UPDATED_TASK_TARGETS.add(Target.builder().key("instanceids").values(UPDATE_TASK_TARGET__VALUES).build());
    }

}
