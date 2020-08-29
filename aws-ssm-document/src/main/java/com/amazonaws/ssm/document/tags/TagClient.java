package com.amazonaws.ssm.document.tags;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

@NoArgsConstructor
public class TagClient {

    private static TagClient INSTANCE;

    public static TagClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagClient();
        }

        return INSTANCE;
    }

    public void addTags(@NonNull final List<Tag> tagsToAdd, @NonNull final String documentName,
                         @NonNull final SsmClient ssmClient,
                         @NonNull final AmazonWebServicesClientProxy proxy) {
        if (tagsToAdd.isEmpty()) {
            return;
        }

        final AddTagsToResourceRequest addTagsToResourceRequest = AddTagsToResourceRequest.builder()
            .resourceId(documentName)
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .tags(tagsToAdd)
            .build();

        proxy.injectCredentialsAndInvokeV2(addTagsToResourceRequest, ssmClient::addTagsToResource);
    }

    public void removeTags(@NonNull final List<Tag> tagsToRemove, @NonNull final String documentName,
                            @NonNull final SsmClient ssmClient,
                            @NonNull final AmazonWebServicesClientProxy proxy) {
        if (tagsToRemove.isEmpty()) {
            return;
        }

        final RemoveTagsFromResourceRequest removeTagsFromResourceRequest = RemoveTagsFromResourceRequest.builder()
            .resourceId(documentName)
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .tagKeys(getTagKeys(tagsToRemove))
            .build();

        proxy.injectCredentialsAndInvokeV2(removeTagsFromResourceRequest, ssmClient::removeTagsFromResource);
    }

    public List<Tag> listTags(@NonNull final String documentName,
                              @NonNull final SsmClient ssmClient,
                              @NonNull final AmazonWebServicesClientProxy proxy) {
        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
            .resourceId(documentName)
            .resourceType(ResourceTypeForTagging.DOCUMENT)
            .build();

        final ListTagsForResourceResponse listTagsForResourceResult =
            proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest, ssmClient::listTagsForResource);

        return listTagsForResourceResult.hasTagList() ? ImmutableList.copyOf(listTagsForResourceResult.tagList()) : ImmutableList.of();
    }

    private List<String> getTagKeys(List<Tag> tags) {
        return tags.stream()
            .map(Tag::key)
            .collect(ImmutableList.toImmutableList());
    }
}
