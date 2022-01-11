# AWS::SSM::PatchBaseline PatchFilter

Defines which patches should be included in a patch baseline.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#values" title="Values">Values</a>" : <i>[ String, ... ]</i>,
    "<a href="#key" title="Key">Key</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#values" title="Values">Values</a>: <i>
      - String</i>
<a href="#key" title="Key">Key</a>: <i>String</i>
</pre>

## Properties

#### Values

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Key

_Required_: No

_Type_: String

_Allowed Values_: <code>PATCH_SET</code> | <code>PRODUCT</code> | <code>PRODUCT_FAMILY</code> | <code>CLASSIFICATION</code> | <code>MSRC_SEVERITY</code> | <code>PATCH_ID</code> | <code>SECTION</code> | <code>PRIORITY</code> | <code>SEVERITY</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

