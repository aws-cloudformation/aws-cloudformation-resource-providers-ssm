package software.amazon.ssm.maintenancewindow.translator;

import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.TargetInUseException;
import software.amazon.awssdk.services.ssm.model.IdempotentParameterMismatchException;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.SsmRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

/**
 * Translator between service model exceptions and CloudFormation exceptions.
 */
public class ExceptionTranslator {

    /**
     * Translates service model exceptions to CloudFormation exceptions.
     *
     * @param serviceException Service model exception to translate.
     * @param request Type of SsmRequest where the service exception came from.
     * used for better exception messaging for the customer.
     * @return CloudFormation-type exception converted from a service model exception.
     */
    public BaseHandlerException translateFromServiceException(final Exception serviceException,
                                                              final SsmRequest request) {

        if (serviceException instanceof AlreadyExistsException) {

            return new CfnAlreadyExistsException(serviceException);
        } else if (serviceException instanceof ResourceLimitExceededException) {

            return new CfnServiceLimitExceededException(serviceException);
        } else if (serviceException instanceof DoesNotExistException) {

            return new CfnNotFoundException(serviceException);
        } else if (serviceException instanceof InternalServerErrorException) {

            return new CfnServiceInternalErrorException(
                    getClassNameWithoutRequestSuffix(request.getClass().getSimpleName()),
                    serviceException);
        } else if (serviceException instanceof FeatureNotAvailableException
                || serviceException instanceof TargetInUseException
                || serviceException instanceof IdempotentParameterMismatchException){

            return new CfnInvalidRequestException(request.toString(), serviceException);
        }else if (serviceException instanceof TooManyUpdatesException) {

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
}
