package com.amazonaws.ssm.document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class GetProgressResponse {
    private final ResourceModel resourceModel;

    private final CallbackContext callbackContext;
}
