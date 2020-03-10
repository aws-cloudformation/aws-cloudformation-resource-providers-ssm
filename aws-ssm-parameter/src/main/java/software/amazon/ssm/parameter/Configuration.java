package software.amazon.ssm.parameter;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ssm-parameter.json");
    }
}
