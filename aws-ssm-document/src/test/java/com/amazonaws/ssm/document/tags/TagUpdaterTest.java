package com.amazonaws.ssm.document.tags;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

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

    private static final Map<String, String> SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
            "tagKey1", "tagValue1",
            "tagKey2", "tagValue2",
            "tagKey3", "tagValue3"
    );

    private static final List<com.amazonaws.ssm.document.Tag> SAMPLE_PREVIOUS_MODEL_TAGS = ImmutableList.of(
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey1").value("tagModelValue1").build(),
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey2").value("tagModelValue2").build()
    );
    private static final List<com.amazonaws.ssm.document.Tag> SAMPLE_MODEL_TAGS = ImmutableList.of(
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey3").value("tagModelValue3").build(),
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey4").value("tagModelValue4").build()
    );

    @Mock
    private SsmClient ssmClient;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private TagClient tagClient;

    @Mock
    private TagUtil tagUtil;

    @Mock
    private SsmException ssmException;

    private TagUpdater unitUnderTest;

    @BeforeEach
    private void setup() {
        unitUnderTest = new TagUpdater(tagClient, tagUtil);
    }

    @Test
    public void testUpdateTags_existingTagsIsEmpty_verifyCalls() {

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, ImmutableMap.of(), SAMPLE_RESOURCE_REQUEST_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        verifyTagClientCalls(SAMPLE_RESOURCE_REQUEST_TAGS_SSM_FORMAT, ImmutableList.of());
    }

    @Test
    public void testUpdateTags_requestedTagsIsEmpty_verifyCalls() {

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, ImmutableMap.of(),
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

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

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, SAMPLE_RESOURCE_REQUEST_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
    }

    @Test
    public void testUpdateTags_existingTagKeyIsUpdated_verifyTagsAddedAndRemoved() {
        final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
                "tagKey1", "tagValue2",
                "tagKey5", "tagValue5",
                "tagKey6", "tagValue6"
        );

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

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, SAMPLE_RESOURCE_REQUEST_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
    }

    @Test
    public void testUpdateTags_addTagsAPIAccessDenied_shouldSoftFail_verifyTagsAddedAndRemoved() {
        final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
                "tagKey1", "tagValue2",
                "tagKey5", "tagValue5",
                "tagKey6", "tagValue6"
        );

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

        Mockito.doThrow(ssmException).when(tagClient).addTags(expectedTagsToAdd, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.doThrow(ssmException).when(tagClient).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.when(tagUtil.shouldSoftFailTags(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmException)).thenReturn(true);

        unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, SAMPLE_RESOURCE_REQUEST_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger);

        verifyTagClientCalls(expectedTagsToAdd, expectedTagsToRemove);
        Mockito.verify(logger, Mockito.times(1)).log(String.format("Soft fail adding tags to %s", SAMPLE_DOCUMENT_NAME));
        Mockito.verify(logger, Mockito.times(1)).log(String.format("Soft fail removing tags from %s", SAMPLE_DOCUMENT_NAME));
    }

    @Test
    public void testUpdateTags_addTagsAPIAccessDenied_shouldNotSoftFail_verifyTagsAddedAndRemoved() {
        final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
                "tagKey1", "tagValue2",
                "tagKey5", "tagValue5",
                "tagKey6", "tagValue6"
        );


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

        Mockito.doThrow(ssmException).when(tagClient).addTags(expectedTagsToAdd, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.when(tagUtil.shouldSoftFailTags(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmException)).thenReturn(false);

        Assertions.assertThrows(SsmException.class, () -> unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME, SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, SAMPLE_RESOURCE_REQUEST_TAGS,
                SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmClient, proxy, logger));

        Mockito.verify(tagClient, Mockito.times(1)).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.verify(logger, Mockito.never()).log(String.format("Soft fail adding tags to %s", SAMPLE_DOCUMENT_NAME));
        Mockito.verify(logger, Mockito.never()).log(String.format("Soft fail removing tags from %s", SAMPLE_DOCUMENT_NAME));
    }

    @Test
    public void testUpdateTags_removeTagsAPIAccessDenied_shouldNotSoftFail_verifyTagsAddedAndRemoved() {
        final Map<String, String> SAMPLE_RESOURCE_REQUEST_TAGS = ImmutableMap.of(
                "tagKey1", "tagValue2",
                "tagKey5", "tagValue5",
                "tagKey6", "tagValue6"
        );

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

        Mockito.doThrow(ssmException).when(tagClient).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.when(tagUtil.shouldSoftFailTags(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmException)).thenReturn(false);

        Assertions.assertThrows(SsmException.class, () -> unitUnderTest.updateTags(SAMPLE_DOCUMENT_NAME,
                SAMPLE_EXISTING_RESOURCE_REQUEST_TAGS, SAMPLE_RESOURCE_REQUEST_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS,
                SAMPLE_MODEL_TAGS, ssmClient, proxy, logger));

        Mockito.verify(tagClient, Mockito.times(1)).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.verify(tagClient, Mockito.never()).addTags(expectedTagsToAdd, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        Mockito.verify(logger, Mockito.never()).log(String.format("Soft fail adding tags to %s", SAMPLE_DOCUMENT_NAME));
        Mockito.verify(logger, Mockito.never()).log(String.format("Soft fail removing tags from %s", SAMPLE_DOCUMENT_NAME));
    }

    private void verifyTagClientCalls(final List<Tag> expectedTagsToAdd, final List<Tag> expectedTagsToRemove) {
        InOrder inOrder = Mockito.inOrder(tagClient);

        inOrder.verify(tagClient, Mockito.times(1)).removeTags(expectedTagsToRemove, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);
        inOrder.verify(tagClient, Mockito.times(1)).addTags(expectedTagsToAdd, SAMPLE_DOCUMENT_NAME, ssmClient, proxy);

    }
}
