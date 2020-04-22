package com.amazonaws.ssm.parameter;

import org.junit.jupiter.api.AfterEach;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssm;

    private UpdateHandler handler;

    private ResourceModel RESOURCE_MODEL;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        ssm = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssm);

        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .value(VALUE)
                .type(TYPE)
                .tags(TAG_SET)
                .build();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder().build();
        when(proxySsmClient.client().listTagsForResource(any(ListTagsForResourceRequest.class))).thenReturn(listTagsForResourceResponse);
        final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
        when(proxySsmClient.client().removeTagsFromResource(any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);
        final AddTagsToResourceResponse addTagsToResourceResponse = AddTagsToResourceResponse.builder().build();
        when(proxySsmClient.client().addTagsToResource(any(AddTagsToResourceRequest.class))).thenReturn(addTagsToResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }
}