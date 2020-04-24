package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncResponse;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends TestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ListHandler();
    }

    @Test
    public void handleRequest_success() {
        final ResourceModel model_two = createSyncFromSourceRDSModel(createSyncSourceModel());

        final ResourceDataSyncItem resourceDataSyncItem_two = Translator.createResourceDataSyncItemFromResourceModel(model_two);

        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .resourceDataSyncItems(Arrays.asList(
                        resourceDataSyncItem_two))
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createSyncFromSourceRDSModel(createSyncSourceModel()))
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(ZERO);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(
                Arrays.asList(createSyncFromSourceRDSModel(createSyncSourceModel())));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_noResourceDataSyncWithSourceTypePresent_success() {
        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createBasicRDSModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(ZERO);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEmpty();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

}
