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
        System.out.print(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                        .baselineId(baselineId)
                                                                        .build();

            System.out.print(String.format("test before Response %n"));

            GetPatchBaselineResponse getPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);

            System.out.print(String.format("test after Response %n"));
            System.out.print(String.format("test after Response id %s %n", getPatchBaselineResponse.baselineId()));
            System.out.print(String.format("test after Response description %s %n", getPatchBaselineResponse.description()));
            System.out.print(String.format("test after Response security %s %n", getPatchBaselineResponse.approvedPatchesEnableNonSecurity()));
            for (String patch : getPatchBaselineResponse.approvedPatches())
                System.out.print(String.format("test after Response accepted patches %s %n", patch));
            for (String patch : getPatchBaselineResponse.rejectedPatches())
                System.out.print(String.format("test after Response rejected patches %s %n", patch));
            System.out.print(String.format("test after Response OS %s %n", getPatchBaselineResponse.operatingSystemAsString()));

            ResourceModel resourcemodel = ReadResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

            System.out.print(String.format("test after Response, after translator %s %n", resourcemodel.getId()));

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
