package com.amazonaws.ssm.parameter;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class CreateHandler extends BaseHandlerStd {
	private Logger logger;

	private final ReadHandler readHandler;

	public CreateHandler() {
		super();
		readHandler = new ReadHandler();
	}

	@VisibleForTesting
	protected CreateHandler(SsmClient ssmClient, ReadHandler readHandler) {
		super(ssmClient);
		this.readHandler = readHandler;
	}

	@Override
	protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final ProxyClient<SsmClient> proxyClient,
		final Logger logger) {
		this.logger = logger;
		final ResourceModel model = request.getDesiredResourceState();

		logger.log("Invoking Create Handler");
		logger.log("CREATE ResourceModel: " + request.getDesiredResourceState().toString());

		if (ParameterType.SECURE_STRING.toString().equalsIgnoreCase(model.getType())) {
			String message = String.format("SSM Parameters of type %s cannot be created using CloudFormation", ParameterType.SECURE_STRING);
			return ProgressEvent.failed(null, null, HandlerErrorCode.InvalidRequest, message);
		}

		// Set model primary ID if absent
		if (model.getName() == null) {
			model.setName(IdentifierUtils.generateResourceIdentifier("CFN",
				request.getLogicalResourceIdentifier(),
				request.getClientRequestToken(), 40));
		}

		Map<String, String> consolidatedTagsMap = Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap());
		consolidatedTagsMap.putAll(Optional.ofNullable(request.getSystemTags()).orElse(Collections.emptyMap()));

		return ProgressEvent.progress(model, callbackContext)
			.then(progress -> proxy.initiate("aws-ssm-parameter::resource-create", proxyClient, model, callbackContext)
				.translateToServiceRequest(resourceModel -> Translator.createPutParameterRequest(resourceModel, consolidatedTagsMap))
				.backoffDelay(getBackOffDelay(model))
				.makeServiceCall((putParameterRequest, ssmProxyClient) ->
					ssmProxyClient.injectCredentialsAndInvokeV2(putParameterRequest, ssmProxyClient.client()::putParameter))
				.stabilize((req, response, client, model1, cbContext) -> stabilize(req, response, client, model1, cbContext, logger))
				.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
				.progress())
			.then(progress -> readHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger));
	}
}
