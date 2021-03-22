package com.amazonaws.ssm.opsmetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.AmazonServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {
    private static final String OPSMETADATA_ARN = "arn:aws:ssm:us-east-1:123456789012:opsmetadata/aws/ssm/XYZ_RG/appmanager";
    private static final String RESOURCE_ID = "arn:aws:resource-groups:us-east-1:123456789012:group/XYZ_RG";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private CreateHandler handler;

    private ResourceModel RESOURCE_MODEL;
    private Map<String, MetadataValue> metadata;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);

        metadata = new HashMap<String, MetadataValue>() {{
            put("some-key-1", MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", MetadataValue.builder().value("some-value-2").build());
        }};

        RESOURCE_MODEL = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .metadata(metadata)
                .build();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final CreateOpsMetadataResponse createOpsMetadataResponse = CreateOpsMetadataResponse.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class))).thenReturn(createOpsMetadataResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_SimpleSuccess_WithoutMetadata() {
        RESOURCE_MODEL = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .build();
        final CreateOpsMetadataResponse createOpsMetadataResponse = CreateOpsMetadataResponse.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class))).thenReturn(createOpsMetadataResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_AmazonServiceException400ThrottlingException() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(429);
        amazonServiceException.setErrorCode("ThrottlingException");

        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnThrottlingException ex) {
            assertThat(ex).isInstanceOf(CfnThrottlingException.class);
        }

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_OpsMetadataAlreadyExistsException() {
        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class)))
                .thenThrow(OpsMetadataAlreadyExistsException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnAlreadyExistsException ex) {
            assertThat(ex).isInstanceOf(CfnAlreadyExistsException.class);
        }

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_AmazonServiceException500Exception() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(500);

        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_AmazonServiceException400NonThrottlingException() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(400);
        amazonServiceException.setErrorCode("Invalid Input");

        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_AmazonServiceExceptionInternalServerError() {
        when(proxySsmClient.client().createOpsMetadata(any(CreateOpsMetadataRequest.class)))
                .thenThrow(InternalServerErrorException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnServiceInternalErrorException ex) {
            assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
        }

        verify(proxySsmClient.client()).createOpsMetadata(any(CreateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }
}
