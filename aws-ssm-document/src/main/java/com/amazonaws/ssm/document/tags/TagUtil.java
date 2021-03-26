package com.amazonaws.ssm.document.tags;

import com.amazonaws.ssm.document.Tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import javax.annotation.Nullable;
import java.util.List;
import software.amazon.awssdk.services.ssm.model.SsmException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagUtil {

    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";

    private static TagUtil INSTANCE;

    public static TagUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagUtil();
        }

        return INSTANCE;
    }

    /**
     * During resource create or update, customer may not have permissions to Tagging APIs, but the cloudformation has a concept of stack level tags.
     * When tags are applied at stack level, it doesn't necessarily mean that customer is intending to apply tags to this particular resource.
     * In this scenario, we do a best effort to apply the stack level tags to resource and not fail even if customer does not have permissions.
     *
     * But if customer provided resource level tags, we do a hard fail if customer does not have permissions.
     *
     * @param previousResourceModelTags tags provided previously before update.
     * @param currentResourceModelTags current resource model tags.
     * @param exception Exception occurred.
     * @return if we should do best effort or hard fail.
     */
    public boolean shouldSoftFailTags(@Nullable final List<Tag> previousResourceModelTags,
                                      @Nullable final List<Tag> currentResourceModelTags,
                                      @NonNull final SsmException exception) {
        final boolean isAccessDeniedException = ACCESS_DENIED_ERROR_CODE.equalsIgnoreCase(exception.awsErrorDetails().errorCode());

        // If customer provided only stack level tags and not resource level tags for the Document resource, do not fail on AccessDenied error.
        final boolean hasCustomerNotSpecifiedModelTags = (previousResourceModelTags == null || previousResourceModelTags.isEmpty())
                && (currentResourceModelTags == null || currentResourceModelTags.isEmpty());

        return isAccessDeniedException && hasCustomerNotSpecifiedModelTags;
    }
}
