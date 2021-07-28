# AWS::SSM::ResourceDataSync

Resource Type definition for AWS::SSM::ResourceDataSync

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SSM::ResourceDataSync",
    "Properties" : {
        "<a href="#s3destination" title="S3Destination">S3Destination</a>" : <i><a href="s3destination.md">S3Destination</a></i>,
        "<a href="#kmskeyarn" title="KMSKeyArn">KMSKeyArn</a>" : <i>String</i>,
        "<a href="#syncsource" title="SyncSource">SyncSource</a>" : <i><a href="syncsource.md">SyncSource</a></i>,
        "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
        "<a href="#bucketregion" title="BucketRegion">BucketRegion</a>" : <i>String</i>,
        "<a href="#syncformat" title="SyncFormat">SyncFormat</a>" : <i>String</i>,
        "<a href="#synctype" title="SyncType">SyncType</a>" : <i>String</i>,
        "<a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SSM::ResourceDataSync
Properties:
    <a href="#s3destination" title="S3Destination">S3Destination</a>: <i><a href="s3destination.md">S3Destination</a></i>
    <a href="#kmskeyarn" title="KMSKeyArn">KMSKeyArn</a>: <i>String</i>
    <a href="#syncsource" title="SyncSource">SyncSource</a>: <i><a href="syncsource.md">SyncSource</a></i>
    <a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
    <a href="#bucketregion" title="BucketRegion">BucketRegion</a>: <i>String</i>
    <a href="#syncformat" title="SyncFormat">SyncFormat</a>: <i>String</i>
    <a href="#synctype" title="SyncType">SyncType</a>: <i>String</i>
    <a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>: <i>String</i>
</pre>

## Properties

#### S3Destination

_Required_: No

_Type_: <a href="s3destination.md">S3Destination</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KMSKeyArn

_Required_: No

_Type_: String

_Maximum_: <code>512</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SyncSource

_Required_: No

_Type_: <a href="syncsource.md">SyncSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketName

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### BucketRegion

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SyncFormat

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SyncType

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### BucketPrefix

_Required_: No

_Type_: String

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the SyncName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### SyncName

Returns the <code>SyncName</code> value.

