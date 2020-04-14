package software.amazon.ssm.patchbaseline;

import java.util.ArrayList;

public class TestConstants {

    public static final String BASELINE_ID = "pb-12345678901234567";

    public static final String BASELINE_NAME = "ubuntu";

    public static final String BASELINE_DESCRIPTION = "pb for ubuntu";

    public static final String OPERATING_SYSTEM = "UBUNTU";

    public static final String BAD_BASELINE_ID = "pb____0987654321";

    public static final String UPDATED_BASELINE_NAME = "UPDATED_UBUNTU";

    public static final String UPDATED_BASELINE_DESC = "NEW AND IMPROVED";

    public static final String CLIENT_REQUEST_TOKEN = "FakeClientRequestToken";

    public static final String CFN_KEY = "cfnkey";

    public static final String CFN_VALUE = "cfnvalue";

    public static final String TAG_KEY = "stage";

    public static final String TAG_VALUE = "Gamma";

    public static final String NEW_TAG_KEY = "newStage";

    public static final String NEW_TAG_VALUE = "newGamma";

    public static final String UPDATED_CFN_KEY = "updatedcfnkey";

    public static final String UPDATED_CFN_VALUE = "updatedcfnvalue";

    public static final String SYSTEM_TAG_KEY = "aws:cloudformation:stack-name";

    public static final ArrayList<String> ACCEPTED_PATCHES = new ArrayList<String>() {{
        add("curl");
        add("apache");
    }};

    public static final ArrayList<String> REJECTED_PATCHES = new ArrayList<String>() {{
        add("something-bad");

    }};

    public static final ArrayList<String> PATCH_GROUPS = new ArrayList<String>() {{
        add("mypatch");
        add("icecream");
    }};

    public static final ArrayList<String> UPDATED_PATCH_GROUPS = new ArrayList<String>() {{
        add("mypatch");
        add("foo");
        add("baz");
    }};

    public static final ArrayList<String> UPDATED_ACCEPTED_PATCHES = new ArrayList<String>() {{
        add("python36");
        add("gcc");
    }};

    public static final ArrayList<String> UPDATED_REJECTED_PATCHES = new ArrayList<String>() {{
        add("vim");
        add("aptitude");
    }};

    public enum ComplianceLevel {UNSPECIFIED, INFORMATIONAL, LOW, MEDIUM, HIGH, CRITICAL};

}
