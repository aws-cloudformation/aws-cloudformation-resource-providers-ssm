package com.amazonaws.ssm.opsmetadata;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.MetadataValue;
import software.amazon.awssdk.services.ssm.model.OpsMetadataNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {
    private static final String OPSMETADATA_ARN = "arn:aws:ssm:us-east-1:123456789012:opsmetadata/aws/ssm/XYZ_RG/appmanager";
    private static final String RESOURCE_ID = "arn:aws:resource-groups:us-east-1:123456789012:group/XYZ_RG";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private ReadHandler handler;

    private ResourceModel RESOURCE_MODEL;

    private Map<String, MetadataValue> metadata;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);

        metadata = new HashMap<String, MetadataValue>() {{
            put("some-key-1", MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", MetadataValue.builder().value("some-value-2").build());
        }};

        RESOURCE_MODEL = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
    }

    @AfterEach
    public void post_execute() {
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final GetOpsMetadataResponse getOpsMetadataResponse = GetOpsMetadataResponse.builder()
                .resourceId(RESOURCE_ID)
                .metadata(metadata)
                .build();
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class)))
                .thenReturn(getOpsMetadataResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder()
                        .build())
                .build();
        final CallbackContext callbackContext = new CallbackContext();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).getOpsMetadata(any(GetOpsMetadataRequest.class));
    }

    @Test
    public void handleRequest_OpsMetadataNotFound() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class)))
                .thenThrow(OpsMetadataNotFoundException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder().opsMetadataArn(OPSMETADATA_ARN)
                        .build())
                .build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnNotFoundException ex) {
            assertThat(ex).isInstanceOf(CfnNotFoundException.class);
        }

        verify(proxySsmClient.client()).getOpsMetadata(any(GetOpsMetadataRequest.class));
    }

    @Test
    public void handleRequest_EmptyGetOpsMetadataResponse() {
        final GetOpsMetadataResponse getOpsMetadataResponse = GetOpsMetadataResponse.builder().build();
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class)))
                .thenReturn(getOpsMetadataResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder().opsMetadataArn(OPSMETADATA_ARN)
                        .build())
                .build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnNotFoundException ex) {
            assertThat(ex).isInstanceOf(CfnNotFoundException.class);
        }

        verify(proxySsmClient.client()).getOpsMetadata(any(GetOpsMetadataRequest.class));
    }

    @Test
    public void handleRequest_AmazonServiceExceptionInternalServerError() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class)))
                .thenThrow(InternalServerErrorException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder()
                        .build())
                .build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnServiceInternalErrorException ex) {
            assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
        }

        verify(proxySsmClient.client()).getOpsMetadata(any(GetOpsMetadataRequest.class));
    }
}
