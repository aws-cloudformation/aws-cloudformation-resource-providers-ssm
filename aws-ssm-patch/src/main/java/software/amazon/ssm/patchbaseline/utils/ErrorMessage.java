package software.amazon.ssm.patchbaseline.utils;

/**
 * Error messages for TagHelper.
 */
public class ErrorMessage {

    public static final String NO_DUPLICATE_TAGS = "Duplicate TagKeys are not permitted.";
    public static final String NO_SYSTEM_TAGS = "One or more tag keys uses the system tag prefix 'aws:'. "
            + "Tag keys with this prefix are for AWS internal use only.";
    public static final String TAG_KEY_NULL = "TagKey cannot be null.";
    public static final String TAG_NULL = "Tag cannot be null.";
}
