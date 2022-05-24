package com.amazonaws.ssm.parameter;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

	public ListHandler() {
		super();
	}

	@VisibleForTesting
	protected ListHandler(SsmClient ssmClient) {
		super(ssmClient);
	}

	protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final ProxyClient<SsmClient> proxyClient,
		final Logger logger) {

		final ResourceModel resourceModel = request.getDesiredResourceState();

		return ProgressEvent.progress(resourceModel, callbackContext)
			.then(progress -> describeParameters(proxy, request, progress, proxyClient, progress.getResourceModel(), progress.getCallbackContext(), logger))
			.then(progress -> ProgressEvent.defaultSuccessHandler(progress.getResourceModel()));
	}

	private ProgressEvent<ResourceModel, CallbackContext> describeParameters(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final ProgressEvent<ResourceModel, CallbackContext> progress,
		final ProxyClient<SsmClient> proxyClient,
		final ResourceModel resourceModel, final CallbackContext callbackContext, final Logger logger) {

		return proxy.initiate("aws-ssm-parameter::resource-list-describeParameters", proxyClient, resourceModel, callbackContext)
			.translateToServiceRequest(model1 -> Translator.describeParametersRequest(request.getNextToken()))
			.makeServiceCall(((describeParametersRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(describeParametersRequest, ssmClientProxyClient.client()::describeParameters)))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
			.done(describeParametersResponse -> {
				String nextToken = describeParametersResponse.nextToken();
				final List<ResourceModel> models = Translator.translateListOfParameters(describeParametersResponse.parameters());

				return ProgressEvent.<ResourceModel, CallbackContext>builder()
					.resourceModels(models)
					.nextToken(nextToken)
					.status(OperationStatus.SUCCESS)
					.build();
			});
	}
}
