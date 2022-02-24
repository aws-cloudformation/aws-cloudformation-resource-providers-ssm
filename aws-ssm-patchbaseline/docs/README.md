# AWS::SSM::PatchBaseline

Resource Type definition for AWS::SSM::PatchBaseline

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::PatchBaseline",
    "Properties" : {
        "<a href="#defaultbaseline" title="DefaultBaseline">DefaultBaseline</a>" : <i>Boolean</i>,
        "<a href="#operatingsystem" title="OperatingSystem">OperatingSystem</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#approvalrules" title="ApprovalRules">ApprovalRules</a>" : <i><a href="rulegroup.md">RuleGroup</a></i>,
        "<a href="#sources" title="Sources">Sources</a>" : <i>[ <a href="patchsource.md">PatchSource</a>, ... ]</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#rejectedpatches" title="RejectedPatches">RejectedPatches</a>" : <i>[ String, ... ]</i>,
        "<a href="#approvedpatches" title="ApprovedPatches">ApprovedPatches</a>" : <i>[ String, ... ]</i>,
        "<a href="#rejectedpatchesaction" title="RejectedPatchesAction">RejectedPatchesAction</a>" : <i>String</i>,
        "<a href="#patchgroups" title="PatchGroups">PatchGroups</a>" : <i>[ String, ... ]</i>,
        "<a href="#approvedpatchescompliancelevel" title="ApprovedPatchesComplianceLevel">ApprovedPatchesComplianceLevel</a>" : <i>String</i>,
        "<a href="#approvedpatchesenablenonsecurity" title="ApprovedPatchesEnableNonSecurity">ApprovedPatchesEnableNonSecurity</a>" : <i>Boolean</i>,
        "<a href="#globalfilters" title="GlobalFilters">GlobalFilters</a>" : <i><a href="patchfiltergroup.md">PatchFilterGroup</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::PatchBaseline
Properties:
    <a href="#defaultbaseline" title="DefaultBaseline">DefaultBaseline</a>: <i>Boolean</i>
    <a href="#operatingsystem" title="OperatingSystem">OperatingSystem</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#approvalrules" title="ApprovalRules">ApprovalRules</a>: <i><a href="rulegroup.md">RuleGroup</a></i>
    <a href="#sources" title="Sources">Sources</a>: <i>
      - <a href="patchsource.md">PatchSource</a></i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#rejectedpatches" title="RejectedPatches">RejectedPatches</a>: <i>
      - String</i>
    <a href="#approvedpatches" title="ApprovedPatches">ApprovedPatches</a>: <i>
      - String</i>
    <a href="#rejectedpatchesaction" title="RejectedPatchesAction">RejectedPatchesAction</a>: <i>String</i>
    <a href="#patchgroups" title="PatchGroups">PatchGroups</a>: <i>
      - String</i>
    <a href="#approvedpatchescompliancelevel" title="ApprovedPatchesComplianceLevel">ApprovedPatchesComplianceLevel</a>: <i>String</i>
    <a href="#approvedpatchesenablenonsecurity" title="ApprovedPatchesEnableNonSecurity">ApprovedPatchesEnableNonSecurity</a>: <i>Boolean</i>
    <a href="#globalfilters" title="GlobalFilters">GlobalFilters</a>: <i><a href="patchfiltergroup.md">PatchFilterGroup</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### DefaultBaseline

Set the baseline as default baseline. Only registering to default patch baseline is allowed.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OperatingSystem

Defines the operating system the patch baseline applies to. The Default value is WINDOWS.

_Required_: No

_Type_: String

_Allowed Values_: <code>WINDOWS</code> | <code>AMAZON_LINUX</code> | <code>AMAZON_LINUX_2</code> | <code>UBUNTU</code> | <code>REDHAT_ENTERPRISE_LINUX</code> | <code>SUSE</code> | <code>CENTOS</code> | <code>ORACLE_LINUX</code> | <code>DEBIAN</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Description

The description of the patch baseline.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApprovalRules

A set of rules defining the approval rules for a patch baseline.

_Required_: No

_Type_: <a href="rulegroup.md">RuleGroup</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Sources

Information about the patches to use to update the instances, including target operating systems and source repository. Applies to Linux instances only.

_Required_: No

_Type_: List of <a href="patchsource.md">PatchSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the patch baseline.

_Required_: Yes

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>128</code>

_Pattern_: <code>^[a-zA-Z0-9_\-.]{3,128}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RejectedPatches

A list of explicitly rejected patches for the baseline.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApprovedPatches

A list of explicitly approved patches for the baseline.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RejectedPatchesAction

The action for Patch Manager to take on patches included in the RejectedPackages list.

_Required_: No

_Type_: String

_Allowed Values_: <code>ALLOW_AS_DEPENDENCY</code> | <code>BLOCK</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PatchGroups

PatchGroups is used to associate instances with a specific patch baseline

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApprovedPatchesComplianceLevel

Defines the compliance level for approved patches. This means that if an approved patch is reported as missing, this is the severity of the compliance violation. The default value is UNSPECIFIED.

_Required_: No

_Type_: String

_Allowed Values_: <code>CRITICAL</code> | <code>HIGH</code> | <code>MEDIUM</code> | <code>LOW</code> | <code>INFORMATIONAL</code> | <code>UNSPECIFIED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApprovedPatchesEnableNonSecurity

Indicates whether the list of approved patches includes non-security updates that should be applied to the instances. The default value is 'false'. Applies to Linux instances only.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GlobalFilters

_Required_: No

_Type_: <a href="patchfiltergroup.md">PatchFilterGroup</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Optional metadata that you assign to a resource. Tags enable you to categorize a resource in different ways.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

The ID of the patch baseline.

