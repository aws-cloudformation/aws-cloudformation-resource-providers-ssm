package software.amazon.ssm.maintenancewindowtarget;

import java.util.Collections;
import java.util.List;

public class TestsInputs {
    public static final String DESCRIPTION = "TestTargetDescription";
    public static final String NEW_NAME = "NewTestTargetName";
    public static final String NAME = "TestTargetName";
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
}