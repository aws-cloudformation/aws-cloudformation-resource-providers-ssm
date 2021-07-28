package com.amazonaws.ssm.opsmetadata.translator.request;

import com.amazonaws.ssm.opsmetadata.ResourceModel;
import com.amazonaws.ssm.opsmetadata.translator.property.MetadataTranslator;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.DeleteOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.ListOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestTranslator {
    private final MetadataTranslator metadataTranslator;

    public RequestTranslator() {
        this.metadataTranslator = new MetadataTranslator();
    }

    public RequestTranslator(final MetadataTranslator metadataTranslator) {
        this.metadataTranslator = metadataTranslator;
    }
    public CreateOpsMetadataRequest createOpsMetadataRequest(final ResourceModel model,
                                                       final Map<String, String> tags) {
        final CreateOpsMetadataRequest.Builder createOpsMetadataRequestBuilder;
        if (translateTagsToSdk(tags).isEmpty()) {
            createOpsMetadataRequestBuilder = CreateOpsMetadataRequest.builder()
                    .resourceId(model.getResourceId());
        } else {
            createOpsMetadataRequestBuilder = CreateOpsMetadataRequest.builder()
                    .resourceId(model.getResourceId())
                    .tags(translateTagsToSdk(tags));
        }
        metadataTranslator.resourceModelPropertyToServiceModel(
                model.getMetadata()).ifPresent(createOpsMetadataRequestBuilder::metadata);

        return createOpsMetadataRequestBuilder.build();
    }

    public UpdateOpsMetadataRequest updateOpsMetadataRequest(final ResourceModel model) {
        final UpdateOpsMetadataRequest.Builder updateOpsMetadataRequestBuilder = UpdateOpsMetadataRequest.builder()
                .opsMetadataArn(model.getOpsMetadataArn());
        metadataTranslator.resourceModelPropertyToServiceModel(
                model.getMetadata()).ifPresent(updateOpsMetadataRequestBuilder::metadataToUpdate);
        return updateOpsMetadataRequestBuilder.build();
    }

    public GetOpsMetadataRequest getOpsMetadataRequest(final ResourceModel model) {
        return GetOpsMetadataRequest.builder()
                .opsMetadataArn(model.getOpsMetadataArn())
                .build();
    }

    public DeleteOpsMetadataRequest deleteOpsMetadataRequest(final ResourceModel model) {
        return DeleteOpsMetadataRequest.builder()
                .opsMetadataArn(model.getOpsMetadataArn())
                .build();
    }

    public ListOpsMetadataRequest listOpsMetadataRequest(final String nextToken) {
        return ListOpsMetadataRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    public AddTagsToResourceRequest addTagsToResourceRequest(final ResourceModel model, List<Tag> tagsToAdd) {
        return AddTagsToResourceRequest.builder()
                .resourceId(getResourceNameFromOpsMetadataArn(model.getOpsMetadataArn()))
                .resourceType(ResourceTypeForTagging.OPS_METADATA)
                .tags(tagsToAdd)
                .build();
    }

    public RemoveTagsFromResourceRequest removeTagsFromResourceRequest(final ResourceModel model, List<Tag> tagsToRemove) {
        return RemoveTagsFromResourceRequest.builder()
                .resourceId(getResourceNameFromOpsMetadataArn(model.getOpsMetadataArn()))
                .resourceType(ResourceTypeForTagging.OPS_METADATA)
                .tagKeys(tagsToRemove.stream().map(tag -> tag.key()).collect(Collectors.toList()))
                .build();
    }

    private String getResourceNameFromOpsMetadataArn(final String opsMetadataArn) {
        int index = opsMetadataArn.indexOf("/");
        return opsMetadataArn.substring(index);
    }

    // Translate tags
    public List<Tag> translateTagsToSdk(final Map<String, String> tags) {
        List<Tag> tagList = Optional.of(tags.entrySet()).orElse(Collections.emptySet())
                .stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
        return tagList;
    }
}
