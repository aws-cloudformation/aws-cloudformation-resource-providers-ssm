# AWS::SSM::PatchBaseline Rule

Defines an approval rule for a patch baseline.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#approveuntildate" title="ApproveUntilDate">ApproveUntilDate</a>" : <i>String</i>,
    "<a href="#enablenonsecurity" title="EnableNonSecurity">EnableNonSecurity</a>" : <i>Boolean</i>,
    "<a href="#patchfiltergroup" title="PatchFilterGroup">PatchFilterGroup</a>" : <i><a href="patchfiltergroup.md">PatchFilterGroup</a></i>,
    "<a href="#approveafterdays" title="ApproveAfterDays">ApproveAfterDays</a>" : <i>Integer</i>,
    "<a href="#compliancelevel" title="ComplianceLevel">ComplianceLevel</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#approveuntildate" title="ApproveUntilDate">ApproveUntilDate</a>: <i>String</i>
<a href="#enablenonsecurity" title="EnableNonSecurity">EnableNonSecurity</a>: <i>Boolean</i>
<a href="#patchfiltergroup" title="PatchFilterGroup">PatchFilterGroup</a>: <i><a href="patchfiltergroup.md">PatchFilterGroup</a></i>
<a href="#approveafterdays" title="ApproveAfterDays">ApproveAfterDays</a>: <i>Integer</i>
<a href="#compliancelevel" title="ComplianceLevel">ComplianceLevel</a>: <i>String</i>
</pre>

## Properties

#### ApproveUntilDate

_Required_: No

_Type_: String

_Maximum_: <code>10</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnableNonSecurity

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PatchFilterGroup

The patch filter group that defines the criteria for the rule.

_Required_: No

_Type_: <a href="patchfiltergroup.md">PatchFilterGroup</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApproveAfterDays

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ComplianceLevel

_Required_: No

_Type_: String

_Allowed Values_: <code>CRITICAL</code> | <code>HIGH</code> | <code>MEDIUM</code> | <code>LOW</code> | <code>INFORMATIONAL</code> | <code>UNSPECIFIED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

