package com.amazonaws.ssm.document.tags;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@ExtendWith(MockitoExtension.class)
public class TagUpdaterTest {

    private static final String SAMPLE_DOCUMENT_NAME = "testDocument";
    private static final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
        "tagKey1", "tagValue1",
        "tagKey5", "tagValue5",
        "tagKey6", "tagValue6"
    );

    private static final List<Tag> SAMPLE_RESOURCE_REQUEST_TAGS_SSM_FORMAT = ImmutableList.of(
        Tag.builder().key("tagKey1").value("tagValue1").build(),
        Tag.builder().key("tagKey5").value("tagValue5").build(),
        Tag.builder().key("tagKey6").value("tagValue6").build()
    );

    private static final List<Tag> SAMPLE_EXISTING_TAGS = ImmutableList.of(
        Tag.builder().key("tagKey1").value("tagValue1").build(),
        Tag.builder().key("tagKey2").value("tagValue2").build(),
        Tag.builder().key("tagKey3").value("tagValue3").build()
    );

    @Mock
    private SsmClient ssmClient;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private TagClient tagClient;

    private TagUpdater unitUnderTest;

    @BeforeEach
    private void setup() {
        unitUnderTest = new TagUpdater(tagClient);
    }

    @Test
    public void testUpdateTags_resourceTagsIsNull_verifyCalls() {
        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_RESOURCE_REQUEST_TAGS, ssmClient, proxy);

        Mockito.verifyZeroInteractions(proxy);
    }

    @Test
    public void testUpdateTags_existingTagsIsEmpty_verifyCalls() {
        Mockito.when(tagClient.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(ImmutableList.of());

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_RESOURCE_REQUEST_TAGS, ssmClient, proxy);

        verifyTagClientCalls(SAMPLE_RESOURCE_REQUEST_TAGS_SSM_FORMAT, ImmutableList.of());
    }

    @Test
    public void testUpdateTags_requestedTagsIsEmpty_verifyCalls() {
        Mockito.when(tagClient.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_EXISTING_TAGS);

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, ImmutableMap.of(), ssmClient, proxy);

        // Expected tags to add
        final List<Tag> expectedTagsToAdd = ImmutableList.of(
        );

        // expected tags to remove
        final List<Tag> expectedTagsToRemove = ImmutableList.of(
            Tag.builder().key("tagKey1").value("tagValue1").build(),
            Tag.builder().key("tagKey2").value("tagValue2").build(),
            Tag.builder().key("tagKey3").value("tagValue3").build()
        );

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
    }

    @Test
    public void testUpdateTags_existingTagsIsNotEmpty_verifyTagsAddedAndRemoved() {
        Mockito.when(tagClient.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_EXISTING_TAGS);

        // Expected tags to add
        final List<Tag> expectedTagsToAdd = ImmutableList.of(
            Tag.builder().key("tagKey5").value("tagValue5").build(),
            Tag.builder().key("tagKey6").value("tagValue6").build()
        );

        // expected tags to remove
        final List<Tag> expectedTagsToRemove = ImmutableList.of(
            Tag.builder().key("tagKey2").value("tagValue2").build(),
            Tag.builder().key("tagKey3").value("tagValue3").build()
        );

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_RESOURCE_REQUEST_TAGS, ssmClient, proxy);

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
   }

    @Test
    public void testUpdateTags_existingTagKeyIsUpdated_verifyTagsAddedAndRemoved() {
        final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
            "tagKey1", "tagValue2",
            "tagKey5", "tagValue5",
            "tagKey6", "tagValue6"
        );

        Mockito.when(tagClient.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_EXISTING_TAGS);

        // Expected tags to add
        final List<Tag> expectedTagsToAdd = ImmutableList.of(
            Tag.builder().key("tagKey1").value("tagValue2").build(),
            Tag.builder().key("tagKey5").value("tagValue5").build(),
            Tag.builder().key("tagKey6").value("tagValue6").build()
        );

        // expected tags to remove
        final List<Tag> expectedTagsToRemove = ImmutableList.of(
            Tag.builder().key("tagKey1").value("tagValue1").build(),
            Tag.builder().key("tagKey2").value("tagValue2").build(),
            Tag.builder().key("tagKey3").value("tagValue3").build()
        );

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_RESOURCE_REQUEST_TAGS, ssmClient, proxy);

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
    }

    private void verifyTagClientCalls(final List<Tag> expectedTagsToAdd, final List<Tag> expectedTagsToRemove) {
        InOrder inOrder = Mockito.inOrder(tagClient);

        inOrder.verify(tagClient, Mockito.times(1)).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        inOrder.verify(tagClient, Mockito.times(1)).addTags(expectedTagsToAdd, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

    }
}
