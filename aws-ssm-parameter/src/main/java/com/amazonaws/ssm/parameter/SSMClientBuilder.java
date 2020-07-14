package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;

public class SSMClientBuilder {
    /**
     * Builds and returns SsmClient with configuration overrides.
     *
     * @return Configured SsmClient.
     */
    public static SsmClient getClient() {
        return SsmClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
