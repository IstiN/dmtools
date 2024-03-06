package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Task extends JSONModel {

    public Task() {
    }

    public Task(String json) throws JSONException {
        super(json);
    }

    public Task(JSONObject json) {
        super(json);
    }

    public String getText() {
        JSONObject content = getJSONObject("content");
        if (content != null) {
            String raw = content.getString("raw");
            if (raw != null) {
                return raw;
            }
        }

        return getString("text");
    }

    public String getState() {
        return getString("state");
    }

}