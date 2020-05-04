package com.amazonaws.ssm.parameter;

import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.Map;
import java.util.Random;

public class CreateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SsmClient> proxyClient,
            Logger logger) {
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

        return proxy.initiate("ssm::create-parameter-group", proxyClient, model, callbackContext)
                .request((resourceModel) -> Translator.createPutParameterRequest(resourceModel, consolidatedTagList))
                .call((createPutParameterRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createPutParameterRequest, proxyInvocation.client()::putParameter))
                .done(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
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
