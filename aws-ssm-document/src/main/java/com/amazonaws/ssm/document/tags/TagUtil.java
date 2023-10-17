package com.amazonaws.ssm.document.tags;

import com.amazonaws.ssm.document.Tag;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.ssm.model.SsmException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagUtil {

    public static final String TAGGING_PERMISSION_MESSAGE_FORMAT = "Did not have IAM permissions to process tags on" +
            " AWS::SSM::Document resource.";

    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    private static final String ADD_TAGS_ACTION = "ssm:AddTagsToResource";
    private static final String REMOVE_TAGS_ACTION = "ssm:RemoveTagsFromResource";

    private static TagUtil INSTANCE;

    public static TagUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagUtil();
        }

        return INSTANCE;
    }

    /**
     * During resource create or update, customer may not have permissions to Tagging APIs, but the cloudformation has a concept of stack level tags.
     * If customer does not have permissions CFN stack must return CfnUnauthorizedTaggingOperationException with error code UnauthorizedTaggingOperation.
     * These cases do need to fail - https://t.corp.amazon.com/P86878814
     *
     * @param previousResourceModelTags tags provided previously before update.
     * @param currentResourceModelTags current resource model tags.
     * @return if resource level tags were modified.
     */
    public boolean isResourceTagModified(@Nullable final List<Tag> previousResourceModelTags,
                                         @Nullable final List<Tag> currentResourceModelTags) {
        if (previousResourceModelTags == null || previousResourceModelTags.isEmpty()) {
            return !(currentResourceModelTags == null || currentResourceModelTags.isEmpty());
        }
        if (currentResourceModelTags == null || currentResourceModelTags.isEmpty()) {
            return !(previousResourceModelTags == null || previousResourceModelTags.isEmpty());
        }
        return !Objects.deepEquals(previousResourceModelTags, currentResourceModelTags);
    }

    /**
     * Check if the exception is caused by missing tagging permissions
     * @param exception
     * @return if the permissions was caused by missing tagging permissions
     */
    public boolean isTaggingPermissionFailure(@NonNull final SsmException exception) {
        final boolean isAccessDeniedException = ACCESS_DENIED_ERROR_CODE.equalsIgnoreCase(exception.awsErrorDetails().errorCode());

        final boolean isTaggingException = exception.awsErrorDetails().errorMessage().contains(ADD_TAGS_ACTION)
                || exception.awsErrorDetails().errorMessage().contains(REMOVE_TAGS_ACTION);

        return isAccessDeniedException && isTaggingException;
    }

    /**
     * Translate document tag to CloudFormation tag
     * @param tags document tag
     * @return map of CloudFormation tag
     */
    public Map<String, String> translateTags(@Nullable final List<Tag> tags) {
        if (tags == null) {
            return ImmutableMap.of();
        }
        return tags.stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    /**
     * Consolidate tags into a list inclusive of the resource level tags, the stack level tags, and the system  tags
     * @param resourceLevelTags tags customer specifies for this particular resource
     * @param stackLevelTags stack level tags specified by the customer to be placed on each resource
     * @param systemTags system prefixed tags passed by CloudFormation
     * @return a consolidated map including all tags
     */
    public Map<String, String> consolidateTags(
            @Nullable final Map<String, String> resourceLevelTags,
            @Nullable final Map<String, String> stackLevelTags,
            @Nullable final Map<String, String> systemTags
    ) {
        final Map<String, String> consolidatedTags = Maps.newHashMap();
        if (stackLevelTags != null) {
            consolidatedTags.putAll(stackLevelTags);
        }
        if (resourceLevelTags != null) {
            consolidatedTags.putAll(resourceLevelTags);
        }
        if (systemTags != null) {
            consolidatedTags.putAll(systemTags);
        }
        return consolidatedTags;
    }
}
