package com.amazonaws.ssm.association.translator;

import com.amazonaws.ssm.association.ResourceModel;
import software.amazon.awssdk.services.ssm.model.AssociationAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.AssociationDoesNotExistException;
import software.amazon.awssdk.services.ssm.model.AssociationLimitExceededException;
import software.amazon.awssdk.services.ssm.model.AssociationVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidAssociationVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidInstanceIdException;
import software.amazon.awssdk.services.ssm.model.InvalidOutputLocationException;
import software.amazon.awssdk.services.ssm.model.InvalidParametersException;
import software.amazon.awssdk.services.ssm.model.InvalidScheduleException;
import software.amazon.awssdk.services.ssm.model.InvalidTargetException;
import software.amazon.awssdk.services.ssm.model.SsmRequest;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.UnsupportedPlatformTypeException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

/**
 * Translator between service model exceptions and CloudFormation exceptions.
 */
public class ExceptionTranslator {

    /**
     * Translates service model exceptions to CloudFormation exceptions.
     *
     * @param serviceException Service model exception to translate.
     * @param request Type of SsmRequest where the service exception came from.
     * @param desiredResourceModel Desired resource model from the request being handled.
     * @return CloudFormation-type exception converted from a service model exception.
     */
    public BaseHandlerException translateFromServiceException(final Exception serviceException,
                                                              final SsmRequest request,
                                                              final ResourceModel desiredResourceModel) {

        if (serviceException instanceof AssociationAlreadyExistsException) {

            return new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                getResourceModelIdentifier(desiredResourceModel),
                serviceException);
        } else if (serviceException instanceof AssociationLimitExceededException
            || serviceException instanceof AssociationVersionLimitExceededException) {

            return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME,
                serviceException.getMessage(),
                serviceException);
        } else if (serviceException instanceof AssociationDoesNotExistException) {

            return new CfnNotFoundException(ResourceModel.TYPE_NAME,
                getResourceModelIdentifier(desiredResourceModel),
                serviceException);
        } else if (serviceException instanceof InternalServerErrorException) {

            return new CfnServiceInternalErrorException(
                getClassNameWithoutRequestSuffix(request.getClass().getSimpleName()),
                serviceException);
        } else if (serviceException instanceof InvalidAssociationVersionException
            || serviceException instanceof InvalidDocumentException
            || serviceException instanceof InvalidDocumentVersionException
            || serviceException instanceof InvalidInstanceIdException
            || serviceException instanceof InvalidOutputLocationException
            || serviceException instanceof InvalidParametersException
            || serviceException instanceof InvalidScheduleException
            || serviceException instanceof InvalidTargetException
            || serviceException instanceof UnsupportedPlatformTypeException) {

            return new CfnInvalidRequestException(serviceException.getMessage(), serviceException);
        } else if (serviceException instanceof TooManyUpdatesException) {

            return new CfnThrottlingException(getClassNameWithoutRequestSuffix(request.getClass().getSimpleName()),
                serviceException);
        } else {
            // in case of unknown/unexpected service exceptions, use a generic exception with the name of the failed operation
            return new CfnGeneralServiceException(getClassNameWithoutRequestSuffix(request.getClass().getSimpleName()),
                serviceException);
        }
    }

    /**
     * Removes suffix "Request" string from input class names.
     *
     * @param simpleSsmRequestClassName Name of SsmRequest class to remove "Request" suffix from.
     * @return Request class name without "Request" in the end.
     */
    private String getClassNameWithoutRequestSuffix(final String simpleSsmRequestClassName) {
        final String classNameSuffixToRemove = "Request";

        if (simpleSsmRequestClassName.endsWith(classNameSuffixToRemove)) {
            return simpleSsmRequestClassName.substring(0, simpleSsmRequestClassName.length() - classNameSuffixToRemove.length());
        }

        return simpleSsmRequestClassName;
    }

    /**
     * Retrieves identifier for the model, handling a special case for legacy associations with no AssociationId.
     *
     * @param resourceModel ResourceModel to get identifier for.
     * @return Identifier for the resource model. Name+InstanceId if no AssociationId is present.
     */
    private String getResourceModelIdentifier(final ResourceModel resourceModel) {
        if (resourceModel.getAssociationId() == null) {
            return String.format("Name=%s,InstanceId=%s", resourceModel.getName(), resourceModel.getInstanceId());
        }
        return resourceModel.getAssociationId();
    }
}
