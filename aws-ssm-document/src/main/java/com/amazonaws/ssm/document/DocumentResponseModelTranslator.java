package com.amazonaws.ssm.document;

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

class DocumentResponseModelTranslator {
    ResourceModel generateResourceModel(@NonNull final GetDocumentResponse response) {
        return ResourceModel.builder()
                .name(response.name())
                .versionName(response.versionName())
                .documentFormat(response.documentFormatAsString())
                .documentType(response.documentTypeAsString())
                .documentVersion(response.documentVersion())
                .status(response.statusAsString())
                .statusInformation(response.statusInformation())
                .content(response.content())
                .requires(translateRequires(response.requires()))
                .attachmentsContent(translateAttachments(response.attachmentsContent()))
                .build();
    }

    private List<DocumentRequires> translateRequires(
            @Nullable final List<software.amazon.awssdk.services.ssm.model.DocumentRequires> documentRequiresList) {
        if (CollectionUtils.isEmpty(documentRequiresList)) {
            return null;
        }

        return documentRequiresList.stream().map(
                documentRequires -> DocumentRequires.builder()
                        .name(documentRequires.name())
                        .version(documentRequires.version())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AttachmentContent> translateAttachments(
            @Nullable final List<software.amazon.awssdk.services.ssm.model.AttachmentContent> attachmentContents) {
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
