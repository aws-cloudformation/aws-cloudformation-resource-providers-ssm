package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesRequest;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesResponse;
import software.amazon.awssdk.services.ssm.model.PatchBaselineIdentity;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends TestBase {

    @InjectMocks
    private ListHandler listHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    ReadHandler readHandler;

    private DescribePatchBaselinesRequest describePatchBaselinesRequest;
    private DescribePatchBaselinesResponse describePatchBaselinesResponse;

    @BeforeEach
    public void setup() {
        listHandler = new ListHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        describePatchBaselinesRequest = DescribePatchBaselinesRequest.builder().maxResults(50).build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        //set up mock for DescribePatchBaselinesResponse
        final List<PatchBaselineIdentity> patchBaselineIdentities = Arrays.asList(PatchBaselineIdentity.builder().baselineId(BASELINE_ID).build());
        describePatchBaselinesResponse = DescribePatchBaselinesResponse.builder()
                .baselineIdentities(patchBaselineIdentities)
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(describePatchBaselinesRequest),
                ArgumentMatchers.<Function<DescribePatchBaselinesRequest, DescribePatchBaselinesResponse>>any()))
                .thenReturn(describePatchBaselinesResponse);

        //Simple unit test to verify the reading-in of read requests.
        final ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                listHandler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    @Test
    public void handleRequest_SimpleSuccessWithNoProperty() {

        //set up mock for DescribePatchBaselinesResponse
        final List<PatchBaselineIdentity> patchBaselineIdentities = Arrays.asList(PatchBaselineIdentity.builder().baselineId(BASELINE_ID).build());
        describePatchBaselinesResponse = DescribePatchBaselinesResponse.builder()
                .baselineIdentities(patchBaselineIdentities)
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(describePatchBaselinesRequest),
                ArgumentMatchers.<Function<DescribePatchBaselinesRequest, DescribePatchBaselinesResponse>>any()))
                .thenReturn(describePatchBaselinesResponse);

        //Simple unit test to verify the reading-in of read requests.
        final ResourceModel model = ResourceModel.builder().id("pb-12345678901234567").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                listHandler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

}
