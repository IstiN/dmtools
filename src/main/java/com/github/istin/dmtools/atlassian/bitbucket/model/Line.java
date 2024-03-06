package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Line extends JSONModel {

    public Line() {
    }

    public Line(String json) throws JSONException {
        super(json);
    }

    public Line(JSONObject json) {
        super(json);
    }

}