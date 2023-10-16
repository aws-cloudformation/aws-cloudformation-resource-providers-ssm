package com.amazonaws.ssm.document.tags;

import com.amazonaws.ssm.document.Tag;
import com.google.common.collect.ImmutableList;
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

@ExtendWith(MockitoExtension.class)
public class TagUtilTest {

    private static final List<Tag> SAMPLE_PREVIOUS_MODEL_TAGS = ImmutableList.of(
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey1").value("tagModelValue1").build(),
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey2").value("tagModelValue2").build()
    );
    private static final List<com.amazonaws.ssm.document.Tag> SAMPLE_MODEL_TAGS = ImmutableList.of(
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey3").value("tagModelValue3").build(),
            com.amazonaws.ssm.document.Tag.builder().key("tagModelKey4").value("tagModelValue4").build()
    );
    private static final AwsErrorDetails ACCESS_DENIED_ERROR_DETAILS = AwsErrorDetails.builder()
            .errorCode("AccessDeniedException")
            .build();

    @Mock
    private SsmException ssmException;

    private TagUtil unitUnderTest;

    @BeforeEach
    private void setup() {
        unitUnderTest = TagUtil.getInstance();
    }

    @Test
    public void testShouldSoftFail_NotAccessDeniedException_isFalse() {
        final AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("AccessDeniedException")
                .build();

        Mockito.when(ssmException.awsErrorDetails()).thenReturn(errorDetails);

        Assertions.assertFalse(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmException));
    }

    @Test
    public void testShouldSoftFail_AccessDeniedException_PreviousModelTagsNonNull_currentModelTagsNull_isFalse() {
        Mockito.when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);

        Assertions.assertFalse(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, null, ssmException));
    }

    @Test
    public void testShouldSoftFail_AccessDeniedException_PreviousModelTagsNull_CurrentModelTagsNonNull_isFalse() {
        Mockito.when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);

        Assertions.assertFalse(unitUnderTest.isResourceTagModified(null, SAMPLE_MODEL_TAGS, ssmException));
    }

    @Test
    public void testShouldSoftFail_AccessDeniedException_PreviousModelTagsNonNull_CurrentModelTagsNonNull_isFalse() {
        Mockito.when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);

        Assertions.assertFalse(unitUnderTest.isResourceTagModified(SAMPLE_PREVIOUS_MODEL_TAGS, SAMPLE_MODEL_TAGS, ssmException));
    }

    @Test
    public void testShouldSoftFail_AccessDeniedException_PreviousModelTagsNull_CurrentModelTagsNull_isTrue() {
        Mockito.when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);

        Assertions.assertTrue(unitUnderTest.isResourceTagModified(null, null, ssmException));
    }

    @Test
    public void testShouldSoftFail_AccessDeniedException_PreviousModelTagsEmpty_CurrentModelTagsEmpty_isTrue() {
        Mockito.when(ssmException.awsErrorDetails()).thenReturn(ACCESS_DENIED_ERROR_DETAILS);

        Assertions.assertTrue(unitUnderTest.isResourceTagModified(ImmutableList.of(), ImmutableList.of(), ssmException));
    }
}
