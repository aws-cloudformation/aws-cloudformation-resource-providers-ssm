# AWS::SSM::Association

The AWS::SSM::Association resource associates an SSM document in AWS Systems Manager with EC2 instances that contain a configuration agent to process the document.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::Association",
    "Properties" : {
        "<a href="#associationname" title="AssociationName">AssociationName</a>" : <i>String</i>,
        "<a href="#documentversion" title="DocumentVersion">DocumentVersion</a>" : <i>String</i>,
        "<a href="#instanceid" title="InstanceId">InstanceId</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#parameters" title="Parameters">Parameters</a>" : <i><a href="parameters.md">Parameters</a></i>,
        "<a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>" : <i>String</i>,
        "<a href="#targets" title="Targets">Targets</a>" : <i>[ <a href="target.md">Target</a>, ... ]</i>,
        "<a href="#outputlocation" title="OutputLocation">OutputLocation</a>" : <i><a href="instanceassociationoutputlocation.md">InstanceAssociationOutputLocation</a></i>,
        "<a href="#automationtargetparametername" title="AutomationTargetParameterName">AutomationTargetParameterName</a>" : <i>String</i>,
        "<a href="#maxerrors" title="MaxErrors">MaxErrors</a>" : <i>String</i>,
        "<a href="#maxconcurrency" title="MaxConcurrency">MaxConcurrency</a>" : <i>String</i>,
        "<a href="#complianceseverity" title="ComplianceSeverity">ComplianceSeverity</a>" : <i>String</i>,
        "<a href="#synccompliance" title="SyncCompliance">SyncCompliance</a>" : <i>String</i>,
        "<a href="#waitforsuccesstimeoutseconds" title="WaitForSuccessTimeoutSeconds">WaitForSuccessTimeoutSeconds</a>" : <i>Integer</i>,
        "<a href="#applyonlyatcroninterval" title="ApplyOnlyAtCronInterval">ApplyOnlyAtCronInterval</a>" : <i>Boolean</i>,
        "<a href="#calendarnames" title="CalendarNames">CalendarNames</a>" : <i>[ String, ... ]</i>,
        "<a href="#scheduleoffset" title="ScheduleOffset">ScheduleOffset</a>" : <i>Integer</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::Association
Properties:
    <a href="#associationname" title="AssociationName">AssociationName</a>: <i>String</i>
    <a href="#documentversion" title="DocumentVersion">DocumentVersion</a>: <i>String</i>
    <a href="#instanceid" title="InstanceId">InstanceId</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#parameters" title="Parameters">Parameters</a>: <i><a href="parameters.md">Parameters</a></i>
    <a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>: <i>String</i>
    <a href="#targets" title="Targets">Targets</a>: <i>
      - <a href="target.md">Target</a></i>
    <a href="#outputlocation" title="OutputLocation">OutputLocation</a>: <i><a href="instanceassociationoutputlocation.md">InstanceAssociationOutputLocation</a></i>
    <a href="#automationtargetparametername" title="AutomationTargetParameterName">AutomationTargetParameterName</a>: <i>String</i>
    <a href="#maxerrors" title="MaxErrors">MaxErrors</a>: <i>String</i>
    <a href="#maxconcurrency" title="MaxConcurrency">MaxConcurrency</a>: <i>String</i>
    <a href="#complianceseverity" title="ComplianceSeverity">ComplianceSeverity</a>: <i>String</i>
    <a href="#synccompliance" title="SyncCompliance">SyncCompliance</a>: <i>String</i>
    <a href="#waitforsuccesstimeoutseconds" title="WaitForSuccessTimeoutSeconds">WaitForSuccessTimeoutSeconds</a>: <i>Integer</i>
    <a href="#applyonlyatcroninterval" title="ApplyOnlyAtCronInterval">ApplyOnlyAtCronInterval</a>: <i>Boolean</i>
    <a href="#calendarnames" title="CalendarNames">CalendarNames</a>: <i>
      - String</i>
    <a href="#scheduleoffset" title="ScheduleOffset">ScheduleOffset</a>: <i>Integer</i>
</pre>

## Properties

#### AssociationName

The name of the association.

_Required_: No

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_\-.]{3,128}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DocumentVersion

The version of the SSM document to associate with the target.

_Required_: No

_Type_: String

_Pattern_: <code>([$]LATEST|[$]DEFAULT|^[1-9][0-9]*$)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InstanceId

The ID of the instance that the SSM document is associated with.

_Required_: No

_Type_: String

_Pattern_: <code>(^i-(\w{8}|\w{17})$)|(^mi-\w{17}$)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the SSM document.

_Required_: Yes

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_\-.:/]{3,200}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Parameters

Parameter values that the SSM document uses at runtime.

_Required_: No

_Type_: <a href="parameters.md">Parameters</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduleExpression

A Cron or Rate expression that specifies when the association is applied to the target.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Targets

The targets that the SSM document sends commands to.

_Required_: No

_Type_: List of <a href="target.md">Target</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutputLocation

_Required_: No

_Type_: <a href="instanceassociationoutputlocation.md">InstanceAssociationOutputLocation</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutomationTargetParameterName

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>50</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxErrors

_Required_: No

_Type_: String

_Pattern_: <code>^([1-9][0-9]{0,6}|[0]|[1-9][0-9]%|[0-9]%|100%)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxConcurrency

_Required_: No

_Type_: String

_Pattern_: <code>^([1-9][0-9]{0,6}|[1-9][0-9]%|[1-9]%|100%)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ComplianceSeverity

_Required_: No

_Type_: String

_Allowed Values_: <code>CRITICAL</code> | <code>HIGH</code> | <code>MEDIUM</code> | <code>LOW</code> | <code>UNSPECIFIED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SyncCompliance

_Required_: No

_Type_: String

_Allowed Values_: <code>AUTO</code> | <code>MANUAL</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WaitForSuccessTimeoutSeconds

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApplyOnlyAtCronInterval

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CalendarNames

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduleOffset

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the AssociationId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AssociationId

Unique identifier of the association.
