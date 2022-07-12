# AWS::SSM::ResourcePolicies

Resource Type definition for AWS::SSM::ResourcePolicies

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::ResourcePolicies",
    "Properties" : {
        "<a href="#resourcearn" title="ResourceArn">ResourceArn</a>" : <i>String</i>,
        "<a href="#policy" title="Policy">Policy</a>" : <i>Map, String</i>,
        "<a href="#policyid" title="PolicyId">PolicyId</a>" : <i>String</i>,
        "<a href="#policyhash" title="PolicyHash">PolicyHash</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::ResourcePolicies
Properties:
    <a href="#resourcearn" title="ResourceArn">ResourceArn</a>: <i>String</i>
    <a href="#policy" title="Policy">Policy</a>: <i>Map, String</i>
    <a href="#policyid" title="PolicyId">PolicyId</a>: <i>String</i>
    <a href="#policyhash" title="PolicyHash">PolicyHash</a>: <i>String</i>
</pre>

## Properties

#### ResourceArn

Arn of OpsItemGroup/Parameter/ManagedInstance etc.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Policy

Actual policy statement in json.

_Required_: Yes

_Type_: Map, String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PolicyId

An unique identifier within the policies of a resource. 

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PolicyHash

The hash value returned when the previous policy was set. Its purpose is to prevent concurrent modifications of a policy. Do not use this parameter if no previous policy has been set.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Policy.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

The provider-assigned unique ID for this managed resource.

