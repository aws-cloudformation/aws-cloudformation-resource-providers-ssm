package com.amazonaws.ssm.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
public class CallbackContext {

    private Boolean createDocumentStarted;

    private Boolean eventStarted;

    private Integer stabilizationRetriesRemaining;

    @JsonIgnore
    public void decrementStabilizationRetriesRemaining() {
        stabilizationRetriesRemaining--;
    }
}
