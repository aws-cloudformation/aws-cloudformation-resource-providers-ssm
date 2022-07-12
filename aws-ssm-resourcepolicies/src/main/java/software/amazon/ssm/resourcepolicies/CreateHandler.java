package software.amazon.ssm.resourcepolicies;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.IdentifierUtils;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int ALLOWED_ID_LENGTH = 100;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest (
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        ResourceModel model = request.getDesiredResourceState();
        if (StringUtils.isEmpty(model.getId())) {
            if (StringUtils.isEmpty(callbackContext.getId())) {
                String id = generateId(request.getStackId(),
                        request.getLogicalResourceIdentifier(),
                        request.getClientRequestToken());
                callbackContext.setId(id);
            }
            model.setId(callbackContext.getId());
        }
/* */
        try {
            GetResourcePoliciesResponse awsResponse;
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getResourcePolicies);
            ResourceModel currentModel = Translator.getOneFromReadResponse(awsResponse, model.getPolicy().toString());
            if (!currentModel.getPolicyId().equals("No policy")){
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, new Exception("Already exists"));
            }
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->

                        proxy.initiate("AWS-SSM-ResourcePolicies::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())

                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    try {
                                        PutResourcePolicyResponse awsResponse;
                                        awsResponse =  client.injectCredentialsAndInvokeV2(awsRequest, client.client()::putResourcePolicy);
                                        //model.setPolicyId(awsResponse.policyId());
                                        //model.setPolicyHash(awsResponse.policyHash());
                                        return awsResponse;
                                    } catch (final AwsServiceException e) {
                                        //throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                                        throw new CfnGeneralServiceException("TA S: " + e.toString()+" END", e);
                                    }
                                })
                                .success()
                )

                .then(progress -> ProgressEvent.success(model, callbackContext));
    }

    protected String generateId(String stackId, String logicalResourceIdentifier, String clientRequestToken) {
        if (StringUtils.isEmpty(stackId)) {
            return IdentifierUtils.generateResourceIdentifier(logicalResourceIdentifier, clientRequestToken,
                    ALLOWED_ID_LENGTH);
        } else {
            return IdentifierUtils.generateResourceIdentifier(stackId, logicalResourceIdentifier, clientRequestToken,
                    ALLOWED_ID_LENGTH);
        }
    }
}
