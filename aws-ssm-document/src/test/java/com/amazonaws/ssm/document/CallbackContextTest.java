package com.amazonaws.ssm.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CallbackContextTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testSerializeAndDeserialize() throws JsonProcessingException {
        final CallbackContext context = CallbackContext.builder()
            .stabilizationRetriesRemaining(30)
            .createDocumentStarted(false)
            .eventStarted(true)
            .build();

        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final String serializedText = OBJECT_MAPPER.writeValueAsString(context);

        Assertions.assertEquals(context, OBJECT_MAPPER.readValue(serializedText, CallbackContext.class));
    }
}
