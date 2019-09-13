package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.InstanceAssociationOutputLocation;
import com.amazonaws.ssm.association.S3OutputLocation;

import java.util.Optional;

/**
 * Property translator for the InstanceAssociationOutputLocation property.
 */
public class InstanceAssociationOutputLocationTranslator
    implements PropertyTranslator<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation, InstanceAssociationOutputLocation> {

    /**
     * Gets ResourceModel InstanceAssociationOutputLocation from service model OutputLocation.
     *
     * @param serviceModelOutputLocation Service model InstanceAssociationOutputLocation to convert to
     * ResourceModel InstanceAssociationOutputLocation.
     * @return Optional with ResourceModel InstanceAssociationOutputLocation converted from the service model;
     * returns Optional.empty() when the service model output location is empty/null.
     */
    @Override
    public Optional<InstanceAssociationOutputLocation> serviceModelPropertyToResourceModel(
        final software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation serviceModelOutputLocation) {

        if (serviceModelOutputLocation != null
            && serviceModelOutputLocation.s3Location() != null) {

            final software.amazon.awssdk.services.ssm.model.S3OutputLocation s3Location =
                serviceModelOutputLocation.s3Location();

            final S3OutputLocation resourceModelS3Location =
                S3OutputLocation.builder()
                    .outputS3BucketName(s3Location.outputS3BucketName())
                    .outputS3Region(s3Location.outputS3Region())
                    .outputS3KeyPrefix(s3Location.outputS3KeyPrefix())
                    .build();

            return Optional.of(
                InstanceAssociationOutputLocation.builder()
                    .s3Location(resourceModelS3Location)
                    .build());
        }

        return Optional.empty();
    }

    /**
     * Gets service model InstanceAssociationOutputLocation from ResourceModel OutputLocation.
     *
     * @param resourceModelOutputLocation ResourceModel InstanceAssociationOutputLocation to convert to
     * service model InstanceAssociationOutputLocation.
     * @return Optional with service model InstanceAssociationOutputLocation converted from the ResourceModel;
     * returns Optional.empty() when the ResourceModel output location is empty/null.
     */
    @Override
    public Optional<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation> resourceModelPropertyToServiceModel(
        final InstanceAssociationOutputLocation resourceModelOutputLocation) {

        if (resourceModelOutputLocation != null
            && resourceModelOutputLocation.getS3Location() != null) {

            final S3OutputLocation s3Location = resourceModelOutputLocation.getS3Location();

            final software.amazon.awssdk.services.ssm.model.S3OutputLocation serviceModelS3Location =
                software.amazon.awssdk.services.ssm.model.S3OutputLocation.builder()
                    .outputS3BucketName(s3Location.getOutputS3BucketName())
                    .outputS3Region(s3Location.getOutputS3Region())
                    .outputS3KeyPrefix(s3Location.getOutputS3KeyPrefix())
                    .build();

            return Optional.of(
                software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation.builder()
                    .s3Location(serviceModelS3Location)
                    .build());

        }

        return Optional.empty();
    }
}
