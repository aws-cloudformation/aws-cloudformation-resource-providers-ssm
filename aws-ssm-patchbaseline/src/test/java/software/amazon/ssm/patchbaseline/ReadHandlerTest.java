package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.Tag;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentMatchers;

import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends TestBase {

    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;

    @InjectMocks
    private ReadHandler readHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Resource resource;
    @Mock
    private TagHelper cfnTagHelper;

    @BeforeEach
    public void setup() {
        cfnTagHelper = mock(TagHelper.class);
        readHandler = new ReadHandler(cfnTagHelper);
    }

    @Test
    public void testSuccess() {

        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        getPatchBaselineResponse = setUpGetPatchBaselineResponse();

        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);

        //set up mock for TagHelper.listTagsForResource()
        List<Tag> tagList = requestTags(TAG_KEY, TAG_VALUE);
        when(cfnTagHelper.listTagsForResource(any(), any(), any(), any())).thenReturn(tagList);

        //Simple unit test to verify the reading-in of read requests.
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                    .desiredResourceState(model)
                                                    .clientRequestToken(CLIENT_REQUEST_TOKEN)
                                                    .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = readHandler.handleRequest(proxy, request, null, logger);

        final ResourceModel expectedModel =  buildDefaultInputRequest().getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(resource);
    }

    @Test
    public void testInvalidBaselineId() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BAD_BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any())).thenThrow(exception400);

        //Verify handler response when given an invalid baseline id
        ResourceModel model = ResourceModel.builder().id(BAD_BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = readHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains(exception400.getMessage()));
    }

    @Test
    public void testServerError() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any())).thenThrow(exception500);

        //Verify handler response when we get 5xx error from SSM
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = readHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains(exception500.getMessage()));
    }

    private GetPatchBaselineResponse setUpGetPatchBaselineResponse() {
        List<PatchSource> sources = requestSources();
        PatchFilterGroup globalFilters = requestGlobalFilters();
        PatchRuleGroup approvalRules = requestApprovalRules();
        return GetPatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .name(TestConstants.BASELINE_NAME)
                .operatingSystem(TestConstants.OPERATING_SYSTEM)
                .description(TestConstants.BASELINE_DESCRIPTION)
                .rejectedPatches(TestConstants.REJECTED_PATCHES)
                .rejectedPatchesAction("BLOCK")
                .approvedPatches(TestConstants.ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(ComplianceLevel.CRITICAL))
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sources)
                .patchGroups(TestConstants.PATCH_GROUPS)
                .build();
    }

}
