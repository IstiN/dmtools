package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Project extends JSONModel {

    private static final String ID = "id";

    public Project() {
    }

    public Project(String json) throws JSONException {
        super(json);
    }

    public Project(JSONObject json) {
        super(json);
    }

    public String getId() {
        return getString(ID);
    }
}