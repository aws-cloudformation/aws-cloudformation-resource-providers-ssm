package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.UpdatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.UpdatePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;

import static software.amazon.ssm.patchbaseline.ResourceModel.TYPE_NAME;
import software.amazon.ssm.patchbaseline.translator.resourcemodel.ResourceModelPropertyTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();
    protected static final String PATCH_BASELINE_RESOURCE_NAME = "PatchBaseline";

    private final TagHelper tagHelper;

    public UpdateHandler() {
        this(new TagHelper());
    }

    public UpdateHandler(TagHelper tagHelper) {
        this.tagHelper = tagHelper;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        // if failed, return previous resource state
        final ResourceModel previousModel = request.getPreviousResourceState();
        String baselineId = model.getId();

        logger.log(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            //Build our request

            UpdatePatchBaselineRequest updatePatchBaselineRequest = updatePatchBaseline(model);

            // build request and send SSM
            // final UpdatePatchBaselineRequest updatePatchBaselineRequest = updatePatchBaseline(model);
            final UpdatePatchBaselineResponse updatePatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(updatePatchBaselineRequest, ssmClient::updatePatchBaseline);
            logger.log(String.format("INFO Updated patch baseline %s successfully %n", baselineId));

            //Get an List of the current groups
            GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                .baselineId(baselineId)
                                                                .build();

            GetPatchBaselineResponse getPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);
            List<String> originalGroups = getPatchBaselineResponse.patchGroups();

            //Get the new/desired patch groups
            // List<String> newGroups = (List<String>) model.getPatchGroups();
            List<String> newGroups = Collections.emptyList();
            if (! CollectionUtils.isNullOrEmpty(model.getPatchGroups())) {
                newGroups = model.getPatchGroups();
            }

            //Compute the intersection of the two lists (the groups that don't need to be changed)
            List<String> intersectingGroups = new ArrayList<>(originalGroups);
            intersectingGroups.retainAll(newGroups);

            //The groups we need to remove are ORIGINAL - INTERSECT
            //The groups we need to add are DESIRED - INTERSECT
            originalGroups.removeAll(intersectingGroups);
            newGroups.removeAll(intersectingGroups);

            //Remove the old groups first
            for (String group : originalGroups) {
                DeregisterPatchBaselineForPatchGroupRequest deregisterRequest =
                         DeregisterPatchBaselineForPatchGroupRequest.builder()
                                    .baselineId(baselineId)
                                    .patchGroup(group)
                                    .build();
                DeregisterPatchBaselineForPatchGroupResponse deregisterResponse =
                        proxy.injectCredentialsAndInvokeV2(deregisterRequest, ssmClient::deregisterPatchBaselineForPatchGroup);
            }
            logger.log(String.format("INFO Deregistered old group(s) from patch baseline %s %n",
                    getPatchBaselineResponse.baselineId()));

            //Add the new groups after
            for (String group : newGroups) {
                RegisterPatchBaselineForPatchGroupRequest groupRequest =
                         RegisterPatchBaselineForPatchGroupRequest.builder()
                                    .baselineId(baselineId)
                                    .patchGroup(group)
                                    .build();
                RegisterPatchBaselineForPatchGroupResponse groupResponse =
                        proxy.injectCredentialsAndInvokeV2(groupRequest, ssmClient::registerPatchBaselineForPatchGroup);
            }
            logger.log(String.format("INFO Registered new group(s) from patch baseline %s", baselineId));

            //Remove old tags (except those that are overwritten) then add new tags
            tagHelper.updateTagsForResource(request, PATCH_BASELINE_RESOURCE_NAME, ssmClient, proxy);
            logger.log(String.format("INFO Updated tags for patch baseline %s %n", baselineId));

            //If we made it here, we're done
            logger.log(String.format("INFO Successfully updated patch baseline %s %n", baselineId));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();

        } catch (Exception e) {
            return Resource.handleException(e, previousModel, baselineId, logger);
        }

    }

    private UpdatePatchBaselineRequest updatePatchBaseline(final ResourceModel model) {

//        return UpdatePatchBaselineRequest.builder()
//                .baselineId(model.getId())
//                .name(model.getName())
//                .globalFilters(ResourceModelPropertyTranslator.translateToRequestGlobalFilters(model.getGlobalFilters()))
//                .approvalRules(ResourceModelPropertyTranslator.translateToRequestApprovalRules(model.getApprovalRules()))
//                .approvedPatches(model.getApprovedPatches())
//                .approvedPatchesComplianceLevel(model.getApprovedPatchesComplianceLevel())
//                .approvedPatchesEnableNonSecurity(model.getApprovedPatchesEnableNonSecurity())
//                .rejectedPatches(model.getRejectedPatches())
//                .rejectedPatchesAction(model.getRejectedPatchesAction())
//                .description(model.getDescription())
//                .sources(ResourceModelPropertyTranslator.translateToRequestSources(model.getSources()))
//                .replace(true)
//                .build();
        return null;

    }
}
