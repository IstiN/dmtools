package com.github.istin.dmtools.atlassian.confluence.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Attachment extends JSONModel {

    public static final String TITLE = "title";

    public Attachment() {
    }

    public Attachment(String json) throws JSONException {
        super(json);
    }

    public Attachment(JSONObject json) {
        super(json);
    }


    public String getTitle() {
        return getString(TITLE);
    }

}