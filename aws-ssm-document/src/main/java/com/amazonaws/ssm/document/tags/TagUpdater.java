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
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@RequiredArgsConstructor
public class TagUpdater {


    private static TagUpdater INSTANCE;

    @NonNull
    private final TagClient tagClient;

    @NonNull
    private final TagUtil tagUtil;

    public static TagUpdater getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagUpdater(new TagClient(), TagUtil.getInstance());
        }

        return INSTANCE;
    }

    public void createTags(@NonNull final String documentName,
                           @Nullable final Map<String, String> desiredResourceTagsFromRequest,
                           @Nullable final List<com.amazonaws.ssm.document.Tag> resourceModelTags,
                           @NonNull final SsmClient ssmClient,
                           @NonNull final AmazonWebServicesClientProxy proxy) {
        if (desiredResourceTagsFromRequest == null) return;

        final List<Tag> tagsToCreate = translateTags(desiredResourceTagsFromRequest);

        addTags(tagsToCreate, documentName, null, resourceModelTags, ssmClient, proxy);
    }

    public void updateTags(@NonNull final String documentName,
                           @Nullable final Map<String, String> desiredResourceTagsFromPreviousRequest,
                           @Nullable final Map<String, String> desiredResourceTagsFromCurrentRequest,
                           @Nullable final List<com.amazonaws.ssm.document.Tag> previousResourceModelTags,
                           @Nullable final List<com.amazonaws.ssm.document.Tag> currentResourceModelTags,
                           @NonNull final SsmClient ssmClient,
                           @NonNull final AmazonWebServicesClientProxy proxy) {


        final List<Tag> existingTags = desiredResourceTagsFromPreviousRequest == null ? ImmutableList.of()
            : translateTags(desiredResourceTagsFromPreviousRequest);

        final List<Tag> requestedTags = desiredResourceTagsFromCurrentRequest == null ? ImmutableList.of()
            : translateTags(desiredResourceTagsFromCurrentRequest);

        final List<Tag> tagsToAdd = getTagsToAdd(requestedTags, existingTags);
        final List<Tag> tagsToRemove = getTagsToRemove(requestedTags, existingTags);

        removeTags(tagsToRemove, documentName, previousResourceModelTags, currentResourceModelTags, ssmClient, proxy);
        addTags(tagsToAdd, documentName, previousResourceModelTags, currentResourceModelTags, ssmClient, proxy);
    }

    private void addTags(final List<Tag> tagsToAdd, final String documentName,
                         final List<com.amazonaws.ssm.document.Tag> previousResourceModelTags,
                         final List<com.amazonaws.ssm.document.Tag> currentResourceModelTags,
                         final SsmClient ssmClient,
                         final AmazonWebServicesClientProxy proxy) {
        try {
            tagClient.addTags(tagsToAdd, documentName, ssmClient, proxy);
        } catch (final SsmException e) {
            if (tagUtil.shouldSoftFailTags(previousResourceModelTags, currentResourceModelTags, e)) {
                return;
            }

            throw e;
        }
    }

    private void removeTags(final List<Tag> tagsToRemove, final String documentName,
                         final List<com.amazonaws.ssm.document.Tag> previousResourceModelTags,
                         final List<com.amazonaws.ssm.document.Tag> currentResourceModelTags,
                         final SsmClient ssmClient,
                         final AmazonWebServicesClientProxy proxy) {
        try {
            tagClient.removeTags(tagsToRemove, documentName, ssmClient, proxy);
        } catch (final SsmException e) {
            if (tagUtil.shouldSoftFailTags(previousResourceModelTags, currentResourceModelTags, e)) {
                return;
            }

            throw e;
        }
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
