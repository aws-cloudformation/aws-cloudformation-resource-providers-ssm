package com.amazonaws.ssm.parameter;

import org.junit.jupiter.api.AfterEach;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterInlinePolicy;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);
    }

    @AfterEach
    public void post_execute() {
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE_STRING)
                        .dataType(TYPE_STRING)
                        .value(VALUE).build())
                .build();
        final DescribeParametersResponse describeParametersResponse = DescribeParametersResponse.builder()
                .parameters(Collections.singletonList(ParameterMetadata.builder()
                        .name("PARAMETER_NAME")
                        .description("description")
                        .tier(ParameterTier.STANDARD)
                        .allowedPattern("pattern")
                        .policies(Collections.singletonList(ParameterInlinePolicy.builder().policyText("{}").build()))
                        .build()))
                .build();
        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder().build();
        when(proxySsmClient.client().listTagsForResource(any(ListTagsForResourceRequest.class))).thenReturn(listTagsForResourceResponse);
        when(proxySsmClient.client().describeParameters(any(DescribeParametersRequest.class)))
                .thenReturn(describeParametersResponse);
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);
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

        verify(proxySsmClient.client()).getParameters(any(GetParametersRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccessWithTags() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE_STRING)
                        .dataType(TYPE_STRING)
                        .value(VALUE).build())
                .build();
        final DescribeParametersResponse describeParametersResponse = DescribeParametersResponse.builder()
                .parameters(Collections.singletonList(ParameterMetadata.builder()
                        .name("PARAMETER_NAME")
                        .description("description")
                        .tier(ParameterTier.STANDARD)
                        .allowedPattern("pattern")
                        .policies(Collections.singletonList(ParameterInlinePolicy.builder().policyText("{}").build()))
                        .build()))
                .build();
        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tagList(TagHelper.convertToList(TagHelper.convertToSet(TAG_SET))).build();
        when(proxySsmClient.client().listTagsForResource(any(ListTagsForResourceRequest.class))).thenReturn(listTagsForResourceResponse);
        when(proxySsmClient.client().describeParameters(any(DescribeParametersRequest.class)))
                .thenReturn(describeParametersResponse);
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);
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

        verify(proxySsmClient.client()).getParameters(any(GetParametersRequest.class));
    }

    @Test
    public void handleRequest_EmptyGetParametersResponse() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder().build();
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder().name("ParameterName")
                        .build())
                .build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnNotFoundException ex) {
            assertThat(ex).isInstanceOf(CfnNotFoundException.class);
        }

        verify(proxySsmClient.client()).getParameters(any(GetParametersRequest.class));
    }

    @Test
    public void handleRequest_AmazonServiceExceptionInternalServerError() {
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class)))
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

        verify(proxySsmClient.client()).getParameters(any(GetParametersRequest.class));
    }
}
