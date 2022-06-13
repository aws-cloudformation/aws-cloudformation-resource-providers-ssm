# AWS::SSM::ResourceDataSync S3Destination

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#kmskeyarn" title="KMSKeyArn">KMSKeyArn</a>" : <i>String</i>,
    "<a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>" : <i>String</i>,
    "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
    "<a href="#bucketregion" title="BucketRegion">BucketRegion</a>" : <i>String</i>,
    "<a href="#syncformat" title="SyncFormat">SyncFormat</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#kmskeyarn" title="KMSKeyArn">KMSKeyArn</a>: <i>String</i>
<a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>: <i>String</i>
<a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
<a href="#bucketregion" title="BucketRegion">BucketRegion</a>: <i>String</i>
<a href="#syncformat" title="SyncFormat">SyncFormat</a>: <i>String</i>
</pre>

## Properties

#### KMSKeyArn

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>512</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketPrefix

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketName

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketRegion

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SyncFormat

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
