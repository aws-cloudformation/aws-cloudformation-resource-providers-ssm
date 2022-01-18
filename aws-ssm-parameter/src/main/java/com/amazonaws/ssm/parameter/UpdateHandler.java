package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.TerminalException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
	private static final String OPERATION = "PutParameter";
	private static final String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
	private Logger logger;

	@Override
	protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final ProxyClient<SsmClient> proxyClient,
		final Logger logger) {
		this.logger = logger;
		final ResourceModel model = request.getDesiredResourceState();

		if (model.getType().equalsIgnoreCase(ParameterType.SECURE_STRING.toString())) {
			String message = String.format("SSM Parameters of type %s cannot be updated using CloudFormation", ParameterType.SECURE_STRING);
			return ProgressEvent.defaultFailureHandler(new TerminalException(message),
				HandlerErrorCode.InvalidRequest);
		}

		return ProgressEvent.progress(model, callbackContext)
			// First validate the resource actually exists per the contract requirements
			// https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
			.then(progress ->
				proxy.initiate("aws-ssm-parameter::validate-resource-exists", proxyClient, model, callbackContext)
					.translateToServiceRequest(Translator::getParametersRequest)
					.makeServiceCall(this::validateResourceExists)
					.progress())

			.then(progress ->
				proxy.initiate("aws-ssm-parameter::resource-update", proxyClient, model, callbackContext)
					.translateToServiceRequest(Translator::updatePutParameterRequest)
					.backoffDelay(getBackOffDelay(model))
					.makeServiceCall(this::updateResource)
					.stabilize(BaseHandlerStd::stabilize)
					.progress())
			.then(progress -> handleTagging(proxy, proxyClient, progress, model, request.getDesiredResourceTags(), request.getPreviousResourceTags()))
			.then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
	}

	private GetParametersResponse validateResourceExists(GetParametersRequest getParametersRequest, ProxyClient<SsmClient> proxyClient) {
		GetParametersResponse getParametersResponse;

		getParametersResponse = proxyClient.injectCredentialsAndInvokeV2(getParametersRequest, proxyClient.client()::getParameters);
		if (getParametersResponse.invalidParameters().size() != 0) {
			throw new CfnNotFoundException(ResourceModel.TYPE_NAME, getParametersRequest.names().get(0));
		}

		return getParametersResponse;
	}

	private PutParameterResponse updateResource(final PutParameterRequest putParameterRequest,
		final ProxyClient<SsmClient> proxyClient) {
		try {
			return proxyClient.injectCredentialsAndInvokeV2(putParameterRequest, proxyClient.client()::putParameter);
		} catch (final ParameterAlreadyExistsException exception) {
			throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, putParameterRequest.name());
		} catch (final InternalServerErrorException exception) {
			throw new CfnServiceInternalErrorException(OPERATION, exception);
		} catch (final AmazonServiceException exception) {
			final Integer errorStatus = exception.getStatusCode();
			final String errorCode = exception.getErrorCode();
			if (errorStatus >= Constants.ERROR_STATUS_CODE_400 && errorStatus < Constants.ERROR_STATUS_CODE_500) {
				if (THROTTLING_ERROR_CODES.contains(errorCode)) {
					logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
					throw new CfnThrottlingException(OPERATION, exception);
				}
			}
			throw new CfnGeneralServiceException(OPERATION, exception);
		}
	}

	private ProgressEvent<ResourceModel, CallbackContext> handleTagging(
		final AmazonWebServicesClientProxy proxy,
		final ProxyClient<SsmClient> proxyClient,
		final ProgressEvent<ResourceModel, CallbackContext> progress,
		final ResourceModel resourceModel,
		final Map<String, String> desiredResourceTags,
		final Map<String, String> previousResourceTags) {

		final Set<Tag> currentTags = new HashSet<>(Translator.translateTagsToSdk(desiredResourceTags));
		final Set<Tag> existingTags = new HashSet<>(Translator.translateTagsToSdk(previousResourceTags));
		// Remove tags with aws prefix as they should not be modified once attached
		existingTags.removeIf(tag -> tag.key().startsWith("aws"));

		final Set<Tag> setTagsToRemove = Sets.difference(existingTags, currentTags);
		final Set<Tag> setTagsToAdd = Sets.difference(currentTags, existingTags);

		final List<Tag> tagsToRemove = setTagsToRemove.stream().collect(Collectors.toList());
		final List<Tag> tagsToAdd = setTagsToAdd.stream().collect(Collectors.toList());

		// Deletes tags only if tagsToRemove is not empty.
		if (!CollectionUtils.isNullOrEmpty(tagsToRemove))
			proxy.injectCredentialsAndInvokeV2(
				Translator.removeTagsFromResourceRequest(resourceModel.getName(), tagsToRemove), proxyClient.client()::removeTagsFromResource);

		// Adds tags only if tagsToAdd is not empty.
		if (!CollectionUtils.isNullOrEmpty(tagsToAdd))
			proxy.injectCredentialsAndInvokeV2(
				Translator.addTagsToResourceRequest(resourceModel.getName(), tagsToAdd), proxyClient.client()::addTagsToResource);

		return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
	}
}
