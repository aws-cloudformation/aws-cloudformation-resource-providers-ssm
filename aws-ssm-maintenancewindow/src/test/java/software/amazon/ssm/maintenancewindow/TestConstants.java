package software.amazon.ssm.maintenancewindow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Hold constants for the test
 */
public class TestConstants {

    public static final String WINDOW_NAME = "TestMaintenanceWindow";

    public static final String WINDOW_ID = "mw-12345678901234567";

    public static final String SCHEDULE = "cron(0 4 ? * SUN *)";

    public static final Integer DURATION = 2;

    public static final Integer CUTOFF = 1;

    public static final Boolean ALLOWED_UNASSOCIATED_TARGETS = false;

    public static final String WINDOW_DESCRIPTION = "This is the description for window with all fields.";

    public static final String RESOURCE_TAG_KEY = "resourceKey";

    public static final String RESOURCE_TAG_VALUE = "resourceValue";

    public static final String STACK_TAG_KEY = "stackKey";

    public static final String STACK_TAG_VALUE = "stackValue";

    public static final String SYSTEM_TAG_KEY = "aws:cloudformation:stack-name";

    public static final String SYSTEM_TAG_VALUE = "testStackName";

    //tags defined for maintenance window resource model, specified in template
    public static final Map<String, String> RESOURCE_MODEL_TAGS = Collections.singletonMap(RESOURCE_TAG_KEY, RESOURCE_TAG_VALUE);

    //tags defined for stack, specified when creating stack
    public static final Map<String, String> STACK_TAGS = Collections.singletonMap(STACK_TAG_KEY, STACK_TAG_VALUE);

    //tags automatically created by cloudformation, never gets changed once created
    public static final Map<String, String> SYSTEM_TAGS = Collections.singletonMap(SYSTEM_TAG_KEY, SYSTEM_TAG_VALUE);

    //consolidated tags for resource model tags and stack tags
    public static final Map<String, String> RESOURCE_TAGS = new LinkedHashMap<String, String>() {{
        put(RESOURCE_TAG_KEY, RESOURCE_TAG_VALUE);
        put(STACK_TAG_KEY, STACK_TAG_VALUE);
    }};

    public static final List<Tag> CONSOLIDATED_RESOURCE_MODEL_TAGS = Stream.of(
            Tag.builder().key(RESOURCE_TAG_KEY).value(RESOURCE_TAG_VALUE).build(),
            Tag.builder().key(STACK_TAG_KEY).value(STACK_TAG_VALUE).build()
    ).collect(Collectors.toList());

    public static final List<Tag> CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS = Stream.of(
            Tag.builder().key(RESOURCE_TAG_KEY).value(RESOURCE_TAG_VALUE).build(),
            Tag.builder().key(SYSTEM_TAG_KEY).value(SYSTEM_TAG_VALUE).build(),
            Tag.builder().key(STACK_TAG_KEY).value(STACK_TAG_VALUE).build()
    ).collect(Collectors.toList());

    public static final List<software.amazon.awssdk.services.ssm.model.Tag> SERVICE_MODEL_TAG_WITHOUT_RESOURCE_TAGS = Collections.singletonList(
            software.amazon.awssdk.services.ssm.model.Tag.builder().key(SYSTEM_TAG_KEY).value(SYSTEM_TAG_VALUE).build()
    );

    public static final List<software.amazon.awssdk.services.ssm.model.Tag> SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS = Stream.of(
            software.amazon.awssdk.services.ssm.model.Tag.builder().key(RESOURCE_TAG_KEY).value(RESOURCE_TAG_VALUE).build(),
            software.amazon.awssdk.services.ssm.model.Tag.builder().key(SYSTEM_TAG_KEY).value(SYSTEM_TAG_VALUE).build(),
            software.amazon.awssdk.services.ssm.model.Tag.builder().key(STACK_TAG_KEY).value(STACK_TAG_VALUE).build())
            .collect(Collectors.toList());

    public static final List<Tag> RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG = Collections.singletonList(
            Tag.builder().key(SYSTEM_TAG_KEY).value(SYSTEM_TAG_VALUE).build()
    );

    public static final Set<software.amazon.awssdk.services.ssm.model.Tag> TRANSLATED_SDK_TAG_WITHOUT_RESOURCE_TAG = Collections.singleton(
            software.amazon.awssdk.services.ssm.model.Tag.builder().key(SYSTEM_TAG_KEY).value(SYSTEM_TAG_VALUE).build()
    );
    public static final String RESOURCE_TYPE = "MaintenanceWindow";

    public static final List<String> TAGS_TO_REMOVE = Stream.of(RESOURCE_TAG_KEY, STACK_TAG_KEY).collect(Collectors.toList());

    public static final String SCHEDULE_OFFSET_SCHEDULE = "cron(0 */5 * ? * THU#2 *)";

    public static final Integer SCHEDULE_OFFSET = 2;
}
