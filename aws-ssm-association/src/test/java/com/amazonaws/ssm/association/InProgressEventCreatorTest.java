package com.amazonaws.ssm.association;

import org.junit.jupiter.api.Test;
import software.amazon.cloudformation.proxy.ProgressEvent;

import static org.assertj.core.api.Assertions.assertThat;

class InProgressEventCreatorTest {

    private static final int CALLBACK_DELAY_SECONDS = 15;

    private final InProgressEventCreator inProgressEventCreator = new InProgressEventCreator();

    @Test
    void nextInProgressEventWithRemainingTimeoutGreaterThanCallbackDelayDecreasesRemainingTimeByCallbackDelay() {
        final int remainingTimeoutSeconds = 35;
        final ResourceModel latestModel = ResourceModel.builder()
            .associationId(TestsInputs.ASSOCIATION_ID)
            .name(TestsInputs.DOCUMENT_NAME)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> nextInProgressEvent =
            inProgressEventCreator.nextInProgressEvent(remainingTimeoutSeconds, latestModel);

        final CallbackContext expectedCallbackContext =
            CallbackContext.builder()
                .remainingTimeoutSeconds(remainingTimeoutSeconds - CALLBACK_DELAY_SECONDS)
                .associationId(latestModel.getAssociationId())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(expectedCallbackContext, CALLBACK_DELAY_SECONDS, latestModel);

        assertThat(nextInProgressEvent).isEqualTo(expectedProgressEvent);
    }

    @Test
    void nextInProgressEventWithRemainingTimeoutSmallerThanCallbackDelayReturnsRemainingTimeAsZeroAndCallbackAsRemainingTime() {
        final int remainingTimeoutSeconds = 5;
        final ResourceModel latestModel = ResourceModel.builder()
            .associationId(TestsInputs.ASSOCIATION_ID)
            .name(TestsInputs.DOCUMENT_NAME)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> nextInProgressEvent =
            inProgressEventCreator.nextInProgressEvent(remainingTimeoutSeconds, latestModel);

        final CallbackContext expectedCallbackContext =
            CallbackContext.builder()
                .remainingTimeoutSeconds(0)
                .associationId(latestModel.getAssociationId())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultInProgressHandler(expectedCallbackContext, remainingTimeoutSeconds, latestModel);

        assertThat(nextInProgressEvent).isEqualTo(expectedProgressEvent);
    }
}
