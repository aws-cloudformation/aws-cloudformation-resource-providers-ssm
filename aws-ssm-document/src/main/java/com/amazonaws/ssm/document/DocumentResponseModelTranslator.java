package com.amazonaws.ssm.document;

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.ssm.model.DocumentStatus;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

class DocumentResponseModelTranslator {

    private static DocumentResponseModelTranslator INSTANCE;

    static DocumentResponseModelTranslator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentResponseModelTranslator();
        }

        return INSTANCE;
    }

    ResourceInformation generateResourceInformation(@NonNull final GetDocumentResponse response) {
        final ResourceModel model = ResourceModel.builder()
                .name(response.name())
                .versionName(response.versionName())
                .documentFormat(response.documentFormatAsString())
                .documentType(response.documentTypeAsString())
                .documentVersion(response.documentVersion())
                .contentAsString(response.content())
                .requires(translateRequires(response))
                .attachmentsContent(translateAttachments(response))
                .build();

        final ResourceStatus state = translateStatus(response.status());

        return ResourceInformation.builder()
                .resourceModel(model)
                .status(state)
                .statusInformation(response.statusInformation())
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

    private List<AttachmentContent> translateAttachments(final GetDocumentResponse response) {
        if (!response.hasAttachmentsContent()) {
            return null;
        }

        final List<software.amazon.awssdk.services.ssm.model.AttachmentContent> attachmentContents = response.attachmentsContent();
        if (CollectionUtils.isEmpty(attachmentContents)) {
            return null;
        }

        return attachmentContents.stream().map(
                attachmentContent -> AttachmentContent.builder()
                        .name(attachmentContent.name())
                        .hash(attachmentContent.hash())
                        .hashType(attachmentContent.hashTypeAsString())
                        .size(attachmentContent.size().intValue())
                        .url(attachmentContent.url())
                        .build())
                .collect(Collectors.toList());
    }
}
