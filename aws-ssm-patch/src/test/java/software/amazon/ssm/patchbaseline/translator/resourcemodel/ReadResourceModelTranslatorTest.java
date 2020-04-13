package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.TestBase;
import software.amazon.ssm.patchbaseline.TestConstants;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class ReadResourceModelTranslatorTest extends TestBase {

    private SimpleTypeValidator simpleTypeValidator;
    private ReadResourceModelTranslator readResourceModelTranslator;
    private GetPatchBaselineResponse getPatchBaselineResponse;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();
        readResourceModelTranslator = new ReadResourceModelTranslator();
    }

    @Test
    void testReadResourceModelTranslatorWith_Nominal() {

        List<PatchSource> sources = requestsources();
        PatchFilterGroup globalFilters = requestglobalFilters();
        PatchRuleGroup approvalRules = requestapprovalRules();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .name(TestConstants.BASELINE_NAME)
                .operatingSystem(TestConstants.OPERATING_SYSTEM)
                .description(TestConstants.BASELINE_DESCRIPTION)
                .rejectedPatches(TestConstants.REJECTED_PATCHES)
                .rejectedPatchesAction("BLOCK")
                .approvedPatches(TestConstants.ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(TestConstants.ComplianceLevel.CRITICAL))
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sources)
                .patchGroups(TestConstants.PATCH_GROUPS)
                .build();

        final ResourceModel resultModel =
                readResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = buildDefaultInputRequest().getDesiredResourceState();
        expectedModel.setTags(null);

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void testReadResourceModelTranslator_Null() {

        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .build();

        final ResourceModel resultModel =
                readResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = ResourceModel.builder().id(TestConstants.BASELINE_ID).build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void testReadResourceModelTranslator_Empty() {

        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .rejectedPatches(new ArrayList<>())
                .approvedPatches(new ArrayList<>())
                .approvalRules(PatchRuleGroup.builder().build())
                .globalFilters(PatchFilterGroup.builder().build())
                .sources(new ArrayList<>())
                .patchGroups(new ArrayList<>())
                .build();

        final ResourceModel resultModel =
                readResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = ResourceModel.builder().id(TestConstants.BASELINE_ID).build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

}
