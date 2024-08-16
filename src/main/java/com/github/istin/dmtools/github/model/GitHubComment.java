package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class GitHubComment extends JSONModel implements IComment {

    public GitHubComment() {
    }

    public GitHubComment(String json) throws JSONException {
        super(json);
    }

    public GitHubComment(JSONObject json) {
        super(json);
    }

    @Override
    public IUser getAuthor() {
        return getModel(GitHubUser.class, "user");
    }

    @Override
    public String getBody() {
        return getString("body");
    }

    @Override
    public String getId() {
        return String.valueOf(getLong("id"));
    }

}