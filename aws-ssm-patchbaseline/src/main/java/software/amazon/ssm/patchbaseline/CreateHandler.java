package software.amazon.ssm.patchbaseline;

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

import java.util.ArrayList;
import java.util.List;

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

        String baselineId = null;
        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            /**Validate, merge, and add 3 sets of Tags to request
             * Tags added to our specific Patch Baseline resource in the template, which is in ResourceModel
             * Tags added to the entire CloudFormation Stack, which is in desiredResourceTag
             * System Tags set by CloudFormation service, which is systemTags
             **/

            List<Tag> createTags = tagHelper.validateAndMergeTagsForCreate(request, model.getTags());
            ResourceModelPropertyTranslator.translateToResourceModelTags(createTags).ifPresent(model::setTags);

            CreatePatchBaselineRequest createPatchBaselineRequest =
                    CreatePatchBaselineRequestTranslator.createPatchBaseline(model, request.getClientRequestToken());

            final CreatePatchBaselineResponse createPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(createPatchBaselineRequest, ssmClient::createPatchBaseline);

            baselineId = createPatchBaselineResponse.baselineId();

            logger.log(String.format("INFO Created patch baseline %s successfully. Adding groups (if any) %n", baselineId));

            // put physical ID to model
            model.setId(baselineId);

            // This is not in the definition for a baseline object but we must receive it from CFN
            // Register the groups for this Patch Baseline
            List<String> patchGroups = CollectionUtils.isNullOrEmpty(model.getPatchGroups()) ? new ArrayList<>() : model.getPatchGroups();

            for (String group : patchGroups) {
                //Each group needs its own Register call
                RegisterPatchBaselineForPatchGroupRequest groupRequest =
                        registerPatchBaselineForPatchGroupRequest(baselineId, group);
                RegisterPatchBaselineForPatchGroupResponse groupResponse =
                        proxy.injectCredentialsAndInvokeV2(groupRequest, ssmClient::registerPatchBaselineForPatchGroup);
            }

            // If we made it here, no exceptions related to the requests were thrown. Success.
            logger.log(String.format("INFO Registered groups to patch baseline %s successfully %n", baselineId));

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
