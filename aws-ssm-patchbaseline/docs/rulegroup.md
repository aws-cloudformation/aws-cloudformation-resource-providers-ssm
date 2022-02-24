# AWS::SSM::PatchBaseline RuleGroup

A set of rules defining the approval rules for a patch baseline.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#patchrules" title="PatchRules">PatchRules</a>" : <i>[ <a href="rule.md">Rule</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#patchrules" title="PatchRules">PatchRules</a>: <i>
      - <a href="rule.md">Rule</a></i>
</pre>

## Properties

#### PatchRules

_Required_: No

_Type_: List of <a href="rule.md">Rule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

