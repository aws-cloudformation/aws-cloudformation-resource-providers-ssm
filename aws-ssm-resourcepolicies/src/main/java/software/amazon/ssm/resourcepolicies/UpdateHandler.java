package software.amazon.ssm.resourcepolicies;

// TODO: replace all usage of SdkClient with your service client type, e.g; YourServiceAsyncClient
// import software.amazon.awssdk.services.yourservice.YourServiceAsyncClient;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        ResourceModel model = request.getDesiredResourceState();
        String implyId = "";
        String implyHash = "";
        if (model.getPolicyId().isEmpty()){
            if (model.getPolicy() != null){
                try {
                    GetResourcePoliciesResponse awsResponse;
                    awsResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getResourcePolicies);
                    ResourceModel currentModel = Translator.getOneFromReadResponse(awsResponse, model.getPolicy().toString());
                    if (!currentModel.getPolicyId().equals("No policy")){
                        //model.setPolicyId(currentModel.getPolicyId());
                        //model.setPolicyHash(currentModel.getPolicyHash());
                        implyId = currentModel.getPolicyId();
                        implyHash = currentModel.getPolicyHash();
                    }
                } catch (final AwsServiceException e) {
                    throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
                }
            }
        }
        final String policyId = implyId;
        final String policyHash = implyHash;
        if (model.getPolicyId().isEmpty() && implyId.equals("")){
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, "");
        }
        return ProgressEvent.progress(model, callbackContext)

                .then(progress ->
                        proxy.initiate("AWS-SSM-ResourcePolicies::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    try {
                                        if (model.getPolicyId().isEmpty()){
                                            PutResourcePolicyRequest putResourcePolicyRequest = PutResourcePolicyRequest.builder()
                                                    .resourceArn(model.getResourceArn())
                                                    .policy(model.getPolicy().toString())
                                                    .policyId(policyId)
                                                    .policyHash(policyHash)
                                                    .build();
                                            return client.injectCredentialsAndInvokeV2(putResourcePolicyRequest, client.client()::putResourcePolicy);
                                        }
                                        return client.injectCredentialsAndInvokeV2(awsRequest, client.client()::putResourcePolicy);
                                    } catch (final AwsServiceException e) {
                                        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                                    }
                                })
                                .success()
                )
                .then(progress -> ProgressEvent.success(model, callbackContext));
    }
}
