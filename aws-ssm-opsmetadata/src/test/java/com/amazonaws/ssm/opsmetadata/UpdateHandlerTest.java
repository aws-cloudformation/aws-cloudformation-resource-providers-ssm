package com.amazonaws.ssm.opsmetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.AmazonServiceException;
import org.junit.jupiter.api.AfterEach;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.OpsMetadataNotFoundException;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
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
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {
    private static final String OPS_METADATA_ARN = "arn:aws:ssm:us-east-1:123456789012:opsmetadata/aws/ssm/XYZ_RG/appmanager";
    private static final String RESOURCE_ID = "arn:aws:resource-groups:us-east-1:123456789012:group/XYZ_RG";

    private Map<String, String> PREVIOUS_TAG_SET_NO_CHANGE;

    private Map<String, String> TAG_SET_WITH_CHANGE;

    private GetOpsMetadataResponse getOpsMetadataResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private ResourceModel RESOURCE_MODEL;

    private UpdateHandler handler;
    private Map<String, MetadataValue> metadata;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);

        metadata = new HashMap<String, MetadataValue>() {{
            put("some-key-1", MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", MetadataValue.builder().value("some-value-2").build());
        }};

        RESOURCE_MODEL = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .opsMetadataArn(OPS_METADATA_ARN)
                .metadata(metadata)
                .build();
        PREVIOUS_TAG_SET_NO_CHANGE = new HashMap<>();
        TAG_SET_WITH_CHANGE = new HashMap<>();

        getOpsMetadataResponse = GetOpsMetadataResponse.builder().resourceId(RESOURCE_ID).build();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        final UpdateOpsMetadataResponse updateOpsMetadataResponse = UpdateOpsMetadataResponse.builder()
                .opsMetadataArn(OPS_METADATA_ARN)
                .build();
        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class))).thenReturn(updateOpsMetadataResponse);

        final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
        when(proxySsmClient.client().removeTagsFromResource(
                any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
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

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_SimpleSuccess_WithoutTagsChange() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        PREVIOUS_TAG_SET_NO_CHANGE.putAll(TAG_SET);
        PREVIOUS_TAG_SET_NO_CHANGE.putAll(SYSTEM_TAGS_SET);

        final UpdateOpsMetadataResponse updateOpsMetadataResponse = UpdateOpsMetadataResponse.builder()
                .opsMetadataArn(OPS_METADATA_ARN)
                .build();
        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenReturn(updateOpsMetadataResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET_NO_CHANGE)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_SimpleSuccess_WithTagsChange() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        TAG_SET_WITH_CHANGE.put("AddTagKey", "AddTagValue");
        PREVIOUS_TAG_SET_NO_CHANGE.putAll(TAG_SET);
        PREVIOUS_TAG_SET_NO_CHANGE.putAll(SYSTEM_TAGS_SET);

        final UpdateOpsMetadataResponse updateOpsMetadataResponse = UpdateOpsMetadataResponse.builder()
                .opsMetadataArn(OPS_METADATA_ARN)
                .build();
        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenReturn(updateOpsMetadataResponse);

        final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
        when(proxySsmClient.client().removeTagsFromResource(any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);
        final AddTagsToResourceResponse addTagsToResourceResponse = AddTagsToResourceResponse.builder().build();
        when(proxySsmClient.client().addTagsToResource(any(AddTagsToResourceRequest.class))).thenReturn(addTagsToResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET_WITH_CHANGE)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET_NO_CHANGE)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_AmazonServiceException400ThrottlingException() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(429);
        amazonServiceException.setErrorCode("ThrottlingException");

        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnThrottlingException ex) {
            assertThat(ex).isInstanceOf(CfnThrottlingException.class);
        }

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_OpsMetadataNotFoundException() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenThrow(OpsMetadataNotFoundException.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnNotFoundException ex) {
            assertThat(ex).isInstanceOf(CfnNotFoundException.class);
        }

        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_AmazonServiceException500Exception() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(500);

        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_AmazonServiceException400NonThrottlingException() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(400);
        amazonServiceException.setErrorCode("Invalid Input");

        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_AmazonServiceExceptionInternalServerError() {
        when(proxySsmClient.client().getOpsMetadata(any(GetOpsMetadataRequest.class))).thenReturn(getOpsMetadataResponse);
        when(proxySsmClient.client().updateOpsMetadata(any(UpdateOpsMetadataRequest.class)))
                .thenThrow(InternalServerErrorException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .previousResourceTags(PREVIOUS_TAG_SET)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnServiceInternalErrorException ex) {
            assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
        }

        verify(proxySsmClient.client()).updateOpsMetadata(any(UpdateOpsMetadataRequest.class));
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

}
