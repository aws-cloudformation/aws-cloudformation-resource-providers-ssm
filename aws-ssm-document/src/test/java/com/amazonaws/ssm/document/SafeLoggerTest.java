package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.Logger;

@ExtendWith(MockitoExtension.class)
public class SafeLoggerTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_ACCOUNT_ID = "123456";
    private static final String SAMPLE_DOCUMENT_CONTENT_STRING = "sampleDocumentContent";
    private static final Map<String, Object> SAMPLE_DOCUMENT_CONTENT = ImmutableMap.of(
        "schemaVersion", "1.2",
        "description", "Join instances to an AWS Directory Service domain."
    );
    private static final ResourceModel SAMPLE_RESOURCE_MODEL = ResourceModel.builder()
        .name(SAMPLE_DOCUMENT_NAME)
        .content(SAMPLE_DOCUMENT_CONTENT)
        .documentType("Command")
        .documentFormat("JSON")
        .build();

    private static final CallbackContext SAMPLE_CALLBACK_CONTEXT = CallbackContext.builder()
        .createDocumentStarted(true)
        .stabilizationRetriesRemaining(20)
        .build();

    @Mock
    private Logger logger;

    private SafeLogger unitUnderTest;

    @BeforeEach
    public void setup() {
        unitUnderTest = new SafeLogger();
    }

    @Test
    public void verifyTest() {
        unitUnderTest.safeLogDocumentInformation(SAMPLE_RESOURCE_MODEL, SAMPLE_CALLBACK_CONTEXT, SAMPLE_ACCOUNT_ID, logger);

        Mockito.verify(logger).log("customerAccountId: 123456");
        Mockito.verify(logger).log("DocumentInfo: documentName=sampleDocument, documentType=Command, documentFormat=JSON");
        Mockito.verify(logger).log("callbackContext: " + SAMPLE_CALLBACK_CONTEXT);
    }
}
