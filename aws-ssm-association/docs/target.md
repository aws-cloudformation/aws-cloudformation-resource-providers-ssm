# AWS::SSM::Association Target

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#key" title="Key">Key</a>" : <i>String</i>,
    "<a href="#values" title="Values">Values</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#key" title="Key">Key</a>: <i>String</i>
<a href="#values" title="Values">Values</a>: <i>
      - String</i>
</pre>

## Properties

#### Key

_Required_: Yes

_Type_: String

_Pattern_: <code>^[\p{L}\p{Z}\p{N}_.:/=+\-@]{1,128}$|resource-groups:Name</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Values

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
