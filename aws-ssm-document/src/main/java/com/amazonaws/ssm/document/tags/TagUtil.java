package com.amazonaws.ssm.document.tags;

import com.amazonaws.ssm.document.Tag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.ssm.model.SsmException;

import javax.annotation.Nullable;
import java.util.List;

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
        return (previousResourceModelTags == null || previousResourceModelTags.isEmpty())
                && (currentResourceModelTags == null || currentResourceModelTags.isEmpty());
    }

    public boolean isTaggingPermissionFailure(@NonNull final SsmException exception) {
        final boolean isAccessDeniedException = ACCESS_DENIED_ERROR_CODE.equalsIgnoreCase(exception.awsErrorDetails().errorCode());

        final boolean isTaggingException = exception.awsErrorDetails().errorMessage().contains(ADD_TAGS_ACTION)
                || exception.awsErrorDetails().errorMessage().contains(REMOVE_TAGS_ACTION);

        return isAccessDeniedException && isTaggingException;
    }
}
