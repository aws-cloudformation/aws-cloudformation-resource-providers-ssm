package com.amazonaws.ssm.document;

import lombok.NonNull;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DocumentResponseModelTranslator {

    private static DocumentResponseModelTranslator INSTANCE;

    static DocumentResponseModelTranslator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentResponseModelTranslator();
        }

        return INSTANCE;
    }

    ResourceInformation generateResourceInformation(@NonNull final GetDocumentResponse response,
                                                    @NonNull final Map<String, String> documentTagMap) {
        final ResourceModel model = ResourceModel.builder()
                .name(response.name())
                .versionName(response.versionName())
                .documentFormat(response.documentFormatAsString())
                .documentType(response.documentTypeAsString())
                .content(response.content())
                .tags(translateToResourceModelTags(documentTagMap))
                .requires(translateRequires(response))
                .build();

        final ResourceStatus state = translateStatus(response.status());

        return ResourceInformation.builder()
                .resourceModel(model)
                .status(state)
                .statusInformation(response.statusInformation())
                .build();
    }

    ResourceInformation generateResourceInformation(@NonNull final DescribeDocumentResponse response,
                                                    @NonNull final Map<String, String> documentTagMap) {
        final ResourceModel model = ResourceModel.builder()
                .name(response.document().name())
                .versionName(response.document().versionName())
                .documentFormat(response.document().documentFormatAsString())
                .documentType(response.document().documentTypeAsString())
                .tags(translateToResourceModelTags(documentTagMap))
                .requires(translateRequires(response))
                .build();

        final ResourceStatus state = translateStatus(response.document().status());

        return ResourceInformation.builder()
                .resourceModel(model)
                .status(state)
                .statusInformation(response.document().statusInformation())
                .build();
    }

    private ResourceStatus translateStatus(final DocumentStatus status) {
        switch (status) {
            case ACTIVE:
                return ResourceStatus.ACTIVE;
            case CREATING:
                return ResourceStatus.CREATING;
            case UPDATING:
                return ResourceStatus.UPDATING;
            case DELETING:
                return ResourceStatus.DELETING;
            case FAILED:
                return ResourceStatus.FAILED;
            default:
                throw new AssertionError(String.format("unknown Document Status: %s", status));
        }
    }

    private List<Tag> translateToResourceModelTags(final Map<String, String> tagMap) {
        return tagMap.entrySet().stream()
            .map(tagEntry -> Tag.builder()
                .key(tagEntry.getKey())
                .value(tagEntry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    private List<DocumentRequires> translateRequires(final GetDocumentResponse response) {
        if (!response.hasRequires()) {
            return null;
        }

        final List<software.amazon.awssdk.services.ssm.model.DocumentRequires> documentRequiresList = response.requires();

        return documentRequiresList.stream().map(
                documentRequires -> DocumentRequires.builder()
                        .name(documentRequires.name())
                        .version(documentRequires.version())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DocumentRequires> translateRequires(final DescribeDocumentResponse response) {
        if (!response.document().hasRequires()) {
            return null;
        }

        final List<software.amazon.awssdk.services.ssm.model.DocumentRequires> documentRequiresList = response.document().requires();

        return documentRequiresList.stream().map(
                documentRequires -> DocumentRequires.builder()
                        .name(documentRequires.name())
                        .version(documentRequires.version())
                        .build())
                .collect(Collectors.toList());
    }
}
