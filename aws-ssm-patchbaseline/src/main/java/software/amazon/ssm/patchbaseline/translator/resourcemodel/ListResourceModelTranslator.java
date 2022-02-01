package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;
import software.amazon.awssdk.services.ssm.model.PatchBaselineIdentity;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesResponse;

import java.util.List;
import java.util.stream.Collectors;

public class ListResourceModelTranslator {

    /**
     * Translate Read Response to Resource Model
     */
    public static ResourceModel translateToResourceModel(final DescribePatchBaselinesResponse describePatchBaselinesResponse) {

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

