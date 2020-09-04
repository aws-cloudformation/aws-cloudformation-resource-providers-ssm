package com.amazonaws.ssm.association;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Context object passed from CloudFormation calls to handlers.
 */
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackContext {
    /**
     * Number of seconds remaining for completion of resource stabilization.
     */
    private int remainingTimeoutSeconds;

    /**
     * Primary identifier of the resource, needed to find the resource in subsequent calls.
     */
    private String associationId;
}
