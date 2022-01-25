package com.amazonaws.ssm.parameter;

import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdateHandler extends BaseHandlerStd {
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
			return ProgressEvent.defaultFailureHandler(new CfnServiceInternalErrorException(message), HandlerErrorCode.InvalidRequest);
		}

		return ProgressEvent.progress(model, callbackContext)
			// First validate the resource actually exists per the contract requirements
			// https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
			.then(progress -> validateResourceExists(proxy, progress, proxyClient, progress.getResourceModel(), progress.getCallbackContext(), logger))
			.then(progress -> updateResourceExceptTagging(proxy, progress, proxyClient, progress.getResourceModel(), progress.getCallbackContext(), logger))
			.then(progress -> handleTagging(proxy, proxyClient, progress, progress.getResourceModel(), request.getDesiredResourceTags(), request.getPreviousResourceTags()))
			.then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, logger));
	}

	private ProgressEvent<ResourceModel, CallbackContext> validateResourceExists(final AmazonWebServicesClientProxy proxy,
		final ProgressEvent<ResourceModel, CallbackContext> progress, final ProxyClient<SsmClient> proxyClient,
		final ResourceModel model, final CallbackContext callbackContext, final Logger logger) {
		return proxy.initiate("aws-ssm-parameter::Update:validate-resource-exists", proxyClient, model, callbackContext)
			.translateToServiceRequest(Translator::getParametersRequest)
			.makeServiceCall((getParametersRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(getParametersRequest, ssmClientProxyClient.client()::getParameters))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, this.logger))
			.done((getParametersRequest, getParametersResponse, proxyClient1, resourceModel, context) -> {
				if (!getParametersResponse.invalidParameters().isEmpty()) {
					throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resourceModel.getName());
				}
				return progress;
			});
	}

	private ProgressEvent<ResourceModel, CallbackContext> updateResourceExceptTagging(AmazonWebServicesClientProxy proxy, ProgressEvent<ResourceModel, CallbackContext> progress,
		ProxyClient<SsmClient> proxyClient, ResourceModel resourceModel, CallbackContext callbackContext, Logger logger) {
		return proxy.initiate("aws-ssm-parameter::Update:resource-update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
			.translateToServiceRequest(Translator::updatePutParameterRequest)
			.backoffDelay(getBackOffDelay(progress.getResourceModel()))
			.makeServiceCall((putParameterRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(putParameterRequest, ssmClientProxyClient.client()::putParameter))
			.stabilize((req, response, client, model1, cbContext) -> stabilize(req, response, client, model1, cbContext, logger))
			.progress();
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
		// Remove tags with aws prefix as they should not be modified (or removed) once attached
		existingTags.removeIf(tag -> tag.key().startsWith("aws:"));

		final Set<Tag> setTagsToRemove = Sets.difference(existingTags, currentTags);
		final Set<Tag> setTagsToAdd = Sets.difference(currentTags, existingTags);

		final List<Tag> tagsToRemove = new ArrayList<>(setTagsToRemove);
		final List<Tag> tagsToAdd = new ArrayList<>(setTagsToAdd);

		return ProgressEvent.progress(resourceModel, progress.getCallbackContext())
			// First validate the resource actually exists per the contract requirements
			.then(progress1 -> removeTags(proxy, progress1, proxyClient, logger, tagsToRemove))
			.then(progress1 -> addTags(proxy, progress1, proxyClient, logger, tagsToAdd))
			.then(progress1 -> ProgressEvent.progress(progress1.getResourceModel(), progress1.getCallbackContext()));
	}

	private ProgressEvent<ResourceModel, CallbackContext> removeTags(AmazonWebServicesClientProxy proxy, ProgressEvent<ResourceModel, CallbackContext> progress,
		ProxyClient<SsmClient> proxyClient, Logger logger, List<Tag> tagsToRemove) {
		if (CollectionUtils.isNullOrEmpty(tagsToRemove))
			return progress;
		return proxy.initiate("aws-ssm-parameter::Update:handle-tagging-remove", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
			.translateToServiceRequest(model -> Translator.removeTagsFromResourceRequest(model.getName(), tagsToRemove))
			.backoffDelay(getBackOffDelay(progress.getResourceModel()))
			.makeServiceCall((removeTagsFromResourceRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(removeTagsFromResourceRequest, ssmClientProxyClient.client()::removeTagsFromResource))
			.progress();
	}

	private ProgressEvent<ResourceModel, CallbackContext> addTags(AmazonWebServicesClientProxy proxy, ProgressEvent<ResourceModel, CallbackContext> progress,
		ProxyClient<SsmClient> proxyClient, Logger logger, List<Tag> tagsToAdd) {
		if (CollectionUtils.isNullOrEmpty(tagsToAdd))
			return progress;
		return proxy.initiate("aws-ssm-parameter::Update:handle-tagging-add", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
			.translateToServiceRequest(model -> Translator.addTagsToResourceRequest(model.getName(), tagsToAdd))
			.backoffDelay(getBackOffDelay(progress.getResourceModel()))
			.makeServiceCall((addTagsToResourceRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(addTagsToResourceRequest, ssmClientProxyClient.client()::addTagsToResource))
			.progress();
	}
}
