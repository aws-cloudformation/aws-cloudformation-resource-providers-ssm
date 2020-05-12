package com.amazonaws.ssm.parameter;

import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private CreateHandler handler;

    private ResourceModel RESOURCE_MODEL;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);

        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .value(VALUE)
                .type(TYPE)
                .tags(TAG_SET)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE)
                        .value(VALUE).build())
                .build();
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccess_With_No_ParameterName_Defined_Exceeding_LogicalResourceId() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE)
                        .value(VALUE).build())
                .build();
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .value(VALUE)
                .type(TYPE)
                .tags(TAG_SET)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier(RandomStringUtils.random(600)).build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccess_With_No_ParameterName_Defined_Not_Exceeding_LogicalResourceId() {
        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE)
                        .value(VALUE).build())
                .build();
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .value(VALUE)
                .type(TYPE)
                .tags(TAG_SET)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccess_WithImageDataType() {
        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .value(VALUE)
                .type(TYPE)
                .tags(TAG_SET)
                .dataType("aws:ec2:image")
                .build();

        final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
                .version(VERSION)
                .tier(ParameterTier.STANDARD)
                .build();
        when(proxySsmClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

        final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
                .parameters(Parameter.builder()
                        .name(NAME)
                        .type(TYPE)
                        .value(VALUE)
                        .version(VERSION).build())
                .build();
        when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }

    @Test
    public void handleRequest_ThrottlingException() {
        when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
                .thenThrow(TooManyUpdatesException.builder().build());

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

        verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
    }
}
