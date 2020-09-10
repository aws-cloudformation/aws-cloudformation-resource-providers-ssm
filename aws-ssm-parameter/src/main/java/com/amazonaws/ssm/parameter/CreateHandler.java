package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import org.apache.commons.lang3.RandomStringUtils;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.TerminalException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateHandler extends BaseHandlerStd {
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

        // Set model primary ID if absent
        if(model.getName() == null) {
            model.setName(generateParameterName(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken()
            ));
        }

        if(model.getType().equalsIgnoreCase(ParameterType.SECURE_STRING.toString())) {
            String message = String.format("SSM Parameters of type %s cannot be created using CloudFormation", ParameterType.SECURE_STRING);
            return ProgressEvent.defaultFailureHandler(new TerminalException(message),
                    HandlerErrorCode.InvalidRequest);
        }

        Map<String, String> consolidatedTagList = new HashMap<>();
        if (request.getDesiredResourceTags() != null) {
            consolidatedTagList.putAll(request.getDesiredResourceTags());
        }
        if (request.getSystemTags() != null) {
            consolidatedTagList.putAll(request.getSystemTags());
        }

        return proxy.initiate("aws-ssm-parameter::resource-create", proxyClient, model, callbackContext)
               .translateToServiceRequest((resourceModel) -> Translator.createPutParameterRequest(resourceModel, consolidatedTagList))
               .backoffDelay(getBackOffDelay(model))
               .makeServiceCall(this::createResource)
               .stabilize(BaseHandlerStd::stabilize)
               .progress()
               .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private PutParameterResponse createResource(final PutParameterRequest putParameterRequest,
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
