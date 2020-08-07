package com.amazonaws.ssm.association;

import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

/**
 * Creates InProgress {@link ProgressEvent} objects for use in handlers requiring progress chaining.
 */
public class InProgressEventCreator {

    /**
     * How many seconds later should the callback happen for the ProgressEvent created here.
     */
    private static final int CALLBACK_DELAY_SECONDS = 15;

    /**
     * Provides the next {@link OperationStatus#IN_PROGRESS} status {@link ProgressEvent} object with a callback context.
     * If the provided remaining timeout is smaller than the {@link InProgressEventCreator#CALLBACK_DELAY_SECONDS}, we:
     *
     *      1. Set the remaining timeout on the callback context to 0. This will be the last callback before timeout.
     *      2. Set the callback delay on the ProgressEvent to the remaining timeout (instead of
     *      {@link InProgressEventCreator#CALLBACK_DELAY_SECONDS}, in order to honor the customer timeout.
     *
     * @param remainingTimeoutSeconds Number of seconds remaining before progress chaining timeout.
     * @param latestModel Resource model to use for the ProgressEvent.
     *
     * @return ProgressEvent with the latest resource model and updated remaining timeout values.
     */
    public ProgressEvent<ResourceModel, CallbackContext> nextInProgressEvent(final int remainingTimeoutSeconds,
                                                                             final ResourceModel latestModel) {
        final String associationId = latestModel.getAssociationId();

        if (remainingTimeoutSeconds < CALLBACK_DELAY_SECONDS) {

            return ProgressEvent.defaultInProgressHandler(
                CallbackContext.builder()
                    .remainingTimeoutSeconds(0)
                    .associationId(associationId)
                    .build(),
                remainingTimeoutSeconds,
                latestModel);
        } else {
            return ProgressEvent.defaultInProgressHandler(
                CallbackContext.builder()
                    .remainingTimeoutSeconds(remainingTimeoutSeconds - CALLBACK_DELAY_SECONDS)
                    .associationId(associationId)
                    .build(),
                CALLBACK_DELAY_SECONDS,
                latestModel);
        }
    }
}
