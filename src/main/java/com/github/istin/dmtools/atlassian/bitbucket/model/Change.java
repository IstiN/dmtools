package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Change extends JSONModel {

    public Change() {
    }

    public Change(String json) throws JSONException {
        super(json);
    }

    public Change(JSONObject json) {
        super(json);
    }

    public String getFilePath() {
        JSONObject path = getJSONObject("path");
        if (path == null) {
            return "";
        }
        return path.getString("toString");
    }

}