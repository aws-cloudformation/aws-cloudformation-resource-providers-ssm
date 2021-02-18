# AWS::SSM::OpsMetadata Metadata

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#metadatakey" title="MetadataKey">MetadataKey</a>" : <i>String</i>,
    "<a href="#metadatavalue" title="MetadataValue">MetadataValue</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#metadatakey" title="MetadataKey">MetadataKey</a>: <i>String</i>
<a href="#metadatavalue" title="MetadataValue">MetadataValue</a>: <i>String</i>
</pre>

## Properties

#### MetadataKey

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^(?!\s*$).+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetadataValue

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>4096</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

