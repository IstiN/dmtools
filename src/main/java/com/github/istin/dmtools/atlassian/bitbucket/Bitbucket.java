package com.github.istin.dmtools.atlassian.bitbucket;

import com.github.istin.dmtools.atlassian.bitbucket.model.*;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.networking.GenericRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bitbucket extends AtlassianRestClient {

    public static final boolean IGNORE_CACHE = false;

    public Bitbucket(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    @Override
    public String path(String path) {
        return getBasePath() + "/rest/api/1.0/" + path;
    }

    public List<PullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests) throws IOException {
        List<PullRequest> result = new ArrayList<>();
        int start = 0;
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/?state=" + state));
        getRequest.param("start", start);
        getRequest.param("limit", "100");
        getRequest.setIgnoreCache(true);
        String response = execute(getRequest);
        if (response == null) {
            return result;
        }

        List<PullRequest> values = new BitbucketResult(response).getValues();
        result.addAll(values);
        while (!values.isEmpty() && checkAllRequests) {
            start = start + 100;
            System.out.println("pull requests: " + start);

            getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/?state=" + state));
            getRequest.param("start", ""+ start);
            getRequest.param("limit", "100");

            response = execute(getRequest);
            if (response == null) {
                return result;
            }

            values = new BitbucketResult(response).getValues();
            System.out.println("pull requests response size: " + values.size());
            if (!values.isEmpty()) {
                System.out.println("pull requests response first item: " + values.get(0).getId());
            }
            result.addAll(values);
        }

        return result;
    }

    public PullRequest pullRequest(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId));
        String response = execute(getRequest);
        if (response == null) {
            return new PullRequest();
        }

        return new PullRequest(response);
    }

    public JSONModel pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/comments"));
        String response = execute(getRequest);
        if (response == null) {
            return new JSONModel();
        }

        return new JSONModel(response);
    }

    public BitbucketResult pullRequestActivities(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/activities"));
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    public BitbucketResult repositories(String workspace) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/"));
        getRequest.param("limit", "1000");
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    public BitbucketResult pullRequestTasks(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/tasks"));
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    public String addTask(Integer commentId, String text) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("tasks"));
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("anchor", new JSONObject().put("id", commentId).put("type", "COMMENT"));
        jsonObject.put("text", text);
        getRequest.setBody(jsonObject.toString());
        return post(getRequest);
    }


    public String createPullRequestCommentAndTaskIfNotExists(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException {
        BitbucketResult bitbucketResult = pullRequestTasks(workspace, repository, pullRequestId);
        if (bitbucketResult.getJSONObject().toString().contains(commentText)) {
            return "";
        }
        return createPullRequestCommentAndTask(workspace, repository, pullRequestId, commentText, taskText);
    }

    public String createPullRequestCommentAndTask(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException {
        BitbucketResult bitbucketResult = pullRequestTasks(workspace, repository, pullRequestId);
        List<Task> tasks = bitbucketResult.getTasks();
        for (Task task : tasks) {
            if (task.getText().equals(taskText) && task.getState().equals("OPEN")) {
                return "";
            }
        }
        String result = addPullRequestComment(workspace, repository, pullRequestId, commentText);
        return addTask(new JSONModel(result).getInt("id"), taskText);
    }

    public String addPullRequestComment(String workspace, String repository, String pullRequestId, String text) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/comments"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        getRequest.setBody(jsonObject.toString());
        return post(getRequest);
    }

    public JSONModel commitComments(String workspace, String repository, String commitId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/commits/" + commitId + "/comments"));
        String response = execute(getRequest);
        if (response == null) {
            return new JSONModel();
        }

        return new JSONModel(response);
    }


    public String renamePullRequest(String workspace, String repository, PullRequest pullRequest, String newTitle) throws IOException {
        String updateTitleIfWip = (pullRequest.isWIP() ? upgradeTitleToWIP(newTitle) : newTitle).trim();

        if (pullRequest.getTitle().equalsIgnoreCase(updateTitleIfWip)) {
            return "";
        }

        GenericRequest putRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequest.getId()));
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("title", updateTitleIfWip);
        jsonObject.put("version", pullRequest.getVersion());
        jsonObject.put("reviewers", pullRequest.getJSONObject().optJSONArray("reviewers"));
        putRequest.setBody(jsonObject.toString());
        return put(putRequest);
    }

    private String upgradeTitleToWIP(String newTitle) {
        return newTitle.startsWith("[WIP]") ? newTitle : "[WIP]"+ newTitle;
    }


    public List<Tag> getTags(String workspace, String repository) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/tags"));
        getRequest.param("limit", "1000");
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Tag.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    public List<Tag> getBranches(String workspace, String repository) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/branches"));
        getRequest.param("limit", "1000");
        getRequest.param("orderBy", "MODIFICATION");
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Tag.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    public List<Commit> getCommitsBetween(String workspace, String repository, String from, String to) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/compare/commits?from="+from+"&to="+to));
        getRequest.param("limit", "1000");
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Commit.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    public List<Commit> getCommitsFromBranch(String workspace, String repository, String branchName) throws IOException {
        List<Commit> commits = new ArrayList<>();
        int start = 0;
        while (start <= 2000) {
            GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/commits?until=refs/heads/"+branchName+"&merges=include"));
            getRequest.param("start", start);
            getRequest.param("limit", "1000");
            start = start + 1000;
            String response = execute(getRequest);
            if (response == null) {
                return commits;
            }
            commits.addAll(JSONModel.convertToModels(Commit.class, new BitbucketResult(response).getJSONObject().optJSONArray("values")));
        }

        return commits;
    }

    public void performCommitsFromBranch(String workspace, String repository, String branchName, Performer<Commit> performer) throws Exception {
        int prevSize = 0;
        int currentSize = 0;
        int start = 0;
        boolean isFirst = true;
        while (isFirst || prevSize != currentSize) {
            isFirst = false;
            prevSize = currentSize;
            GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/commits?until=refs/heads/"+branchName+"&merges=include"));
            getRequest.param("start", start);
            getRequest.param("limit", "1000");
            getRequest.setIgnoreCache(true);
            start = start + 1000;
            String response = execute(getRequest);
            if (response == null) {
                return;
            }

            List<Commit> values = JSONModel.convertToModels(Commit.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
            for (Commit commit : values) {
                if (performer.perform(commit)) {
                    break;
                }
            }
            currentSize = currentSize + values.size();
        }
    }

    public BitbucketResult getCommitDiff(String workspace, String repository, String commitId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/commits/" + commitId +"/diff?AUTOSRCPATH&CONTEXTLINES&SINCE&SRCPATH&WHITESPACE&WITHCOMMENTS"));
        try {
            String response = execute(getRequest);
            if (response == null) {
                return new BitbucketResult("{}");
            }
            return new BitbucketResult(response);
        } catch (Exception e) {
            clearCache(getRequest);
            return new BitbucketResult("{}");
        }
    }

    public List<Change> getChanges(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId +"/changes"));
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return new BitbucketResult(response).getChanges();
    }
}
