package com.aws.ssm.association;

import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ProgressEvent;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yngfn
 * @date 05/15/2019
 */
public class Utils {
    public static ProgressEvent defaultFailureHandler(final ProgressEvent progressEvent, final Exception e, final HandlerErrorCode handlerErrorCode) {
        progressEvent.setMessage(e.getMessage());
        progressEvent.setStatus(OperationStatus.FAILED);
        progressEvent.setErrorCode(handlerErrorCode);

        return progressEvent;
    }

    public static ProgressEvent defaultSuccessHandler(final ProgressEvent progressEvent, final ResourceModel resourceModel) {
        progressEvent.setStatus(OperationStatus.SUCCESS);
        progressEvent.setResourceModel(resourceModel);

        return progressEvent;
    }

    public static Map<String, List<String>> getMapFromParameters(final Map<String, ParameterValues> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, pv -> pv.getValue().getParameterValues()));
    }

    public static software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation translateInstanceAssociationOutputLocation(final InstanceAssociationOutputLocation instanceAssociationOutputLocation) {
        if(instanceAssociationOutputLocation == null) {
            return null;
        }
        return software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation
                .builder()
                .s3Location(translateS3OutputLocation(instanceAssociationOutputLocation.getS3Location()))
                .build();
    }

    public static Collection<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation> translateInstanceAssociationOutputLocationCollection(final Collection<InstanceAssociationOutputLocation> instanceAssociationOutputLocationCollection) {
        if(instanceAssociationOutputLocationCollection == null) {
            return null;
        }
        return Collections2.transform(instanceAssociationOutputLocationCollection, Utils::translateInstanceAssociationOutputLocation);
    }

    public static software.amazon.awssdk.services.ssm.model.S3OutputLocation translateS3OutputLocation(final S3OutputLocation s3OutputLocation) {
        if(s3OutputLocation == null) {
            return null;
        }
        return software.amazon.awssdk.services.ssm.model.S3OutputLocation
                .builder()
                .outputS3BucketName(s3OutputLocation.getOutputS3BucketName())
                .outputS3KeyPrefix(s3OutputLocation.getOutputS3KeyPrefix())
                .build();
    }

    public static Collection<software.amazon.awssdk.services.ssm.model.S3OutputLocation> translateS3OutputLocationCollection(final Collection<S3OutputLocation> s3OutputLocationCollection) {
        if(s3OutputLocationCollection == null) return null;
        return Collections2.transform(s3OutputLocationCollection, Utils::translateS3OutputLocation);
    }

    public static software.amazon.awssdk.services.ssm.model.Target translateTarget(final Target target) {
        if(target == null) {
            return null;
        }
        return software.amazon.awssdk.services.ssm.model.Target
                .builder()
                .key(target.getKey())
                .values(target.getValues())
                .build();
    }

    public static Collection<software.amazon.awssdk.services.ssm.model.Target> translateTargetCollection(final Collection<Target> targetCollection) {
        if(targetCollection == null) {
            return null;
        }
        return Collections2.transform(targetCollection, Utils::translateTarget);
    }
}
