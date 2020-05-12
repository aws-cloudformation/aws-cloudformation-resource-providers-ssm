package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssm;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        ssm = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssm);
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
                .desiredResourceState(ResourceModel.builder()
                        .build())
                .build();
        final CallbackContext callbackContext = new CallbackContext();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
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
}
