package software.amazon.ssm.patchbaseline;

import com.amazonaws.AmazonServiceException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;

import software.amazon.awssdk.services.ssm.SsmClient;

import org.mockito.Mock;

import java.util.*;


public class TestBase {
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    protected SsmClient ssmClient;
    @Mock
    protected Logger logger;

    protected ResourceHandlerRequest<ResourceModel> buildDefaultInputRequest() {
        List<software.amazon.ssm.patchbaseline.Tag> tags = tags();
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();

        Map<String, String> desiredResourceTagsMap = new HashMap<>();
        desiredResourceTagsMap.put(TestConstants.CFN_KEY, TestConstants.CFN_VALUE);

        Map<String, String> systemTagsMap = new HashMap<>();
        systemTagsMap.put(TestConstants.SYSTEM_TAG_KEY, TestConstants.BASELINE_NAME);

        ResourceModel model = buildDefaultInputModel(tags, sources, globalFilters, approvalRules);
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                            .desiredResourceTags(desiredResourceTagsMap)
                                                            .desiredResourceState(model)
                                                            .systemTags(systemTagsMap)
                                                            .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                                                            .build();
        return request;
    }

    protected ResourceModel buildDefaultInputModel(List<software.amazon.ssm.patchbaseline.Tag> tags,
                                                   List<software.amazon.ssm.patchbaseline.PatchSource> sources,
                                                   software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters,
                                                   software.amazon.ssm.patchbaseline.RuleGroup approvalRules ) {
        return ResourceModel.builder()
                .id(TestConstants.BASELINE_ID)
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
                .tags(tags)
                .patchGroups(TestConstants.PATCH_GROUPS)
                .build();
    }

    protected List<software.amazon.ssm.patchbaseline.Tag> tags() {
        software.amazon.ssm.patchbaseline.Tag tag = software.amazon.ssm.patchbaseline.Tag.builder().key("stage").value("Gamma").build();
        return Collections.singletonList(tag);
    }

    protected List<software.amazon.ssm.patchbaseline.PatchSource> sources() {
        software.amazon.ssm.patchbaseline.PatchSource ps1 = software.amazon.ssm.patchbaseline.PatchSource.builder()
                .name("main")
                .products(Collections.singletonList("*"))
                .configuration("deb http://example.com distro component")
                .build();
        software.amazon.ssm.patchbaseline.PatchSource ps2 = software.amazon.ssm.patchbaseline.PatchSource.builder()
                .name("universe")
                .products(Collections.singletonList("Ubuntu14.04"))
                .configuration("deb http://example.com distro universe")
                .build();
        List<software.amazon.ssm.patchbaseline.PatchSource> sourcesList= new ArrayList<>();
        sourcesList.add(ps1);
        sourcesList.add(ps2);
        return sourcesList;
    }

    protected software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters() {
        software.amazon.ssm.patchbaseline.PatchFilter pf3 = software.amazon.ssm.patchbaseline.PatchFilter.builder()
                .key("PRIORITY")
                .values(Collections.singletonList("high"))
                .build();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = software.amazon.ssm.patchbaseline.PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf3))
                .build();
        return globalFilters;
    }

    protected software.amazon.ssm.patchbaseline.RuleGroup approvalRules() {
        software.amazon.ssm.patchbaseline.PatchFilter pf1 = software.amazon.ssm.patchbaseline.PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
//        software.amazon.ssm.patchbaseline.PatchFilter pf2 = software.amazon.ssm.patchbaseline.PatchFilter.builder()
//                .key("SECTION")
//                .values(Collections.singletonList("python"))
//                .build();
//        List<software.amazon.ssm.patchbaseline.PatchFilter> patchFilterList = Collections.emptyList();
//        patchFilterList.add(pf1);
//        patchFilterList.add(pf2);
        software.amazon.ssm.patchbaseline.PatchFilterGroup patchFilterGroup = software.amazon.ssm.patchbaseline.PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf1))
                .build();
        software.amazon.ssm.patchbaseline.Rule patchRule = software.amazon.ssm.patchbaseline.Rule.builder()
                .patchFilterGroup(patchFilterGroup)
                .approveAfterDays(10)
                .complianceLevel(getComplianceString(TestConstants.ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = software.amazon.ssm.patchbaseline.RuleGroup.builder()
                .patchRules(Collections.singletonList(patchRule))
                .build();
        return approvalRules;
    }


    protected RegisterPatchBaselineForPatchGroupRequest buildRegisterGroupRequest(String baselineId, String group) {
        RegisterPatchBaselineForPatchGroupRequest registerPatchBaselineForPatchGroupRequest =
                 RegisterPatchBaselineForPatchGroupRequest.builder()
                            .baselineId(baselineId)
                            .patchGroup(group)
                            .build();
        return registerPatchBaselineForPatchGroupRequest;
    }

    protected DeregisterPatchBaselineForPatchGroupRequest buildDeregisterGroupRequest(String baselineId, String group) {
        DeregisterPatchBaselineForPatchGroupRequest deregisterPatchBaselineForPatchGroupRequest =
                   DeregisterPatchBaselineForPatchGroupRequest.builder()
                            .baselineId(baselineId)
                            .patchGroup(group)
                            .build();
        return deregisterPatchBaselineForPatchGroupRequest;
    }

    protected static String getComplianceString(TestConstants.ComplianceLevel cl) {
        switch (cl) {
            case INFORMATIONAL:
                return "INFORMATIONAL";
            case LOW:
                return "LOW";
            case MEDIUM:
                return "MEDIUM";
            case HIGH:
                return "HIGH";
            case CRITICAL:
                return "CRITICAL";
            default:
            case UNSPECIFIED:
                return "UNSPECIFIED";
        }
    }

//    @AfterAll
//    public void tearDown() {
//        verifyNoMoreInteractions(proxy);
//    }

    protected final String clientRequestToken = TestConstants.CLIENT_REQUEST_TOKEN;

    protected final static AmazonServiceException exception500 = new AmazonServiceException("Server error");
    protected final static AmazonServiceException exception400 = new AmazonServiceException("Client error");
    protected final static RuntimeException unknownException = new RuntimeException("Runtime error");

    static {
        exception500.setStatusCode(500);
        exception400.setStatusCode(400);
    }





}
