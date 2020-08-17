package com.amazonaws.ssm.document.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@RequiredArgsConstructor
public class TagReader {
    private static TagReader INSTANCE;

    @NonNull
    private final TagClient tagClient;

    public static TagReader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagReader(TagClient.getInstance());
        }

        return INSTANCE;
    }

    public Map<String, String> getDocumentTags(@NonNull final String documentName,
                                                     @NonNull final SsmClient ssmClient,
                                                     @NonNull final AmazonWebServicesClientProxy proxy) {
        final List<Tag> documentTags = tagClient.listTags(documentName, ssmClient, proxy);

        return translateTags(documentTags);
    }

    private Map<String, String> translateTags(final List<Tag> tags) {
        return tags.stream()
            .collect(ImmutableMap.toImmutableMap(Tag::key, Tag::value));
    }
}
