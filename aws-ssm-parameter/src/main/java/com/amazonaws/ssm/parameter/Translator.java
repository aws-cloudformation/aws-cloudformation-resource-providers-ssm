package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Set;
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

    static DescribeParametersRequest describeParametersRequestForSingleParameter(final ResourceModel model) {
        ParameterStringFilter nameEqualsFilter = ParameterStringFilter.builder()
                .key("Name")
                .option("Equals")
                .values(model.getName())
                .build();
        return DescribeParametersRequest.builder()
                .maxResults(Constants.MAX_RESULTS)
                .parameterFilters(nameEqualsFilter)
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

    // Translate tags
    static List<Tag> translateTagsToSdk(final Map<String, String> tags) {
        return Optional.of(tags.entrySet()).orElse(Collections.emptySet())
                .stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
    }

    /**
     * Request to add tags to a resource
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AddTagsToResourceRequest tagResourceRequest(final ResourceModel model, List<Tag> tagsToAdd) {
        String parameterName = model.getName();
        return AddTagsToResourceRequest.builder()
                .resourceId(parameterName)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tags(tagsToAdd)
                .build();
    }

    /**
     * Request to add tags to a resource
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static RemoveTagsFromResourceRequest untagResourceRequest(final ResourceModel model, List<String> tagsToRemove) {
        String parameterName = model.getName();
        return RemoveTagsFromResourceRequest.builder()
                .resourceId(parameterName)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tagKeys(tagsToRemove)
                .build();
    }

    static ListTagsForResourceRequest listResourceTagRequest(final ResourceModel model) {
        String parameterName = model.getName();
        return ListTagsForResourceRequest.builder()
                .resourceId(parameterName)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .build();
    }
    
    static String policyToString(ParameterMetadata parameterMetadata) {
        if (parameterMetadata.policies() == null || parameterMetadata.policies().isEmpty()) {
            return null;
        }
        return "[" + parameterMetadata.policies().stream().map(policy -> policy.policyText())
                .collect(Collectors.joining(",")) + "]";
    }
}
