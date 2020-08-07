package com.amazonaws.ssm.association.util;

import com.amazonaws.ssm.association.ResourceModel;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceHandlerRequestToStringConverterTest {

    private static final String DESIRED_RESOURCE_STATE_ASSOCIATION_ID = "testAssociationId";
    private static final String PREVIOUS_RESOURCE_STATE_ASSOCIATION_ID = "testAssociationIdTwo";
    private static final String DESIRED_RESOURCE_STATE_STRING =
        String.format("ResourceModel(associationId=%s)", DESIRED_RESOURCE_STATE_ASSOCIATION_ID);
    private static final String PREVIOUS_RESOURCE_STATE_STRING =
        String.format("ResourceModel(associationId=%s)", PREVIOUS_RESOURCE_STATE_ASSOCIATION_ID);

    private static final String CLIENT_REQUEST_TOKEN = "testClientRequestToken";
    private static final ResourceModel DESIRED_RESOURCE_STATE = ResourceModel.builder().associationId(DESIRED_RESOURCE_STATE_ASSOCIATION_ID).build();
    private static final ResourceModel PREVIOUS_RESOURCE_STATE = ResourceModel.builder().associationId(PREVIOUS_RESOURCE_STATE_ASSOCIATION_ID).build();
    private static final ImmutableMap<String, String> RESOURCE_TAGS = ImmutableMap.of("ResourceTag", "ResourceTagValue");
    private static final ImmutableMap<String, String> SYSTEM_TAGS = ImmutableMap.of("SystemTag", "SystemTagValue");
    private static final String AWS_ACCOUNT_ID = "testAwsAccountId";
    private static final String AWS_PARTITION = "testAwsPartition";
    private static final String LOGICAL_RESOURCE_IDENTIFIER = "abcdef-12345-ghijk";
    private static final String NEXT_TOKEN = "testNextToken";
    private static final String REGION = "us-east-1";

    private static final ResourceHandlerRequest<ResourceModel> REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
        .clientRequestToken(CLIENT_REQUEST_TOKEN)
        .desiredResourceState(DESIRED_RESOURCE_STATE)
        .previousResourceState(PREVIOUS_RESOURCE_STATE)
        .desiredResourceTags(RESOURCE_TAGS)
        .systemTags(SYSTEM_TAGS)
        .awsAccountId(AWS_ACCOUNT_ID)
        .awsPartition(AWS_PARTITION)
        .logicalResourceIdentifier(LOGICAL_RESOURCE_IDENTIFIER)
        .nextToken(NEXT_TOKEN)
        .region(REGION)
        .build();

    @Mock
    private ResourceModelToStringConverter resourceModelToStringConverter;

    private ResourceHandlerRequestToStringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ResourceHandlerRequestToStringConverter(resourceModelToStringConverter);
    }

    @Test
    void convertNullRequestReturnsNull() {
        final String result = converter.convert(null);

        assertEquals("null", result);
    }

    @Test
    void convertRequestReturnsRequestParameters() {
        when(resourceModelToStringConverter.convert(DESIRED_RESOURCE_STATE))
            .thenReturn(DESIRED_RESOURCE_STATE_STRING);
        when(resourceModelToStringConverter.convert(PREVIOUS_RESOURCE_STATE))
            .thenReturn(PREVIOUS_RESOURCE_STATE_STRING);

        final String result = converter.convert(REQUEST);

        assertTrue(result.contains(String.format("clientRequestToken=%s", CLIENT_REQUEST_TOKEN)));
        assertTrue(result.contains(String.format("desiredResourceState=%s", DESIRED_RESOURCE_STATE_STRING)));
        assertTrue(result.contains(String.format("previousResourceState=%s", PREVIOUS_RESOURCE_STATE_STRING)));
        assertTrue(result.contains(String.format("desiredResourceTags=%s", RESOURCE_TAGS)));
        assertTrue(result.contains(String.format("systemTags=%s", SYSTEM_TAGS)));
        assertTrue(result.contains(String.format("awsAccountId=%s", AWS_ACCOUNT_ID)));
        assertTrue(result.contains(String.format("awsPartition=%s", AWS_PARTITION)));
        assertTrue(result.contains(String.format("logicalResourceIdentifier=%s", LOGICAL_RESOURCE_IDENTIFIER)));
        assertTrue(result.contains(String.format("nextToken=%s", NEXT_TOKEN)));
        assertTrue(result.contains(String.format("region=%s", REGION)));
    }

    @Test
    void convertRequestUsesResourceModelConverter() {
        when(resourceModelToStringConverter.convert(DESIRED_RESOURCE_STATE))
            .thenReturn(DESIRED_RESOURCE_STATE_STRING);
        when(resourceModelToStringConverter.convert(PREVIOUS_RESOURCE_STATE))
            .thenReturn(PREVIOUS_RESOURCE_STATE_STRING);

        converter.convert(REQUEST);

        verify(resourceModelToStringConverter).convert(DESIRED_RESOURCE_STATE);
        verify(resourceModelToStringConverter).convert(PREVIOUS_RESOURCE_STATE);
    }
}
