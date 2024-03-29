package com.amazonaws.ssm.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.ssm.model.AttachmentsSource;
import software.amazon.awssdk.services.ssm.model.CreateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DeleteDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DocumentFormat;
import software.amazon.awssdk.services.ssm.model.DocumentKeyValuesFilter;
import software.amazon.awssdk.services.ssm.model.DocumentRequires;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.ListDocumentsRequest;
import software.amazon.awssdk.services.ssm.model.Tag;

import lombok.NonNull;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentRequest;
import software.amazon.awssdk.services.ssm.model.UpdateDocumentDefaultVersionRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Translates CloudFormation's resource model to AWS resource model.
 */
class DocumentModelTranslator {

    private static final List<String> AWS_SSM_DOCUMENT_RESERVED_PREFIXES = ImmutableList.of(
            "aws-", "amazon", "amzn"
    );
    private static final int DOCUMENT_NAME_MAX_LENGTH = 128;
    private static final String DOCUMENT_NAME_DELIMITER = "-";
    private static final String LATEST_DOCUMENT_VERSION = "$LATEST";
    private static final ImmutableMap<String, ObjectMapper> mappers = ImmutableMap.<String, ObjectMapper>builder()
            .put(DocumentFormat.JSON.toString(), new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT))
            .put(DocumentFormat.YAML.toString(), new ObjectMapper(new YAMLFactory()))
            .build();

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
                                                        @NonNull final String logicalResourceId,
                                                        @Nullable final Map<String, String> systemTags,
                                                        @Nullable final Map<String, String> resourceTags,
                                                        @NonNull final String requestToken) {
        final String documentName;

        if (StringUtils.isEmpty(model.getName())) {
            documentName = generateName(systemTags, requestToken, logicalResourceId);
        } else {
            documentName = model.getName();
        }

        final String documentContent = processDocumentContent(model.getContent(), model.getDocumentFormat());

        return CreateDocumentRequest.builder()
                .name(documentName)
                .versionName(model.getVersionName())
                .content(documentContent)
                .documentFormat(model.getDocumentFormat())
                .documentType(model.getDocumentType())
                .targetType(model.getTargetType())
                .tags(translateTags(resourceTags))
                .attachments(translateAttachments(model.getAttachments()))
                .requires(translateRequires(model.getRequires()))
                .build();

    }

    GetDocumentRequest generateGetDocumentRequest(@NonNull final ResourceModel model) {
        return GetDocumentRequest.builder()
                .name(model.getName())
                .documentVersion(LATEST_DOCUMENT_VERSION)
                .documentFormat(model.getDocumentFormat())
                .build();
    }

    DescribeDocumentRequest generateDescribeDocumentRequest(@NonNull final ResourceModel model) {
        return DescribeDocumentRequest.builder()
                .name(model.getName())
                .documentVersion(LATEST_DOCUMENT_VERSION)
                .build();
    }

    UpdateDocumentRequest generateUpdateDocumentRequest(@NonNull final ResourceModel model) {
        final String documentContent = processDocumentContent(model.getContent(), model.getDocumentFormat());

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

    UpdateDocumentDefaultVersionRequest generateUpdateDocumentDefaultVersionRequest(@NonNull final String name, @NonNull final String documentVersion) {
        return UpdateDocumentDefaultVersionRequest.builder()
                .name(name)
                .documentVersion(documentVersion)
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
     * <stack-name> - <logical-resource-id> - <autogenerated ID>
     */
    private String generateName(final Map<String, String> systemTags, final String requestToken, final String logicalResourceId) {
        final StringBuilder identifierPrefix = new StringBuilder();

        final Optional<String> stackNameOptional = getStackName(systemTags);
        stackNameOptional.ifPresent(stackName -> identifierPrefix.append(stackName).append(DOCUMENT_NAME_DELIMITER));

        identifierPrefix.append(logicalResourceId);

        // This utility function will add the auto-generated ID after the prefix.
        return IdentifierUtils.generateResourceIdentifier(
                identifierPrefix.toString(),
                requestToken,
                DOCUMENT_NAME_MAX_LENGTH);
    }

    private Optional<String> getStackName(final Map<String, String> systemTags) {
        if (MapUtils.isEmpty(systemTags)) {
            return Optional.empty();
        }

        final String stackName = systemTags.get("aws:cloudformation:stack-name");

        final boolean stackNameMatchesReservedPrefix =
                AWS_SSM_DOCUMENT_RESERVED_PREFIXES.stream().anyMatch(prefix -> stackName.toLowerCase().startsWith(prefix));

        if (stackNameMatchesReservedPrefix) {
            return Optional.empty();
        }

        return Optional.of(stackName);
    }

    private String processDocumentContent(final Object content, final String documentFormat) {
        if (content instanceof Map) {
            final ObjectMapper mapper;

            // DocumentFormat is not required, default DocumentFormat is JSON.
            if (documentFormat == null) {
                mapper = mappers.get(DocumentFormat.JSON.toString());
            } else {
                mapper = mappers.get(documentFormat);
            }

            if (mapper == null) {
                throw new InvalidDocumentContentException("Document format not supported " + documentFormat);
            }
            try {
                return mapper.writeValueAsString(content);
            } catch (final JsonProcessingException e) {
                throw new InvalidDocumentContentException("Document Content is not valid", e);
            }
        }

        return (String)content;
    }

    private List<Tag> translateTags(@Nullable final Map<String, String> tags) {
        if (tags == null) {
            return null;
        }

        return tags.entrySet().stream().map(
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
