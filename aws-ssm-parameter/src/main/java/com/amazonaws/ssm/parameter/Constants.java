package com.amazonaws.ssm.parameter;

public class Constants {
    // ParameterName limit is 1024 chars. To make sure that we never hit that limit with auto name generation,
    // lets have a number less than the allowed limit i.e 1000.
    // The total of following three variables should be less than 1000.
    // This can be increased in the future.
    public static final int ALLOWED_LOGICAL_RESOURCE_ID_LENGTH = 500;
    public static final String CF_PARAMETER_NAME_PREFIX = "CFN";
    public static final int GUID_LENGTH = 12;

    public static final int ERROR_STATUS_CODE_400 = 400;
    public static final int ERROR_STATUS_CODE_500 = 500;

    public static final Integer MAX_RESULTS = 50;
    public static final String AWS_EC2_IMAGE_DATATYPE = "aws:ec2:image";
}
