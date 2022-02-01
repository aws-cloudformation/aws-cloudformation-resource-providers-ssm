package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
public class DeleteHandlerTest extends AbstractTestBase {

	@Mock
	private AmazonWebServicesClientProxy proxy;
	@Mock
	private ProxyClient<SsmClient> proxyClient;
	@Mock
	SsmClient ssmClient;

	private DeleteHandler handler;

	private ResourceModel RESOURCE_MODEL;

	@BeforeEach
	public void setup() {
		handler = new DeleteHandler(ssmClient);
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
			.logicalResourceIdentifier("logicalId").build();
		final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(response.getCallbackContext()).isNull();
		assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
		assertThat(response.getResourceModels()).isNull();
		assertThat(response.getMessage()).isNull();
		assertThat(response.getErrorCode()).isNull();

		verify(proxyClient.client()).deleteParameter(any(DeleteParameterRequest.class));
	}


	@Test
	public void handleRequest_FailWithException() {
		//Exceptions while calling the API
		Exception[] exceptions = {
			ParameterNotFoundException.builder().build(),
			// AwsServiceException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("ThrottlingException").build()).build(),
			// AwsServiceException.builder().build(),
			// SdkException.builder().build()
		};

		HandlerErrorCode[] handlerErrorCodes = {
			HandlerErrorCode.NotFound,
			// HandlerErrorCode.Throttling,
			// HandlerErrorCode.GeneralServiceException,
			// HandlerErrorCode.InternalFailure,
		};

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logical_id").build();

		for (int i = 0; i < exceptions.length; i++) {
			//response is empty for pre check success
			when(proxyClient.client().deleteParameter(any(DeleteParameterRequest.class)))
				.thenThrow(exceptions[i]);

			final ProgressEvent<ResourceModel, CallbackContext> response = handler
				.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
			assertFailureErrorCode(request, response, handlerErrorCodes[i]);
		}
	}

	@Test
	public void handleRequest_NonThrottlingAmazonServiceException() {
		AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
		amazonServiceException.setStatusCode(500);

		when(proxyClient.client().deleteParameter(any(DeleteParameterRequest.class)))
			.thenThrow(amazonServiceException);

		final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
			.clientRequestToken("token")
			.desiredResourceTags(TAG_SET)
			.systemTags(SYSTEM_TAGS_SET)
			.desiredResourceState(RESOURCE_MODEL)
			.logicalResourceIdentifier("logical_id").build();

		try {
			handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
		} catch (CfnGeneralServiceException ex) {
			assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
		}

		verify(proxyClient.client()).deleteParameter(any(DeleteParameterRequest.class));
	}
}
