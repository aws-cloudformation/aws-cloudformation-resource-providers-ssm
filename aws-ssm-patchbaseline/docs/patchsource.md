# AWS::SSM::PatchBaseline PatchSource

Information about the patches to use to update the instances, including target operating systems and source repository. Applies to Linux instances only.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#products" title="Products">Products</a>" : <i>[ String, ... ]</i>,
    "<a href="#configuration" title="Configuration">Configuration</a>" : <i>String</i>,
    "<a href="#name" title="Name">Name</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#products" title="Products">Products</a>: <i>
      - String</i>
<a href="#configuration" title="Configuration">Configuration</a>: <i>String</i>
<a href="#name" title="Name">Name</a>: <i>String</i>
</pre>

## Properties

#### Products

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Configuration

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

_Required_: No

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_\-.]{3,50}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

