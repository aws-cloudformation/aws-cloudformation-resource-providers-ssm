# AWS::SSM::ResourceDataSync AwsOrganizationsSource

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#organizationalunits" title="OrganizationalUnits">OrganizationalUnits</a>" : <i>[ String, ... ]</i>,
    "<a href="#organizationsourcetype" title="OrganizationSourceType">OrganizationSourceType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#organizationalunits" title="OrganizationalUnits">OrganizationalUnits</a>: <i>
      - String</i>
<a href="#organizationsourcetype" title="OrganizationSourceType">OrganizationSourceType</a>: <i>String</i>
</pre>

## Properties

#### OrganizationalUnits

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OrganizationSourceType

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

