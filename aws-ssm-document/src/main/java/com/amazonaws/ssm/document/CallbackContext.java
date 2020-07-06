package com.amazonaws.ssm.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
@EqualsAndHashCode
public class CallbackContext {

    /**
     * Boolean that tells whether the create operation on resource started.
     */
    private Boolean createDocumentStarted;

    /**
     * Boolean that tells whether the operation on resource started.
     */
    private Boolean eventStarted;

    private Integer stabilizationRetriesRemaining;

    @JsonIgnore
    public void decrementStabilizationRetriesRemaining() {
        stabilizationRetriesRemaining--;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CallbackContextBuilder {
    }
}
