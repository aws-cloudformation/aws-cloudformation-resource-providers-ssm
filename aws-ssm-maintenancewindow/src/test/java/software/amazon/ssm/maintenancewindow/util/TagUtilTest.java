package software.amazon.ssm.maintenancewindow.util;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.ssm.maintenancewindow.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindow.TestConstants.CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SYSTEM_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.TAGS_TO_REMOVE;
import static software.amazon.ssm.maintenancewindow.TestConstants.TRANSLATED_SDK_TAG_WITHOUT_RESOURCE_TAG;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_ID;

public class TagUtilTest {

    @Test
    void consolidateTagsWithNullResourceTagsAndNullSystemTags() {
        final List<Tag> consolidatedTags = TagUtil.consolidateTags(null, null);
        assertThat(consolidatedTags).isNull();
    }

    @Test
    void consolidatedTagsWithEmptyResourceTagsAndEmptySystemTags() {
        final List<Tag> consolidatedTags = TagUtil.consolidateTags(Collections.emptyMap(), Collections.emptyMap());

        assertThat(consolidatedTags).isEmpty();
    }

    @Test
    void consolidatedTagsWithResourceTagsAndSystemTags() {
        final List<Tag> consolidatedTags = TagUtil.consolidateTags(RESOURCE_TAGS, SYSTEM_TAGS);

        assertThat(consolidatedTags).isEqualTo(CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS);
    }

    @Test
    void buildAddTagsToResourceRequestTest() {
        final AddTagsToResourceRequest addTagsRequest = TagUtil.buildAddTagsToResourceRequest(WINDOW_ID, SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS);

        final AddTagsToResourceRequest expectedRequest = AddTagsToResourceRequest.builder()
                .resourceId(WINDOW_ID)
                .resourceType(RESOURCE_TYPE)
                .tags(SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS)
                .build();
        assertThat(addTagsRequest).isEqualTo(expectedRequest);
    }

    @Test
    void buildRemoveTagsFromResourceRequestTest() {
        final RemoveTagsFromResourceRequest removeTagsRequest = TagUtil.buildRemoveTagsFromResourceRequest(WINDOW_ID, TAGS_TO_REMOVE);

        final RemoveTagsFromResourceRequest expectedRequest = RemoveTagsFromResourceRequest.builder()
                .resourceId(WINDOW_ID)
                .resourceType(RESOURCE_TYPE)
                .tagKeys(TAGS_TO_REMOVE)
                .build();
        assertThat(removeTagsRequest).isEqualTo(expectedRequest);
    }

    @Test
    void buildListTagsForResourceRequestTest() {
        final ListTagsForResourceRequest listTagsRequest = TagUtil.buildListTagsForResourceRequest(WINDOW_ID);

        final ListTagsForResourceRequest expectedRequest = ListTagsForResourceRequest.builder()
                .resourceId(WINDOW_ID)
                .resourceType(RESOURCE_TYPE)
                .build();
        assertThat(listTagsRequest).isEqualTo(expectedRequest);
    }

    @Test
    void translateTagsToSdkTest() {
        final Set<software.amazon.awssdk.services.ssm.model.Tag> sdkTags = TagUtil.translateTagsToSdk(RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG);

        assertThat(sdkTags).isEqualTo(TRANSLATED_SDK_TAG_WITHOUT_RESOURCE_TAG);
    }

    @Test
    void translateTagsToSdkTestWithNullTags() {
        final Set<software.amazon.awssdk.services.ssm.model.Tag> sdkTags = TagUtil.translateTagsToSdk(null);

        assertThat(sdkTags).isNull();
    }
}
