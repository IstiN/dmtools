package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Tag extends JSONModel {

    public Tag() {
    }

    public Tag(String json) throws JSONException {
        super(json);
    }

    public Tag(JSONObject json) {
        super(json);
    }

    public String getId() {
        return getString("id");
    }

    public String getName() {
        if (getId() == null) {
            return getString("name");
        }

        return getId().replace("refs/tags/", "");
    }

    public String getLatestCommit() {
        return getString("latestCommit");
    }

}