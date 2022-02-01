package com.amazonaws.ssm.parameter;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

	@Mock
	private AmazonWebServicesClientProxy proxy;
	@Mock
	private ProxyClient<SsmClient> proxyClient;
	@Mock
	SsmClient ssmClient;
	@Mock
	private ReadHandler readHandler;

	private CreateHandler handler;

	private ResourceModel RESOURCE_MODEL;

	@BeforeEach
	public void setup() {
		proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
		ssmClient = mock(SsmClient.class);
		proxyClient = MOCK_PROXY(proxy, ssmClient);
		handler = new CreateHandler(ssmClient, readHandler);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.name(NAME)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.build();
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
		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logicalId")
			.build();

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxyClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, new CallbackContext()));

		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
	}

	@Tag("skipSdkInteraction")
	@Test
	public void handleRequest_SecureStringFailure() {
		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_SECURE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.build();

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logicalId").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNotNull();
		assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
	}

	@Test
	public void handleRequest_SimpleSuccess_With_No_ParameterName_Defined_Exceeding_LogicalResourceId() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxyClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.build();

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, new CallbackContext()));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier(RandomStringUtils.random(600)).build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
	}

	@Test
	public void handleRequest_SimpleSuccess_With_No_ParameterName_Defined_Not_Exceeding_LogicalResourceId() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxyClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.build();

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, new CallbackContext()));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logical_id").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
	}

	@Test
	public void handleRequest_SimpleSuccess_WithImageDataType() {
		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.name(NAME)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.dataType("aws:ec2:image")
			.build();

		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxyClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, new CallbackContext()));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logicalId").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();
	}

	@Test
	public void handleRequest_FailWithException() {
		//Exceptions while calling the API
		Exception[] exceptions = {
			AwsServiceException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("ThrottlingException").build()).build(),
			ParameterAlreadyExistsException.builder().build(),
			AwsServiceException.builder().build(),
			SdkException.builder().build()
		};

		HandlerErrorCode[] handlerErrorCodes = {
			HandlerErrorCode.Throttling,
			HandlerErrorCode.AlreadyExists,
			HandlerErrorCode.GeneralServiceException,
			HandlerErrorCode.InternalFailure,
		};

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logical_id").build();

		for (int i = 0; i < exceptions.length; i++) {
			//response is empty for pre check success
			when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
				.thenThrow(exceptions[i]);

			final ProgressEvent<ResourceModel, CallbackContext> response = handler
				.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
			assertFailureErrorCode(request, response, handlerErrorCodes[i]);
		}
	}
}
