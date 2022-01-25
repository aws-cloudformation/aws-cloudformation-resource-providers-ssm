package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;
import software.amazon.awssdk.services.ssm.model.PatchBaselineIdentity;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadResourceModelTranslator {

    /**
     * Translate Read Response to Resource Model
     */
    public static ResourceModel translateToResourceModel(final GetPatchBaselineResponse getPatchBaselineResponse,
                                                         final List<Tag> tags) {

        final ResourceModel model = new ResourceModel();

        model.setId(getPatchBaselineResponse.baselineId());
        model.setApprovedPatchesEnableNonSecurity(getPatchBaselineResponse.approvedPatchesEnableNonSecurity());

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.name())
                .ifPresent(model::setName);

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.operatingSystemAsString())
                .ifPresent(model::setOperatingSystem);

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.description())
                .ifPresent(model::setDescription);

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.rejectedPatchesActionAsString())
                .ifPresent(model::setRejectedPatchesAction);

        SimpleTypeValidator.getValidatedString(getPatchBaselineResponse.approvedPatchesComplianceLevelAsString())
                .ifPresent(model::setApprovedPatchesComplianceLevel);

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.rejectedPatches())
                .ifPresent(model::setRejectedPatches);

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.approvedPatches())
                .ifPresent(model::setApprovedPatches);

        SimpleTypeValidator.getValidatedList(getPatchBaselineResponse.patchGroups())
                .ifPresent(model::setPatchGroups);

        ResourceModelPropertyTranslator.translateToResourceModelSources(getPatchBaselineResponse.sources())
                .ifPresent(model::setSources);

        ResourceModelPropertyTranslator.translateToResourceModelGlobalFilters(getPatchBaselineResponse.globalFilters())
                .ifPresent(model::setGlobalFilters);

        ResourceModelPropertyTranslator.translateToResourceModelApprovalRules(getPatchBaselineResponse.approvalRules())
                .ifPresent(model::setApprovalRules);

        ResourceModelPropertyTranslator.translateToResourceModelTags(tags)
                .ifPresent(model::setTags);

        return model;
    }

    /**
     * Translate Read Response to Resource Model
     */
    public static ResourceModel translateToResourceModel(final DescribePatchBaselinesResponse describePatchBaselinesResponse,
                                                         final List<Tag> tags) {

        final ResourceModel model = new ResourceModel();

        List<PatchBaselineIdentity>  patchBaselineIdentityList  = describePatchBaselinesResponse.baselineIdentities().stream().collect(Collectors.toList());
        for(PatchBaselineIdentity patchBaselineEntity :  patchBaselineIdentityList){
            model.setId(patchBaselineEntity.baselineId());

            SimpleTypeValidator.getValidatedString(patchBaselineEntity.baselineName())
                    .ifPresent(model::setName);

            SimpleTypeValidator.getValidatedString(patchBaselineEntity.operatingSystemAsString())
                    .ifPresent(model::setOperatingSystem);

            SimpleTypeValidator.getValidatedString(patchBaselineEntity.baselineDescription())
                    .ifPresent(model::setDescription);

        }

        return model;
    }

}
