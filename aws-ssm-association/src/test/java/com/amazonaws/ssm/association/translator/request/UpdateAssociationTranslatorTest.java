package com.amazonaws.ssm.association.translator.request;

import com.amazonaws.ssm.association.ResourceModel;
import com.amazonaws.ssm.association.translator.property.InstanceAssociationOutputLocationTranslator;
import com.amazonaws.ssm.association.translator.property.TargetsListTranslator;
import com.amazonaws.ssm.association.util.SimpleTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;

import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_ID;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.AUTOMATION_TARGET_PARAMETER_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.COMPLIANCE_SEVERITY;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_VERSION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MAX_CONCURRENCY;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MAX_ERRORS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_TARGETS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.PARAMETERS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SCHEDULE_EXPRESSION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_TARGETS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAssociationTranslatorTest {

    @Mock
    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
    @Mock
    private TargetsListTranslator targetsListTranslator;

    private UpdateAssociationTranslator updateAssociationTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        updateAssociationTranslator =
            new UpdateAssociationTranslator(simpleTypeValidator,
                instanceAssociationOutputLocationTranslator,
                targetsListTranslator);
    }

    @Test
    void resourceModelToRequestWithAllModelParametersPresent() {
        when(instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(MODEL_OUTPUT_LOCATION))
            .thenReturn(Optional.of(SERVICE_OUTPUT_LOCATION));
        when(targetsListTranslator.resourceModelPropertyToServiceModel(MODEL_TARGETS))
            .thenReturn(Optional.of(SERVICE_TARGETS));

        final ResourceModel modelToTranslate =
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
                .build();

        final UpdateAssociationRequest createAssociationRequest =
            updateAssociationTranslator.resourceModelToRequest(modelToTranslate);

        final UpdateAssociationRequest expectedRequest =
            UpdateAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .documentVersion(DOCUMENT_VERSION)
                .parameters(PARAMETERS)
                .targets(SERVICE_TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors(MAX_ERRORS)
                .outputLocation(SERVICE_OUTPUT_LOCATION)
                .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
                .build();

        assertThat(createAssociationRequest).isEqualTo(expectedRequest);
    }

    @Test
    void resourceModelToRequestWithSomeModelParametersMissingOrNull() {
        when(instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(null))
            .thenReturn(Optional.empty());
        when(targetsListTranslator.resourceModelPropertyToServiceModel(MODEL_TARGETS))
            .thenReturn(Optional.of(SERVICE_TARGETS));

        final ResourceModel modelToTranslate =
            ResourceModel.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .parameters(PARAMETERS)
                .targets(MODEL_TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .maxErrors("")
                .outputLocation(null)
                .build();

        final UpdateAssociationRequest createAssociationRequest =
            updateAssociationTranslator.resourceModelToRequest(modelToTranslate);

        final UpdateAssociationRequest expectedRequest =
            UpdateAssociationRequest.builder()
                .associationId(ASSOCIATION_ID)
                .associationName(ASSOCIATION_NAME)
                .name(DOCUMENT_NAME)
                .parameters(PARAMETERS)
                .targets(SERVICE_TARGETS)
                .scheduleExpression(SCHEDULE_EXPRESSION)
                .complianceSeverity(COMPLIANCE_SEVERITY)
                .maxConcurrency(MAX_CONCURRENCY)
                .build();

        assertThat(createAssociationRequest).isEqualTo(expectedRequest);
    }
}
