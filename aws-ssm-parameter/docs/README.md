# AWS::SSM::Parameter

Resource Type definition for AWS::SSM::Parameter

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::Parameter",
    "Properties" : {
        "<a href="#type" title="Type">Type</a>" : <i>String</i>,
        "<a href="#value" title="Value">Value</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#policies" title="Policies">Policies</a>" : <i>String</i>,
        "<a href="#allowedpattern" title="AllowedPattern">AllowedPattern</a>" : <i>String</i>,
        "<a href="#tier" title="Tier">Tier</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i><a href="tags.md">Tags</a></i>,
        "<a href="#datatype" title="DataType">DataType</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::Parameter
Properties:
    <a href="#type" title="Type">Type</a>: <i>String</i>
    <a href="#value" title="Value">Value</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#policies" title="Policies">Policies</a>: <i>String</i>
    <a href="#allowedpattern" title="AllowedPattern">AllowedPattern</a>: <i>String</i>
    <a href="#tier" title="Tier">Tier</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i><a href="tags.md">Tags</a></i>
    <a href="#datatype" title="DataType">DataType</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
</pre>

## Properties

#### Type

The type of the parameter.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>String</code> | <code>StringList</code> | <code>SecureString</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Value

The value associated with the parameter.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>32768</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

The information about the parameter.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Policies

The policies attached to the parameter.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AllowedPattern

The regular expression used to validate the parameter value.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tier

The corresponding tier of the parameter.

_Required_: No

_Type_: String

_Allowed Values_: <code>Standard</code> | <code>Advanced</code> | <code>Intelligent-Tiering</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A key-value pair to associate with a resource.

_Required_: No

_Type_: <a href="tags.md">Tags</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataType

The corresponding DataType of the parameter.

_Required_: No

_Type_: String

_Allowed Values_: <code>text</code> | <code>aws:ec2:image</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the parameter.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>2048</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Name.
