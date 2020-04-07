package software.amazon.ssm.patchbaseline.translator.request;

import software.amazon.awssdk.services.ssm.model.UpdatePatchBaselineRequest;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.translator.resourcemodel.ResourceModelPropertyTranslator;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;

public class UpdatePatchBaselineRequestTranslator {

    public static UpdatePatchBaselineRequest updatePatchBaseline(final ResourceModel model) {


        final UpdatePatchBaselineRequest.Builder updatePatchBaselineRequestBuilder =
                UpdatePatchBaselineRequest.builder()
                        .baselineId(model.getId())
                        .replace(true)
                        .approvedPatchesEnableNonSecurity(model.getApprovedPatchesEnableNonSecurity());

        SimpleTypeValidator.getValidatedString(model.getName())
                .ifPresent(updatePatchBaselineRequestBuilder::name);

        SimpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(updatePatchBaselineRequestBuilder::description);

        SimpleTypeValidator.getValidatedString(model.getRejectedPatchesAction())
                .ifPresent(updatePatchBaselineRequestBuilder::rejectedPatchesAction);

        SimpleTypeValidator.getValidatedString(model.getApprovedPatchesComplianceLevel())
                .ifPresent(updatePatchBaselineRequestBuilder::approvedPatchesComplianceLevel);

        SimpleTypeValidator.getValidatedList(model.getApprovedPatches())
                .ifPresent(updatePatchBaselineRequestBuilder::approvedPatches);

        SimpleTypeValidator.getValidatedList(model.getRejectedPatches())
                .ifPresent(updatePatchBaselineRequestBuilder::rejectedPatches);

        ResourceModelPropertyTranslator.translateToRequestSources(model.getSources())
                .ifPresent(updatePatchBaselineRequestBuilder::sources);

        ResourceModelPropertyTranslator.translateToRequestGlobalFilters(model.getGlobalFilters())
                .ifPresent(updatePatchBaselineRequestBuilder::globalFilters);

        ResourceModelPropertyTranslator.translateToRequestApprovalRules(model.getApprovalRules())
                .ifPresent(updatePatchBaselineRequestBuilder::approvalRules);

        return updatePatchBaselineRequestBuilder.build();
    }
}
