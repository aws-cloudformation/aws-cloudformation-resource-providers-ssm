package software.amazon.ssm.maintenancewindowtarget;

import java.util.Collections;
import java.util.List;

public class TestsInputs {
    public static final String DESCRIPTION = "TestTargetDescription";
    public static final String UPDATED_NAME = "NewTestTargetName";
    public static final String NAME = "TestTargetName";
    public static final String NEXT_TOKEN = "TestNextToken";
    public static final String OWNER_INFORMATION = "TestTargetOwnerInformation";
    public static final String RESOURCE_TYPE = "TestTargetResourceType";
    public static final String WINDOW_ID = "mw-01234567890123456";
    public static final String WINDOW_TARGET_ID = "01234567-abcd-8901-efgh-ijklmnopqrst";


    public static final String TARGET_KEY = "tag:domain";
    public static final String TARGET_VALUE = "test";
    public static final List<Target> MODEL_TARGETS =
        Collections.singletonList(
            new Target(TARGET_KEY, Collections.singletonList(TARGET_VALUE)));
    public static final List<software.amazon.awssdk.services.ssm.model.Target> SERVICE_TARGETS =
        Collections.singletonList(
            software.amazon.awssdk.services.ssm.model.Target.builder()
                .key(TARGET_KEY)
                .values(TARGET_VALUE)
                .build());

    public static final String FILTER_KEY = "WindowTargetId";
    public static final String FILTER_VALUE = WINDOW_TARGET_ID;
    public static final List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter> SERVICE_FILTERS =
            Collections.singletonList(
                    software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter.builder()
                            .key(FILTER_KEY)
                            .values(FILTER_VALUE)
                            .build());

    public static final List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowTarget> SERVICE_MAINTENANCE_WINDOW_TARGETS =
            Collections.singletonList(
                    software.amazon.awssdk.services.ssm.model.MaintenanceWindowTarget.builder()
                            .description(DESCRIPTION)
                            .name(NAME)
                            .ownerInformation(OWNER_INFORMATION)
                            .resourceType(RESOURCE_TYPE)
                            .targets(SERVICE_TARGETS)
                            .windowId(WINDOW_ID)
                            .windowTargetId(WINDOW_TARGET_ID)
                            .build());

    public static final String LOGGED_RESOURCE_HANDLER_REQUEST = "StringifiedResourceHandlerRequest";
}
