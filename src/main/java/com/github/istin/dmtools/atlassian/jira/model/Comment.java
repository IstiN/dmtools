package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment extends JSONModel {

    private static final String ID = "id";
    private static final String AUTHOR = "author";
    public static final String BODY = "body";


    public Comment() {
    }

    public Comment(String json) throws JSONException {
        super(json);
    }

    public Comment(JSONObject json) {
        super(json);
    }


    public String getId() {
        return getString(ID);
    }

    public String getBody() {
        return getString(BODY);
    }

    public Assignee getAuthor() {
        return getModel(Assignee.class, AUTHOR);
    }

}