package software.amazon.ssm.patchbaseline.utils;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.ssm.SsmClient;

public class SsmClientBuilder {
    public static SsmClient getClient() {
        return SsmClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                        .build())
                .build();
    }
}
