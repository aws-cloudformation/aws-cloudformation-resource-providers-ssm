package com.amazonaws.ssm.parameter;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.Optional;
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
			Optional.ofNullable(callbackContext).orElse(new CallbackContext()),
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
	protected Boolean stabilize(
		final PutParameterRequest putParameterRequest,
		final PutParameterResponse putParameterResponse,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel resourceModel,
		final CallbackContext callbackContext,
		final Logger logger
	) {
		try {
			logger.log(String.format("Trying to stabilize %s [%s]", ResourceModel.TYPE_NAME, putParameterRequest.name()));
			GetParametersResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.getParametersRequest(resourceModel), proxyClient.client()::getParameters);

			// if invalid parameters list is not empty return false as the validation for
			// DataType has not been completed and the parameter has not been created yet.
			if (response == null || !response.invalidParameters().isEmpty()) {
				return false;
			}
			return (response.parameters() != null && response.parameters().get(0).version() == putParameterResponse.version());
		} catch (Exception e) {
			logger.log(String.format("Failed during stabilization of SsmParameter [%s] with error: [%s]", putParameterRequest.name(), e.getMessage()));
			throw e;
		}
	}

	protected ProgressEvent<ResourceModel, CallbackContext> handleError(SsmRequest ssmRequest, Exception e, ProxyClient<SsmClient> proxyClient,
		ResourceModel model, CallbackContext context, Logger logger) {

		BaseHandlerException ex;

		if (e instanceof AwsServiceException) {
			if (e instanceof ParameterAlreadyExistsException) {
				ex = new CfnAlreadyExistsException(e);
			} else if (e instanceof ParameterNotFoundException) {
				ex = new CfnNotFoundException(e);
			} else if (e instanceof InternalServerErrorException) {
				ex = new CfnServiceInternalErrorException(e);
			} else if (hasThrottled(e)) {
				ex = new CfnThrottlingException(e);
			} else {
				ex = new CfnGeneralServiceException(e);
			}
		} else {
			// InternalFailure: An unexpected error occurred within the handler.
			ex = new CfnInternalFailureException(e);
		}
		logger.log(String.format("Handled Exception: error code [%s], message [%s]", ex.getErrorCode(), ex.getMessage()));
		return ProgressEvent.failed(model, context, ex.getErrorCode(), ex.getMessage());
	}

	private boolean hasThrottled(Exception e) {
		String errorCode = getErrorCode(e);
		return (THROTTLING_ERROR_CODES.contains(errorCode));
	}

	protected String getErrorCode(Exception e) {
		if (e instanceof AwsServiceException && ((AwsServiceException) e).awsErrorDetails() != null) {
			return ((AwsServiceException) e).awsErrorDetails().errorCode();
		}
		return e.getMessage();
	}
}
