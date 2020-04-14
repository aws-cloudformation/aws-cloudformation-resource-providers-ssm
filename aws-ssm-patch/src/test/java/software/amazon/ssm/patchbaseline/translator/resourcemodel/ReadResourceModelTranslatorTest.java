package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.TestBase;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class ReadResourceModelTranslatorTest extends TestBase {

    private GetPatchBaselineResponse getPatchBaselineResponse;

    @Test
    void testReadResourceModelTranslatorWith_Nominal() {

        List<PatchSource> sources = requestSources();
        PatchFilterGroup globalFilters = requestGlobalFilters();
        PatchRuleGroup approvalRules = requestApprovalRules();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(BASELINE_ID)
                .name(BASELINE_NAME)
                .operatingSystem(OPERATING_SYSTEM)
                .description(BASELINE_DESCRIPTION)
                .rejectedPatches(REJECTED_PATCHES)
                .rejectedPatchesAction("BLOCK")
                .approvedPatches(ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(ComplianceLevel.CRITICAL))
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sources)
                .patchGroups(PATCH_GROUPS)
                .build();

        final ResourceModel resultModel =
                ReadResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = buildDefaultInputRequest().getDesiredResourceState();
        expectedModel.setTags(null);

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void testReadResourceModelTranslator_Null() {

        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(BASELINE_ID)
                .build();

        final ResourceModel resultModel =
                ReadResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = ResourceModel.builder().id(BASELINE_ID).build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void testReadResourceModelTranslator_Empty() {

        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(BASELINE_ID)
                .rejectedPatches(new ArrayList<>())
                .approvedPatches(new ArrayList<>())
                .approvalRules(PatchRuleGroup.builder().build())
                .globalFilters(PatchFilterGroup.builder().build())
                .sources(new ArrayList<>())
                .patchGroups(new ArrayList<>())
                .build();

        final ResourceModel resultModel =
                ReadResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = ResourceModel.builder().id(BASELINE_ID).build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }



}
