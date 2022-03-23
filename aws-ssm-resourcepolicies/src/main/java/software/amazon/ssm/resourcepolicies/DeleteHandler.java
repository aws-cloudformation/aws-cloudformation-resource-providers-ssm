package software.amazon.ssm.resourcepolicies;

// TODO: replace all usage of SdkClient with your service client type, e.g; YourServiceAsyncClient
// import software.amazon.awssdk.services.yourservice.YourServiceAsyncClient;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteResourcePolicyResponse;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        ResourceModel model = request.getDesiredResourceState();

        if (model.getPolicyId().isEmpty()){
            if (model.getPolicy() != null){
                try {
                    GetResourcePoliciesResponse awsResponse;
                    awsResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getResourcePolicies);
                    ResourceModel currentModel = Translator.getOneFromReadResponse(awsResponse, model.getPolicy().toString());
                    if (!currentModel.getPolicyId().equals("No policy")){
                        model.setPolicyId(currentModel.getPolicyId());
                        model.setPolicyHash(currentModel.getPolicyHash());
                    }
                } catch (final AwsServiceException e) {
                    throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
                }
            }
        }

        ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SSM-ResourcePolicies::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                                .translateToServiceRequest(Translator::translateToDeleteRequest)

                                .makeServiceCall((awsRequest, client) -> {
                                    if (model.getPolicyId().isEmpty()){
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, "");
                                    }
                                    DeleteResourcePolicyResponse awsResponse = null;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::deleteResourcePolicy);
                                    } catch (final AwsServiceException e) {
                                        //throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                                        throw new CfnGeneralServiceException("TA S: " + e.toString()+" NADES", e);
                                    }

                                    logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                })
                                .success()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));

         return ProgressEvent.success(null, callbackContext);

    }
}
