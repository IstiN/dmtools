package com.github.istin.dmtools.github;

import com.github.istin.dmtools.atlassian.bitbucket.model.Commit;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.github.model.*;
import com.github.istin.dmtools.job.JobRunner;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GitHub extends AbstractRestClient implements SourceCode {
    private static final Logger logger = LogManager.getLogger(GitHub.class);
    private static final String API_VERSION = "v3";

    public GitHub(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    @Override
    public String path(String path) {
        return getBasePath() + "/" + path;
    }

    private volatile Boolean isDiff = false;

    @Override
    public synchronized Request.Builder sign(Request.Builder builder) {
        synchronized (isDiff) {
            return builder
                    .header("Authorization", "Bearer " + authorization)
                    .header("Accept", isDiff ? "application/vnd.github.diff" : "application/vnd.github.v3+json")
                    .header("Content-Type", "application/json");
        }
    }

    @Override
    public List<IPullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests) throws IOException {
        if (state.equalsIgnoreCase("merged")) {
            state = "closed";
        }
        List<IPullRequest> allPullRequests = new ArrayList<>();
        int perPage = 100; // Maximum allowed by GitHub
        int currentPage = 1;

        while (true) {
            String path = path(String.format("repos/%s/%s/pulls?state=%s&per_page=%d&page=%d",
                    workspace, repository, state, perPage, currentPage));
            GenericRequest getRequest = new GenericRequest(this, path);
            String response = execute(getRequest);

            if (response == null || response.isEmpty()) {
                break;
            }

            List<IPullRequest> pullRequests = JSONModel.convertToModels(GitHubPullRequest.class, new JSONArray(response));
            allPullRequests.addAll(pullRequests);

            if (!checkAllRequests || pullRequests.size() < perPage) {
                // If not checking all requests, or if fewer entries than the max per page are returned, we're done
                break;
            }

            // Move to next page
            currentPage++;
        }

        return allPullRequests;
    }

    @Override
    public IPullRequest pullRequest(String workspace, String repository, String pullRequestId) throws IOException {
        String response = getPullRequestResponse(workspace, repository, pullRequestId);
        return new GitHubPullRequest(response);
    }

    private String getPullRequestResponse(String workspace, String repository, String pullRequestId) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        return response;
    }

    @Override
    public List<IComment> pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException {
        String path = path(String.format("repos/%s/%s/issues/%s/comments", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return new ArrayList<>();
        }
        return JSONModel.convertToModels(GitHubComment.class, new JSONArray(response));
    }

    @Override
    public List<IActivity> pullRequestActivities(String workspace, String repository, String pullRequestId) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s/reviews", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return new ArrayList<>();
        }
        return JSONModel.convertToModels(GitHubActivity.class, new JSONArray(response));
    }

    @Override
    public List<ITask> pullRequestTasks(String workspace, String repository, String pullRequestId) throws IOException {
        List<IActivity> activities = pullRequestActivities(workspace, repository, pullRequestId);
        List<ITask> tasks = new ArrayList<>();
        for (IActivity activity : activities) {
            if (activity.getAction().equalsIgnoreCase(GitHubActivity.State.CHANGES_REQUESTED.name())) {
                tasks.add(new GitHubTask(((GitHubActivity)activity).getJSONObject()));
            }
        }
        return tasks;
    }

    @Override
    public String addTask(Integer commentId, String text) throws IOException {
        // GitHub does not have a direct equivalent for adding tasks to comments
        throw new UnsupportedOperationException("GitHub does not support adding tasks to comments.");
    }

    @Override
    public String createPullRequestCommentAndTaskIfNotExists(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException {
        // GitHub does not have a direct equivalent for creating comments and tasks
        throw new UnsupportedOperationException("GitHub does not support creating comments and tasks.");
    }

    @Override
    public String createPullRequestCommentAndTask(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException {
        // GitHub does not have a direct equivalent for creating comments and tasks
        throw new UnsupportedOperationException("GitHub does not support creating comments and tasks.");
    }

    @Override
    public String addPullRequestComment(String workspace, String repository, String pullRequestId, String text) throws IOException {
        String path = path(String.format("repos/%s/%s/issues/%s/comments", workspace, repository, pullRequestId));
        GenericRequest postRequest = new GenericRequest(this, path);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", text);
        postRequest.setBody(jsonObject.toString());
        return post(postRequest);
    }

    @Override
    public JSONModel commitComments(String workspace, String repository, String commitId) throws IOException {
        String path = path(String.format("repos/%s/%s/commits/%s/comments", workspace, repository, commitId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return new JSONModel();
        }
        return new JSONModel(response);
    }

    @Override
    public String renamePullRequest(String workspace, String repository, IPullRequest pullRequest, String newTitle) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s", workspace, repository, pullRequest.getId()));
        GenericRequest patchRequest = new GenericRequest(this, path);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", newTitle);
        patchRequest.setBody(jsonObject.toString());
        return patch(patchRequest);
    }

    @Override
    public List<ITag> getTags(String workspace, String repository) throws IOException {
        String path = path(String.format("repos/%s/%s/tags", workspace, repository));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(GithubTag.class, new JSONArray(response));
    }

    @Override
    public List<IRepository> getRepositories(String namespace) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public List<ITag> getBranches(String workspace, String repository) throws IOException {
        String path = path(String.format("repos/%s/%s/branches", workspace, repository));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(GithubTag.class, new JSONArray(response));
    }

    @Override
    public List<ICommit> getCommitsBetween(String workspace, String repository, String from, String to) throws IOException {
        String path = path(String.format("repos/%s/%s/compare/%s...%s", workspace, repository, from, to));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(Commit.class, new JSONObject(response).getJSONArray("commits"));
    }

    @Override
    public List<ICommit> getCommitsFromBranch(String workspace, String repository, String branchName) throws IOException {
        String path = path(String.format("repos/%s/%s/commits?sha=%s", workspace, repository, branchName));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(GitHubCommit.class, new JSONArray(response));
    }

    @Override
    public void performCommitsFromBranch(String workspace, String repository, String branchName, Performer<ICommit> performer) throws Exception {
        List<ICommit> commits = getCommitsFromBranch(workspace, repository, branchName);
        for (ICommit commit : commits) {
            if (performer.perform(commit)) {
                break;
            }
        }
    }

    @Override
    public IDiffStats getCommitDiffStat(String workspace, String repository, String commitId) throws IOException {
        return getCommitAsObject(workspace, repository, commitId);
    }

    @Override
    public IBody getCommitDiff(String workspace, String repository, String commitId) throws IOException {
        String response = getCommitAsDiff(workspace, repository, commitId);
        return () -> response;
    }

    @Override
    public IDiffStats getPullRequestDiff(String workspace, String repository, String pullRequestID) throws IOException {
        if (true) {
            return new IDiffStats.Empty();
        }
        synchronized (isDiff) {
            try {
                isDiff = true;
                try {
                    String pullRequestResponse = getPullRequestResponse(workspace, repository, pullRequestID);
                    return parseDiffStats(pullRequestResponse);
                } catch (AtlassianRestClient.JiraException e) {
                    e.printStackTrace();
                    return new IDiffStats.Empty();
                }
            } finally {
                isDiff = false;
            }
        }
    }

    public static IDiffStats parseDiffStats(String diff) {
        int addedLines = 0;
        int removedLines = 0;

        String[] lines = diff.split("\n");

        for (String line : lines) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                addedLines++;
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                removedLines++;
            }
        }

        int changedLines = Math.min(addedLines, removedLines);
        int finalRemovedLines = removedLines;
        int finalAddedLines = addedLines;
        return new IDiffStats() {
            @Override
            public IStats getStats() {
                return new IStats() {
                    @Override
                    public int getTotal() {
                        return finalAddedLines + finalRemovedLines;
                    }

                    @Override
                    public int getAdditions() {
                        return finalAddedLines;
                    }

                    @Override
                    public int getDeletions() {
                        return finalRemovedLines;
                    }
                };
            }

            @Override
            public List<IChange> getChanges() {
                return Collections.emptyList();
            }
        };
    }

    private GitHubCommit getCommitAsObject(String workspace, String repository, String commitId) throws IOException {
        String response = getCommit(workspace, repository, commitId);
        return new GitHubCommit(response);
    }

    private String getCommitAsDiff(String workspace, String repository, String commitId) throws IOException {
        synchronized (isDiff) {
            try {
                isDiff = true;
                return getCommit(workspace, repository, commitId);
            } finally {
                isDiff = false;
            }
        }
    }

    private String getCommit(String workspace, String repository, String commitId) throws IOException {
        String path = path(String.format("repos/%s/%s/commits/%s", workspace, repository, commitId));
        GenericRequest getRequest = new GenericRequest(this, path);
        return execute(getRequest);
    }

    @Override
    public IBody getDiff(String workspace, String repository, String pullRequestId) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s/files", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        return new IBody() {
            @Override
            public String getBody() {
                return response;
            }
        };
    }

    @Override
    public List<IFile> getListOfFiles(String workspace, String repository, String branchName) throws IOException {
        String path = path(String.format("repos/%s/%s/git/trees/%s?recursive=1", workspace, repository, branchName));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return Collections.emptyList();
        }
        return JSONModel.convertToModels(GitHubFile.class, new JSONObject(response).getJSONArray("tree"));
    }

    @Override
    public String getFileContent(String selfLink) throws IOException {
        GenericRequest getRequest = new GenericRequest(this, selfLink);
        String response = execute(getRequest);
        String content = new JSONModel(response).getString("content").replaceAll("\\r\\n|\\r|\\n", "");
        return JobRunner.decodeBase64(content);
    }

    @Override
    public String getDefaultRepository() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultBranch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultWorkspace() {
        throw new UnsupportedOperationException();
    }
}