package com.amazonaws.ssm.association.translator;

import com.amazonaws.ssm.association.ResourceModel;
import com.amazonaws.ssm.association.translator.property.InstanceAssociationOutputLocationTranslator;
import com.amazonaws.ssm.association.translator.property.TargetsListTranslator;
import com.amazonaws.ssm.association.util.SimpleTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_ID;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_VERSION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.AUTOMATION_TARGET_PARAMETER_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.COMPLIANCE_SEVERITY;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_VERSION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.INSTANCE_ID;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.LAST_EXECUTION_DATE;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.LAST_SUCCESSFUL_EXECUTION_DATE;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.LAST_UPDATE_ASSOCIATION_DATE;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MAX_CONCURRENCY;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MAX_ERRORS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_TARGETS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.PARAMETERS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SCHEDULE_EXPRESSION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_TARGETS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SYNC_COMPLIANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssociationDescriptionTranslatorTest {

    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
    @Mock
    private TargetsListTranslator targetsListTranslator;

    private AssociationDescriptionTranslator associationDescriptionTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        associationDescriptionTranslator =
            new AssociationDescriptionTranslator(simpleTypeValidator,
                instanceAssociationOutputLocationTranslator,
                targetsListTranslator);
    }

    @Test
    void associationDescriptionToResourceModelWithAllPropertiesPresent() {
        when(instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(SERVICE_OUTPUT_LOCATION))
            .thenReturn(Optional.of(MODEL_OUTPUT_LOCATION));
        when(targetsListTranslator.serviceModelPropertyToResourceModel(SERVICE_TARGETS))
            .thenReturn(Optional.of(MODEL_TARGETS));

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .associationName(ASSOCIATION_NAME)
                .parameters(PARAMETERS)
                .targets(SERVICE_TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(SERVICE_OUTPUT_LOCATION)
                .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
                .instanceId(INSTANCE_ID)
                // parameter below are not present in the ResourceModel, they should get ignored during conversion
                .associationVersion(ASSOCIATION_VERSION)
                .lastUpdateAssociationDate(LAST_UPDATE_ASSOCIATION_DATE)
                .lastExecutionDate(LAST_EXECUTION_DATE)
                .lastSuccessfulExecutionDate(LAST_SUCCESSFUL_EXECUTION_DATE)
                .syncCompliance(SYNC_COMPLIANCE)
                .build();

        final ResourceModel resultModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription);

        final ResourceModel expectedModel =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .parameters(PARAMETERS)
                .targets(MODEL_TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(MODEL_OUTPUT_LOCATION)
                .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
                .instanceId(INSTANCE_ID)
                .syncCompliance(SYNC_COMPLIANCE)
                .build();

        assertThat(resultModel).isEqualTo(expectedModel);
    }

    @Test
    void associationDescriptionToResourceModelWithNoTargetsAndNoParametersSet() {
        when(instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(null))
            .thenReturn(Optional.empty());
        when(targetsListTranslator.serviceModelPropertyToResourceModel(Collections.emptyList()))
            .thenReturn(Optional.empty());

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .associationName(ASSOCIATION_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .build();

        final ResourceModel resultModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription);

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
        when(instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(null))
            .thenReturn(Optional.empty());
        when(targetsListTranslator.serviceModelPropertyToResourceModel(Collections.emptyList()))
            .thenReturn(Optional.empty());

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(ASSOCIATION_ID)
                .name(DOCUMENT_NAME)
                .associationName(ASSOCIATION_NAME)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .parameters(new HashMap<>())
                .targets(Collections.emptyList())
                .build();

        final ResourceModel resultModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription);

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
