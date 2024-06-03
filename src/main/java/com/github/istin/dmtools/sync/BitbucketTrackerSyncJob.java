package com.github.istin.dmtools.sync;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.PullRequest;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.tracker.model.Status;
import com.github.istin.dmtools.report.model.KeyTime;
import io.github.furstenheim.CopyDown;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class BitbucketTrackerSyncJob {

    public static interface StatusSyncDelegate {

        void onMerged(PullRequest pullRequest, ITicket ticket, TrackerClient tracker) throws IOException;

        void onCreated(PullRequest pullRequest, ITicket ticket, TrackerClient tracker) throws IOException;

    }

    public static void checkAndSyncPullRequests(Bitbucket bitbucket, String workspace, String repository, String pullRequestState, IssuesIDsParser issuesIDsParser, TrackerClient tracker, Function<String, String> priorityToIcon, StatusSyncDelegate statusSyncDelegate) throws IOException {
        List<PullRequest> bitbucketPullRequests = bitbucket.pullRequests(workspace, repository, pullRequestState, true);
        for (PullRequest pullRequest : bitbucketPullRequests) {
            List<String> keys = issuesIDsParser.parseIssues(pullRequest.getTitle(), pullRequest.getSourceBranchName(), pullRequest.getDescription());
            boolean wasRenamed = false;
            for (String key : keys) {
                System.out.println(key);
                wasRenamed = syncTicket(bitbucket, workspace, repository, pullRequestState, tracker, priorityToIcon, statusSyncDelegate, pullRequest, key, wasRenamed);
            }
        }
    }

    public static boolean syncTicket(Bitbucket bitbucket, String workspace, String repository, String pullRequestState, TrackerClient tracker, Function<String, String> priorityToIcon, StatusSyncDelegate statusSyncDelegate, PullRequest pullRequest, String key, boolean wasRenamed) throws IOException {
        ITicket ticket = tracker.performTicket(key, tracker.getDefaultQueryFields());
        if (ticket == null) {
            return false;
        }


        if (pullRequestState.equals(Bitbucket.PullRequestState.STATE_MERGED)) {
            List<KeyTime> reopened = ChangelogAssessment.findDatesWhenTicketWasInStatus(tracker, key, ticket,"reopened");
            boolean wasReopened = false;
            if (!reopened.isEmpty()) {
                for (KeyTime keyTime : reopened) {
                    if (keyTime.getWhen().compareTo(pullRequest.getCreatedDateAsCalendar()) > 0) {
                        wasReopened = true;
                    }
                }
            }
            if (wasReopened) {
                return false;
            }

            if (statusSyncDelegate != null) {
                statusSyncDelegate.onMerged(pullRequest, ticket, tracker);
            }
        } else {
            if (!wasRenamed) {
                wasRenamed = true;
                renamePullRequest(workspace, repository, bitbucket, pullRequest, ticket, priorityToIcon);
            }

            if (statusSyncDelegate != null) {
                statusSyncDelegate.onCreated(pullRequest, ticket, tracker);
            }


            addBitbucketCommentIfNotExists(bitbucket, pullRequest, key, ticket, workspace, repository);
        }
        addTrackerCommentIfNotExists(bitbucket, workspace, repository, tracker, pullRequest, key);
        return wasRenamed;
    }

    private static void renamePullRequest(String workspace, String repo, Bitbucket bitbucket, PullRequest pullRequest, ITicket ticket, Function<String, String> priorityToIcon) throws IOException {
        String summary = ticket.getTicketTitle();
        String issueType = ticket.getIssueType();
        if (IssueType.isBug(issueType)) {
            issueType = "\uD83D\uDC1E";
        } else {
            issueType = "\uD83D\uDD16";
        }
        String priority = priorityToIcon == null ? ticket.getPriority() : priorityToIcon.apply(ticket.getPriority());
        bitbucket.renamePullRequest(workspace, repo, pullRequest,  priority + " " + issueType + " " + ticket.getKey() + " " + summary);
    }

    private static void addTrackerCommentIfNotExists(Bitbucket bitbucket, String workspace, String repository, TrackerClient tracker, PullRequest pullRequest, String key) throws IOException {
        String url = bitbucket.getBasePath().replaceAll("api.", "") + "/" + workspace + "/" + repository + "/pull-requests/" + pullRequest.getId();
        String author = pullRequest.getAuthor().getDisplayName();
        if (tracker.getTextType() == TrackerClient.TextType.HTML) {
            String comment = "Merge request by <b>" + author + "</b> <br/> <a href=\"" + url + "\">" + url + "</a>";
            tracker.postCommentIfNotExists(key, comment);
        } else {
            String comment = "Merge request by <b>" + author + "</b> <br/>" + url;
            CopyDown converter = new CopyDown();
            tracker.postCommentIfNotExists(key, converter.convert(comment).replaceAll("\\*\\*", "*"));
        }
    }

    public static void addBitbucketCommentIfNotExists(Bitbucket bitbucket, PullRequest pullRequest, String key, ITicket ticket, String workspace, String repository) throws IOException {
        String ticketUrl = ticket.getTicketLink();
        String message = "[" + key + "]("+ticketUrl+")" + " " + ticket.getTicketTitle().replaceAll("\"", "") + " ";
        String pullRequestId = pullRequest.getId().toString();
        JSONModel comments = bitbucket.pullRequestComments(workspace, repository, pullRequestId);
        if (!comments.toString().replaceAll("\\\\\"", "").contains(message)) {
            bitbucket.addPullRequestComment(workspace, repository, pullRequestId, message);
        }
    }

}
