package com.aws.ssm.association.util;

import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.aws.ssm.association.InstanceAssociationOutputLocation;
import com.aws.ssm.association.ResourceModel;
import com.aws.ssm.association.S3OutputLocation;
import com.aws.ssm.association.Target;
import com.amazonaws.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for converting between ResourceModel and service model object types.
 */
public class ResourceModelServiceModelConverter {

    /**
     * Converts State Manager's AssociationDescription service object to ResourceModel.
     *
     * @param association AssociationDescription representing association to convert to the model.
     * @return ResourceModel representation of the association.
     */
    public static ResourceModel associationDescriptionToResourceModel(
        final AssociationDescription association) {

        final ResourceModel model = new ResourceModel();

        model.setAssociationId(association.getAssociationId());
        model.setName(association.getName());

        if (!StringUtils.isNullOrEmpty(association.getAssociationName())) {
            model.setAssociationName(association.getAssociationName());
        }

        if (!StringUtils.isNullOrEmpty(association.getDocumentVersion())) {
            model.setDocumentVersion(association.getDocumentVersion());
        }

        if (!StringUtils.isNullOrEmpty(association.getInstanceId())) {
            model.setInstanceId(association.getInstanceId());
        }

        if (MapUtils.isNotEmpty(association.getParameters())) {
            model.setParameters(association.getParameters());
        }

        if (!StringUtils.isNullOrEmpty(association.getScheduleExpression())) {
            model.setScheduleExpression(association.getScheduleExpression());
        }

        if (CollectionUtils.isNotEmpty(association.getTargets())) {
            final List<Target> convertedTargets =
                association.getTargets().stream()
                    .map(associationTarget -> {
                        Target resourceModelTarget = new Target();
                        resourceModelTarget.setKey(associationTarget.getKey());
                        resourceModelTarget.setValues(associationTarget.getValues());
                        return resourceModelTarget;
                    })
                    .collect(Collectors.toList());

            model.setTargets(convertedTargets);
        }

        final com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation serviceModelOutputLocation =
            association.getOutputLocation();

        if (serviceModelOutputLocation != null
            && serviceModelOutputLocation.getS3Location() != null) {

            final com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation s3Location =
                serviceModelOutputLocation.getS3Location();

            final S3OutputLocation resourceModelS3Location = new S3OutputLocation();

            resourceModelS3Location.setOutputS3BucketName(s3Location.getOutputS3BucketName());
            resourceModelS3Location.setOutputS3Region(s3Location.getOutputS3Region());
            resourceModelS3Location.setOutputS3KeyPrefix(s3Location.getOutputS3KeyPrefix());

            final InstanceAssociationOutputLocation resourceModelOutputLocation = new InstanceAssociationOutputLocation();

            resourceModelOutputLocation.setS3Location(resourceModelS3Location);

            model.setOutputLocation(resourceModelOutputLocation);
        }

        if (!StringUtils.isNullOrEmpty(association.getAutomationTargetParameterName())) {
            model.setAutomationTargetParameterName(association.getAutomationTargetParameterName());
        }

        if (!StringUtils.isNullOrEmpty(association.getMaxErrors())) {
            model.setMaxErrors(association.getMaxErrors());
        }

        if (!StringUtils.isNullOrEmpty(association.getMaxConcurrency())) {
            model.setMaxConcurrency(association.getMaxConcurrency());
        }

        if (!StringUtils.isNullOrEmpty(association.getComplianceSeverity())) {
            model.setComplianceSeverity(association.getComplianceSeverity());
        }

        return model;
    }
}
