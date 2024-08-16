package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IActivity;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class GitHubActivity extends JSONModel implements IActivity {

    public enum State {
        PENDING,
        COMMENTED,
        APPROVED,
        CHANGES_REQUESTED,
        DISMISSED
    }

    public GitHubActivity() {
    }

    public GitHubActivity(String json) throws JSONException {
        super(json);
    }

    public GitHubActivity(JSONObject json) {
        super(json);
    }

    /**
     * PENDING
     * COMMENTED
     * APPROVED
     * CHANGES_REQUESTED
     * DISMISSED
     * @return State
     */
    @Override
    public String getAction() {
        return getString("state");
    }

    @Override
    public IComment getComment() {
        return new IComment() {
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
        };
    }

    @Override
    public IUser getApproval() {
        return getModel(GitHubUser.class, "user");
    }
}