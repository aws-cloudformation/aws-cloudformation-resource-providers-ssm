package software.amazon.ssm.resourcepolicies;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Translator {

  static PutResourcePolicyRequest translateToCreateRequest(final ResourceModel model) {
    return PutResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .policy(model.getPolicy().toString())
            .build();
  }


  static GetResourcePoliciesRequest translateToReadRequest(final ResourceModel model) {
    return GetResourcePoliciesRequest.builder()
            .resourceArn(model.getResourceArn())
            .build();
  }

  static List<ResourceModel> translateFromReadResponseToList(final GetResourcePoliciesResponse awsResponse) {
    List<ResourceModel> policies = new ArrayList<>();
    if (awsResponse != null && awsResponse.policies() != null && (!awsResponse.policies().isEmpty())) {
      List<GetResourcePoliciesResponseEntry> policyList = awsResponse.policies();
      for (int i = 0; i < policyList.size(); i++) {
        GetResourcePoliciesResponseEntry entry = policyList.get(i);
        ResourceModel model = ResourceModel.builder()
                .policy(entry.policy())
                .policyId(entry.policyId())
                .policyHash(entry.policyHash())
                .build();
        policies.add(model);
      }
    }
    return policies;
  }

  static ResourceModel getOneFromReadResponse(final GetResourcePoliciesResponse awsResponse, String policy) {
    if (awsResponse.policies() != null && (!awsResponse.policies().isEmpty())) {
      List<GetResourcePoliciesResponseEntry> policyList = awsResponse.policies();
      for (int i = 0; i < policyList.size(); i++) {
        GetResourcePoliciesResponseEntry entry = policyList.get(i);
        if (entry.policy().equals(policy)) {
          return ResourceModel.builder()
                  .policy(entry.policy())
                  .policyId(entry.policyId())
                  .policyHash(entry.policyHash())
                  .build();
        }
      }
    }
    return ResourceModel.builder()
            .policyId("No policy")
            .build();
  }

  static DeleteResourcePolicyRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .policyId(model.getPolicyId())
            .policyHash(model.getPolicyHash())
            .build();
  }

  static PutResourcePolicyRequest translateToUpdateRequest(final ResourceModel model) {
    return PutResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .policy(model.getPolicy().toString())
            .policyId(model.getPolicyId())
            .policyHash(model.getPolicyHash())
            .build();
  }


  static AwsRequest translateToListRequest(final String nextToken) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L26-L31
    return awsRequest;
  }

  static List<ResourceModel> translateFromListResponse(final AwsResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
    return streamOfOrEmpty(Lists.newArrayList())
            .map(resource -> ResourceModel.builder()
                    // include only primary identifier
                    .build())
            .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
  }

  static AwsRequest tagResourceRequest(final ResourceModel model, final Map<String, String> addedTags) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
    return awsRequest;
  }

  static AwsRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
    return awsRequest;
  }
}
