package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.ssm.patchbaseline.utils.TagUtils;
import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.MapUtils;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;

public class TagHelper {
    public static final String NO_DUPLICATE_TAGS = "Duplicate TagKeys are not permitted.";
    public static final String NO_SYSTEM_TAGS = "One or more tag keys uses the system tag prefix 'aws:'. "
            + "Tag keys with this prefix are for AWS internal use only.";
    public static final String TAG_KEY_NULL = "TagKey cannot be null.";
    public static final String TAG_NULL = "Tag cannot be null.";
    public static final String SYSTEM_TAG_PREFIX = "aws:";
    public static final String TAG_KEY_PROPERTY = "Key";
    public static final String TAG_VALUE_PROPERTY = "Value";

    /**
     * Validates and merges tags from desiredResourceTags, CloudFormation System Tags and ResourceModel Tags
     * into a single list to pass on to the tag-on-create SSM call.
     * @param request request
     * @param tags  Tags in desired model
     * @return list of all tags that should be included in the tag-on-create SSM call
     */
    public List<Tag> validateAndMergeTagsForCreate(final ResourceHandlerRequest<ResourceModel> request,
                                                   final List<software.amazon.ssm.patchbaseline.Tag> tags) {
        Map<String, String> desiredResourceTags = request.getDesiredResourceTags();
        Map<String, String> systemTags = request.getSystemTags();
        validateCustomerSuppliedTags(desiredResourceTags);
        Map<String, String> tagsMap = convertResourceModelTagsToMap(tags);

        Map<String, String> tagsToAdd = TagUtils.consolidateTags(desiredResourceTags, systemTags, tagsMap);

        return convertToTagList(tagsToAdd);
    }


    /**
     * Convert map into SSM Tag List.
     * @param tags Tags as a map
     * @return Tags as a list of SSM tags
     */
    @VisibleForTesting
    static List<Tag> convertToTagList(final Map<String, String> tags) {
        List<Tag> tagList = new ArrayList<>();
        if (tags != null) {
            tagList = tags.entrySet()
                    .stream()
                    .collect(Collectors.mapping(entry ->
                                    Tag.builder()
                                       .key(entry.getKey())
                                       .value(entry.getValue())
                                       .build(),
                            Collectors.toList()));
        }
        return tagList;
    }

    /**
     * Validate Customer-supplied tags, which can be either Resource-Level tags or Stack-Level
     * tags, so we need to validate both. We defer to the SSM APIs to perform most validation, but
     * some constraints must be validated prior to calling the APIs.
     * @param customerTags Tags that were provided by an end user
     */
    @VisibleForTesting
    protected void validateCustomerSuppliedTags(Map<String, String> customerTags) {
        if (MapUtils.isEmpty(customerTags)) {
            return;
        }
        for (Map.Entry<String, String> tag : customerTags.entrySet()) {
            if (tag.getKey() == null) {
                throw new SsmCfnClientSideException(TAG_KEY_NULL);
            }
            if (tag.getKey().toLowerCase().startsWith(SYSTEM_TAG_PREFIX)) {
                throw new SsmCfnClientSideException(NO_SYSTEM_TAGS);
            }
        }
    }

    /**
     * Make one SSM call to remove old tags and a second SSM call to add/overwrite new tags.  Will only
     * operate on tags that are defined in the old/new versions of the CloudFormation template. Tag keys
     * that are not included in the old/new CloudFormation templates should remain untouched.
     * @param request  Request data passed to update handler
     * @param ssmResourceType SSM resource type that would be used in AddTagsToResource (or Remove...) SSM calls
     * @param ssmClient Amazon SSM client
     * @param proxy AmazonWebServicesClientProxy
     */
    public void updateTagsForResource(final ResourceHandlerRequest<ResourceModel> request,
                                      String ssmResourceType,
                                      SsmClient ssmClient,
                                      final AmazonWebServicesClientProxy proxy) {
        ResourceModel model = request.getDesiredResourceState();
        String baselineId = model.getId();

        List<Tag> newTags = validateAndMergeTagsForCreate(request, request.getDesiredResourceState().getTags());
        ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                                                                        .resourceType(ssmResourceType)
                                                                        .resourceId(baselineId)
                                                                        .build();
        ListTagsForResourceResponse listTagsForResourceResponse =
                proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest, ssmClient::listTagsForResource);
        List<Tag> oldTags = listTagsForResourceResponse.tagList();

        Map<String, String> newTagsMap = convertRequestTagsToMap(newTags);
        Map<String, String> oldTagsMap = convertRequestTagsToMap(oldTags);

        Map<String, String> tagsToRemove = TagUtils.getTagsToDelete(newTagsMap, oldTagsMap);
        Map<String, String> tagsToAdd = TagUtils.getTagsToCreate(newTagsMap, oldTagsMap);

        List<String> ssmKeysToRemove = new ArrayList<>(tagsToRemove.keySet());
        List<Tag> ssmTagsToAdd = convertToTagList(tagsToAdd);

        if (!ssmKeysToRemove.isEmpty()) {
            RemoveTagsFromResourceRequest removeTagsRequest = RemoveTagsFromResourceRequest.builder()
                                                                        .resourceType(ssmResourceType)
                                                                        .resourceId(baselineId)
                                                                        .tagKeys(ssmKeysToRemove)
                                                                        .build();
            RemoveTagsFromResourceResponse removeTagsFromResourceResponse =
                    proxy.injectCredentialsAndInvokeV2(removeTagsRequest, ssmClient::removeTagsFromResource);
        }

        if (!ssmTagsToAdd.isEmpty()) {
            AddTagsToResourceRequest addTagsRequest = AddTagsToResourceRequest.builder()
                                                            .resourceType(ssmResourceType)
                                                            .resourceId(baselineId)
                                                            .tags(ssmTagsToAdd)
                                                            .build();
            AddTagsToResourceResponse addTagsToResourceResponse =
                    proxy.injectCredentialsAndInvokeV2(addTagsRequest, ssmClient::addTagsToResource);
        }
    }

    /**
     * Convert tag list to map and perform validation.
     * @param tags List of SSM tags
     * @return Tags as a map
     */
    @VisibleForTesting
    protected Map<String, String> convertRequestTagsToMap(List<Tag> tags) {
        return convertResourceTagsToMapAndValidate(tags, Tag::key, Tag::value);
    }

    /**
     * Convert tag list to map and perform validation.
     * @param tags List of SSM tags
     * @return Tags as a map
     */
    @VisibleForTesting
    protected Map<String, String> convertResourceModelTagsToMap(List<software.amazon.ssm.patchbaseline.Tag> tags) {
        return convertResourceTagsToMapAndValidate(tags, software.amazon.ssm.patchbaseline.Tag::getKey,
                software.amazon.ssm.patchbaseline.Tag::getValue);
    }
    /**
     * Convert tag list to map and perform validation. Resource tags are passed as a list, so it's possible
     * that null or duplicate entries exist
     * @param tags List of SSM tags
     * @return Tags as a map
     */
    private <T> Map<String, String> convertResourceTagsToMapAndValidate(
            List<T> tags, Function<T, String> keyMapper, Function<T, String> valueMapper) {
        if (CollectionUtils.isNullOrEmpty(tags)) {
            return new HashMap<>();
        }

        Map<String, String> tagSet = new HashMap<>();

        for (T tag : tags) {
            if (tag == null) {
                throw new SsmCfnClientSideException(TAG_NULL);
            }

            String tagKey = keyMapper.apply(tag);

            if (tagSet.containsKey(tagKey)) {
                throw new SsmCfnClientSideException(NO_DUPLICATE_TAGS);
            }

            tagSet.put(tagKey, valueMapper.apply(tag));
        }

        validateCustomerSuppliedTags(tagSet);

        return tagSet;
    }



}
