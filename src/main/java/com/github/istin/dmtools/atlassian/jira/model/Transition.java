package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Transition extends JSONModel {

    private static final String ID = "id";
    private static final String NAME = "name";

    public Transition() {
    }

    public Transition(String json) throws JSONException {
        super(json);
    }

    public Transition(JSONObject json) {
        super(json);
    }

    public String getId() {
        return getString(ID);
    }

    public String getValue() {
        return getString(NAME);
    }

    public JSONObject setId(String id) {
        set(ID, id);
        return getJSONObject();
    }
}
