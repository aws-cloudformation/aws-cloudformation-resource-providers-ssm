package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static software.amazon.ssm.patchbaseline.ResourceModel.TYPE_NAME;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.ssm.patchbaseline.translator.resourcemodel.ReadResourceModelTranslator;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String baselineId = model.getId();

        logger.log(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                        .baselineId(baselineId)
                                                                        .build();

            GetPatchBaselineResponse getPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);

            ResourceModel resourcemodel = ReadResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

            //Send a success response to CloudFormation with the JSON
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourcemodel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            return Resource.handleException(e, model, baselineId, logger);
        }
    }
}
