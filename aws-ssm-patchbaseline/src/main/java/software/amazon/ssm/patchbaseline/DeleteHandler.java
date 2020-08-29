package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static software.amazon.ssm.patchbaseline.ResourceModel.TYPE_NAME;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeletePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.DeletePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;


public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String baselineId = model.getId();

        logger.log(String.format(
                "INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));
        try {
            removePatchGroupsFromBaseline(baselineId, logger, proxy, ssmClient);

            //Now delete the patch baseline
            //For nonexistent baselines, this should return success and the CloudFormation Delete should succeed.
            final DeletePatchBaselineRequest deletePatchBaselineRequest = DeletePatchBaselineRequest.builder()
                                                                                    .baselineId(baselineId)
                                                                                    .build();

            final DeletePatchBaselineResponse deletePatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(deletePatchBaselineRequest, ssmClient::deletePatchBaseline);

            logger.log(String.format("INFO Deleted patch baseline %s successfully %n", baselineId));

            //If we made it here, the delete request succeeded. Success.
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(null)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            return Resource.handleException(e, model, baselineId, logger);
        }

    }

    private void removePatchGroupsFromBaseline(final String baselineId,
                                               final Logger logger,
                                               final AmazonWebServicesClientProxy proxy,
                                               final SsmClient ssmClient) {
        //First, get a list of all the registered groups of the baseline
        GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                .baselineId(baselineId)
                                                                .build();

        GetPatchBaselineResponse getPatchBaselineResponse =
                proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);

        //Remove each group from the baseline
        //TODO: Look into rate limiting this if we get large numbers of patch groups registered with a baseline
        for (String group : getPatchBaselineResponse.patchGroups()) {
            DeregisterPatchBaselineForPatchGroupRequest deregisterRequest =
                    DeregisterPatchBaselineForPatchGroupRequest.builder()
                                .baselineId(baselineId)
                                .patchGroup(group)
                                .build();

            DeregisterPatchBaselineForPatchGroupResponse deregisterResponse =
                    proxy.injectCredentialsAndInvokeV2(deregisterRequest, ssmClient::deregisterPatchBaselineForPatchGroup);
        }

        logger.log(String.format("INFO Deregistered group(s) from patch baseline %s %n", baselineId));
    }
}
