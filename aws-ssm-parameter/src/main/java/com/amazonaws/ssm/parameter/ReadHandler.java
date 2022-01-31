package com.amazonaws.ssm.parameter;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

	public ReadHandler() {
		super();
	}

	@VisibleForTesting
	protected ReadHandler(SsmClient ssmClient) {
		super(ssmClient);
	}

	@Override
	protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final ProxyClient<SsmClient> proxyClient,
		final Logger logger) {
		final ResourceModel model = request.getDesiredResourceState();

		logger.log("Invoking Read Handler");
		logger.log("READ ResourceModel: " + model.toString());

		return ProgressEvent.progress(model, callbackContext)
			.then(progress -> getParameters(proxy, progress, proxyClient, model, callbackContext, logger))
			.then(progress -> describeParameters(proxy, progress, proxyClient, progress.getResourceModel(), progress.getCallbackContext(), logger))
			.then(progress -> listTagsForResourceRequestForParameters(proxy, progress, proxyClient, progress.getResourceModel(), progress.getCallbackContext(), logger))
			.then(progress -> ProgressEvent.defaultSuccessHandler(progress.getResourceModel()));
	}

	private ProgressEvent<ResourceModel, CallbackContext> getParameters(
		final AmazonWebServicesClientProxy proxy,
		final ProgressEvent<ResourceModel, CallbackContext> progress,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel model, final CallbackContext callbackContext, final Logger logger) {

		return proxy.initiate("aws-ssm-parameter::resource-read-getParameters", proxyClient, model, callbackContext)
			.translateToServiceRequest(Translator::getParametersRequest)
			.makeServiceCall(((getParametersRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(getParametersRequest, ssmClientProxyClient.client()::getParameters)))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
			.done((getParametersRequest, getParametersResponse, proxyClient1, resourceModel, context) -> {
				if (getParametersResponse.parameters().isEmpty()) {
					throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName());
				}
				final Parameter parameter = getParametersResponse.parameters().get(0);

				return ProgressEvent.progress(
					ResourceModel.builder()
						.name(parameter.name())
						.type(parameter.typeAsString())
						.value(parameter.value())
						.dataType(parameter.dataType())
						.build(),
					context);
			});
	}

	private ProgressEvent<ResourceModel, CallbackContext> describeParameters(
		final AmazonWebServicesClientProxy proxy,
		final ProgressEvent<ResourceModel, CallbackContext> progress,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel model, final CallbackContext callbackContext, final Logger logger) {

		return proxy.initiate("aws-ssm-parameter::resource-read-describeParameters", proxyClient, model, callbackContext)
			.translateToServiceRequest(Translator::describeParametersRequestWithFilter)
			.makeServiceCall(((describeParametersRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(describeParametersRequest, ssmClientProxyClient.client()::describeParameters)))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
			.done((describeParametersRequest, describeParametersResponse, proxyClient1, resourceModel, context) -> {
				if (describeParametersResponse.parameters().isEmpty()) {
					throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName());
				}
				final ParameterMetadata parameterMetadata = describeParametersResponse.parameters().get(0);
				resourceModel.setAllowedPattern(parameterMetadata.allowedPattern());
				resourceModel.setDescription(parameterMetadata.description());
				resourceModel.setPolicies(parameterMetadata.policies().toString());
				resourceModel.setTier(parameterMetadata.tierAsString());
				return ProgressEvent.progress(resourceModel, context);
			});
	}

	private ProgressEvent<ResourceModel, CallbackContext> listTagsForResourceRequestForParameters(
		final AmazonWebServicesClientProxy proxy,
		final ProgressEvent<ResourceModel, CallbackContext> progress,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel model, final CallbackContext callbackContext, final Logger logger) {

		return proxy.initiate("aws-ssm-parameter::resource-read-listTagsForResourceRequestForParameters", proxyClient, model, callbackContext)
			.translateToServiceRequest(Translator::listTagsForResourceRequest)
			.makeServiceCall(((listTagsForResourceRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(listTagsForResourceRequest, ssmClientProxyClient.client()::listTagsForResource)))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
			.done((listTagsForResourceRequest, listTagsForResourceResponse, proxyClient1, resourceModel, context) -> {
				resourceModel.setTags(Translator.translateTagsFromSdk(listTagsForResourceResponse.tagList()));
				return ProgressEvent.progress(resourceModel, context);
			});
	}
}
