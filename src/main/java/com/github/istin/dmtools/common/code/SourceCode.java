package com.github.istin.dmtools.common.code;

import com.github.istin.dmtools.atlassian.bitbucket.model.*;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.networking.AbstractRestClient;

import java.io.IOException;
import java.util.List;

public interface SourceCode {

    List<PullRequest> pullRequests(String workspace, String repository, String state, boolean checkAllRequests) throws IOException;

    PullRequest pullRequest(String workspace, String repository, String pullRequestId) throws IOException;

    JSONModel pullRequestComments(String workspace, String repository, String pullRequestId) throws IOException;

    BitbucketResult pullRequestActivities(String workspace, String repository, String pullRequestId) throws IOException;

    BitbucketResult pullRequestTasks(String workspace, String repository, String pullRequestId) throws IOException;

    String addTask(Integer commentId, String text) throws IOException;

    String createPullRequestCommentAndTaskIfNotExists(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException;

    String createPullRequestCommentAndTask(String workspace, String repository, String pullRequestId, String commentText, String taskText) throws IOException;

    String addPullRequestComment(String workspace, String repository, String pullRequestId, String text) throws IOException;

    JSONModel commitComments(String workspace, String repository, String commitId) throws IOException;

    String renamePullRequest(String workspace, String repository, PullRequest pullRequest, String newTitle) throws IOException;

    List<Tag> getTags(String workspace, String repository) throws IOException;

    List<Tag> getBranches(String workspace, String repository) throws IOException;

    List<Commit> getCommitsBetween(String workspace, String repository, String from, String to) throws IOException;

    List<Commit> getCommitsFromBranch(String workspace, String repository, String branchName) throws IOException;

    void performCommitsFromBranch(String workspace, String repository, String branchName, AbstractRestClient.Performer<Commit> performer) throws Exception;

    BitbucketResult getCommitDiff(String workspace, String repository, String commitId) throws IOException;

    String getDiff(String workspace, String repository, String pullRequestId) throws IOException;

    List<File> getListOfFiles(String workspace, String repository, String branchName)  throws IOException;

    String getFileContent(String selfLink) throws IOException;

    String getDefaultRepository();
    String getDefaultBranch();
    String getDefaultWorkspace();

}
