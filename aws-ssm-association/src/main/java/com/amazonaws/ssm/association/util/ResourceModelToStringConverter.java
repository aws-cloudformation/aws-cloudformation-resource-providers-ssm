package com.amazonaws.ssm.association.util;

import com.amazonaws.ssm.association.ResourceModel;

/**
 * Used for safe printing of {@link ResourceModel} objects.
 */
public class ResourceModelToStringConverter {

    /**
     * Converts ResourceModel objects into Strings that are safe to store. No confidential data will be returned.
     *
     * @param resourceModel Model object that needs to be converted into String.
     * @return String representation of the model object that is safe to store.
     */
    public String convert(final ResourceModel resourceModel) {
        if (resourceModel == null) {
            return "null";
        }

        return String.format("ResourceModel(" +
                "associationId=%s," +
                "associationName=%s," +
                "documentVersion=%s," +
                "instanceId=%s," +
                "name=%s," +
                "scheduleExpression=%s," +
                "targets=%s," +
                "outputLocation=%s," +
                "automationTargetParameterName=%s," +
                "maxErrors=%s," +
                "maxConcurrency=%s," +
                "complianceSeverity=%s," +
                "syncCompliance=%s," +
                "waitForSuccessTimeoutSeconds=%s" +
                ")",
            resourceModel.getAssociationId(),
            resourceModel.getAssociationName(),
            resourceModel.getDocumentVersion(),
            resourceModel.getInstanceId(),
            resourceModel.getName(),
            resourceModel.getScheduleExpression(),
            resourceModel.getTargets(),
            resourceModel.getOutputLocation(),
            resourceModel.getAutomationTargetParameterName(),
            resourceModel.getMaxErrors(),
            resourceModel.getMaxConcurrency(),
            resourceModel.getComplianceSeverity(),
            resourceModel.getSyncCompliance(),
            resourceModel.getWaitForSuccessTimeoutSeconds()
        );
    }
}
