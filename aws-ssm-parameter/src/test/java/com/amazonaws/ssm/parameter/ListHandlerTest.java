package com.amazonaws.ssm.parameter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

	@Mock
	private AmazonWebServicesClientProxy proxy;
	@Mock
	private ProxyClient<SsmClient> proxyClient;
	@Mock
	SsmClient ssmClient;

	private ListHandler handler;

	private static final String PARAMETER_NAME = "parameterName";
	private static final String NEXT_TOKEN = "4b90a7e4-b790-456b";

	@BeforeEach
	public void setup() {
		handler = new ListHandler(ssmClient);
		ssmClient = mock(SsmClient.class);
		proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
		proxyClient = MOCK_PROXY(proxy, ssmClient);
	}

	@AfterEach
	public void tear_down() {
		verify(ssmClient, atLeastOnce()).serviceName();
		verifyNoMoreInteractions(proxyClient.client());
	}

	@Test
	public void handleRequest_SimpleSuccess() {
		final DescribeParametersResponse describeParametersResponse = DescribeParametersResponse.builder()
			.parameters(Collections.singletonList(ParameterMetadata.builder().name(PARAMETER_NAME).build()))
			.nextToken(NEXT_TOKEN).build();
		when(proxyClient.client().describeParameters(any(DescribeParametersRequest.class)))
			.thenReturn(describeParametersResponse);

		final ResourceModel model = ResourceModel.builder().build();

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.desiredResourceState(model)
			.build();

		final ProgressEvent<ResourceModel, CallbackContext> response =
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModel()).isNull();
		assertThat(response.getResourceModels()).isNotNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
		assertThat(response.getNextToken()).isEqualTo(NEXT_TOKEN);
	}
}
