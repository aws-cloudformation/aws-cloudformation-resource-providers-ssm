package software.amazon.ssm.resourcepolicies;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        if (model.getPolicy() != null) {
            boolean isExist = true;
            try {
                GetResourcePoliciesResponse awsResponse;
                awsResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getResourcePolicies);
                ResourceModel currentModel = Translator.getOneFromReadResponse(awsResponse, model.getPolicy().toString());
                if (currentModel.getPolicyId().equals("No policy")) {
                    isExist = false;
                }
            } catch (final AwsServiceException e) {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
            }
            if (!isExist){
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, "");
            }
        }

        return proxy.initiate("AWS-SSM-ResourcePolicies::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)

                .makeServiceCall((awsRequest, client) -> {
                    try {
                        GetResourcePoliciesResponse awsResponse;
                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::getResourcePolicies);
                        return awsResponse;
                    } catch (final AwsServiceException e) { // ResourceNotFoundException
                        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
                    }
                })
                //.done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse)));
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(model));
    }
}
