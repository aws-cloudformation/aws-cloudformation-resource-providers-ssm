package software.amazon.ssm.patchbaseline.translator.request;

import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.translator.resourcemodel.ResourceModelPropertyTranslator;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;

public class CreatePatchBaselineRequestTranslator {

    public static CreatePatchBaselineRequest createPatchBaseline(final ResourceModel model,
                                                           final ResourceHandlerRequest<ResourceModel> request,
                                                           final Logger logger) {
        final CreatePatchBaselineRequest.Builder createPatchBaselineRequestBuilder =
                CreatePatchBaselineRequest.builder()
                        .approvedPatchesEnableNonSecurity(model.getApprovedPatchesEnableNonSecurity());

        SimpleTypeValidator.getValidatedString(model.getName())
                .ifPresent(createPatchBaselineRequestBuilder::name);

        SimpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(createPatchBaselineRequestBuilder::description);

        SimpleTypeValidator.getValidatedString(request.getClientRequestToken())
                .ifPresent(createPatchBaselineRequestBuilder::clientToken);

        SimpleTypeValidator.getValidatedString(model.getOperatingSystem())
                .ifPresent(createPatchBaselineRequestBuilder::operatingSystem);

        SimpleTypeValidator.getValidatedString(model.getRejectedPatchesAction())
                .ifPresent(createPatchBaselineRequestBuilder::rejectedPatchesAction);

        SimpleTypeValidator.getValidatedString(model.getApprovedPatchesComplianceLevel())
                .ifPresent(createPatchBaselineRequestBuilder::approvedPatchesComplianceLevel);

        SimpleTypeValidator.getValidatedList(model.getApprovedPatches())
                .ifPresent(createPatchBaselineRequestBuilder::approvedPatches);

        SimpleTypeValidator.getValidatedList(model.getRejectedPatches())
                .ifPresent(createPatchBaselineRequestBuilder::rejectedPatches);

        ResourceModelPropertyTranslator.translateToRequestTags(model.getTags())
                .ifPresent(createPatchBaselineRequestBuilder::tags);

        ResourceModelPropertyTranslator.translateToRequestSources(model.getSources())
                .ifPresent(createPatchBaselineRequestBuilder::sources);

        ResourceModelPropertyTranslator.translateToRequestGlobalFilters(model.getGlobalFilters())
                .ifPresent(createPatchBaselineRequestBuilder::globalFilters);

        ResourceModelPropertyTranslator.translateToRequestApprovalRules(model.getApprovalRules())
                .ifPresent(createPatchBaselineRequestBuilder::approvalRules);

        return createPatchBaselineRequestBuilder.build();
    }
}
