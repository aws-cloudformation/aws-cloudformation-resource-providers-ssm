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

        System.out.print(String.format("This is a input with baselineId %s %n", model.getId()));

        SimpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(updatePatchBaselineRequestBuilder::description);

        System.out.print(String.format("This is a input with description %s %n", model.getDescription()));

        SimpleTypeValidator.getValidatedString(model.getRejectedPatchesAction())
                .ifPresent(updatePatchBaselineRequestBuilder::rejectedPatchesAction);

        System.out.print(String.format("This is a input with rejectedPatchesActionAsString %s %n", model.getRejectedPatchesAction()));

        SimpleTypeValidator.getValidatedString(model.getApprovedPatchesComplianceLevel())
                .ifPresent(updatePatchBaselineRequestBuilder::approvedPatchesComplianceLevel);

        System.out.print(String.format("This is a input with approvedPatchesComplianceLevelAsString %s %n", model.getApprovedPatchesComplianceLevel()));

        SimpleTypeValidator.getValidatedList(model.getApprovedPatches())
                .ifPresent(updatePatchBaselineRequestBuilder::approvedPatches);

        for (String str : model.getApprovedPatches())
            System.out.print(String.format("This is a input with approvedPatches %s %n", str));

        SimpleTypeValidator.getValidatedList(model.getRejectedPatches())
                .ifPresent(updatePatchBaselineRequestBuilder::rejectedPatches);

        for (String str : model.getRejectedPatches())
            System.out.print(String.format("This is a input with getRejectedPatches %s %n", str));

        ResourceModelPropertyTranslator.translateToRequestSources(model.getSources())
                .ifPresent(updatePatchBaselineRequestBuilder::sources);

        // log sources
        for (software.amazon.ssm.patchbaseline.PatchSource source : model.getSources()) {
            System.out.print(String.format("test Request convert model source with name %s, config %s. %n", source.getName(), source.getConfiguration()));

            for (String product : source.getProducts()) {
                System.out.print(String.format("test Request convert model source with product %s %n", product));
            }
        }

        ResourceModelPropertyTranslator.translateToRequestGlobalFilters(model.getGlobalFilters())
                .ifPresent(updatePatchBaselineRequestBuilder::globalFilters);

        //log global filters
        for (software.amazon.ssm.patchbaseline.PatchFilter patchFilter : model.getGlobalFilters().getPatchFilters()) {
            for (String value : patchFilter.getValues()) {
                System.out.print(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.getKey(), value));
            }
        }

        ResourceModelPropertyTranslator.translateToRequestApprovalRules(model.getApprovalRules())
                .ifPresent(updatePatchBaselineRequestBuilder::approvalRules);

        // log approval rules
        for (software.amazon.ssm.patchbaseline.Rule patchRule: model.getApprovalRules().getPatchRules()) {
            System.out.print(String.format("test  enableNonSecurity %b %n", patchRule.getEnableNonSecurity()));
            System.out.print(String.format("test  approval after %d %n", patchRule.getApproveAfterDays()));
            System.out.print(String.format("test  complianceLevelAsString %s %n", patchRule.getComplianceLevel()));
            for (software.amazon.ssm.patchbaseline.PatchFilter patchFilter : patchRule.getPatchFilterGroup().getPatchFilters()) {
                for (String value : patchFilter.getValues()) {
                    System.out.print(String.format("test Request approvalRules model patch filter %s, %s %n", patchFilter.getKey(), value));
                }
            }
        }

        return updatePatchBaselineRequestBuilder.build();
    }
}
