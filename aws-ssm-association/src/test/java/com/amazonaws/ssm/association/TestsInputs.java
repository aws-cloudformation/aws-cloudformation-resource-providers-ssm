package com.amazonaws.ssm.association;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestsInputs {
    public static final String DOCUMENT_NAME = "NewTestDocument";
    public static final String ASSOCIATION_ID = "test-12345-associationId";
    public static final String ASSOCIATION_NAME = "TestAssociation";
    public static final String ASSOCIATION_VERSION = "3";
    public static final String DOCUMENT_VERSION = "2";
    public static final String SCHEDULE_EXPRESSION = "rate(30)";
    public static final String SYNC_COMPLIANCE = "MANUAL";
    public static final String COMPLIANCE_SEVERITY = "CRITICAL";
    public static final String MAX_CONCURRENCY = "50%";
    public static final String MAX_ERRORS = "10%";
    public static final String INSTANCE_ID = "i-1234abcd";
    public static final String AUTOMATION_TARGET_PARAMETER_NAME = "InstanceId";
    public static final Instant LAST_UPDATE_ASSOCIATION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 9, 11, 50).toInstant(ZoneOffset.UTC))
            .toInstant();
    public static final Instant LAST_EXECUTION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 10, 12, 0).toInstant(ZoneOffset.UTC))
            .toInstant();
    public static final Instant LAST_SUCCESSFUL_EXECUTION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 10, 12, 0).toInstant(ZoneOffset.UTC))
            .toInstant();

    public static final Map<String, List<String>> PARAMETERS =
        ImmutableMap.<String, List<String>>builder()
            .put("command", Collections.singletonList("echo 'hello world'"))
            .build();

    public static final String S3_BUCKET_REGION = "us-east-1";
    public static final String S3_BUCKET_NAME = "test-bucket";
    public static final String S3_KEY_PREFIX = "test-association-output-location";
    public static final InstanceAssociationOutputLocation MODEL_OUTPUT_LOCATION =
        new InstanceAssociationOutputLocation(
            new S3OutputLocation(S3_BUCKET_REGION, S3_BUCKET_NAME, S3_KEY_PREFIX));
    public static final software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation SERVICE_OUTPUT_LOCATION =
        software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation.builder()
            .s3Location(
                software.amazon.awssdk.services.ssm.model.S3OutputLocation.builder()
                    .outputS3Region(S3_BUCKET_REGION)
                    .outputS3BucketName(S3_BUCKET_NAME)
                    .outputS3KeyPrefix(S3_KEY_PREFIX)
                    .build())
            .build();

    public static final String TARGET_KEY = "tag:domain";
    public static final String TARGET_VALUE = "test";
    public static final List<Target> MODEL_TARGETS =
        Collections.singletonList(
            new Target(TARGET_KEY, Collections.singletonList(TARGET_VALUE)));
    public static final List<software.amazon.awssdk.services.ssm.model.Target> SERVICE_TARGETS =
        Collections.singletonList(
            software.amazon.awssdk.services.ssm.model.Target.builder()
                .key(TARGET_KEY)
                .values(TARGET_VALUE)
                .build());

    public static final int WAIT_FOR_SUCCESS_TIMEOUT_IN_SECONDS = 45;

    public static final String LOGGED_RESOURCE_HANDLER_REQUEST = "StringifiedResourceHandlerRequest";
}
