package com.aws.ssm.association;

import java.io.InputStream;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ssm-association.json");
    }

    public InputStream resourceSchema() {
        return this.getClass().getClassLoader().getResourceAsStream(schemaFilename);
    }

}
