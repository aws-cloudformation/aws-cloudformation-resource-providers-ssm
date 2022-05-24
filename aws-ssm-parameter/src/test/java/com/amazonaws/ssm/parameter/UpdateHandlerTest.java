package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

	@Mock
	private AmazonWebServicesClientProxy proxy;
	@Mock
	private ProxyClient<SsmClient> proxyClient;
	@Mock
	SsmClient ssmClient;
	@Mock
	private ReadHandler readHandler;

	private UpdateHandler handler;

	private ResourceModel RESOURCE_MODEL;

	private Map<String, String> PREVIOUS_TAG_SET_NO_CHANGE;

	private Map<String, String> TAG_SET_WITH_CHANGE;

	@BeforeEach
	public void setup() {
		handler = new UpdateHandler(ssmClient, readHandler);
		ssmClient = mock(SsmClient.class);
		proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
		proxyClient = MOCK_PROXY(proxy, ssmClient);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.name(NAME)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(toResourceModelTags(TAG_SET))
			.build();

		PREVIOUS_TAG_SET_NO_CHANGE = new HashMap<String, String>();
		TAG_SET_WITH_CHANGE = new HashMap<String, String>();
	}

	@AfterEach
	public void tear_down(org.junit.jupiter.api.TestInfo testInfo) {
		if (!testInfo.getTags().contains("skipSdkInteraction")) {
			verify(ssmClient, atLeastOnce()).serviceName();
		}
	}

	@Test
	public void handleRequest_SimpleSuccess() {
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

		final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
		when(proxyClient.client().removeTagsFromResource(any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, null));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logicalId").build();
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
	public void handleRequest_SimpleSuccess_WithoutTagsChange() {
		PREVIOUS_TAG_SET_NO_CHANGE.putAll(TAG_SET);
		PREVIOUS_TAG_SET_NO_CHANGE.putAll(SYSTEM_TAGS_SET);

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
			ProgressEvent.success(RESOURCE_MODEL, null));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET_NO_CHANGE)
			.logicalResourceIdentifier("logicalId").build();
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
	public void handleRequest_SimpleSuccess_WithTagsChange() {
		TAG_SET_WITH_CHANGE.put("AddTagKey", "AddTagValue");
		PREVIOUS_TAG_SET_NO_CHANGE.putAll(TAG_SET);
		PREVIOUS_TAG_SET_NO_CHANGE.putAll(SYSTEM_TAGS_SET);

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

		final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
		when(proxyClient.client().removeTagsFromResource(any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);
		final AddTagsToResourceResponse addTagsToResourceResponse = AddTagsToResourceResponse.builder().build();
		when(proxyClient.client().addTagsToResource(any(AddTagsToResourceRequest.class))).thenReturn(addTagsToResourceResponse);

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, null));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET_WITH_CHANGE)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET_NO_CHANGE)
			.logicalResourceIdentifier("logicalId").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackContext()).isNull();
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
			.previousResourceTags(PREVIOUS_TAG_SET)
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
	public void handleRequest_SimpleSuccess_WithImageDataType() {
		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxyClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

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

		final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
		when(proxyClient.client().removeTagsFromResource(any(RemoveTagsFromResourceRequest.class))).thenReturn(removeTagsFromResourceResponse);

		when(readHandler.handleRequest(any(), any(), any(), any(), any())).thenReturn(
			ProgressEvent.success(RESOURCE_MODEL, null));

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logicalId").build();
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
	public void handleRequest_AmazonServiceException400ThrottlingException() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(429);
		amazonServiceException.setErrorCode("ThrottlingException");

		when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(amazonServiceException);

		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnThrottlingException ex) {
			assertThat(ex).isInstanceOf(CfnThrottlingException.class);
		}
	}

	@Test
	public void handleRequest_ParameterAlreadyExistsException() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(ParameterAlreadyExistsException.builder().build());

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnAlreadyExistsException ex) {
			assertThat(ex).isInstanceOf(CfnAlreadyExistsException.class);
		}
	}

	@Test
	public void handleRequest_AmazonServiceException500Exception() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(500);

		when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(amazonServiceException);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnGeneralServiceException ex) {
			assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
		}
	}

	@Test
	public void handleRequest_AmazonServiceException400NonThrottlingException() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(400);
		amazonServiceException.setErrorCode("Invalid Input");

		when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(amazonServiceException);
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnGeneralServiceException ex) {
			assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
		}
	}

	@Test
	public void handleRequest_AmazonServiceExceptionInternalServerError() {
		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxyClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		when(proxyClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(InternalServerErrorException.builder().build());

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.previousResourceTags(PREVIOUS_TAG_SET)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnServiceInternalErrorException ex) {
			assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
		}
	}
}
