package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncResponse;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncInvalidConfigurationException;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends TestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_success() {

        final ResourceModel inputModel = createSyncFromSourceRDSModel(createSyncSourceModel());

        final ResourceDataSyncItem resourceDataSyncItem = Translator.createResourceDataSyncItemFromResourceModel(inputModel);

        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .resourceDataSyncItems(resourceDataSyncItem)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(inputModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        final ResourceModel outputModel = createSyncFromSourceRDSModel(createSyncSourceModel());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(ZERO);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(outputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_noResourceDataSyncPresent() {
        final ResourceModel inputModel = createSyncFromSourceRDSModel(createSyncSourceModel());

        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(inputModel)
                .build();
        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_ResourceDataSyncInvalidConfigurationException() {
        final ResourceModel inputModel = createSyncFromSourceRDSModel(createSyncSourceModel());

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncInvalidConfigurationException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(inputModel)
                .build();
        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
