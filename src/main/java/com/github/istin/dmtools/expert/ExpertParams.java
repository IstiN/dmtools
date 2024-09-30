package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.job.BaseJobParams;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpertParams extends BaseJobParams {

    public static final String PROJECT_CONTEXT = "projectContext";

    public static final String REQUEST = "request";

    public ExpertParams() {
    }

    public ExpertParams(String json) throws JSONException {
        super(json);
    }

    public ExpertParams(JSONObject json) {
        super(json);
    }

    public String getProjectContext() {
        return getString(PROJECT_CONTEXT);
    }

    public String getRequest() {
        return getString(REQUEST);
    }

}