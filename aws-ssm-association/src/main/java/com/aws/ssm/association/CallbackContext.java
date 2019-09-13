package com.aws.ssm.association;

import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallbackContext {
    private Integer stabilizationRetriesRemaining;
    private AssociationDescription associationDescription;
}
