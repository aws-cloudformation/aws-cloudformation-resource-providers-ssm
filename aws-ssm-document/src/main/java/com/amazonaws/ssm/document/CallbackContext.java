package com.amazonaws.ssm.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
public class CallbackContext {
    private Boolean createDocumentStarted;

    private Integer stabilizationRetriesRemaining;

    @JsonIgnore
    public void decrementStabilizationRetriesRemaining() {
        stabilizationRetriesRemaining--;
    }
}
