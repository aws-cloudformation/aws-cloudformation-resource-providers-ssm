package software.amazon.ssm.maintenancewindowtarget.util;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * Builder of AWS SDK's SsmClient.
 */
public class SsmClientBuilder {
    /**
     * Builds and returns SsmClient with configuration overrides.
     *
     * @return Configured SsmClient.
     */
    public static SsmClient getClient() {
        return SsmClient.builder()
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                .build())
            .build();
    }
}
