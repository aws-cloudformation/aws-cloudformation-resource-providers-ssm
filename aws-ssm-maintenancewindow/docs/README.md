# AWS::SSM::MaintenanceWindow

Resource Type definition for AWS::SSM::MaintenanceWindow

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::MaintenanceWindow",
    "Properties" : {
        "<a href="#startdate" title="StartDate">StartDate</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#allowunassociatedtargets" title="AllowUnassociatedTargets">AllowUnassociatedTargets</a>" : <i>Boolean</i>,
        "<a href="#cutoff" title="Cutoff">Cutoff</a>" : <i>Integer</i>,
        "<a href="#schedule" title="Schedule">Schedule</a>" : <i>String</i>,
        "<a href="#duration" title="Duration">Duration</a>" : <i>Integer</i>,
        "<a href="#enddate" title="EndDate">EndDate</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#scheduletimezone" title="ScheduleTimezone">ScheduleTimezone</a>" : <i>String</i>,
        "<a href="#scheduleoffset" title="ScheduleOffset">ScheduleOffset</a>" : <i>Integer</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::MaintenanceWindow
Properties:
    <a href="#startdate" title="StartDate">StartDate</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#allowunassociatedtargets" title="AllowUnassociatedTargets">AllowUnassociatedTargets</a>: <i>Boolean</i>
    <a href="#cutoff" title="Cutoff">Cutoff</a>: <i>Integer</i>
    <a href="#schedule" title="Schedule">Schedule</a>: <i>String</i>
    <a href="#duration" title="Duration">Duration</a>: <i>Integer</i>
    <a href="#enddate" title="EndDate">EndDate</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#scheduletimezone" title="ScheduleTimezone">ScheduleTimezone</a>: <i>String</i>
    <a href="#scheduleoffset" title="ScheduleOffset">ScheduleOffset</a>: <i>Integer</i>
</pre>

## Properties

#### StartDate

The date and time, in ISO-8601 Extended format, for when the maintenance window is scheduled to become active.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

A description of the maintenance window.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AllowUnassociatedTargets

Enables a maintenance window task to run on managed instances, even if you have not registered those instances as targets.

_Required_: Yes

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Cutoff

The number of hours before the end of the maintenance window that Systems Manager stops scheduling new tasks for execution.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Schedule

The schedule of the maintenance window in the form of a cron or rate expression.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Duration

The duration of the maintenance window in hours.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndDate

The date and time, in ISO-8601 Extended format, for when the maintenance window is scheduled to become inactive.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Optional metadata that you assign to a resource in the form of an arbitrary set of tags (key-value pairs).

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the maintenance window.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>3</code>

_Maximum Length_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduleTimezone

The time zone that the scheduled maintenance window executions are based on, in Internet Assigned Numbers Authority (IANA) format.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduleOffset

The number of days to wait to run a maintenance window after the scheduled CRON expression date and time.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the WindowId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### WindowId

Unique identifier of the maintenance window.
