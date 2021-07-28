package com.amazonaws.ssm.opsmetadata;

import org.junit.jupiter.api.AfterEach;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ListOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.ListOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.OpsMetadata;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase  {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private ListHandler handler;

    private static final String OPSMETADATA_ARN = "arn:aws:ssm:us-east-1:123456789012:opsmetadata/aws/ssm/XYZ_RG/appmanager";
    private static final String NEXT_TOKEN = "4b90a7e4-b790-456b";

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListOpsMetadataResponse listOpsMetadataResponse = ListOpsMetadataResponse.builder()
                .opsMetadataList(Collections.singletonList(OpsMetadata.builder().opsMetadataArn(OPSMETADATA_ARN).build()))
                .nextToken(NEXT_TOKEN).build();
        when(proxySsmClient.client().listOpsMetadata(any(ListOpsMetadataRequest.class)))
                .thenReturn(listOpsMetadataResponse);

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceModel expectedModel = ResourceModel.builder().opsMetadataArn(OPSMETADATA_ARN).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).containsExactly(expectedModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(NEXT_TOKEN);

        verify(proxySsmClient.client()).listOpsMetadata(any(ListOpsMetadataRequest.class));
    }
}
