package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
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

		logger.log("Invoking Delete Handler");
		logger.log("DELETE ResourceModel: " + model.toString());

		return proxy.initiate("aws-ssm-parameter::resource-delete", proxyClient, model, callbackContext)
			.translateToServiceRequest(Translator::deleteParameterRequest)
			.makeServiceCall(((deleteParameterRequest, ssmClientProxyClient) ->
				ssmClientProxyClient.injectCredentialsAndInvokeV2(deleteParameterRequest, ssmClientProxyClient.client()::deleteParameter)))
			.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
			.done((deleteParameterRequest, deleteParameterResponse, client1, model1, callbackContext1) -> ProgressEvent.defaultSuccessHandler(null));
	}
}
