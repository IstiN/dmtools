package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IActivity;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;

/**
 * Wraps a GitHubComment (inline review comment) as an IActivity
 * so it can be included in pullRequestActivities results.
 */
public class GitHubCommentActivity implements IActivity {

    private final GitHubComment comment;

    public GitHubCommentActivity(GitHubComment comment) {
        this.comment = comment;
    }

    @Override
    public String getAction() {
        return "COMMENTED";
    }

    @Override
    public IComment getComment() {
        return comment;
    }

    @Override
    public IUser getApproval() {
        return null;
    }
}
