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
import software.amazon.cloudformation.proxy.Logger;

import java.util.Optional;

/**
 * Translator between service model exceptions and CloudFormation exceptions.
 */
public class ExceptionTranslator {

    /**
     * Translates service model exceptions to CloudFormation exceptions.
     *
     * @param serviceException Service model exception to translate.
     * @param request Type of SsmRequest where the service exception came from.
     * @param optionalAssociationId Optional of associationId for which the exception came from;
     * used for better exception messaging for the customer.
     * @return CloudFormation-type exception converted from a service model exception.
     */
    public BaseHandlerException translateFromServiceException(final Exception serviceException,
                                                              final SsmRequest request,
                                                              final Optional<String> optionalAssociationId) {

        if (serviceException instanceof AssociationAlreadyExistsException) {

            return new CfnAlreadyExistsException(serviceException);
        } else if (serviceException instanceof AssociationLimitExceededException
            || serviceException instanceof AssociationVersionLimitExceededException) {

            return new CfnServiceLimitExceededException(serviceException);
        } else if (serviceException instanceof AssociationDoesNotExistException) {

            return optionalAssociationId
                .map(associationId -> new CfnNotFoundException(ResourceModel.TYPE_NAME, associationId, serviceException))
                .orElseGet(() -> new CfnNotFoundException(serviceException));
        } else if (serviceException instanceof InternalServerErrorException) {

            return new CfnServiceInternalErrorException(serviceException);
        } else if (serviceException instanceof InvalidAssociationVersionException
            || serviceException instanceof InvalidDocumentException
            || serviceException instanceof InvalidDocumentVersionException
            || serviceException instanceof InvalidInstanceIdException
            || serviceException instanceof InvalidOutputLocationException
            || serviceException instanceof InvalidParametersException
            || serviceException instanceof InvalidScheduleException
            || serviceException instanceof InvalidTargetException
            || serviceException instanceof UnsupportedPlatformTypeException) {

            return new CfnInvalidRequestException(request.toString(), serviceException);
        } else if (serviceException instanceof TooManyUpdatesException) {

            return new CfnThrottlingException(serviceException);
        } else {
            // in case of unknown/unexpected service exceptions, use a generic exception with the name of the failed operation
            return new CfnGeneralServiceException(request.getClass().getSimpleName(), serviceException);
        }
    }
}
