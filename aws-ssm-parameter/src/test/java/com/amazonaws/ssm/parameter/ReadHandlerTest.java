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
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

	@Mock
	private AmazonWebServicesClientProxy proxy;
	@Mock
	private ProxyClient<SsmClient> proxyClient;
	@Mock
	SsmClient ssmClient;

	private ReadHandler handler;

	@BeforeEach
	public void setup() {
		handler = new ReadHandler(ssmClient);
		ssmClient = mock(SsmClient.class);
		proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
		proxyClient = MOCK_PROXY(proxy, ssmClient);
	}

	@AfterEach
	public void tear_down(org.junit.jupiter.api.TestInfo testInfo) {
		if (!testInfo.getTags().contains("skipSdkInteraction")) {
			verify(ssmClient, atLeastOnce()).serviceName();
		}
		verifyNoMoreInteractions(proxyClient.client());
	}

	@Test
	public void handleRequest_SimpleSuccess() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final DescribeParametersResponse describeParametersResponse = DescribeParametersResponse.builder()
			.parameters(ParameterMetadata.builder()
				.description(DESCRIPTION)
				.tier(ParameterTier.STANDARD)
				.build())
			.build();
		when(proxyClient.client().describeParameters(any(DescribeParametersRequest.class))).thenReturn(describeParametersResponse);

		final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
			.tagList(
				Tag.builder().key("k1").value("v1").build(),
				Tag.builder().key("k2").value("v2").build()
			)
			.build();
		when(proxyClient.client().listTagsForResource(any(ListTagsForResourceRequest.class))).thenReturn(listTagsForResourceResponse);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.desiredResourceState(ResourceModel.builder().build())
			.build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
	}

	@Test
	public void handleRequest_EmptyGetParametersResponse() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder().build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.desiredResourceState(ResourceModel.builder().name("ParameterName")
				.build())
			.build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnNotFoundException ex) {
			assertThat(ex).isInstanceOf(CfnNotFoundException.class);
		}
	}

	@Test
	public void handleRequest_EmptyDescribeParametersResponse() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final DescribeParametersResponse describeParametersResponse = DescribeParametersResponse.builder().build();
		when(proxyClient.client().describeParameters(any(DescribeParametersRequest.class))).thenReturn(describeParametersResponse);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.desiredResourceState(ResourceModel.builder().name("ParameterName")
				.build())
			.build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnNotFoundException ex) {
			assertThat(ex).isInstanceOf(CfnNotFoundException.class);
		}
	}

	@Test
	public void handleRequest_AmazonServiceExceptionInternalServerError() {
		when(proxyClient.client().getParameters(any(GetParametersRequest.class)))
			.thenThrow(InternalServerErrorException.builder().build());

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.desiredResourceState(ResourceModel.builder()
				.build())
			.build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnServiceInternalErrorException ex) {
			assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
		}
	}
}
