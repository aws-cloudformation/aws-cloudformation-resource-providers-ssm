package com.amazonaws.ssm.document.tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@RequiredArgsConstructor
public class TagUpdater {

    private static TagUpdater INSTANCE;

    @NonNull
    private final TagClient tagClient;

    public static TagUpdater getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagUpdater(new TagClient());
        }

        return INSTANCE;
    }

    public void updateTags(@NonNull final String documentName,
                    @Nullable final Map<String, String> resourceTags,
                    @NonNull final SsmClient ssmClient,
                    @NonNull final AmazonWebServicesClientProxy proxy) {
        if (resourceTags == null) return;

        final List<Tag> requestedTags = translateTags(resourceTags);
        final List<Tag> existingTags = tagClient.listTags(documentName, ssmClient, proxy);

        final List<Tag> tagsToAdd = getTagsToAdd(requestedTags, existingTags);
        final List<Tag> tagsToRemove = getTagsToRemove(requestedTags, existingTags);

        tagClient.addTags(tagsToAdd, documentName, ssmClient, proxy);
        tagClient.removeTags(tagsToRemove, documentName, ssmClient, proxy);
    }

    private List<Tag> getTagsToAdd(List<Tag> requestedTags, List<Tag> existingTags) {
        final Set<Tag> tagsToAdd = Sets.difference(ImmutableSet.copyOf(requestedTags), ImmutableSet.copyOf(existingTags));

        return ImmutableList.copyOf(tagsToAdd);
    }

    private List<Tag> getTagsToRemove(List<Tag> requestedTags, List<Tag> existingTags) {
        final Set<Tag> tagsToRemove = Sets.difference(ImmutableSet.copyOf(existingTags), ImmutableSet.copyOf(requestedTags));

        return ImmutableList.copyOf(tagsToRemove);
    }

    private List<Tag> translateTags(final Map<String, String> tags) {
        return tags.entrySet().stream().map(
            tag -> Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build())
            .collect(Collectors.toList());
    }
}
