package software.amazon.ssm.maintenancewindow.util;

import com.google.common.collect.Maps;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.ssm.maintenancewindow.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class TagUtil {

    private static final String MAINTENANCE_WINDOW_RESOURCE_NAME = "MaintenanceWindow";

    /**
     * combine the resource tags and system tags
     *
     * @param resourceTags resource tags and stack tags of the Resource model
     * @param systemTags   system tags of the Resource Handler request
     * @return List of the consolidated tags of the Resource Handler request
     */
    public static List<Tag> consolidateTags(final Map<String, String> resourceTags, final Map<String, String> systemTags) {
        if (resourceTags == null && systemTags == null) return null;
        final Map<String, String> consolidatedTags = Maps.newHashMap();
        if (!CollectionUtils.isNullOrEmpty(resourceTags)) {
            consolidatedTags.putAll(resourceTags); //This will overwrite tags with same keys but different values. tags have both resource tag and stack tag
        }
        if (!CollectionUtils.isNullOrEmpty(systemTags)) {
            consolidatedTags.putAll(systemTags);
        }
        return consolidatedTags.keySet().stream().map(key -> Tag.builder()
                .key(key)
                .value(consolidatedTags.get(key))
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * build AddTagsToResourceRequest
     *
     * @param windowId  Id of the window to add the tags to
     * @param tagsToAdd list of tags to add
     * @return AddTagsToResourceRequest
     */
    public static AddTagsToResourceRequest buildAddTagsToResourceRequest(final String windowId,
                                                                         final List<software.amazon.awssdk.services.ssm.model.Tag> tagsToAdd) {
        return AddTagsToResourceRequest.builder()
                .resourceType(MAINTENANCE_WINDOW_RESOURCE_NAME)
                .resourceId(windowId)
                .tags(tagsToAdd)
                .build();
    }

    /**
     * build RemoveTagsFromResourceRequest
     *
     * @param windowId     Id of the window to remove the tags from
     * @param tagsToRemove list of tags keys to remove
     * @return RemoveTagsFromResourceRequest
     */
    public static RemoveTagsFromResourceRequest buildRemoveTagsFromResourceRequest(final String windowId, final List<String> tagsToRemove) {
        return RemoveTagsFromResourceRequest.builder()
                .resourceType(MAINTENANCE_WINDOW_RESOURCE_NAME)
                .resourceId(windowId)
                .tagKeys(tagsToRemove)
                .build();
    }

    /**
     * build ListTagsForResourceRequest
     *
     * @param windowId Id of the window to list tags of
     * @return ListTagsForResourceRequest
     */
    public static ListTagsForResourceRequest buildListTagsForResourceRequest(final String windowId) {
        return ListTagsForResourceRequest.builder()
                .resourceType(MAINTENANCE_WINDOW_RESOURCE_NAME)
                .resourceId(windowId)
                .build();
    }

    /**
     * translate list of resource model tags to service model tags
     *
     * @param tags list of resource model tags to translate
     * @return list of service model tags
     */
    public static Set<software.amazon.awssdk.services.ssm.model.Tag> translateTagsToSdk(final List<Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(tag -> software.amazon.awssdk.services.ssm.model.Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build()
        ).collect(Collectors.toSet());
    }

}
