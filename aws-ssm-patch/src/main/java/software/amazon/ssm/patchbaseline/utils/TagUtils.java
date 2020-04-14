package software.amazon.ssm.patchbaseline.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TagUtils {

    /**
     * Use this method in the CREATE workflow
     * when first adding tags to get a list inclusive of the resource level tags, the stack level tags, and the awsPrefixedTags
     * @param desiredResourceTags tags customer specifies for this particular resource, and stack level tags specified by the customer to be placed on each resource
     * @param systemTags aws prefixed tags passed by CloudFormation
     * @return a consolidated map including all tags
     */
    public static Map<String, String> consolidateTags(final Map<String, String> desiredResourceTags,
                                                      final Map<String, String> systemTags,
                                                      final Map<String, String> tags) {
        Map<String, String> consolidatedTags = Maps.newHashMap();
        if (!CollectionUtils.isNullOrEmpty(systemTags))
            consolidatedTags.putAll(systemTags);
        if (!CollectionUtils.isNullOrEmpty(desiredResourceTags))
            consolidatedTags.putAll(desiredResourceTags); //This will overwrite tags with same keys but different values.
        if (!CollectionUtils.isNullOrEmpty(tags))
            consolidatedTags.putAll(tags); //This will overwrite tags with same keys but different values.
        return consolidatedTags;
    }

    /**
     * @param newTags consolidated current tags
     * @param oldTags consolidated previous tags
     * @return the tags to delete
     */
    public static Map<String, String> getTagsToDelete(final Map<String, String> newTags, final Map<String, String> oldTags) {
        final Map<String, String> tags = new HashMap<>();
        final Set<String> removedKeys = Sets.difference(oldTags.keySet(), newTags.keySet());
        for (String key : removedKeys) {
            tags.put(key, oldTags.get(key));
        }
        return tags;
    }

    /**
     * @param newTags consolidated current tags
     * @param oldTags consolidated previous tags
     * @return the tags to create
     */
    public static Map<String, String> getTagsToCreate(final Map<String, String> newTags, final Map<String, String> oldTags) {
        final Map<String, String> tags = new HashMap<>();
        final Set<Map.Entry<String, String>> entriesToCreate = Sets.difference(newTags.entrySet(), oldTags.entrySet());
        for (Map.Entry<String, String> entry : entriesToCreate) {
            tags.put(entry.getKey(), entry.getValue());
        }
        return tags;
    }

}
