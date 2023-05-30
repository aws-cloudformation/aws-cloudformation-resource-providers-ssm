package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {
    private static final String OPERATION = "ReadParameter";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SsmClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        // read model only contains primary key
        ResourceModel model = request.getDesiredResourceState();
        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-read", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::getParametersRequest)
                                .makeServiceCall(this::ReadResource)
                                .done((getParametersRequest, getParametersResponse, proxyInvocation, resourceModel, context) -> {
                                    if(getParametersResponse.parameters().size() == 0) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getName());
                                    }
                                    final Parameter parameter = getParametersResponse.parameters().stream().findFirst().get();
                                    model.setName(parameter.name());
                                    model.setType(parameter.typeAsString());
                                    model.setValue(parameter.value());
                                    model.setDataType(parameter.dataType());
                                    return ProgressEvent.progress(model, callbackContext);
                                })
                )
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-read-tags", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::listResourceTagRequest)
                                .makeServiceCall(this::ReadResourceTags)
                                .done((listResourceTagRequest, listTagsForResourceResponse, proxyInvocation, resourceModel, context) -> {
                                    List<Tag> tags = listTagsForResourceResponse.tagList();
                                    model.setTags(TagHelper.convertToMap(tags));
                                    return ProgressEvent.progress(model, callbackContext);
                                })
                )
                .then(progress ->
                        proxy.initiate("aws-ssm-parameter::resource-read-metadata", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::describeParametersRequestForSingleParameter)
                                .makeServiceCall(this::ReadResourceMetaData)
                                .done((describeParametersRequest, describeParametersResponse, proxyInvocation, resourceModel, context) -> {
                                    if(describeParametersResponse.parameters().size() == 0) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getName());
                                    }
                                    final ParameterMetadata parameterMetadata = describeParametersResponse.parameters().stream().findFirst().get();
                                    model.setDescription(parameterMetadata.description());
                                    model.setTier(parameterMetadata.tierAsString());
                                    model.setAllowedPattern(parameterMetadata.allowedPattern());
                                    model.setPolicies(Translator.policyToString(parameterMetadata));
                                    return ProgressEvent.defaultSuccessHandler(model);
                                })
                );
    }

    private GetParametersResponse ReadResource(final GetParametersRequest getParametersRequest,
                                               final ProxyClient<SsmClient> proxyClient) {
        try{
            return proxyClient.injectCredentialsAndInvokeV2(getParametersRequest, proxyClient.client()::getParameters);
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }
    }

    private DescribeParametersResponse ReadResourceMetaData(final DescribeParametersRequest describeParametersRequest,
                                                            final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(describeParametersRequest, proxyClient.client()::describeParameters);
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }
    }

    private ListTagsForResourceResponse ReadResourceTags(final ListTagsForResourceRequest listTagsForResourceRequest,
                                                         final ProxyClient<SsmClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(listTagsForResourceRequest, proxyClient.client()::listTagsForResource);
        } catch (final InternalServerErrorException exception) {
            throw new CfnServiceInternalErrorException(OPERATION, exception);
        }
    }
}
