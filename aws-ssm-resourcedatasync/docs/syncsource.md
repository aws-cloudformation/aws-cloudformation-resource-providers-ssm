# AWS::SSM::ResourceDataSync SyncSource

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#includefutureregions" title="IncludeFutureRegions">IncludeFutureRegions</a>" : <i>Boolean</i>,
    "<a href="#sourceregions" title="SourceRegions">SourceRegions</a>" : <i>[ String, ... ]</i>,
    "<a href="#sourcetype" title="SourceType">SourceType</a>" : <i>String</i>,
    "<a href="#awsorganizationssource" title="AwsOrganizationsSource">AwsOrganizationsSource</a>" : <i><a href="awsorganizationssource.md">AwsOrganizationsSource</a></i>
}
</pre>

### YAML

<pre>
<a href="#includefutureregions" title="IncludeFutureRegions">IncludeFutureRegions</a>: <i>Boolean</i>
<a href="#sourceregions" title="SourceRegions">SourceRegions</a>: <i>
      - String</i>
<a href="#sourcetype" title="SourceType">SourceType</a>: <i>String</i>
<a href="#awsorganizationssource" title="AwsOrganizationsSource">AwsOrganizationsSource</a>: <i><a href="awsorganizationssource.md">AwsOrganizationsSource</a></i>
</pre>

## Properties

#### IncludeFutureRegions

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceRegions

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceType

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AwsOrganizationsSource

_Required_: No

_Type_: <a href="awsorganizationssource.md">AwsOrganizationsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
