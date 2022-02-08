package com.amazonaws.ssm.parameter;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
			model.setName(generateParameterName(request.getLogicalResourceIdentifier(), request.getClientRequestToken()));
		}

		Map<String, String> consolidatedTagsMap = Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap());
		consolidatedTagsMap.putAll(Optional.ofNullable(request.getSystemTags()).orElse(Collections.emptyMap()));

		return ProgressEvent.progress(model, callbackContext)
			.then(progress -> proxy.initiate("aws-ssm-parameter::resource-create", proxyClient, model, callbackContext)
				.translateToServiceRequest(resourceModel -> Translator.createPutParameterRequest(resourceModel, consolidatedTagsMap))
				.backoffDelay(getBackOffDelay(model))
				.makeServiceCall((putParameterRequest, ssmProxyClient) ->
					ssmProxyClient.injectCredentialsAndInvokeV2(putParameterRequest, ssmProxyClient.client()::putParameter))
				.stabilize((req, response, client, model1, cbContext) -> {
					if (isStabilizationNeeded(model1.getDataType()))
						return stabilize(req, response, client, model1, cbContext, logger);
					else
						return true;
				})
				.handleError((req, e, proxy1, model1, context1) -> handleError(req, e, proxy1, model1, context1, logger))
				.progress())
			.then(progress -> readHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger));
	}

	// We support this special use case of auto-generating names only for CloudFormation.
	// Name format: Prefix - logical resource id - randomString
	private String generateParameterName(final String logicalResourceId, final String clientRequestToken) {
		StringBuilder sb = new StringBuilder();
		int endIndex = Math.min(logicalResourceId.length(), Constants.ALLOWED_LOGICAL_RESOURCE_ID_LENGTH);

		sb.append(Constants.CF_PARAMETER_NAME_PREFIX);
		sb.append("-");
		sb.append(logicalResourceId.substring(0, endIndex));
		sb.append("-");

		sb.append(RandomStringUtils.random(
			Constants.GUID_LENGTH,
			0,
			0,
			true,
			true,
			null,
			new Random(clientRequestToken.hashCode())));
		return sb.toString();
	}
}
