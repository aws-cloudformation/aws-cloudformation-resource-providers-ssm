package software.amazon.ssm.maintenancewindowtask.translator;


import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.IdempotentParameterMismatchException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.ssm.model.SsmRequest;
import software.amazon.awssdk.services.ssm.model.TargetInUseException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;


/**
 * Translator between service model exceptions and CloudFormation exceptions.
 */
public class ExceptionTranslator {

    /**
     * Translates service model exceptions to CloudFormation exceptions.
     *
     * @param serviceException     Service model exception to translate.
     * @param request              Type of SsmRequest where the service exception came from.
    * @param desiredResourceModel Desired resource model from the request being handled.
     * @return CloudFormation-type exception converted from a service model exception.
     */
    public BaseHandlerException translateFromServiceException(final Exception serviceException,
                                                              final SsmRequest request,
                                                              final ResourceModel desiredResourceModel) {
        if (serviceException instanceof AlreadyExistsException) {

            return new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                    desiredResourceModel.getWindowTaskId(),
                    serviceException);
        } else if (serviceException instanceof ResourceLimitExceededException) {

            return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME,
                    serviceException.getMessage(),
                    serviceException);
        } else if (serviceException instanceof DoesNotExistException) {

            return new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    desiredResourceModel.getWindowTaskId(),
                    serviceException);
        } else if (serviceException instanceof InternalServerErrorException) {

            return new CfnServiceInternalErrorException(
                    getClassNameWithoutRequestSuffix(request.getClass().getSimpleName()),
                    serviceException);
        } else if (serviceException instanceof FeatureNotAvailableException
                || serviceException instanceof TargetInUseException
                || serviceException instanceof IdempotentParameterMismatchException) {

            return new CfnInvalidRequestException(serviceException.getMessage(), serviceException);
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
