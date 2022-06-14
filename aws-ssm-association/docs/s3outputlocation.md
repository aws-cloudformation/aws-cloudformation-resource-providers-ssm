# AWS::SSM::Association S3OutputLocation

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#outputs3region" title="OutputS3Region">OutputS3Region</a>" : <i>String</i>,
    "<a href="#outputs3bucketname" title="OutputS3BucketName">OutputS3BucketName</a>" : <i>String</i>,
    "<a href="#outputs3keyprefix" title="OutputS3KeyPrefix">OutputS3KeyPrefix</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#outputs3region" title="OutputS3Region">OutputS3Region</a>: <i>String</i>
<a href="#outputs3bucketname" title="OutputS3BucketName">OutputS3BucketName</a>: <i>String</i>
<a href="#outputs3keyprefix" title="OutputS3KeyPrefix">OutputS3KeyPrefix</a>: <i>String</i>
</pre>

## Properties

#### OutputS3Region

_Required_: No

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>20</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutputS3BucketName

_Required_: No

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>63</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutputS3KeyPrefix

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
