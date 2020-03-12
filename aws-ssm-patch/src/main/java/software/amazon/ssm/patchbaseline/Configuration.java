package software.amazon.ssm.patchbaseline;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ssm-patchbaseline.json");
    }
}
