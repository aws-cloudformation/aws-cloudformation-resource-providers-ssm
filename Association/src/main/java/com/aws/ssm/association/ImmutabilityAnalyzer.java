package com.aws.ssm.association;

import com.google.common.base.Objects;

/**
 * @author yngfn
 * @date 05/15/2019
 */
public class ImmutabilityAnalyzer {

    public static final boolean isImmutableChange(final ResourceModel previous, final ResourceModel current) {
        return !Objects.equal(previous.getName(), current.getName()) ||
                !Objects.equal(previous.getInstanceId(), current.getInstanceId()) ||
                !Objects.equal(previous.getTargets(), current.getTargets());
    }
}
