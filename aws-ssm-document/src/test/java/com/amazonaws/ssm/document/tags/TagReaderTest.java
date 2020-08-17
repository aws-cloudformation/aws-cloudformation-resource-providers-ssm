package com.amazonaws.ssm.document.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@ExtendWith(MockitoExtension.class)
public class TagReaderTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";

    private static final List<Tag> SAMPLE_EXISTING_TAGS = ImmutableList.of(
        Tag.builder().key("tagKey1").value("tagValue1").build(),
        Tag.builder().key("tagKey2").value("tagValue2").build()
    );

    private static final Map<String, String> SAMPLE_TAG_MAP = ImmutableMap.of(
        "tagKey1", "tagValue1",
        "tagKey2", "tagValue2"
    );

    @Mock
    private SsmClient ssmClient;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private TagClient tagClient;

    private TagReader unitUnderTest;

    @BeforeEach
    private void setup() {
        unitUnderTest = new TagReader(tagClient);
    }

    @Test
    public void testGetDocumentTags_verifyResponse() {
        Mockito.when(tagClient.listTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy)).thenReturn(SAMPLE_EXISTING_TAGS);

        Assertions.assertEquals(SAMPLE_TAG_MAP, unitUnderTest.getDocumentTags(SAMPLE_DOCUMENT_NAME, ssmClient, proxy));
    }
}
