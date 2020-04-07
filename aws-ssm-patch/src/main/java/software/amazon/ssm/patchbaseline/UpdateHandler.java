package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.ssm.patchbaseline.translator.request.UpdatePatchBaselineRequestTranslator;
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
            UpdatePatchBaselineRequest updatePatchBaselineRequest = UpdatePatchBaselineRequestTranslator.updatePatchBaseline(model);

            System.out.print(String.format("test Request baselineId %s %n", updatePatchBaselineRequest.baselineId()));
            System.out.print(String.format("test Request description %s %n", updatePatchBaselineRequest.description()));
            System.out.print(String.format("test Request description %b %n", updatePatchBaselineRequest.replace()));
            System.out.print(String.format("test Request previous description %s %n", previousModel.getDescription()));
            // log sources
            for (PatchSource source : updatePatchBaselineRequest.sources()) {
                System.out.print(String.format("test Request convert model source with name %s, config %s. %n", source.name(), source.configuration()));
                logger.log(String.format("test Request convert model source with name %s, config %s. %n", source.name(), source.configuration()));

                for (String product : source.products()) {
                    System.out.print(String.format("test Request convert model source with product %s %n", product));
                    logger.log(String.format("test Request convert model source with product %s %n", product));
                }
            }
            //log global filters
            for (software.amazon.awssdk.services.ssm.model.PatchFilter patchFilter : updatePatchBaselineRequest.globalFilters().patchFilters()) {
                for (String value : patchFilter.values()) {
                    System.out.print(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.keyAsString(), value));
                    logger.log(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.keyAsString(), value));
                }
            }
            // log approval rules
            for (PatchRule patchRule : updatePatchBaselineRequest.approvalRules().patchRules()) {
                System.out.print(String.format("test Request enableNonSecurity %b %n", patchRule.enableNonSecurity()));
                System.out.print(String.format("test Request approval after %d %n", patchRule.approveAfterDays()));
                System.out.print(String.format("test Request complianceLevelAsString %s %n", patchRule.complianceLevelAsString()));
                logger.log(String.format("test Request enableNonSecurity %b %n", patchRule.enableNonSecurity()));
                logger.log(String.format("test Request approval after %d %n", patchRule.approveAfterDays()));
                logger.log(String.format("test Request complianceLevelAsString %s %n", patchRule.complianceLevelAsString()));
                for (PatchFilter patchFilter : patchRule.patchFilterGroup().patchFilters()) {
                    for (String value : patchFilter.values()) {
                        System.out.print(String.format("test Request approvalRules model patch filter %s, %s %n", patchFilter.keyAsString(), value));
                        logger.log(String.format("test Request approvalRules model patch filter %s, %s %n", patchFilter.keyAsString(), value));
                    }
                }
            }

            // build request and send SSM
            // final UpdatePatchBaselineRequest updatePatchBaselineRequest = updatePatchBaseline(model);
            final UpdatePatchBaselineResponse updatePatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(updatePatchBaselineRequest, ssmClient::updatePatchBaseline);

            logger.log(String.format("INFO Updated patch baseline %s successfully %n", baselineId));

            System.out.print(String.format("INFO Updated patch baseline %s successfully %n", baselineId));

            //Get an List of the current groups
            GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                .baselineId(baselineId)
                                                                .build();

            GetPatchBaselineResponse getPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);
            List<String> originalGroups = getPatchBaselineResponse.patchGroups();

            for (String group : getPatchBaselineResponse.patchGroups())
                System.out.print(String.format("INFO original patch groups %s %n", group));

            //Get the new/desired patch groups
            List<String> newGroups = new ArrayList<>();
            if (! CollectionUtils.isNullOrEmpty(model.getPatchGroups())) {
                newGroups = model.getPatchGroups();
            }

            for (String group : model.getPatchGroups())
                System.out.print(String.format("INFO new patch groups %s %n", group));

            //Compute the intersection of the two lists (the groups that don't need to be changed)
//            List<String> intersectingGroups = new ArrayList<>(originalGroups);
//            intersectingGroups.retainAll(newGroups);
//
//            for (String group : intersectingGroups)
//                System.out.print(String.format("intersecting Groups to remain %s %n", group));
//
//            //The groups we need to remove are ORIGINAL - INTERSECT
//            //The groups we need to add are DESIRED - INTERSECT
//            newGroups.removeAll(intersectingGroups);
//
//            for (String group : newGroups)
//                System.out.print(String.format("expected new groups to add %s %n", group));
//
//            originalGroups.removeAll(intersectingGroups);
//
//            for (String group : originalGroups)
//                System.out.print(String.format("expected original groups to remove %s %n", group));

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

            System.out.print(String.format("INFO Deregistered old group(s) from patch baseline %s %n",
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
            logger.log(String.format("INFO Registered new group(s) from patch baseline %s %n", baselineId));

            System.out.print(String.format("INFO Registered new group(s) from patch baseline %s %n", baselineId));

            //Remove old tags (except those that are overwritten) then add new tags

            tagHelper.updateTagsForResource(request, PATCH_BASELINE_RESOURCE_NAME, ssmClient, proxy);

            logger.log(String.format("INFO Updated tags for patch baseline %s %n", baselineId));

            System.out.print(String.format("INFO Updated tags for patch baseline %s %n", baselineId));

            //If we made it here, we're done
            logger.log(String.format("INFO Successfully updated patch baseline %s %n", baselineId));

            System.out.print(String.format("INFO Successfully updated patch baseline %s %n", baselineId));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();

        } catch (Exception e) {
            return Resource.handleException(e, previousModel, baselineId, logger);
        }

    }

}
