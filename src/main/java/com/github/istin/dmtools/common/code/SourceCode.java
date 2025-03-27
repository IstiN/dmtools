package com.github.istin.dmtools.common.code;

import com.github.istin.dmtools.atlassian.bitbucket.BasicBitbucket;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.github.BasicGithub;
import com.github.istin.dmtools.gitlab.BasicGitLab;
import com.github.istin.dmtools.networking.AbstractRestClient;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public interface SourceCode {

    List<IPullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests, Calendar startDate) throws IOException;

    IPullRequest pullRequest(String workspace, String repository, String pullRequestId) throws IOException;

    List<IComment> pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException;

    List<IActivity> pullRequestActivities(String workspace, String repository, String pullRequestId) throws IOException;

    List<ITask> pullRequestTasks(String workspace, String repository, String pullRequestId) throws IOException;

    String addTask(Integer commentId, String text) throws IOException;

    String createPullRequestCommentAndTaskIfNotExists(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException;

    String createPullRequestCommentAndTask(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException;

    String addPullRequestComment(String workspace, String repository, String pullRequestId, String text) throws IOException;

    JSONModel commitComments(String workspace, String repository, String commitId) throws IOException;

    String renamePullRequest(String workspace, String repository, IPullRequest pullRequest, String newTitle) throws IOException;

    List<ITag> getTags(String workspace, String repository) throws IOException;

    List<ITag> getBranches(String workspace, String repository) throws IOException;

    void addPullRequestLabel(String workspace, String repository, String pullRequestId, String label) throws IOException;

    List<IRepository> getRepositories(String namespace) throws IOException;

    List<ICommit> getCommitsBetween(String workspace, String repository, String from, String to) throws IOException;

    List<ICommit> getCommitsFromBranch(String workspace, String repository, String branchName, String startDate, String endDate) throws IOException;

    void performCommitsFromBranch(String workspace, String repository, String branchName, AbstractRestClient.Performer<ICommit> performer) throws Exception;

    IDiffStats getCommitDiffStat(String workspace, String repository, String commitId) throws IOException;

    IDiffStats getPullRequestDiff(String workspace, String repository, String pullRequestID) throws IOException;

    IBody getCommitDiff(String workspace, String repository, String commitId) throws IOException;

    IBody getDiff(String workspace, String repository, String pullRequestId) throws IOException;

    List<IFile> getListOfFiles(String workspace, String repository, String branchName)  throws IOException;

    String getFileContent(String selfLink) throws IOException;

    String getDefaultRepository();

    String getDefaultBranch();

    String getDefaultWorkspace();

    boolean isConfigured();

    String getBasePath();

    String getPullRequestUrl(String workspace, String repository, String id);

    List<IFile> searchFiles(String workspace, String repository, String query, int filesLimit) throws IOException, InterruptedException;

    SourceCodeConfig getDefaultConfig();

    /**
     * Adds a review comment to a pull request in the specified repository and workspace.
     *
     * This method is used to leave a comment on a specific line of a file within a pull request.
     * Comments can be added to either the line specified or a range of lines (if `startLine` is provided).
     *
     * @param workspace     The workspace or repository owner where the pull request resides.
     *                      For example, "myworkspace" or "myorganization".
     * @param repository    The name of the repository containing the pull request.
     *                      For example, "my-repo".
     * @param pullRequestId The ID of the pull request to which the comment should be added.
     * @param commitId      The ID of the commit associated with the pull request where the comment is being added.
     * @param filePath      The relative path to the file being commented on, within the repository.
     *                      For example, "src/main/java/MyFile.java".
     * @param line          The line number in the file where the comment should be added.
     *                      This is the target line for the comment on the "RIGHT" side of the diff.
     * @param startLine     (Optional) The starting line number for a range being commented on.
     *                      If specified and differs from the `line`, it represents a multi-line comment range.
     *                      Pass `null` to specify only a single-line comment.
     * @param comment       The text of the comment to be added to the pull request.
     *                      This is the content or body of your review comment.
     * @return              A string response from the server, representing the result of the comment creation.
     * @throws IOException  If there is a failure in network communication or an error occurs during the request.
     */
    default String addPullRequestReviewComment(String workspace, String repository, String pullRequestId, String commitId, String filePath, int line, Integer startLine, String comment) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * Retrieves a list of commits associated with a pull request from the specified repository.
     *
     * <p>This method constructs the API endpoint path based on the provided workspace, repository,
     * and pull request ID, then sends a GET request to fetch the list of commits. If the response
     * is null, an empty list is returned. Otherwise, the response is parsed into a list of commit
     * objects of type {@code ICommit}.
     *
     * @param workspace The workspace (or organization) identifier. Must not be null or empty.
     * @param repository The name of the repository where the pull request exists. Must not be null or empty.
     * @param pullRequestId The ID of the pull request whose commits need to be retrieved. Must not be null or empty.
     * @return A list of {@code ICommit} objects representing the commits in the specified pull request.
     *         If no commits are found or if the response is null, an empty list is returned.
     * @throws IOException If an I/O error occurs while making the API request, or if the response
     *         cannot be parsed correctly.
     * @see JSONModel#convertToModels(Class, org.json.JSONArray) For conversion of JSON data to model objects.
     */
    default List<ICommit> getCommitsFromPullRequest(String workspace, String repository, String pullRequestId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    class Impl {

        public static List<SourceCode> getConfiguredSourceCodes(JSONArray sources) throws IOException {
            List<SourceCode> sourceCodes = new ArrayList<>();
            if (sources == null || sources.isEmpty()) {
                addGitHubSource(sourceCodes);
                addBitbucketSource(sourceCodes);
                addGitlabSource(sourceCodes);
            } else {
                for (int i = 0; i < sources.length(); i++) {
                    String sourceCode = sources.getString(i);
                    if (sourceCode.equalsIgnoreCase("github")) {
                        addGitHubSource(sourceCodes);
                    } else if (sourceCode.equalsIgnoreCase("bitbucket")) {
                        addBitbucketSource(sourceCodes);
                    } else if (sourceCode.equalsIgnoreCase("gitlab")) {
                        addGitlabSource(sourceCodes);
                    }
                }
            }

            return sourceCodes;
        }

        private static void addGitHubSource(List<SourceCode> sourceCodes) throws IOException {
            SourceCode githubSourceCode = BasicGithub.getInstance();
            if (githubSourceCode.isConfigured()) {
                sourceCodes.add(githubSourceCode);
            }
        }

    }

    static void addBitbucketSource(List<SourceCode> sourceCodes) throws IOException {
        SourceCode bitbucketSourceCode = BasicBitbucket.getInstance();
        if (bitbucketSourceCode.isConfigured()) {
            sourceCodes.add(bitbucketSourceCode);
        }
    }

    static void addGitlabSource(List<SourceCode> sourceCodes) throws IOException {
        SourceCode gitlabSourceCode = BasicGitLab.getInstance();
        if (gitlabSourceCode.isConfigured()) {
            if (gitlabSourceCode.getDefaultRepository() == null) {
                List<IRepository> repositories = gitlabSourceCode.getRepositories(gitlabSourceCode.getDefaultWorkspace());
                for (IRepository repository : repositories) {
                    sourceCodes.add(new BasicGitLab() {
                        @Override
                        public String getDefaultBranch() {
                            return "main";
                        }

                        @Override
                        public String getDefaultRepository() {
                            return repository.getName();
                        }
                    });
                }
            } else {
                sourceCodes.add(gitlabSourceCode);
            }
        }
    }
}