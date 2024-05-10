package com.github.istin.dmtools.atlassian.bitbucket;

import com.github.istin.dmtools.atlassian.bitbucket.model.*;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.networking.GenericRequest;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bitbucket extends AtlassianRestClient {

    public static final boolean IGNORE_CACHE = false;

    public enum ApiVersion {
        V1,
        V2
    }

    public static class PullRequestState {
        public static String STATE_MERGED = "merged";
        public static String STATE_OPEN = "open";
    }

    private ApiVersion apiVersion = ApiVersion.V1;

    private int defaultLimit = 50;

    public Bitbucket(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    public void setApiVersion(ApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    @Override
    public String path(String path) {
        if (apiVersion == ApiVersion.V1) {
            return getBasePath() + "/rest/api/1.0/" + path;
        } else {
            return getBasePath() + "/2.0/" + path;
        }
    }

    public List<PullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests) throws IOException {
        List<PullRequest> result = new ArrayList<>();
        int start = getInitialStartValue();
        String stateConverted = apiVersion == ApiVersion.V1 ? state : state.toUpperCase();
        GenericRequest getRequest = new GenericRequest(this, buildPullRequestsPath(workspace, repository, stateConverted));
        getRequest.param(getNextPageField(), start);
        getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
        getRequest.setIgnoreCache(true);
        String response = execute(getRequest);
        if (response == null) {
            return result;
        }

        List<? extends PullRequest> values = new BitbucketResult(response).getValues(apiVersion);
        result.addAll(values);
        while (!values.isEmpty() && checkAllRequests) {
            start = buildNexPage(start);
            System.out.println("pull requests: " + start);

            getRequest = new GenericRequest(this, buildPullRequestsPath(workspace, repository, stateConverted));
            getRequest.param(getNextPageField(), ""+ start);
            getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
            getRequest.setIgnoreCache(true);

            response = execute(getRequest);
            if (response == null) {
                return result;
            }

            values = new BitbucketResult(response).getValues(apiVersion);
            System.out.println("pull requests response size: " + values.size());
            if (!values.isEmpty()) {
                System.out.println("pull requests response first item: " + values.get(0).getId());
            }
            result.addAll(values);
        }

        return result;
    }

    private int getInitialStartValue() {
        return (apiVersion == ApiVersion.V1) ? 0 : 1;
    }

    @NotNull
    private String getNextPageField() {
        if (apiVersion == ApiVersion.V1) {
            return "start";
        } else {
            return "page";
        }
    }

    private int buildNexPage(int start) {
        if (apiVersion == ApiVersion.V1) {
            start = start + defaultLimit;
            return start;
        } else {
            return ++start;
        }
    }

    @NotNull
    private String getPageLimitField() {
        if (apiVersion == ApiVersion.V1) {
            return "limit";
        } else {
            return "pagelen";
        }
    }

    private String buildPullRequestsPath(String workspace, String repository, String state) {
        if (apiVersion == ApiVersion.V1) {
            return path("projects/" + workspace + "/repos/" + repository + "/pull-requests/?state=" + state);
        } else {
            return path("repositories/" + workspace + "/" + repository + "/pullrequests/?state=" + state);
        }
    }

    public PullRequest pullRequest(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildPullRequestPath(workspace, repository, pullRequestId)));
        getRequest.setIgnoreCache(true);
        String response = execute(getRequest);
        return PullRequest.create(apiVersion, response);
    }

    @NotNull
    private String buildPullRequestPath(String workspace, String repository, String pullRequestId) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId;
        } else {
            return "repositories/" + workspace + "/" + repository + "/pullrequests/" + pullRequestId;
        }
    }

    public JSONModel pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = createPullRequestCommentsRequest(workspace, repository, pullRequestId);
        String response = execute(getRequest);
        if (response == null) {
            return new JSONModel();
        }

        return new JSONModel(response);
    }

    @NotNull
    private GenericRequest createPullRequestCommentsRequest(String workspace, String repository, String pullRequestId) {
        return new GenericRequest(this, path(buildPullRequestCommentsPath(workspace, repository, pullRequestId)));
    }

    @NotNull
    private String buildPullRequestCommentsPath(String workspace, String repository, String pullRequestId) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/comments";
        } else {
            return "repositories/" + workspace + "/"+ repository + "/pullrequests/" + pullRequestId + "/comments";
        }
    }

    public BitbucketResult pullRequestActivities(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildPullRequestActivityPath(workspace, repository, pullRequestId)));
        if (apiVersion == ApiVersion.V2) {
            getRequest.param(getNextPageField(), getInitialStartValue());
            getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
        }
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    @NotNull
    private String buildPullRequestActivityPath(String workspace, String repository, String pullRequestId) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/activities";
        } else {
            return "repositories/" + workspace + "/" + repository + "/pullrequests/" + pullRequestId + "/activity";
        }
    }

    public BitbucketResult repositories(String workspace) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildRepositoriesPath(workspace)));
        getRequest.param(getPageLimitField(), defaultLimit);
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    @NotNull
    private String buildRepositoriesPath(String workspace) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/";
        } else {
            return "repositories/" + workspace;
        }
    }

    public BitbucketResult pullRequestTasks(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildPullRequestTasksPath(workspace, repository, pullRequestId)));
        String response = execute(getRequest);
        if (response == null) {
            return new BitbucketResult();
        }
        return new BitbucketResult(response);
    }

    @NotNull
    private String buildPullRequestTasksPath(String workspace, String repository, String pullRequestId) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/pull-requests/" + pullRequestId + "/tasks";
        } else {
            return "repositories/" + workspace + "/" + repository + "/pullrequests/" + pullRequestId + "/tasks";
        }
    }

    public String addTask(Integer commentId, String text) throws IOException {
        //it's not supported in api V2
        GenericRequest getRequest = new GenericRequest(this, path("tasks"));
        JSONObject jsonObject = new JSONObject();

        createTaskObject(commentId, text, jsonObject);
        getRequest.setBody(jsonObject.toString());
        return post(getRequest);
    }

    private void createTaskObject(Integer commentId, String text, JSONObject jsonObject) {
        if (apiVersion == ApiVersion.V1) {
            jsonObject.put("anchor", new JSONObject().put("id", commentId).put("type", "COMMENT"));
            jsonObject.put("text", text);
        } else {
            jsonObject.put("comment", new JSONObject().put("id", String.valueOf(commentId)));
            jsonObject.put("content", new JSONObject().put("raw", text));
            jsonObject.put("pending", false);
        }
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
            if (task.getText().equals(taskText) && (task.getState().equals("OPEN") || task.getState().equals("UNRESOLVED"))) {
                return "";
            }
        }
        String result = addPullRequestComment(workspace, repository, pullRequestId, commentText);
        return addTask(new JSONModel(result).getInt("id"), taskText);
    }

    public String addPullRequestComment(String workspace, String repository, String pullRequestId, String text) throws IOException {
        GenericRequest getRequest = createPullRequestCommentsRequest(workspace, repository, pullRequestId);
        JSONObject jsonObject = createPullRequestCommentObject(text);
        getRequest.setBody(jsonObject.toString());
        clearCache(getRequest);
        return post(getRequest);
    }

    @NotNull
    private JSONObject createPullRequestCommentObject(String text) {
        if (apiVersion == ApiVersion.V1) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("text", text);
            return jsonObject;
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", new JSONObject().put("raw", text));
            return jsonObject;
        }
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

        GenericRequest putRequest = new GenericRequest(this, path(buildPullRequestPath(workspace, repository, String.valueOf(pullRequest.getId()))));
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("title", updateTitleIfWip);
//        jsonObject.put("version", pullRequest.getVersion());
//        jsonObject.put("reviewers", pullRequest.getJSONObject().optJSONArray("reviewers"));
        putRequest.setBody(jsonObject.toString());
        return put(putRequest);
    }

    private String upgradeTitleToWIP(String newTitle) {
        return newTitle.startsWith("[WIP]") ? newTitle : "[WIP]"+ newTitle;
    }


    public List<Tag> getTags(String workspace, String repository) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, buildTagsPath(workspace, repository));
        getRequest.param(getPageLimitField(), defaultLimit);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Tag.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    private String buildTagsPath(String workspace, String repository) {
        if (apiVersion == ApiVersion.V1) {
            return path("projects/" + workspace + "/repos/" + repository + "/tags");
        } else {
            return path("repositories/" + workspace + "/" + repository + "/refs/tags");
        }
    }

    public List<Tag> getBranches(String workspace, String repository) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildGetBranchesPath(workspace, repository)));
        getRequest.param(getPageLimitField(), defaultLimit);
        getRequest.param("orderBy", "MODIFICATION");
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Tag.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    @NotNull
    private String buildGetBranchesPath(String workspace, String repository) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/branches";
        } else {
            return "repositories/" + workspace + "/" + repository + "/refs/branches";
        }
    }

    public List<Commit> getCommitsBetween(String workspace, String repository, String from, String to) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path(buildPathGetCommitsBetween(workspace, repository, from, to)));
        getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Commit.class, new BitbucketResult(response).getJSONObject().optJSONArray("values"));
    }

    @NotNull
    private String buildPathGetCommitsBetween(String workspace, String repository, String from, String to) {
        if (apiVersion == ApiVersion.V1) {
            return "projects/" + workspace + "/repos/" + repository + "/compare/commits?from=" + from + "&to=" + to;
        } else {
            return "repositories/" + workspace + "/" + repository + "/commits?include=" + from + "&exclude=" + to;
        }
    }

    public List<Commit> getCommitsFromBranch(String workspace, String repository, String branchName) throws IOException {
        List<Commit> commits = new ArrayList<>();
        int start = getInitialStartValue();
        while (start <= 2000) {
            GenericRequest getRequest = new GenericRequest(this, buildCommitsPath(workspace, repository, branchName));
            getRequest.param(getNextPageField(), start);
            getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
            start = buildNexPage(start);
            String response = execute(getRequest);
            if (response == null) {
                return commits;
            }
            JSONArray values = new BitbucketResult(response).getJSONObject().optJSONArray("values");
            if (values.isEmpty()) {
                break;
            }
            commits.addAll(JSONModel.convertToModels(Commit.class, values));
        }

        return commits;
    }

    private String buildCommitsPath(String workspace, String repository, String branchName) {
        if (apiVersion == ApiVersion.V1) {
            return path("projects/" + workspace + "/repos/" + repository + "/commits?until=refs/heads/" + branchName + "&merges=include");
        } else {
            return path("repositories/" + workspace + "/" + repository + "/commits?include=" + branchName);
        }
    }

    public void performCommitsFromBranch(String workspace, String repository, String branchName, Performer<Commit> performer) throws Exception {
        int prevSize = 0;
        int currentSize = 0;
        int start = getInitialStartValue();
        boolean isFirst = true;
        while (isFirst || prevSize != currentSize) {
            isFirst = false;
            prevSize = currentSize;
            GenericRequest getRequest = new GenericRequest(this, buildCommitsPath(workspace, repository, branchName));
            getRequest.param(getNextPageField(), start);

            getRequest.param(getPageLimitField(), String.valueOf(defaultLimit));
            getRequest.setIgnoreCache(true);
            start = buildNexPage(start);
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

    public String getDiff(String workspace, String repository, String pullRequestId) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("repositories/" + workspace + "/" + repository + "/pullrequests/" + pullRequestId +"/diff"));
        return execute(getRequest);
    }

    public List<File> getListOfFiles(String workspace, String repository, String branchName)  throws IOException {
        GenericRequest getRequest = new GenericRequest(this, path("repositories/" + workspace + "/" + repository + "/src/" + branchName +"/?max_depth=50"));
//        getRequest.param(getNextPageField(), 1);
        getRequest.param(getPageLimitField(), 100);
        BitbucketResult bitbucketResult = new BitbucketResult(execute(getRequest));
        List<File> result = bitbucketResult.getFiles();
        while (bitbucketResult.getNext() != null) {
            getRequest = new GenericRequest(this, bitbucketResult.getNext());
            bitbucketResult = new BitbucketResult(execute(getRequest));
            result.addAll(bitbucketResult.getFiles());
        }
        return result;
    }

    public String getFileContent(String selfLink) throws IOException {
        GenericRequest request = new GenericRequest(this, selfLink);
        return request.execute();
    }
}
