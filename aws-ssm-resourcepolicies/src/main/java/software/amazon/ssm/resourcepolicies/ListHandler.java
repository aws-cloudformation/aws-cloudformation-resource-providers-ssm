package software.amazon.ssm.resourcepolicies;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.*;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        List<ResourceModel> models = new ArrayList<>();
        ResourceModel model = request.getDesiredResourceState();

        try {
            GetResourcePoliciesResponse awsResponse;
            awsResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), ClientBuilder.getClient()::getResourcePolicies);
            models = Translator.translateFromReadResponseToList(awsResponse);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
        }

        //models.add(model);



        // STEP 1 [TODO: construct a body of a request]
        final AwsRequest awsRequest = Translator.translateToListRequest(request.getNextToken());

        // STEP 2 [TODO: make an api call]
        AwsResponse awsResponse = null; // proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeLogGroups);

        // STEP 3 [TODO: get a token for the next page]
        String nextToken = null;

        // STEP 4 [TODO: construct resource models]
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/master/aws-logs-loggroup/src/main/java/software/amazon/logs/loggroup/ListHandler.java#L19-L21

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
