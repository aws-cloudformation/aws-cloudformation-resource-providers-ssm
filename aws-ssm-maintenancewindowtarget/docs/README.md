# AWS::SSM::MaintenanceWindowTarget

Resource Type definition for AWS::SSM::MaintenanceWindowTarget

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::MaintenanceWindowTarget",
    "Properties" : {
        "<a href="#clienttoken" title="ClientToken">ClientToken</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#ownerinformation" title="OwnerInformation">OwnerInformation</a>" : <i>String</i>,
        "<a href="#replace" title="Replace">Replace</a>" : <i>Boolean</i>,
        "<a href="#resourcetype" title="ResourceType">ResourceType</a>" : <i>String</i>,
        "<a href="#safe" title="Safe">Safe</a>" : <i>Boolean</i>,
        "<a href="#targets" title="Targets">Targets</a>" : <i>[ <a href="targets.md">Targets</a>, ... ]</i>,
        "<a href="#windowtargetid" title="WindowTargetId">WindowTargetId</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::MaintenanceWindowTarget
Properties:
    <a href="#clienttoken" title="ClientToken">ClientToken</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#ownerinformation" title="OwnerInformation">OwnerInformation</a>: <i>String</i>
    <a href="#replace" title="Replace">Replace</a>: <i>Boolean</i>
    <a href="#resourcetype" title="ResourceType">ResourceType</a>: <i>String</i>
    <a href="#safe" title="Safe">Safe</a>: <i>Boolean</i>
    <a href="#targets" title="Targets">Targets</a>: <i>
      - <a href="targets.md">Targets</a></i>
    <a href="#windowtargetid" title="WindowTargetId">WindowTargetId</a>: <i>String</i>
</pre>

## Properties

#### ClientToken

User-provided idempotency token.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

A description for the target.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name for the maintenance window target.

_Required_: No

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>128</code>

_Pattern_: <code>^[a-zA-Z0-9_\-.]{3,128}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OwnerInformation

A user-provided value that will be included in any CloudWatch events that are raised while running tasks for these targets in this maintenance window.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Replace

If True, then all fields that are required by the RegisterTargetWithMaintenanceWindow action are also required for this API request. Optional fields that are not specified are set to null.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ResourceType

The type of target that is being registered with the maintenance window.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>INSTANCE</code> | <code>RESOURCE_GROUP</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Safe

The system checks if the target is being referenced by a task. If the target is being referenced, the system returns an error and does not deregister the target from the maintenance window.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Targets

The targets to register with the maintenance window. In other words, the instances to run commands on when the maintenance window runs.

_Required_: Yes

_Type_: List of <a href="targets.md">Targets</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WindowTargetId

The ID of the target.

_Required_: No

_Type_: String

_Minimum_: <code>36</code>

_Maximum_: <code>36</code>

_Pattern_: <code>^[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the WindowId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### WindowId

The ID of the maintenance window to register the target with.

