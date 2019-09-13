package com.aws.ssm.association.util;

import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.aws.ssm.association.InstanceAssociationOutputLocation;
import com.aws.ssm.association.ResourceModel;
import com.aws.ssm.association.S3OutputLocation;
import com.aws.ssm.association.Target;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceModelServiceModelConverterTest {

    private static final String DOCUMENT_NAME = "NewTestDocument";
    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String ASSOCIATION_NAME = "TestAssociation";
    private static final String ASSOCIATION_VERSION = "3";
    private static final String DOCUMENT_VERSION = "2";
    private static final String SCHEDULE_EXPRESSION = "rate(30)";
    private static final String COMPLIANCE_SEVERITY = "CRITICAL";
    private static final String MAX_CONCURRENCY = "50%";
    private static final String MAX_ERRORS = "10%";
    private static final String INSTANCE_ID = "i-1234abcd";
    private static final String AUTOMATION_TARGET_PARAMETER_NAME = "InstanceId";
    private static final Date LAST_UPDATE_ASSOCIATION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 9, 11, 50).toInstant(ZoneOffset.UTC));
    private static final Date LAST_EXECUTION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 10, 12, 0).toInstant(ZoneOffset.UTC));
    private static final Date LAST_SUCCESSFUL_EXECUTION_DATE =
        Date.from(LocalDateTime.of(2019, 9, 10, 12, 0).toInstant(ZoneOffset.UTC));

    private static final String S3_BUCKET_REGION = "us-east-1";
    private static final String S3_BUCKET_NAME = "test-bucket";
    private static final String S3_KEY_PREFIX = "test-association-output-location";
    private static final InstanceAssociationOutputLocation OUTPUT_LOCATION =
        new InstanceAssociationOutputLocation(
            new S3OutputLocation(S3_BUCKET_REGION, S3_BUCKET_NAME, S3_KEY_PREFIX));

    private static final String TARGET_KEY = "tag:domain";
    private static final String TARGET_VALUE = "test";
    private static final List<Target> TARGETS =
        Collections.singletonList(
            new Target(TARGET_KEY, Collections.singletonList(TARGET_VALUE)));

    private static final Map<String, List<String>> PARAMETERS =
        ImmutableMap.<String, List<String>>builder()
            .put("command", Collections.singletonList("echo 'hello world'"))
            .build();

    @Test
    void associationDescriptionToResourceModelWithAllModelParametersPresent() {
        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withDocumentVersion(DOCUMENT_VERSION)
                .withAssociationName(ASSOCIATION_NAME)
                .withParameters(PARAMETERS)
                .withTargets(Collections.singletonList(
                    new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(TARGET_KEY)
                        .withValues(TARGET_VALUE)))
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withComplianceSeverity(COMPLIANCE_SEVERITY)
                .withMaxConcurrency(MAX_CONCURRENCY)
                .withMaxErrors(MAX_ERRORS)
                .withOutputLocation(new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(
                        new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                            .withOutputS3Region(S3_BUCKET_REGION)
                            .withOutputS3BucketName(S3_BUCKET_NAME)
                            .withOutputS3KeyPrefix(S3_KEY_PREFIX)))
                .withAutomationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
                .withInstanceId(INSTANCE_ID)
                // parameter below are not present in the ResourceModel, they should get ignored during conversion
                .withAssociationVersion(ASSOCIATION_VERSION)
                .withLastUpdateAssociationDate(LAST_UPDATE_ASSOCIATION_DATE)
                .withLastExecutionDate(LAST_EXECUTION_DATE)
                .withLastSuccessfulExecutionDate(LAST_SUCCESSFUL_EXECUTION_DATE);

        final ResourceModel resultModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(associationDescription);

        final ResourceModel expectedModel =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .parameters(PARAMETERS)
                .targets(TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(OUTPUT_LOCATION)
                .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
                .instanceId(INSTANCE_ID)
                .build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void associationDescriptionToResourceModelWithNoTargetsAndNoParametersSet() {
        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withScheduleExpression(SCHEDULE_EXPRESSION);

        final ResourceModel resultModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(associationDescription);

        final ResourceModel expectedModel =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void associationDescriptionToResourceModelWithEmptyTargetsAndParametersSet() {
        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withParameters(new HashMap<>())
                .withTargets(Collections.emptyList());

        final ResourceModel resultModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(associationDescription);

        final ResourceModel expectedModel =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void associationDescriptionToResourceModelWithS3OutputLocationMissingFromInstanceAssociationOutputLocation() {
        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME)
                .withAssociationName(ASSOCIATION_NAME)
                .withScheduleExpression(SCHEDULE_EXPRESSION)
                .withOutputLocation(new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation());

        final ResourceModel resultModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(associationDescription);

        final ResourceModel expectedModel =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }
}