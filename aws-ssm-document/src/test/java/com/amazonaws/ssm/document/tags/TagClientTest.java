package com.amazonaws.ssm.document.tags;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@ExtendWith(MockitoExtension.class)
public class TagClientTest {

    private static final String SAMPLE_DOCUMENT_NAME = "testDocument";
    private static final List<Tag> SAMPLE_TAGS = ImmutableList.of(
        Tag.builder().key("tagKey5").value("tagValue5").build(),
        Tag.builder().key("tagKey6").value("tagValue6").build()
    );

    private final TagClient unitUnderTest = new TagClient();

    @Mock
    private SsmClient ssmClient;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Test
    public void testAddTags_EmptyList_verifyNoCall() {
        unitUnderTest.addTags(ImmutableList.of(), SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

        Mockito.verifyZeroInteractions(proxy);
    }

    @Test
    public void testAddTags_verifyCall() {
        final AddTagsToResourceRequest addTagsRequest = AddTagsToResourceRequest.builder()
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .resourceId(SAMPLE_DOCUMENT_NAME)
            .tags(SAMPLE_TAGS)
            .build();

        unitUnderTest.addTags(SAMPLE_TAGS, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

        Mockito.verify(proxy, Mockito.times(1)).injectCredentialsAndInvokeV2(eq(addTagsRequest), any());
    }

    @Test
    public void testRemoveTags_EmptyList_verifyNoCall() {
        unitUnderTest.removeTags(ImmutableList.of(), SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

        Mockito.verifyZeroInteractions(proxy);
    }

    @Test
    public void testRemoveTags_EmptyList_verifyCall() {
        final List<String> tagKeysToRemove = ImmutableList.of(
            "tagKey5", "tagKey6"
        );

        final RemoveTagsFromResourceRequest removeTagsRequest = RemoveTagsFromResourceRequest.builder()
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .resourceId(SAMPLE_DOCUMENT_NAME)
            .tagKeys(tagKeysToRemove)
            .build();

        unitUnderTest.removeTags(SAMPLE_TAGS, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

        Mockito.verify(proxy, Mockito.times(1)).injectCredentialsAndInvokeV2(eq(removeTagsRequest), any());
    }

    @Test
    public void testListTags_verifyCall() {
        final ListTagsForResourceRequest listTagsRequest = ListTagsForResourceRequest.builder()
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .resourceId(SAMPLE_DOCUMENT_NAME)
            .build();

        final ListTagsForResourceResponse resourceResponse = ListTagsForResourceResponse.builder()
            .tagList(SAMPLE_TAGS)
            .build();
        Mockito.when(proxy.injectCredentialsAndInvokeV2(eq(listTagsRequest), any())).thenReturn(resourceResponse);

        final List<Tag> tags = unitUnderTest.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

        Assertions.assertEquals(SAMPLE_TAGS, tags);

    }
}
