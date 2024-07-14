package com.github.istin.dmtools.job;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseJobParams extends JSONModel {

    public static final String INPUT_JQL = "inputJql";
    public static final String INITIATOR = "initiator";

    public BaseJobParams() {
    }

    public BaseJobParams(String json) throws JSONException {
        super(json);
    }

    public BaseJobParams(JSONObject json) {
        super(json);
    }

    public String getInputJQL() {
        return getString(INPUT_JQL);
    }

    public String getInitiator() {
        return getString(INITIATOR);
    }

}