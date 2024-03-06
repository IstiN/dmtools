package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.cloud.CloudPullRequest;
import com.github.istin.dmtools.atlassian.bitbucket.model.server.ServerPullRequest;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BitbucketResult extends JSONModel {

    public static final String VALUES = "values";

    public static final String DIFFS = "diffs";


    public BitbucketResult() {
    }

    public BitbucketResult(String json) throws JSONException {
        super(json);
    }

    public BitbucketResult(JSONObject json) {
        super(json);
    }


    public List<? extends  PullRequest> getValues(Bitbucket.ApiVersion apiVersion) {
        if (apiVersion == Bitbucket.ApiVersion.V1) {
            return getModels(ServerPullRequest.class, VALUES);
        } else {
            return getModels(CloudPullRequest.class, VALUES);
        }
    }

    public List<Activity> getActivities() {
        return getModels(Activity.class, VALUES);
    }

    public List<Task> getTasks() {
        return getModels(Task.class, VALUES);
    }

    public List<Diff> getDiffs() {
        return getModels(Diff.class, DIFFS);
    }

    public List<Repository> getRepositories() {
        return getModels(Repository.class, VALUES);
    }

    public List<Change> getChanges() {
        return getModels(Change.class, VALUES);
    }
}