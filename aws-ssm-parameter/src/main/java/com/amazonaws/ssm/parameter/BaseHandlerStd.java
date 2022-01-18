package com.amazonaws.ssm.parameter;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.Set;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
	protected static final Set<String> THROTTLING_ERROR_CODES = ImmutableSet.of(
		"ThrottlingException",
		"TooManyUpdates");

	@Override
	public ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final Logger logger) {
		return handleRequest(
			proxy,
			request,
			callbackContext != null ? callbackContext : new CallbackContext(),
			proxy.newProxy(SSMClientBuilder::getClient),
			logger);
	}

	protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		AmazonWebServicesClientProxy proxy,
		ResourceHandlerRequest<ResourceModel> request,
		CallbackContext callbackContext,
		ProxyClient<SsmClient> client,
		Logger logger);

	protected Constant getBackOffDelay(final ResourceModel model) {
		if (model.getDataType() != null && model.getDataType() == Constants.AWS_EC2_IMAGE_DATATYPE) {
			return Constant.of()
				.timeout(Duration.ofMinutes(5))
				.delay(Duration.ofSeconds(30))
				.build();
		} else {
			return Constant.of()
				.timeout(Duration.ofMinutes(5))
				.delay(Duration.ofSeconds(5))
				.build();
		}
	}

	/**
	 * If your resource requires some form of stabilization (e.g. service does not provide strong
	 * consistency), you will need to ensure that your code accounts for any potential issues, so that
	 * a subsequent read/update requests will not cause any conflicts (e.g.
	 * NotFoundException/InvalidRequestException) for more information ->
	 * https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
	 *
	 * @param putParameterRequest  the aws service request to create a resource
	 * @param putParameterResponse the aws service response to create a resource
	 * @param proxyClient          the aws service client to make the call
	 * @param resourceModel        resource model
	 * @param callbackContext      callback context
	 * @return boolean state of stabilized or not
	 */
	protected static boolean stabilize(
		final PutParameterRequest putParameterRequest,
		final PutParameterResponse putParameterResponse,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel resourceModel,
		final CallbackContext callbackContext
	) {
		final GetParametersResponse response;
		try {
			response = proxyClient.injectCredentialsAndInvokeV2(Translator.getParametersRequest(resourceModel), proxyClient.client()::getParameters);
		} catch (final InternalServerErrorException exception) {
			return false;
		}

		// if invalid parameters list is not empty return false as the validation for
		// DataType has not been completed and the parameter has not been created yet.
		if (response == null || response.invalidParameters().size() != 0) {
			return false;
		}
		return (response.parameters() != null &&
			response.parameters().get(0).version() == putParameterResponse.version());
	}
}
