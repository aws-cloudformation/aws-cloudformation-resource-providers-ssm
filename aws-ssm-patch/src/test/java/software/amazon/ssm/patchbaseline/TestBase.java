package software.amazon.ssm.patchbaseline;

import com.amazonaws.AmazonServiceException;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.awssdk.services.ssm.SsmClient;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestBase {
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    protected SsmClient ssmClient;
    @Mock
    protected Logger logger;

    protected ResourceHandlerRequest<ResourceModel> buildDefaultInputRequest() {
        List<software.amazon.ssm.patchbaseline.Tag> tags = tags(TAG_KEY, TAG_VALUE);
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();

        Map<String, String> desiredResourceTagsMap = new HashMap<>();
        desiredResourceTagsMap.put(TestConstants.CFN_KEY, TestConstants.CFN_VALUE);

        Map<String, String> systemTagsMap = new HashMap<>();
        systemTagsMap.put(TestConstants.SYSTEM_TAG_KEY, TestConstants.BASELINE_NAME);

        ResourceModel model = buildDefaultInputModel(tags, sources, globalFilters, approvalRules,
                BASELINE_ID, BASELINE_NAME, OPERATING_SYSTEM, BASELINE_DESCRIPTION,
                REJECTED_PATCHES, getPatchActionString(PatchAction.BLOCK),
                ACCEPTED_PATCHES, getComplianceString(ComplianceLevel.CRITICAL), PATCH_GROUPS);
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(desiredResourceTagsMap)
                .desiredResourceState(model)
                .systemTags(systemTagsMap)
                .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .build();
        return request;
    }

    protected ResourceHandlerRequest<ResourceModel> buildUpdateDefaultInputRequest() {
        List<software.amazon.ssm.patchbaseline.Tag> tags = tags(TAG_KEY, TAG_VALUE);
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();

        List<software.amazon.ssm.patchbaseline.Tag> updatedTags = tags(NEW_TAG_KEY, NEW_TAG_VALUE);

        Map<String, String> updatedDesiredResourceTagsMap = new HashMap<>();
        updatedDesiredResourceTagsMap.put(UPDATED_CFN_KEY, UPDATED_CFN_VALUE);

        Map<String, String> systemTagsMap = new HashMap<>();
        systemTagsMap.put(SYSTEM_TAG_KEY, UPDATED_BASELINE_NAME);

        ResourceModel previousModel = buildDefaultInputModel(tags, sources, globalFilters, approvalRules,
                                                        BASELINE_ID, BASELINE_NAME, OPERATING_SYSTEM, BASELINE_DESCRIPTION,
                                                        REJECTED_PATCHES, getPatchActionString(PatchAction.BLOCK),
                                     ACCEPTED_PATCHES, getComplianceString(ComplianceLevel.CRITICAL), PATCH_GROUPS);

        ResourceModel updatedModel = buildDefaultInputModel(updatedTags, sources, globalFilters, approvalRules,
                                                BASELINE_ID, UPDATED_BASELINE_NAME, OPERATING_SYSTEM, UPDATED_BASELINE_DESC,
                                                UPDATED_REJECTED_PATCHES, getPatchActionString(PatchAction.ALLOW_AS_DEPENDENCY),
                                                UPDATED_ACCEPTED_PATCHES, getComplianceString(ComplianceLevel.MEDIUM),
                                                new ArrayList<String>(UPDATED_PATCH_GROUPS));

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(updatedDesiredResourceTagsMap)
                .previousResourceState(previousModel)
                .desiredResourceState(updatedModel)
                .systemTags(systemTagsMap)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();
        return request;
    }

    protected ResourceModel buildDefaultInputModel(List<software.amazon.ssm.patchbaseline.Tag> tags,
                                                   List<software.amazon.ssm.patchbaseline.PatchSource> sources,
                                                   software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters,
                                                   software.amazon.ssm.patchbaseline.RuleGroup approvalRules,
                                                   String baselineId,
                                                   String name,
                                                   String operatingSystem,
                                                   String description,
                                                   List<String> rejectedPatched,
                                                   String rejectedPatchesAction,
                                                   List<String> acceptedPatched,
                                                   String approvedPatchesComplianceLevel,
                                                   List<String> patchGroups) {
        return ResourceModel.builder()
                .id(baselineId)
                .name(name)
                .operatingSystem(operatingSystem)
                .description(description)
                .rejectedPatches(rejectedPatched)
                .rejectedPatchesAction(rejectedPatchesAction)
                .approvedPatches(acceptedPatched)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(approvedPatchesComplianceLevel)
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sources)
                .tags(tags)
                .patchGroups(patchGroups)
                .build();
    }

    protected List<software.amazon.ssm.patchbaseline.Tag> tags( String key, String value) {
        software.amazon.ssm.patchbaseline.Tag tag =
                software.amazon.ssm.patchbaseline.Tag.builder().key(key).value(value).build();
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

    protected List<Tag> requesttags(String key, String value) {
        Tag tag = Tag.builder().key(key).value(value).build();
        return Collections.singletonList(tag);
    }

    protected List<PatchSource> requestsources() {
        PatchSource ps1 = PatchSource.builder()
                .name("main")
                .products(Collections.singletonList("*"))
                .configuration("deb http://example.com distro component")
                .build();
        PatchSource ps2 = PatchSource.builder()
                .name("universe")
                .products(Collections.singletonList("Ubuntu14.04"))
                .configuration("deb http://example.com distro universe")
                .build();
        List<PatchSource> sourcesList= new ArrayList<>();
        sourcesList.add(ps1);
        sourcesList.add(ps2);
        return sourcesList;
    }

    protected PatchFilterGroup requestglobalFilters() {
        PatchFilter pf3 = PatchFilter.builder()
                .key("PRIORITY")
                .values(Collections.singletonList("high"))
                .build();
        PatchFilterGroup globalFilters = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf3))
                .build();
        return globalFilters;
    }

    protected PatchRuleGroup requestapprovalRules() {
        PatchFilter pf1 = PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
        PatchFilterGroup patchFilterGroup = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf1))
                .build();
        PatchRule patchRule = PatchRule.builder()
                .patchFilterGroup(patchFilterGroup)
                .approveAfterDays(10)
                .complianceLevel(getComplianceString(ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
        PatchRuleGroup approvalRules = PatchRuleGroup.builder()
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

    protected static String getComplianceString(ComplianceLevel cl) {
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

    protected static String getPatchActionString(PatchAction cl) {
        switch (cl) {
            case BLOCK:
                return "BLOCK";
            default:
            case ALLOW_AS_DEPENDENCY:
                return "ALLOW_AS_DEPENDENCY";
        }
    }

    @AfterAll
    public void tearDown() {
        verifyNoMoreInteractions(proxy);
    }

    protected final static AmazonServiceException exception500 = new AmazonServiceException("Server error");
    protected final static AmazonServiceException exception400 = new AmazonServiceException("Client error");
    protected final static RuntimeException unknownException = new RuntimeException("Runtime error");

    static {
        exception500.setStatusCode(500);
        exception400.setStatusCode(400);
    }

}
