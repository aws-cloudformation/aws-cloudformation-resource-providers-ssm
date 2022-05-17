package com.amazonaws.ssm.document;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.annotation.Nullable;

@Data
@Builder
class ResourceInformation {

    @NonNull
    private final ResourceModel resourceModel;

    @NonNull
    private final ResourceStatus status;

    @Nullable
    private final String statusInformation;

    @Nullable
    private final String latestVersion;

    @Nullable
    private final String defaultVersion;
}
