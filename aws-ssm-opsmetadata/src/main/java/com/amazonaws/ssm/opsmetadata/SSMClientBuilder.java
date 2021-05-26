package com.amazonaws.ssm.opsmetadata;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;

public class SSMClientBuilder {
    private static final RetryPolicy RETRY_POLICY =
            RetryPolicy.builder()
                    .numRetries(16)
                    .retryCondition(RetryCondition.defaultRetryCondition())
                    .build();
    /**
     * Builds and returns SsmClient with configuration overrides.
     *
     * @return Configured SsmClient.
     */
    public static SsmClient getClient() {
        return SsmClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RETRY_POLICY).build())
                .build();
    }
}
