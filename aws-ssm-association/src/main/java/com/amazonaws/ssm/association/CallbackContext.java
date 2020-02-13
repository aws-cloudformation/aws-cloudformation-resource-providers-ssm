package com.amazonaws.ssm.association;

import lombok.Builder;
import lombok.Value;

/**
 * Context object passed from CloudFormation calls to handlers.
 */
@Value
@Builder
public class CallbackContext {
    /**
     * Number of seconds remaining for completion of resource stabilization.
     */
    private final int remainingTimeoutSeconds;

    /**
     * Primary identifier of the resource, needed to find the resource in subsequent calls.
     */
    private final String associationId;
}
