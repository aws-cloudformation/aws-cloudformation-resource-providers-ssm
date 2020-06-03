package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Translator {
    static PutParameterRequest createPutParameterRequest(final ResourceModel model,
                                                         final Map<String, String> tags) {
        return PutParameterRequest.builder()
                .description(model.getDescription())
                .name(model.getName())
                .value(model.getValue())
                .type(model.getType())
                .overwrite(Boolean.FALSE)
                .allowedPattern(model.getAllowedPattern())
                .policies(model.getPolicies())
                .tier(model.getTier())
                .tags(translateTagsToSdk(tags))
                .dataType(model.getDataType())
                .build();
    }

    static PutParameterRequest updatePutParameterRequest(final ResourceModel model) {
        return PutParameterRequest.builder()
                .description(model.getDescription())
                .name(model.getName())
                .value(model.getValue())
                .type(model.getType())
                .overwrite(Boolean.TRUE)
                .allowedPattern(model.getAllowedPattern())
                .policies(model.getPolicies())
                .tier(model.getTier())
                .dataType(model.getDataType())
                .build();
    }

    static GetParametersRequest getParametersRequest(final ResourceModel model) {
        return GetParametersRequest.builder()
                .names(model.getName())
                .withDecryption(Boolean.FALSE)
                .build();
    }

    static DescribeParametersRequest describeParametersRequest(final String nextToken) {
        return DescribeParametersRequest.builder()
                .nextToken(nextToken)
                .maxResults(Constants.MAX_RESULTS)
                .build();
    }

    static DeleteParameterRequest deleteParameterRequest(final ResourceModel model) {
        return DeleteParameterRequest.builder()
                .name(model.getName())
                .build();
    }

    static ListTagsForResourceRequest listTagsForResourceRequest(final ResourceModel model) {
        return ListTagsForResourceRequest.builder()
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .resourceId(model.getName())
                .build();
    }

    static RemoveTagsFromResourceRequest removeTagsFromResourceRequest(final String parameterName, List<Tag> tagsToRemove) {
        return RemoveTagsFromResourceRequest.builder()
                .resourceId(parameterName)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tagKeys(tagsToRemove.stream().map(tag -> tag.key()).collect(Collectors.toList()))
                .build();
    }

    static AddTagsToResourceRequest addTagsToResourceRequest(final String parameterName, List<Tag> tagsToAdd) {
        return AddTagsToResourceRequest.builder()
                .resourceId(parameterName)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tags(tagsToAdd)
                .build();
    }

    // Translate tags
    static List<Tag> translateTagsToSdk(final Map<String, String> tags) {
        return Optional.of(tags.entrySet()).orElse(Collections.emptySet())
                .stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
    }
}
