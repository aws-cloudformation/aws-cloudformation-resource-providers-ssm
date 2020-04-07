package software.amazon.ssm.patchbaseline;


import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupResponse;
import software.amazon.ssm.patchbaseline.translator.request.CreatePatchBaselineRequestTranslator;
import software.amazon.ssm.patchbaseline.translator.resourcemodel.ResourceModelPropertyTranslator;

import static software.amazon.ssm.patchbaseline.ResourceModel.TYPE_NAME;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;

import java.util.List;
import java.util.Collections;
import java.util.Map;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();

    private final TagHelper tagHelper;

    public CreateHandler() {
        this(new TagHelper());
    }

    public CreateHandler(TagHelper tagHelper) {
        this.tagHelper = tagHelper;
    }


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        //final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        String baselineId = null;
        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        System.out.print(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            /**Validate, merge, and add 3 sets of Tags to request
             * Tags added to our specific Patch Baseline resource in the template, which is in ResourceModel
             * Tags added to the entire CloudFormation Stack, which is in desiredResourceTag
             * System Tags set by CloudFormation service, which is systemTags
             **/

            List<Tag> createTags = tagHelper.validateAndMergeTagsForCreate(request, model.getTags());
            ResourceModelPropertyTranslator.translateToResourceModelTags(createTags).ifPresent(model::setTags);

            // log tag
            for (Map.Entry<String,String> entry : request.getDesiredResourceTags().entrySet()) {
                System.out.print("getDesiredResourceTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
                logger.log("getDesiredResourceTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
            }
            for (Map.Entry<String,String> entry : request.getSystemTags().entrySet()) {
                System.out.print("getSystemTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
                logger.log("getSystemTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
            }
            for (software.amazon.ssm.patchbaseline.Tag entry : model.getTags()) {
                System.out.print("getTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
                logger.log("getTags, Key = " + entry.getKey() + ", Value = " + entry.getValue() + String.format("%n"));
            }

            CreatePatchBaselineRequest createPatchBaselineRequest =
                    CreatePatchBaselineRequestTranslator.createPatchBaseline(model, request, logger);

            // log tag
            for (Tag tag : createPatchBaselineRequest.tags()) {
                System.out.print(String.format("test Request tag key %s,value %s %n", tag.key(), tag.value()));
                logger.log(String.format("test Request tag key %s,value %s %n", tag.key(), tag.value()));
            }
            // log sources
            for (PatchSource source : createPatchBaselineRequest.sources()) {
                System.out.print(String.format("test Request convert model source with name %s, config %s. %n", source.name(), source.configuration()));
                logger.log(String.format("test Request convert model source with name %s, config %s. %n", source.name(), source.configuration()));

                for (String product : source.products()) {
                    System.out.print(String.format("test Request convert model source with product %s %n", product));
                    logger.log(String.format("test Request convert model source with product %s %n", product));
                }
            }
            //log global filters
            for (PatchFilter patchFilter : createPatchBaselineRequest.globalFilters().patchFilters()) {
                for (String value : patchFilter.values()) {
                    System.out.print(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.keyAsString(), value));
                    logger.log(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.keyAsString(), value));
                }
            }
            // log approval rules
            for (PatchRule patchRule : createPatchBaselineRequest.approvalRules().patchRules()) {
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

            final CreatePatchBaselineResponse createPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(createPatchBaselineRequest, ssmClient::createPatchBaseline);

            baselineId = createPatchBaselineResponse.baselineId();

            logger.log(String.format("INFO Created patch baseline %s successfully. Adding groups (if any) %n", baselineId));
            System.out.print(String.format("INFO Created patch baseline %s successfully. Adding groups (if any) %n", baselineId));

            // This is not in the definition for a baseline object but we must receive it from CFN
            // Register the groups for this Patch Baseline
            // List<String> patchGroups = (List<String>) model.getPatchGroups();
            List<String> patchGroups = Collections.emptyList();
            if (! CollectionUtils.isNullOrEmpty(model.getPatchGroups())) {
                patchGroups = model.getPatchGroups();
            }

            for (String group : patchGroups) {
                //Each group needs its own Register call
                RegisterPatchBaselineForPatchGroupRequest groupRequest =
                        registerPatchBaselineForPatchGroupRequest(baselineId, group);
                RegisterPatchBaselineForPatchGroupResponse groupResponse =
                        proxy.injectCredentialsAndInvokeV2(groupRequest, ssmClient::registerPatchBaselineForPatchGroup);
            }

            //If we made it here, no exceptions related to the requests were thrown. Success.
            logger.log(String.format("INFO Registered groups to patch baseline %s successfully %n", baselineId));
            System.out.print(String.format("INFO Registered groups to patch baseline %s successfully %n", baselineId));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();

        } catch (Exception e) {
            return Resource.handleException(e, model, baselineId, logger);
        }

    }

    private RegisterPatchBaselineForPatchGroupRequest registerPatchBaselineForPatchGroupRequest(final String baselineId,
                                                                   String group) {
        return RegisterPatchBaselineForPatchGroupRequest.builder()
                .baselineId(baselineId)
                .patchGroup(group)
                .build() ;
    }


}

