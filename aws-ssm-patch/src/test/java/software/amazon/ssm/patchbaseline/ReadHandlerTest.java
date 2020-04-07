package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentMatchers;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends TestBase {

    private ReadHandler readHandler;
    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private Resource resource;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        resource = mock(Resource.class);
        readHandler = new ReadHandler();
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(TestConstants.BASELINE_ID).build();
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
    }

    @Test
    public void testSuccess() {
        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);

        //Simple unit test to verify the reading-in of read requests.
        ResourceModel model = ResourceModel.builder().id(TestConstants.BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                    .desiredResourceState(model)
                                                    .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                                                    .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = readHandler.handleRequest(proxy, request, null, logger);

        final ResourceModel expectedModel =  buildDefaultInputRequest().getDesiredResourceState();
        expectedModel.setTags(null);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isEqualTo(expectedProgressEvent);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verifyZeroInteractions(resource);

    }

}
