package com.github.istin.dmtools.atlassian.bitbucket.model.cloud;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class CloudBitbucketComment extends JSONModel implements IComment {

    public CloudBitbucketComment() {
    }

    public CloudBitbucketComment(String json) throws JSONException {
        super(json);
    }

    public CloudBitbucketComment(JSONObject json) {
        super(json);
    }

    @Override
    public IUser getAuthor() {
        return getModel(Assignee.class, "user");
    }

    @Override
    public String getBody() {
        return getJSONObject("content").getString("raw");
    }

    @Override
    public String getId() {
        return String.valueOf(getLong("id"));
    }

}