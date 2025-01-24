package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IDiffHunk;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class GitHubComment extends JSONModel implements IComment, IDiffHunk {

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

    @Override
    public Date getCreated() {
        return DateUtils.parseIsoDate(getString(   "created_at"));
    }

    @Override
    public String getDiffHunk() {
        return getString("diff_hunk");
    }

}