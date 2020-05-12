package com.amazonaws.ssm.parameter;

import org.apache.commons.lang3.RandomStringUtils;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

public class CreateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        Map<String, String> consolidatedTagList = request.getDesiredResourceTags();
        consolidatedTagList.putAll(request.getSystemTags());

        // Set model primary ID if absent
        if(model.getName() == null) {
            model.setName(generateParameterName(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken()
            ));
        }

        if(model.getDataType() != null && model.getDataType() == Constants.AWS_EC2_IMAGE_DATATYPE) {
            return ProgressEvent.progress(model, callbackContext)
                    .then(progress ->
                            // If your service API throws 'ResourceAlreadyExistsException' for create requests then CreateHandler can return just proxy.initiate construction
                            // STEP 2.0 [initialize a proxy context]
                            proxy.initiate("aws-ssm-parameter::resource-create", proxyClient, model, callbackContext)
                                    .translateToServiceRequest((resourceModel) -> Translator.createPutParameterRequest(resourceModel, consolidatedTagList))
                                    .backoffDelay(
                                            Constant.of()
                                                    .timeout(Duration.ofMinutes(5))
                                                    .delay(Duration.ofSeconds(30))
                                                    .build())
                                    .makeServiceCall((createPutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createPutParameterRequest, proxyInvocation.client()::putParameter))
                                    .stabilize(BaseHandlerStd::stabilize)
                                    .handleError((putParameterRequest, exception, _proxyClient, _model, _callbackContext) -> handleError("aws-ssm-parameter::resource-create", exception, _model, _callbackContext, logger))
                                    .progress())
                    .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        // If your service API throws 'ResourceAlreadyExistsException' for create requests then CreateHandler can return just proxy.initiate construction
                        // STEP 2.0 [initialize a proxy context]
                        proxy.initiate("aws-ssm-parameter::resource-create", proxyClient, model, callbackContext)
                                .translateToServiceRequest((resourceModel) -> Translator.createPutParameterRequest(resourceModel, consolidatedTagList))
                                .makeServiceCall((createPutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createPutParameterRequest, proxyInvocation.client()::putParameter))
                                .handleError((putParameterRequest, exception, _proxyClient, _model, _callbackContext) -> handleError("aws-ssm-parameter::resource-create", exception, _model, _callbackContext, logger))
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    // We support this special use case of auto-generating names only for CloudFormation.
    // Name format: Prefix - logical resource id - randomString
    private String generateParameterName(final String logicalResourceId, final String clientRequestToken) {
        StringBuilder sb = new StringBuilder();
        int endIndex = logicalResourceId.length() > Constants.ALLOWED_LOGICAL_RESOURCE_ID_LENGTH
                ? Constants.ALLOWED_LOGICAL_RESOURCE_ID_LENGTH : logicalResourceId.length();

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
