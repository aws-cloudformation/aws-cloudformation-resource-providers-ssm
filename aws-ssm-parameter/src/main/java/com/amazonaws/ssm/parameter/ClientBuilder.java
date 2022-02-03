package com.amazonaws.ssm.parameter;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.EqualJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;

import java.time.Duration;

public class ClientBuilder {

	private static final BackoffStrategy BACKOFF_THROTTLING_STRATEGY =
		EqualJitterBackoffStrategy.builder()
			.baseDelay(Duration.ofMillis(2000)) //1st retry is ~2 sec
			.maxBackoffTime(SdkDefaultRetrySetting.MAX_BACKOFF) //default is 20s
			.build();

	private static final RetryPolicy RETRY_POLICY =
		RetryPolicy.builder()
			.numRetries(4)
			.retryCondition(RetryCondition.defaultRetryCondition())
			.throttlingBackoffStrategy(BACKOFF_THROTTLING_STRATEGY)
			.build();

	public static SsmClient getClient() {
		return SsmClient.builder()
			.httpClient(LambdaWrapper.HTTP_CLIENT)
			.overrideConfiguration(ClientOverrideConfiguration.builder()
				.retryPolicy(RETRY_POLICY)
				.build())
			.build();
	}
}
