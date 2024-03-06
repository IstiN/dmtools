package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment extends JSONModel {

    public Comment() {
    }

    public Comment(String json) throws JSONException {
        super(json);
    }

    public Comment(JSONObject json) {
        super(json);
    }

    public String getText() {
        return getString("text");
    }

    public Assignee getUser() {
        Assignee author = getModel(Assignee.class, "author");
        if (author == null) {
            author = getModel(Assignee.class, "user");
        }
        return author;
    }

}