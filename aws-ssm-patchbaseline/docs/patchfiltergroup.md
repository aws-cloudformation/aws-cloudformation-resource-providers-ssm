# AWS::SSM::PatchBaseline PatchFilterGroup

The patch filter group that defines the criteria for the rule.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#patchfilters" title="PatchFilters">PatchFilters</a>" : <i>[ <a href="patchfilter.md">PatchFilter</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#patchfilters" title="PatchFilters">PatchFilters</a>: <i>
      - <a href="patchfilter.md">PatchFilter</a></i>
</pre>

## Properties

#### PatchFilters

_Required_: No

_Type_: List of <a href="patchfilter.md">PatchFilter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

