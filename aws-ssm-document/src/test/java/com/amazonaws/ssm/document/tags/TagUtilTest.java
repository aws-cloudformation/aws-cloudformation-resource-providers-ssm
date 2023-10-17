package com.amazonaws.ssm.document.tags;

import static org.mockito.Mockito.when;

import com.amazonaws.ssm.document.Tag;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ssm.model.SsmException;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TagUtilTest {

    private static final List<Tag> SAMPLE_PREVIOUS_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey1").value("tagModelValue1").build(),
            Tag.builder().key("tagModelKey2").value("tagModelValue2").build()
    );
    private static final List<Tag> SAMPLE_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagModelKey3").value("tagModelValue3").build(),
            Tag.builder().key("tagModelKey4").value("tagModelValue4").build()
    );
    private static final Map<String, String> SAMPLE_MODEL_TAGS_MAP = ImmutableMap.of(
            "tagModelKey3", "tagModelValue3",
            "tagModelKey4", "tagModelValue4"
    );
    private static final Map<String, String> SAMPLE_STACK_TAGS_MAP = ImmutableMap.of(
            "tagStackKey1", "tagStackValue1",
            "tagStackKey2", "tagStackValue2"
    );
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS_MAP = ImmutableMap.of(
            "tagSystemKey1", "tagSystemValue1",
            "tagSystemKey2", "tagSystemValue2"
    );

    @Mock
    private SsmException ssmException;

    private TagUtil unitUnderTest;

    @BeforeEach
    private void setup() {
        unitUnderTest = TagUtil.getInstance();
    }

    @Test
    public void testIsResourceTagModified_isTrue() {
        Assertions.assertTrue(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS));
    }

    @Test
    public void testIsResourceTagModified_isFalse() {
        Assertions.assertFalse(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_PREVIOUS_MODEL_TAGS));
    }

    @Test
    public void testIsResourceTagModified_PreviousModelTagsNonNull_currentModelTagsNull_isTrue() {
        Assertions.assertTrue(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, null));
    }

    @Test
    public void testIsResourceTagModified_PreviousModelTagsNull_CurrentModelTagsNonNull_isTrue() {
        Assertions.assertTrue(unitUnderTest.isResourceTagModified(null, SAMPLE_MODEL_TAGS));
    }

    @Test
    public void testIsResourceTagModified_PreviousModelTagsNull_CurrentModelTagsNull_isFalse() {
        Assertions.assertFalse(unitUnderTest.isResourceTagModified(null, null));
    }

    @Test
    public void testTranslateTags_HandlesTagList_ReturnsStringStringMap() {
        Assertions.assertEquals(SAMPLE_MODEL_TAGS_MAP, unitUnderTest.translateTags(SAMPLE_MODEL_TAGS));
    }

    @Test
    public void testTranslateTags_HandlesEmptyList_ReturnsEmptyMap() {
        Assertions.assertEquals(ImmutableMap.of(), unitUnderTest.translateTags(ImmutableList.of()));
    }

    @Test
    public void testTranslateTags_HandlesNullList_ReturnsEmptyMap() {
        Assertions.assertEquals(ImmutableMap.of(), unitUnderTest.translateTags(null));
    }

    @Test
    public void testIsTaggingPermissionFailure_NotAccessDeniedException_ReturnsFalse() {
        when(ssmException.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorCode("InternalServerError")
                .errorMessage("InternalServerError")
                .build());

        Assertions.assertFalse(unitUnderTest.isTaggingPermissionFailure(ssmException));
    }

    @Test
    public void testIsTaggingPermissionFailure_AccessDeniedException_CauseNotFromTagAction_ReturnsFalse() {
        when(ssmException.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorCode("AccessDeniedException")
                .errorMessage("InternalServerError")
                .build());

        Assertions.assertFalse(unitUnderTest.isTaggingPermissionFailure(ssmException));

    }

    @Test
    public void testIsTaggingPermissionFailure_AccessDeniedException_CauseFromAddTagsAction_ReturnsTrue() {
        when(ssmException.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorCode("AccessDeniedException")
                .errorMessage("ssm:AddTagsToResource")
                .build());

        Assertions.assertTrue(unitUnderTest.isTaggingPermissionFailure(ssmException));
    }

    @Test
    public void testIsTaggingPermissionFailure_AccessDeniedException_CauseFromRemoveTagsAction_ReturnsTrue() {
        when(ssmException.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorCode("AccessDeniedException")
                .errorMessage("ssm:RemoveTagsFromResource")
                .build());

        Assertions.assertTrue(unitUnderTest.isTaggingPermissionFailure(ssmException));
    }

    @Test
    public void testConsolidateTags_HandlesAllPresent_ReturnsConsolidatedMap() {
        final Map<String, String> expectedResult = ImmutableMap.<String, String>builder()
                .putAll(SAMPLE_MODEL_TAGS_MAP)
                .putAll(SAMPLE_STACK_TAGS_MAP)
                .putAll(SAMPLE_SYSTEM_TAGS_MAP).build();

        Assertions.assertEquals(
                expectedResult,
                unitUnderTest.consolidateTags(SAMPLE_MODEL_TAGS_MAP, SAMPLE_STACK_TAGS_MAP, SAMPLE_SYSTEM_TAGS_MAP));
    }

    @Test
    public void testConsolidateTags_HandlesAllEmpty_ReturnsEmptyMap() {
        Assertions.assertEquals(
                ImmutableMap.of(),
                unitUnderTest.consolidateTags(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of()));
    }

    @Test
    public void testConsolidateTags_HandlesAllNull_ReturnsEmptyMap() {
        Assertions.assertEquals(
                ImmutableMap.of(),
                unitUnderTest.consolidateTags(null, null, null));
    }
}
