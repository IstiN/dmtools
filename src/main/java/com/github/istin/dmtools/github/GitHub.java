package com.github.istin.dmtools.github;

import com.github.istin.dmtools.atlassian.bitbucket.model.Commit;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.PropertyReader;
import com.github.istin.dmtools.github.model.*;
import com.github.istin.dmtools.job.JobRunner;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GitHub extends AbstractRestClient implements SourceCode {
    private static final Logger logger = LogManager.getLogger(GitHub.class);
    private static final String API_VERSION = "v3";
    private static final boolean IS_READ_PULL_REQUEST_DIFF;

    static {
        PropertyReader propertyReader = new PropertyReader();
        IS_READ_PULL_REQUEST_DIFF = propertyReader.isReadPullRequestDiff();
    }

    public GitHub(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    @Override
    public String path(String path) {
        return getBasePath() + "/" + path;
    }

    @Override
    public synchronized Request.Builder sign(Request.Builder builder) {
        return builder
                .header("Authorization", "Bearer " + authorization)
                .header("Accept","application/vnd.github.v3+json")
                .header("Content-Type", "application/json");
    }

    @Override
    public List<IPullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests, Calendar startDate) throws IOException {
        boolean isMerged = state.equalsIgnoreCase(IPullRequest.PullRequestState.STATE_MERGED);
        if (isMerged) {
            state = "closed";
        } else if (state.equalsIgnoreCase(IPullRequest.PullRequestState.STATE_OPEN)) {
            state = "open";
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

            JSONArray pullRequestsInResponse = new JSONArray(response);
            List<GitHubPullRequest> pullRequests = JSONModel.convertToModels(GitHubPullRequest.class, pullRequestsInResponse);
            if (isMerged) {
                pullRequests = pullRequests.stream()
                        .filter(GitHubPullRequest::isMerged)
                        .collect(Collectors.toList());
            }
            allPullRequests.addAll(pullRequests);

            if (startDate != null && !pullRequests.isEmpty() && pullRequests.getLast().getCreatedDate() < startDate.getTimeInMillis()) {
                break;
            }

            if (!checkAllRequests || pullRequestsInResponse.length() < perPage) {
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
        String response = getPullRequestResponse(workspace, repository, pullRequestId, false);
        return new GitHubPullRequest(response);
    }

    public String triggerAction(String workspace, String repository, JSONObject params) throws IOException {
        String path = path(String.format("repos/%s/%s/dispatches", workspace, repository));
        GenericRequest postRequest = new GenericRequest(this, path);
        postRequest.setBody(params.toString());
        return post(postRequest);
    }

    private String getPullRequestResponse(String workspace, String repository, String pullRequestId, boolean isDiff) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        if (isDiff) {
            addDiffHeader(getRequest);
        }
        return execute(getRequest);
    }

    private static void addDiffHeader(GenericRequest getRequest) {
        getRequest.header("Accept", "application/vnd.github.diff");
    }

    @Override
    public List<IComment> pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException {
        String path = path(String.format("repos/%s/%s/pulls/%s/comments", workspace, repository, pullRequestId));
        GenericRequest getRequest = new GenericRequest(this, path);
        String response = execute(getRequest);
        if (response == null) {
            return new ArrayList<>();
        }
        List<IComment> comments = JSONModel.convertToModels(GitHubComment.class, new JSONArray(response));
        comments.addAll(pullRequestCommentsFromIssue(workspace, repository, pullRequestId));
        comments.sort((c1, c2) -> c1.getCreated().compareTo(c2.getCreated()));
        return comments;
    }

    public List<IComment> pullRequestCommentsFromIssue(String workspace, String repository, String pullRequestId) throws IOException {
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
        String updatedTitleIfWip = IPullRequest.Utils.upgradeTitleIfWip(pullRequest, newTitle);

        if (pullRequest.getTitle().equalsIgnoreCase(updatedTitleIfWip)) {
            return "";
        }

        String path = path(String.format("repos/%s/%s/pulls/%s", workspace, repository, pullRequest.getId()));
        GenericRequest patchRequest = new GenericRequest(this, path);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", updatedTitleIfWip);
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
    public void addPullRequestLabel(String workspace, String repository, String pullRequestId, String label) throws IOException {
        String path = path(String.format("repos/%s/%s/issues/%s/labels", workspace, repository, pullRequestId));
        GenericRequest postRequest = new GenericRequest(this, path);
        JSONArray labelsArray = new JSONArray();
        labelsArray.put(label);
        postRequest.setBody(labelsArray.toString());
        post(postRequest);
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
    public List<ICommit> getCommitsFromBranch(String workspace, String repository, String branchName, String startDate, String endDate) throws IOException {
        List<ICommit> allCommits = new ArrayList<>();
        int page = 1;
        boolean hasMoreData = true;
        int perPage = 100; // Set a default value for perPage

        while (hasMoreData) {
            // Build the URL with optional date filtering and pagination
            StringBuilder path = new StringBuilder(String.format("repos/%s/%s/commits?sha=%s", workspace, repository, branchName));

            if (startDate != null && !startDate.isEmpty()) {
                path.append("&since=").append(startDate).append("T00:00:00Z");
            }
            if (endDate != null && !endDate.isEmpty()) {
                path.append("&until=").append(endDate).append("T23:59:59Z");
            }

            // Add pagination parameters
            path.append("&page=").append(page)
                    .append("&per_page=").append(perPage);

            GenericRequest getRequest = new GenericRequest(this, path(path.toString()));
            String response = execute(getRequest);

            if (response == null || response.isEmpty()) {
                break;
            }

            List<ICommit> commits = JSONModel.convertToModels(GitHubCommit.class, new JSONArray(response));

            if (commits.isEmpty()) {
                hasMoreData = false;
            } else {
                allCommits.addAll(commits);
                page++;
            }
        }

        return allCommits;
    }

    @Override
    public void performCommitsFromBranch(String workspace, String repository, String branchName, Performer<ICommit> performer) throws Exception {
        List<ICommit> commits = getCommitsFromBranch(workspace, repository, branchName, null, null);
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
        if (!IS_READ_PULL_REQUEST_DIFF) {
            return new IDiffStats.Empty();
        }
        try {
            String pullRequestResponse = getPullRequestResponse(workspace, repository, pullRequestID, true);
            return parseDiffStats(pullRequestResponse);
        } catch (AtlassianRestClient.RestClientException e) {
            e.printStackTrace();
            return new IDiffStats.Empty();
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
        String response = getCommit(workspace, repository, commitId, false);
        return new GitHubCommit(response);
    }

    private String getCommitAsDiff(String workspace, String repository, String commitId) throws IOException {
        return getCommit(workspace, repository, commitId, true);
    }

    private String getCommit(String workspace, String repository, String commitId, boolean isDiff) throws IOException {
        String path = path(String.format("repos/%s/%s/commits/%s", workspace, repository, commitId));
        GenericRequest getRequest = new GenericRequest(this, path);
        if (isDiff) {
            addDiffHeader(getRequest);
        }
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

    public String getSingleFileContent(String workspace, String repository, String branchName, String filePath) throws IOException {
        String path = path(String.format("repos/%s/%s/contents/%s", workspace, repository, filePath));
        GenericRequest getRequest = new GenericRequest(this, path);

        // Add a query parameter for the branch
        getRequest.param("ref", branchName);

        String response = execute(getRequest);
        if (response == null) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        JSONObject jsonResponse = new JSONObject(response);
        String content = jsonResponse.optString("content", "");
        String encoding = jsonResponse.optString("encoding", "");

        if ("base64".equalsIgnoreCase(encoding)) {
            return JobRunner.decodeBase64(content.replaceAll("\\r\\n|\\r|\\n", ""));
        } else {
            // If it's not base64 encoded, return the content as is
            return content;
        }
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

    @Override
    public String getPullRequestUrl(String workspace, String repository, String id) {
        return getBasePath().replaceAll("api.", "") + "/" + workspace + "/" + repository + "/pull/" + id;
    }

    @Override
    public List<IFile> searchFiles(String workspace, String repository, String query) throws IOException, InterruptedException {
        List<IFile> allFiles = new ArrayList<>();
        int perPage = 100; // GitHub's max items per page
        int page = 1;
        int maxResults = 1000; // Limit on the total number of items

        while (allFiles.size() < maxResults) {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String path = path(String.format("search/code?q=%s+repo:%s/%s&per_page=%d&page=%d",
                    encodedQuery, workspace, repository, perPage, page));

            GenericRequest getRequest = new GenericRequest(this, path);
            getRequest.header("Accept", "application/vnd.github.v3.text-match+json");
            String response = null;

            try {
                response = execute(getRequest);
            } catch (RateLimitException rateLimitException) {
                Response errorResponse = rateLimitException.getResponse();
                String resetTimeStr = errorResponse.header("X-RateLimit-Reset");

                if (resetTimeStr != null) {
                    long resetTime = Long.parseLong(resetTimeStr) * 1000; // Convert seconds to milliseconds
                    long currentTime = System.currentTimeMillis();
                    long waitTime = resetTime - currentTime;

                    if (waitTime > 0) {
                        logger.warn("Rate limit reached. Waiting for " + (waitTime / 1000) + " seconds.");
                        Thread.sleep(waitTime + 1000);
                    }
                } else {
                    logger.warn("Rate limit reached. Default waiting for 60 seconds.");
                    Thread.sleep(60000);
                }
                response = execute(getRequest);
            }

            if (response == null) {
                break;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");

            if (items.length() == 0) {
                break;
            }

            List<IFile> pageFiles = JSONModel.convertToModels(GitHubFile.class, items);
            allFiles.addAll(pageFiles);

            if (allFiles.size() >= maxResults || items.length() < perPage) {
                // Trim the list if necessary
                if (allFiles.size() > maxResults) {
                    return allFiles.subList(0, maxResults);
                }
                break;
            }

            page++;
        }

        return allFiles;
    }

    private boolean isRateLimited(JSONObject response) {
        if (response.has("rate")) {
            JSONObject rate = response.getJSONObject("rate");
            int remaining = rate.getInt("remaining");
            return remaining == 0;
        }
        return false;
    }
}