package com.aws.ssm.association;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;

import java.net.URI;

/**
 * @author yngfn
 * @date 05/15/2019
 */
public class ClientBuilder {

    public static final SsmClient getSsmClient() {
        return SsmClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                        .build())
                .build();
    }

    public static final SsmClient getSsmClient(final String endpoint, final String region) {
        SsmClientBuilder builder = SsmClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                        .build());
        if (StringUtils.isNotEmpty(region)) {
            builder.region(Region.of(region));
        }
        if (StringUtils.isNotEmpty(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
