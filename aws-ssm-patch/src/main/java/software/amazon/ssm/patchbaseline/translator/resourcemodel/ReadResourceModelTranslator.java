package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;

import java.util.Collections;

public class ReadResourceModelTranslator {

    /**
     * Translate Read Response to Resource Model
     */
    public static ResourceModel translateToResourceModel(final GetPatchBaselineResponse getPatchBaselineResponse) {

        if (CollectionUtils.isNullOrEmpty(Collections.singleton(getPatchBaselineResponse)))
            System.out.print(String.format("This is a null input %n"));

        final ResourceModel model = new ResourceModel();

        model.setId(getPatchBaselineResponse.baselineId());
        model.setApprovedPatchesEnableNonSecurity(getPatchBaselineResponse.approvedPatchesEnableNonSecurity());

        System.out.print(String.format("This is a input with baselineId %s %n", getPatchBaselineResponse.baselineId()));
        System.out.print(String.format("This is a input with baselineId %s %n", model.getId()));

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.name())
                .ifPresent(model::setName);

        System.out.print(String.format("This is a input with name %s %n", model.getName()));

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.operatingSystemAsString())
                .ifPresent(model::setOperatingSystem);

        System.out.print(String.format("This is a input with OS %s %n", model.getOperatingSystem()));

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.description())
                .ifPresent(model::setDescription);

        System.out.print(String.format("This is a input with description %s %n", model.getDescription()));

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.rejectedPatchesActionAsString())
                .ifPresent(model::setRejectedPatchesAction);

        System.out.print(String.format("This is a input with rejectedPatchesActionAsString %s %n", model.getRejectedPatchesAction()));

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.approvedPatchesComplianceLevelAsString())
                .ifPresent(model::setApprovedPatchesComplianceLevel);

        System.out.print(String.format("This is a input with approvedPatchesComplianceLevelAsString %s %n", model.getApprovedPatchesComplianceLevel()));

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.rejectedPatches())
                .ifPresent(model::setRejectedPatches);

        for (String str : model.getRejectedPatches())
            System.out.print(String.format("This is a input with getRejectedPatches %s %n", str));

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.approvedPatches())
                .ifPresent(model::setApprovedPatches);

        for (String str : model.getApprovedPatches())
            System.out.print(String.format("This is a input with approvedPatches %s %n", str));

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.patchGroups())
                .ifPresent(model::setPatchGroups);

        for (String str : model.getPatchGroups())
            System.out.print(String.format("This is a input with patchGroups %s %n", str));

        ResourceModelPropertyTranslator.translateToResourceModelSources(getPatchBaselineResponse.sources())
                .ifPresent(model::setSources);

        // log sources
        for (software.amazon.ssm.patchbaseline.PatchSource source : model.getSources()) {
            System.out.print(String.format("test Request convert model source with name %s, config %s. %n", source.getName(), source.getConfiguration()));

            for (String product : source.getProducts()) {
                System.out.print(String.format("test Request convert model source with product %s %n", product));
            }
        }

        ResourceModelPropertyTranslator.translateToResourceModelGlobalFilters(getPatchBaselineResponse.globalFilters())
                .ifPresent(model::setGlobalFilters);

        //log global filters
        for (software.amazon.ssm.patchbaseline.PatchFilter patchFilter : model.getGlobalFilters().getPatchFilters()) {
            for (String value : patchFilter.getValues()) {
                System.out.print(String.format("test Request model getGlobalFilters %s, %s %n", patchFilter.getKey(), value));
            }
        }

        ResourceModelPropertyTranslator.translateToResourceModelApprovalRules(getPatchBaselineResponse.approvalRules())
                .ifPresent(model::setApprovalRules);

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

        return model;
    }

}
