package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.*;
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
import org.mockito.ArgumentMatchers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Optional;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends TestBase{

    private DeleteHandler deleteHandler;
    private DeletePatchBaselineRequest deletePatchBaselineRequest;
    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;
   // @Mock   //We just need an object for ssmClient to return
    private DeletePatchBaselineResponse deletePatchBaselineResponse;

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

        deleteHandler = new DeleteHandler();

        deletePatchBaselineRequest =  DeletePatchBaselineRequest.builder().baselineId(TestConstants.BASELINE_ID).build();
        deletePatchBaselineResponse =  DeletePatchBaselineResponse.builder().baselineId(TestConstants.BASELINE_ID).build();

        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(TestConstants.BASELINE_ID).build();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder().baselineId(TestConstants.BASELINE_ID).patchGroups(TestConstants.PATCH_GROUPS).build();

    }

    @Test
    public void testSuccess() {
        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);
//
//        when(proxy.injectCredentialsAndInvokeV2(eq(deletePatchBaselineRequest),
//                ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any()))
//                .thenReturn(deletePatchBaselineResponse);

        //Each delete request sends to SSM (1) get baseline request(n) deregister group requests (1) delete baseline request
        ResourceModel model = ResourceModel.builder().id(TestConstants.BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                            .desiredResourceState(model)
                                                            .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                                                            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(null);

        // need to check that the get/deletePatchBaseline was invoked with the correct request made from the model
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        for (String group : getPatchBaselineResponse.patchGroups()) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(deletePatchBaselineRequest),
                        ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any());

        assertThat(response).isEqualTo(expectedProgressEvent);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(null);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verifyZeroInteractions(resource);

    }


}
