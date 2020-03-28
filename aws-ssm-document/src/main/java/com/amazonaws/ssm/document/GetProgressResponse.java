package com.amazonaws.ssm.document;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class GetProgressResponse {

    @NonNull
    private final ResourceInformation resourceInformation;

    @NonNull
    private final CallbackContext callbackContext;
}
