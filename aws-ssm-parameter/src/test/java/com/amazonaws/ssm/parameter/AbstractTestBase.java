package com.amazonaws.ssm.parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final org.slf4j.Logger delegate;
    protected static final LoggerProxy logger;

    protected static final String DESCRIPTION;
    protected static final String NAME;
    protected static final String TYPE;
    protected static final String VALUE;
    protected static final Long VERSION;
    protected static final Map<String, String> TAG_SET;
    protected static final Map<String, String> SYSTEM_TAGS_SET;

    static {
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss:SSS Z");
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");

        delegate = LoggerFactory.getLogger("testing");
        logger = new LoggerProxy();

        DESCRIPTION = "sample description";
        NAME = "ParameterName";
        TYPE = "String";
        VALUE = "dummy value";
        VERSION = 1L;
        TAG_SET = new HashMap<String, String>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        SYSTEM_TAGS_SET = new HashMap<String, String>() {
            {
                put("aws:cloudformation:stack-name", "DummyStackName");
                put("aws:cloudformation:logical-id", "DummyLogicalId");
                put("aws:cloudformation:stack-id", "DummyStackArn");
            }
        };
    }

    static ProxyClient<SsmClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final SsmClient ssmClient
    ) {
        return new ProxyClient<SsmClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT injectCredentialsAndInvokeV2(RequestT requestT, Function<RequestT, ResponseT> function) {
                return proxy.injectCredentialsAndInvokeV2(requestT, function);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> CompletableFuture<ResponseT> injectCredentialsAndInvokeV2Async(RequestT requestT, Function<RequestT, CompletableFuture<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>> IterableT injectCredentialsAndInvokeIterableV2(RequestT requestT, Function<RequestT, IterableT> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT> injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT> injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SsmClient client() {
                return ssmClient;
            }
        };
    }
}
