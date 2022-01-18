package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {
	protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
		final AmazonWebServicesClientProxy proxy,
		final ResourceHandlerRequest<ResourceModel> request,
		final CallbackContext callbackContext,
		final ProxyClient<SsmClient> proxyClient,
		final Logger logger) {
		final DescribeParametersResponse describeParametersResponse = proxy.injectCredentialsAndInvokeV2(Translator.describeParametersRequest(request.getNextToken()),
			proxyClient.client()::describeParameters);

		final List<ResourceModel> models = describeParametersResponse
			.parameters()
			.stream().map(parameterMetadata -> ResourceModel.builder().name(parameterMetadata.name()).build()).collect(Collectors.toList());

		return ProgressEvent.<ResourceModel, CallbackContext>builder()
			.resourceModels(models)
			.nextToken(describeParametersResponse.nextToken())
			.status(OperationStatus.SUCCESS)
			.build();
	}
}
