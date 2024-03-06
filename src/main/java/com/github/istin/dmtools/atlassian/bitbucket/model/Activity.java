package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity extends JSONModel {

    public Activity() {
    }

    public Activity(String json) throws JSONException {
        super(json);
    }

    public Activity(JSONObject json) {
        super(json);
    }

    public String getAction() {
        return getString("action");
    }

    public Comment getComment() {
        return getModel(Comment.class, "comment");
    }

    public Approval getApproval() {
        return getModel(Approval.class, "approval");
    }

}