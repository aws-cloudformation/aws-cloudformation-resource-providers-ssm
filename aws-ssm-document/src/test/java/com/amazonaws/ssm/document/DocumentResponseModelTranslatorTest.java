package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DocumentResponseModelTranslatorTest {
    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_DOCUMENT_CONTENT = "sampleDocumentContent";
    private static final String SAMPLE_VERSION_NAME = "versionName";
    private static final String SAMPLE_DOCUMENT_FORMAT = "format";
    private static final String SAMPLE_DOCUMENT_TYPE = "Command";
    private static final String SAMPLE_DOCUMENT_VERSION = "3";
    private static final String SAMPLE_TARGET_TYPE = "targetType";
    private static final List<Tag> SAMPLE_RESOURCE_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagKey1").value("tagValue1").build(),
            Tag.builder().key("tagKey2").value("tagValue2").build()
    );
    private static final Map<String, String> SAMPLE_TAG_MAP = ImmutableMap.of(
        "tagKey1", "tagValue1",
        "tagKey2", "tagValue2"
    );

    private static final List<software.amazon.awssdk.services.ssm.model.AttachmentContent> SAMPLE_GET_RESPONSE_ATTACHMENTS = ImmutableList.of(
            software.amazon.awssdk.services.ssm.model.AttachmentContent.builder()
                    .name("name1").size(1L).hash("hash1").hashType("hashType1").url("url1").build(),
            software.amazon.awssdk.services.ssm.model.AttachmentContent.builder()
                    .name("name2").size(2L).hash("hash2").hashType("hashType2").url("url2").build()
    );
    private static final List<DocumentRequires> SAMPLE_RESOURCE_MODEL_REQUIRES = ImmutableList.of(
            DocumentRequires.builder().name("sampleRequires1").version("1").build(),
            DocumentRequires.builder().name("sampleRequires2").version("2").build()
    );
    private static final List<software.amazon.awssdk.services.ssm.model.DocumentRequires> SAMPLE_GET_RESPONSE_REQUIRES = ImmutableList.of(
            software.amazon.awssdk.services.ssm.model.DocumentRequires.builder().name("sampleRequires1").version("1").build(),
            software.amazon.awssdk.services.ssm.model.DocumentRequires.builder().name("sampleRequires2").version("2").build()
    );
    private static final ResourceStatus RESOURCE_MODEL_ACTIVE_STATE = ResourceStatus.ACTIVE;
    private static final DocumentStatus SAMPLE_GET_RESPONSE_ACTIVE_STATE = DocumentStatus.ACTIVE;
    private static final String SAMPLE_STATUS_INFO = "resource status info";

    private final DocumentResponseModelTranslator unitUnderTest = new DocumentResponseModelTranslator();

    @Test
    public void testGenerateResourceInformation_DocumentNameIsProvided_verifyResult() {
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final ResourceInformation resourceInformation =
                unitUnderTest.generateResourceInformation(createGetDocumentResponseWithAllAttributes(), SAMPLE_TAG_MAP);

        Assertions.assertEquals(expectedResourceInformation, resourceInformation);
    }

    @Test
    public void testGenerateResourceInformation_DocumentRequiresIsNull_verifyResult() {
        final ResourceModel expectedModel = createResourceModelWithAllAttributes();
        expectedModel.setRequires(null);
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(expectedModel)
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes().toBuilder()
                .requires((Collection<software.amazon.awssdk.services.ssm.model.DocumentRequires>)null).build();

        final ResourceInformation resourceInformation =
                unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP);

        Assertions.assertEquals(expectedResourceInformation, resourceInformation);
    }

    @Test
    public void testGenerateResourceInformation_DocumentStatusIsActive_verifyResult() {
        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(RESOURCE_MODEL_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes();

        Assertions.assertEquals(expectedResourceInformation,
            unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP));
    }

    @Test
    public void testGenerateResourceInformation_DocumentStatusIsCreating_verifyResult() {
        final DocumentStatus documentStatus = DocumentStatus.CREATING;
        final ResourceStatus creating = ResourceStatus.CREATING;

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(creating)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes().toBuilder()
                .status(documentStatus).build();

        Assertions.assertEquals(expectedResourceInformation,
            unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP));
    }

    @Test
    public void testGenerateResourceInformation_DocumentStatusIsUpdating_verifyResult() {
        final DocumentStatus documentStatus = DocumentStatus.UPDATING;
        final ResourceStatus expectedResourceStatus = ResourceStatus.UPDATING;

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(expectedResourceStatus)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes().toBuilder()
                .status(documentStatus).build();

        Assertions.assertEquals(expectedResourceInformation,
            unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP));
    }

    @Test
    public void testGenerateResourceInformation_DocumentStatusIsDeleting_verifyResult() {
        final DocumentStatus documentStatus = DocumentStatus.DELETING;
        final ResourceStatus expectedResourceStatus = ResourceStatus.DELETING;

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(expectedResourceStatus)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes().toBuilder()
                .status(documentStatus).build();

        Assertions.assertEquals(expectedResourceInformation,
            unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP));
    }

    @Test
    public void testGenerateResourceInformation_DocumentStatusIsFailed_verifyResult() {
        final DocumentStatus documentStatus = DocumentStatus.FAILED;
        final ResourceStatus expectedResourceStatus = ResourceStatus.FAILED;

        final ResourceInformation expectedResourceInformation = ResourceInformation.builder()
                .resourceModel(createResourceModelWithAllAttributes())
                .status(expectedResourceStatus)
                .statusInformation(SAMPLE_STATUS_INFO)
                .build();

        final GetDocumentResponse getDocumentResponse = createGetDocumentResponseWithAllAttributes().toBuilder()
                .status(documentStatus).build();

        Assertions.assertEquals(expectedResourceInformation,
            unitUnderTest.generateResourceInformation(getDocumentResponse, SAMPLE_TAG_MAP));
    }

    private ResourceModel createResourceModelWithAllAttributes() {
        return ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .tags(SAMPLE_RESOURCE_MODEL_TAGS)
                .requires(SAMPLE_RESOURCE_MODEL_REQUIRES)
                .build();
    }

    private GetDocumentResponse createGetDocumentResponseWithAllAttributes() {
        return GetDocumentResponse.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentVersion(SAMPLE_DOCUMENT_VERSION)
                .status(SAMPLE_GET_RESPONSE_ACTIVE_STATE)
                .statusInformation(SAMPLE_STATUS_INFO)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .attachmentsContent(SAMPLE_GET_RESPONSE_ATTACHMENTS)
                .requires(SAMPLE_GET_RESPONSE_REQUIRES)
                .build();
    }
}
