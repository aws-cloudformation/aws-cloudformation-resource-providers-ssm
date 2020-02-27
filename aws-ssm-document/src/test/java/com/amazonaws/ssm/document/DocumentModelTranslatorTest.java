package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;

import java.util.List;
import java.util.Map;

public class DocumentModelTranslatorTest {
    private static final String DEFAULT_DOCUMENT_NAME_PREFIX = "document";
    private static final String EMPTY_STACK_NAME = "";
    private static final int DOCUMENT_NAME_MAX_LENGTH = 128;
    private static final String DOCUMENT_NAME_DELIMITER = "-";

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_DOCUMENT_CONTENT = "sampleDocumentContent";
    private static final Map<String, String> SAMPLE_SYSTEM_TAGS = ImmutableMap.of("aws:cloudformation:stack-name", "testStack");
    private static final String SAMPLE_REQUEST_TOKEN = "sampleRequestToken";
    private static final String SAMPLE_VERSION_NAME = "versionName";
    private static final String SAMPLE_DOCUMENT_FORMAT = "format";
    private static final String SAMPLE_DOCUMENT_TYPE = "type";
    private static final String SAMPLE_TARGET_TYPE = "targetType";
    private static final List<Tag> SAMPLE_RESOURCE_MODEL_TAGS = ImmutableList.of(
            Tag.builder().key("tagKey1").value("tagValue1").build(),
            Tag.builder().key("tagKey2").value("tagValue2").build()
    );
    private static final List<software.amazon.awssdk.services.ssm.model.Tag> SAMPLE_CREATE_REQUEST_TAGS = ImmutableList.of(
            software.amazon.awssdk.services.ssm.model.Tag.builder().key("tagKey1").value("tagValue1").build(),
            software.amazon.awssdk.services.ssm.model.Tag.builder().key("tagKey2").value("tagValue2").build()
    );
    private static final List<AttachmentsSource> SAMPLE_RESOURCE_MODEL_ATTACHMENTS = ImmutableList.of(
            AttachmentsSource.builder().name("name1").key("key1").values(ImmutableList.of("value11", "value12")).build(),
            AttachmentsSource.builder().name("name2").key("key2").values(ImmutableList.of("value21", "value22")).build()
    );
    private static final List<software.amazon.awssdk.services.ssm.model.AttachmentsSource> SAMPLE_CREATE_REQUEST_ATTACHMENTS = ImmutableList.of(
            software.amazon.awssdk.services.ssm.model.AttachmentsSource.builder()
                    .name("name1").key("key1").values(ImmutableList.of("value11", "value12")).build(),
            software.amazon.awssdk.services.ssm.model.AttachmentsSource.builder()
                    .name("name2").key("key2").values(ImmutableList.of("value21", "value22")).build()
    );
    private static final List<DocumentRequires> SAMPLE_RESOURCE_MODEL_REQUIRES = ImmutableList.of(
            DocumentRequires.builder().name("sampleRequires1").version("1").build(),
            DocumentRequires.builder().name("sampleRequires2").version("2").build()
    );
    private static final List<software.amazon.awssdk.services.ssm.model.DocumentRequires> SAMPLE_CREATE_REQUEST_REQUIRES = ImmutableList.of(
            software.amazon.awssdk.services.ssm.model.DocumentRequires.builder().name("sampleRequires1").version("1").build(),
            software.amazon.awssdk.services.ssm.model.DocumentRequires.builder().name("sampleRequires2").version("2").build()
    );


    private final DocumentModelTranslator unitUnderTest = new DocumentModelTranslator();

    @Test
    public void testGenerateCreateDocumentRequest_DocumentNameIsProvided_verifyResult() {
        final ResourceModel model = createResourceModel();

        final CreateDocumentRequest expectedRequest = CreateDocumentRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .attachments(SAMPLE_CREATE_REQUEST_ATTACHMENTS)
                .tags(SAMPLE_CREATE_REQUEST_TAGS)
                .requires(SAMPLE_CREATE_REQUEST_REQUIRES)
                .build();

        final CreateDocumentRequest request =
                unitUnderTest.generateCreateDocumentRequest(model, SAMPLE_SYSTEM_TAGS, SAMPLE_REQUEST_TOKEN);

        Assertions.assertEquals(expectedRequest, request);
    }

    @Test
    public void testGenerateCreateDocumentRequest_DocumentNameIsNotProvided_verifyResult() {
        final ResourceModel resourceModel = createResourceModel();
        resourceModel.setName(null);

        final CreateDocumentRequest expectedRequest = CreateDocumentRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .attachments(SAMPLE_CREATE_REQUEST_ATTACHMENTS)
                .tags(SAMPLE_CREATE_REQUEST_TAGS)
                .requires(SAMPLE_CREATE_REQUEST_REQUIRES)
                .build();

        final CreateDocumentRequest request =
                unitUnderTest.generateCreateDocumentRequest(resourceModel, SAMPLE_SYSTEM_TAGS, SAMPLE_REQUEST_TOKEN);

        Assertions.assertTrue(request.name().startsWith("testStack-document"));
        Assertions.assertEquals(expectedRequest.versionName(), request.versionName());
        Assertions.assertEquals(expectedRequest.content(), request.content());
        Assertions.assertEquals(expectedRequest.documentFormat(), request.documentFormat());
        Assertions.assertEquals(expectedRequest.documentType(), request.documentType());
        Assertions.assertEquals(expectedRequest.targetType(), request.targetType());
        Assertions.assertEquals(expectedRequest.attachments(), request.attachments());
        Assertions.assertEquals(expectedRequest.tags(), request.tags());
        Assertions.assertEquals(expectedRequest.requires(), request.requires());
    }

    @Test
    public void testGenerateCreateDocumentRequest_DocumentNameIsNotProvided_SystemTagsIsNull_verifyResult() {
        final ResourceModel resourceModel = createResourceModel();
        resourceModel.setName(null);

        final CreateDocumentRequest expectedRequest = CreateDocumentRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .attachments(SAMPLE_CREATE_REQUEST_ATTACHMENTS)
                .tags(SAMPLE_CREATE_REQUEST_TAGS)
                .requires(SAMPLE_CREATE_REQUEST_REQUIRES)
                .build();

        final CreateDocumentRequest request =
                unitUnderTest.generateCreateDocumentRequest(resourceModel, null, SAMPLE_REQUEST_TOKEN);

        Assertions.assertTrue(request.name().startsWith("document"));
        Assertions.assertEquals(expectedRequest.versionName(), request.versionName());
        Assertions.assertEquals(expectedRequest.content(), request.content());
        Assertions.assertEquals(expectedRequest.documentFormat(), request.documentFormat());
        Assertions.assertEquals(expectedRequest.documentType(), request.documentType());
        Assertions.assertEquals(expectedRequest.targetType(), request.targetType());
        Assertions.assertEquals(expectedRequest.attachments(), request.attachments());
        Assertions.assertEquals(expectedRequest.tags(), request.tags());
        Assertions.assertEquals(expectedRequest.requires(), request.requires());
    }

    @Test
    public void testGenerateCreateDocumentRequest_AttachmentsIsNull_verifyResult() {
        final ResourceModel resourceModel = createResourceModel();
        resourceModel.setAttachments(null);

        final CreateDocumentRequest expectedRequest = CreateDocumentRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .tags(SAMPLE_CREATE_REQUEST_TAGS)
                .requires(SAMPLE_CREATE_REQUEST_REQUIRES)
                .build();

        final CreateDocumentRequest request =
                unitUnderTest.generateCreateDocumentRequest(resourceModel, null, SAMPLE_REQUEST_TOKEN);

        Assertions.assertEquals(expectedRequest, request);
    }

    @Test
    public void testGenerateCreateDocumentRequest_DocumentRequiresListIsNull_verifyResult() {
        final ResourceModel resourceModel = createResourceModel();
        resourceModel.setRequires(null);

        final CreateDocumentRequest expectedRequest = CreateDocumentRequest.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .tags(SAMPLE_CREATE_REQUEST_TAGS)
                .attachments(SAMPLE_CREATE_REQUEST_ATTACHMENTS)
                .build();

        final CreateDocumentRequest request =
                unitUnderTest.generateCreateDocumentRequest(resourceModel, null, SAMPLE_REQUEST_TOKEN);

        Assertions.assertEquals(expectedRequest, request);
    }

    private ResourceModel createResourceModel() {
        return ResourceModel.builder()
                .name(SAMPLE_DOCUMENT_NAME)
                .content(SAMPLE_DOCUMENT_CONTENT)
                .versionName(SAMPLE_VERSION_NAME)
                .documentFormat(SAMPLE_DOCUMENT_FORMAT)
                .documentType(SAMPLE_DOCUMENT_TYPE)
                .targetType(SAMPLE_TARGET_TYPE)
                .attachments(SAMPLE_RESOURCE_MODEL_ATTACHMENTS)
                .tags(SAMPLE_RESOURCE_MODEL_TAGS)
                .requires(SAMPLE_RESOURCE_MODEL_REQUIRES)
                .build();
    }

}
