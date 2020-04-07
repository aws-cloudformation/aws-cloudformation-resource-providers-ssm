package software.amazon.ssm.patchbaseline;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static software.amazon.ssm.patchbaseline.TestConstants.*;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends TestBase{

    private UpdateHandler updateHandler;
    private UpdatePatchBaselineRequest updatePatchBaselineRequest;
    private UpdatePatchBaselineResponse updatePatchBaselineResponse;
    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;
    private ListTagsForResourceRequest listTagsForResourceRequest;
    private ListTagsForResourceResponse listTagsForResourceResponse;
    private DeregisterPatchBaselineForPatchGroupResponse deregisterPatchBaselineForPatchGroupResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);

        updateHandler = new UpdateHandler();
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder().baselineId(BASELINE_ID).patchGroups(PATCH_GROUPS).build();
        listTagsForResourceRequest = ListTagsForResourceRequest.builder().resourceType(PATCH_BASELINE_RESOURCE_NAME).resourceId(BASELINE_ID).build();
        //        deregisterPatchBaselineForPatchGroupResponse = new DeregisterPatchBaselineForPatchGroupResponse();
        PatchFilter pf1 = PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
        PatchFilter pf3 = PatchFilter.builder()
                .key("PRIORITY")
                .values(Collections.singletonList("high"))
                .build();
        PatchFilterGroup patchFilterGroup = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf1))
                .build();
        PatchRule patchRule = PatchRule.builder()
                .patchFilterGroup(patchFilterGroup)
                .approveAfterDays(10)
                .complianceLevel(getComplianceString(TestConstants.ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
//        List<PatchRule> patchRuleList = Collections.emptyList();
//        patchRuleList.add(patchRule);
        PatchRuleGroup approvalRules = PatchRuleGroup.builder()
                .patchRules(Collections.singletonList(patchRule))
                .build();
        PatchFilterGroup globalFilters = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf3))
                .build();
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
        List<PatchSource> sourcesList = new ArrayList<>();
        sourcesList.add(ps1);
        sourcesList.add(ps2);

        //Build update request. It takes a lot of space because of all the nesting.
        updatePatchBaselineRequest = UpdatePatchBaselineRequest.builder()
                .name(UPDATED_BASELINE_NAME)
                .description(UPDATED_BASELINE_DESC)
                .baselineId(BASELINE_ID)
                .rejectedPatches(UPDATED_REJECTED_PATCHES)
                .rejectedPatchesAction(PatchAction.ALLOW_AS_DEPENDENCY)
                .approvedPatches(UPDATED_ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(ComplianceLevel.MEDIUM))
                .approvedPatchesEnableNonSecurity(true)
                .sources(sourcesList)
                .globalFilters(globalFilters)
                .replace(true)
                .build();

        updatePatchBaselineResponse = UpdatePatchBaselineResponse.builder().baselineId(BASELINE_ID).build();

    }

    @Test
    public void testSuccess() {
        when(proxy.injectCredentialsAndInvokeV2(eq(updatePatchBaselineRequest),
                ArgumentMatchers.<Function<UpdatePatchBaselineRequest, UpdatePatchBaselineResponse>>any()))
                .thenReturn(updatePatchBaselineResponse);

        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);

        //Invoke the handler
        ResourceHandlerRequest<ResourceModel>  request = buildUpdateDefaultInputRequest();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = updateHandler.handleRequest(proxy, request, null, logger);

        System.out.print(String.format("Update Handler Response Status %s %n", response.getStatus()));

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(updatePatchBaselineRequest),
                        ArgumentMatchers.<Function<UpdatePatchBaselineRequest, UpdatePatchBaselineResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        List<String> originalGroups = PATCH_GROUPS;
        List<String> newGroups = UPDATE_PATCH_GROUPS;

        //Compute the intersection of the two lists (the groups that don't need to be changed)
        List<String> intersectingGroups = new ArrayList<>(originalGroups);
        intersectingGroups.retainAll(newGroups);

        //The groups we need to remove are ORIGINAL - INTERSECT
        //The groups we need to add are DESIRED - INTERSECT
//        originalGroups.removeAll(intersectingGroups);
//        newGroups.removeAll(intersectingGroups);

        for (String group : originalGroups) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        for (String group : newGroups) {
            if (!TestConstants.PATCH_GROUPS.contains(group)) {
                verify(proxy)
                        .injectCredentialsAndInvokeV2(
                                eq(buildRegisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                                ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
            }
        }

        List<String> expectedRemoveTags = Arrays.asList(CFN_KEY,SYSTEM_TAG_KEY);

        for (String key : expectedRemoveTags)
            System.out.print(String.format("expected removed tag %s %n", key));

        List<Tag> expectedAddTags = Arrays.asList(
                 Tag.builder().key(UPDATED_CFN_KEY).value(UPDATED_CFN_VALUE).build(),
                 Tag.builder().key(NEW_TAG_KEY).value(NEW_TAG_VALUE).build(),
                 Tag.builder().key(SYSTEM_TAG_KEY).value(UPDATED_BASELINE_NAME).build());

        for (Tag tag : expectedAddTags)
            System.out.print(String.format("expected added tag key %s, tag value %s %n", tag.key(), tag.value()));

        listTagsForResourceResponse = ListTagsForResourceResponse.builder().tagList(expectedAddTags).build();

        when(proxy.injectCredentialsAndInvokeV2(
                eq(listTagsForResourceRequest),
                ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(listTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any());

        for (Tag tag : listTagsForResourceResponse.tagList())
            System.out.print(String.format("listTagsForResourceResponse tag key %s, tag val %s %n", tag.key(), tag.value()));

        ArgumentCaptor<RemoveTagsFromResourceRequest> removeTagsRequest = ArgumentCaptor.forClass(RemoveTagsFromResourceRequest.class);
        ArgumentCaptor<AddTagsToResourceRequest> addTagsRequest = ArgumentCaptor.forClass(AddTagsToResourceRequest.class);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(removeTagsRequest.capture()),
                        ArgumentMatchers.<Function<RemoveTagsFromResourceRequest, RemoveTagsFromResourceResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(addTagsRequest.capture()),
                        ArgumentMatchers.<Function<AddTagsToResourceRequest, AddTagsToResourceResponse>>any());

        RemoveTagsFromResourceRequest actualRemoveTags = removeTagsRequest.getValue();
        AddTagsToResourceRequest actualAddTags = addTagsRequest.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        //assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModel().getId()).isEqualTo(actualRemoveTags.resourceId());
        assertThat(PATCH_BASELINE_RESOURCE_NAME).isEqualTo(actualRemoveTags.resourceTypeAsString());

        assertThat(response.getResourceModel().getId()).isEqualTo(actualAddTags.resourceId());
        assertThat(PATCH_BASELINE_RESOURCE_NAME).isEqualTo(actualAddTags.resourceTypeAsString());

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expectedRemoveTags);
        Collections.sort(actualRemoveTags.tagKeys());
        assertThat(expectedRemoveTags).isEqualTo(actualRemoveTags.tagKeys());

        Collections.sort(expectedAddTags, Comparator.comparing(Tag::key));
        Collections.sort(actualAddTags.tags(), Comparator.comparing(Tag::key));
        assertThat(expectedAddTags).isEqualTo(actualAddTags.tags());
    }


}
