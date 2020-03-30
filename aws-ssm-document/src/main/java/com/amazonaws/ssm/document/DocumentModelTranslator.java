package com.amazonaws.ssm.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.ssm.model.AttachmentsSource;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DeleteDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DocumentKeyValuesFilter;
import software.amazon.awssdk.services.ssm.model.DocumentRequires;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.ListDocumentsRequest;
import software.amazon.awssdk.services.ssm.model.Tag;

import lombok.NonNull;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates CloudFormation's resource model to AWS resource model.
 */
class DocumentModelTranslator {
    private static final String DEFAULT_DOCUMENT_NAME_PREFIX = "document";
    private static final String EMPTY_STACK_NAME = "";
    private static final int DOCUMENT_NAME_MAX_LENGTH = 128;
    private static final String DOCUMENT_NAME_DELIMITER = "-";
    private static final String LATEST_DOCUMENT_VERSION = "$LATEST";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static DocumentModelTranslator INSTANCE;

    static DocumentModelTranslator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentModelTranslator();
        }

        return INSTANCE;
    }

    /**
     * Generate CreateDocumentRequest from the CreateResource request.
     */
    CreateDocumentRequest generateCreateDocumentRequest(@NonNull final ResourceModel model,
                                                        @Nullable final Map<String, String> systemTags,
                                                        @NonNull final String requestToken) {
        final String documentName;

        if (StringUtils.isEmpty(model.getName())) {
            documentName = generateName(systemTags, requestToken);
        } else {
            documentName = model.getName();
        }

        final String documentContent = processDocumentContent(model.getContent(), model.getContentAsString());

        return CreateDocumentRequest.builder()
                .name(documentName)
                .versionName(model.getVersionName())
                .content(documentContent)
                .documentFormat(model.getDocumentFormat())
                .documentType(model.getDocumentType())
                .targetType(model.getTargetType())
                .tags(translateTags(model.getTags()))
                .attachments(translateAttachments(model.getAttachments()))
                .requires(translateRequires(model.getRequires()))
                .build();

    }

    GetDocumentRequest generateGetDocumentRequest(@NonNull final ResourceModel model) {
        return GetDocumentRequest.builder()
                .name(model.getName())
                .documentVersion(model.getDocumentVersion())
                .documentFormat(model.getDocumentFormat())
                .build();
    }

    UpdateDocumentRequest generateUpdateDocumentRequest(@NonNull final ResourceModel model) {
        final String documentContent = processDocumentContent(model.getContent(), model.getContentAsString());

        return UpdateDocumentRequest.builder()
                .name(model.getName())
                .content(documentContent)
                .versionName(model.getVersionName())
                .documentVersion(LATEST_DOCUMENT_VERSION)
                .documentFormat(model.getDocumentFormat())
                .targetType(model.getTargetType())
                .attachments(translateAttachments(model.getAttachments()))
                .build();
    }

    DeleteDocumentRequest generateDeleteDocumentRequest(@NonNull final ResourceModel model) {
        return DeleteDocumentRequest.builder()
                .name(model.getName())
                .force(true) // This is required for certain document types. If the user does not have permissions to use this flag, the call will fail.
                .build();
    }

    ListDocumentsRequest generateListDocumentsRequest() {
        List<DocumentKeyValuesFilter> keyValuesFilters =
                ImmutableList.of(DocumentKeyValuesFilter.builder().key("owner").values("self").build());

        return ListDocumentsRequest.builder()
                .filters(keyValuesFilters)
                .build();
    }

    /**
     * When a document name is not provided, CFN will autogenerate the document name to be in the
     * format:
     * <stack-name> - <resource type> - <autogenerated ID>
     */
    private String generateName(final Map<String, String> systemTags, final String requestToken) {
        final StringBuilder identifierPrefix = new StringBuilder();

        final String stackName = MapUtils.isNotEmpty(systemTags) ?
                systemTags.get("aws:cloudformation:stack-name") + DOCUMENT_NAME_DELIMITER :
                EMPTY_STACK_NAME;
        identifierPrefix.append(stackName);

        identifierPrefix.append(DEFAULT_DOCUMENT_NAME_PREFIX);

        // This utility function will add the auto-generated ID after the prefix.
        return IdentifierUtils.generateResourceIdentifier(
                identifierPrefix.toString(),
                requestToken,
                DOCUMENT_NAME_MAX_LENGTH);
    }

    private String processDocumentContent(final Map<String, Object> jsonContent, final String contentAsString) {
        if (jsonContent != null && contentAsString != null) {
            throw new InvalidDocumentContentException("Only one of Content and ContentAsString attributes must be provided");
        }

        try {
            return jsonContent != null ? OBJECT_MAPPER.writeValueAsString(jsonContent) : contentAsString;
        } catch (final JsonProcessingException e) {
            throw new InvalidDocumentContentException("Document Content is not valid", e);
        }
    }

    private List<Tag> translateTags(@Nullable final List<com.amazonaws.ssm.document.Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }

        return tags.stream().map(
                tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AttachmentsSource> translateAttachments(@Nullable final List<com.amazonaws.ssm.document.AttachmentsSource> attachmentsSources) {
        if (CollectionUtils.isEmpty(attachmentsSources)) {
            return null;
        }

        return attachmentsSources.stream().map(
                attachmentsSource -> AttachmentsSource.builder()
                        .key(attachmentsSource.getKey())
                        .values(attachmentsSource.getValues())
                        .name(attachmentsSource.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DocumentRequires> translateRequires(@Nullable final List<com.amazonaws.ssm.document.DocumentRequires> requires) {
        if (CollectionUtils.isEmpty(requires)) {
            return null;
        }

        return requires.stream().map(
                documentRequires -> DocumentRequires.builder()
                        .name(documentRequires.getName())
                        .version(documentRequires.getVersion())
                        .build())
                .collect(Collectors.toList());
    }
}
