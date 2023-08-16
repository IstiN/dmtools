package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Attachment extends JSONModel {

    public static final String AUTHOR = "author";

    private static final String FILENAME = "filename";

    public Attachment() {
    }

    public Attachment(String json) throws JSONException {
        super(json);
    }

    public Attachment(JSONObject json) {
        super(json);
    }

    public Assignee getAuthor() {
        return getModel(Assignee.class, AUTHOR);
    }

    public String getFilename() {
        return getString(FILENAME);
    }

}