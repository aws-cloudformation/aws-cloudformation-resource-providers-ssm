package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
			.type(TYPE_STRING)
			.tags(TAG_SET)
			.build();
	}

	@AfterEach
	public void post_execute() {
		verifyNoMoreInteractions(proxySsmClient.client());
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
		when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

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
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_SecureStringFailure() {
		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_SECURE_STRING)
			.tags(TAG_SET)
			.build();

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logicalId").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isEqualTo("SSM Parameters of type SecureString cannot be created using CloudFormation");
		assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

		verify(ssmClient, never()).serviceName();
		verifyNoMoreInteractions(proxySsmClient.client());
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
		when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_STRING)
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
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
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
		when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.value(VALUE)
			.type(TYPE_STRING)
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
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_SimpleSuccess_WithImageDataType() {
		RESOURCE_MODEL = ResourceModel.builder()
			.description(DESCRIPTION)
			.name(NAME)
			.value(VALUE)
			.type(TYPE_STRING)
			.tags(TAG_SET)
			.dataType("aws:ec2:image")
			.build();

		final GetParametersResponse getParametersResponse = GetParametersResponse.builder()
			.parameters(Parameter.builder()
				.name(NAME)
				.type(TYPE_STRING)
				.value(VALUE)
				.version(VERSION).build())
			.build();
		when(proxySsmClient.client().getParameters(any(GetParametersRequest.class))).thenReturn(getParametersResponse);

		final PutParameterResponse putParameterResponse = PutParameterResponse.builder()
			.version(VERSION)
			.tier(ParameterTier.STANDARD)
			.build();
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class))).thenReturn(putParameterResponse);

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
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_AmazonServiceException400ThrottlingException() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(429);
		amazonServiceException.setErrorCode("ThrottlingException");

		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
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

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_ParameterAlreadyExistsException() {
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
			.thenThrow(ParameterAlreadyExistsException.builder().build());

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

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_AmazonServiceException500Exception() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(500);

		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
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

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_AmazonServiceException400NonThrottlingException() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(400);
		amazonServiceException.setErrorCode("Invalid Input");

		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
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

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}

	@Test
	public void handleRequest_AmazonServiceExceptionInternalServerError() {
		when(proxySsmClient.client().putParameter(any(PutParameterRequest.class)))
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

		verify(proxySsmClient.client()).putParameter(any(PutParameterRequest.class));
		verify(ssmClient, atLeastOnce()).serviceName();
	}
}
