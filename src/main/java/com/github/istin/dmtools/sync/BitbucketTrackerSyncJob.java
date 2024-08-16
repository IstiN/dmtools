package com.github.istin.dmtools.sync;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IPullRequest;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.report.model.KeyTime;
import io.github.furstenheim.CopyDown;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class BitbucketTrackerSyncJob {
    private static final Logger logger = LogManager.getLogger(BitbucketTrackerSyncJob.class);
    public static interface StatusSyncDelegate {

        void onMerged(IPullRequest pullRequest, ITicket ticket, TrackerClient tracker) throws IOException;

        void onCreated(IPullRequest pullRequest, ITicket ticket, TrackerClient tracker) throws IOException;

    }

    public static void checkAndSyncPullRequests(SourceCode sourceCode, String workspace, String repository, String pullRequestState, IssuesIDsParser issuesIDsParser, TrackerClient tracker, Function<String, String> priorityToIcon, StatusSyncDelegate statusSyncDelegate) throws IOException {
        List<IPullRequest> bitbucketPullRequests = sourceCode.pullRequests(workspace, repository, pullRequestState, true);
        for (IPullRequest pullRequest : bitbucketPullRequests) {
            List<String> keys = issuesIDsParser.parseIssues(pullRequest.getTitle(), pullRequest.getSourceBranchName(), pullRequest.getDescription());
            boolean wasRenamed = false;
            for (String key : keys) {
                logger.info(key);
                wasRenamed = syncTicket(sourceCode, workspace, repository, pullRequestState, tracker, priorityToIcon, statusSyncDelegate, pullRequest, key, wasRenamed);
            }
        }
    }

    public static boolean syncTicket(SourceCode sourceCode, String workspace, String repository, String pullRequestState, TrackerClient tracker, Function<String, String> priorityToIcon, StatusSyncDelegate statusSyncDelegate, IPullRequest pullRequest, String key, boolean wasRenamed) throws IOException {
        ITicket ticket = null;
        try {
            ticket = tracker.performTicket(key, tracker.getDefaultQueryFields());
        } catch (AtlassianRestClient.JiraException ignored) {
            ignored.printStackTrace();
        }
        if (ticket == null) {
            return false;
        }


        if (pullRequestState.equals(Bitbucket.PullRequestState.STATE_MERGED)) {
            List<KeyTime> reopened = ChangelogAssessment.findDatesWhenTicketWasInStatus(tracker, key, ticket,"reopened");
            boolean wasReopened = false;
            if (!reopened.isEmpty()) {
                for (KeyTime keyTime : reopened) {
                    if (keyTime.getWhen().compareTo(IPullRequest.Utils.getCreatedDateAsCalendar(pullRequest)) > 0) {
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
                renamePullRequest(workspace, repository, sourceCode, pullRequest, ticket, priorityToIcon);
            }

            if (statusSyncDelegate != null) {
                statusSyncDelegate.onCreated(pullRequest, ticket, tracker);
            }


            addBitbucketCommentIfNotExists(sourceCode, pullRequest, key, ticket, workspace, repository);
        }
        addTrackerCommentIfNotExists(sourceCode, workspace, repository, tracker, pullRequest, key);
        return wasRenamed;
    }

    private static void renamePullRequest(String workspace, String repo, SourceCode sourceCode, IPullRequest pullRequest, ITicket ticket, Function<String, String> priorityToIcon) throws IOException {
        String summary = ticket.getTicketTitle();
        String issueType = ticket.getIssueType();
        if (IssueType.isBug(issueType)) {
            issueType = "\uD83D\uDC1E";
        } else {
            issueType = "\uD83D\uDD16";
        }
        String priority = priorityToIcon == null ? ticket.getPriority() : priorityToIcon.apply(ticket.getPriority());
        sourceCode.renamePullRequest(workspace, repo, pullRequest,  priority + " " + issueType + " " + ticket.getKey() + " " + summary);
    }

    private static void addTrackerCommentIfNotExists(SourceCode sourceCode, String workspace, String repository, TrackerClient tracker, IPullRequest pullRequest, String key) throws IOException {
        String url = sourceCode.getBasePath().replaceAll("api.", "") + "/" + workspace + "/" + repository + "/pull-requests/" + pullRequest.getId();
        String author = pullRequest.getAuthor().getFullName();
        if (tracker.getTextType() == TrackerClient.TextType.HTML) {
            String comment = "Merge request by <b>" + author + "</b> <br/> <a href=\"" + url + "\">" + url + "</a>";
            tracker.postCommentIfNotExists(key, comment);
        } else {
            String comment = "Merge request by <b>" + author + "</b> <br/>" + url;
            CopyDown converter = new CopyDown();
            tracker.postCommentIfNotExists(key, converter.convert(comment).replaceAll("\\*\\*", "*"));
        }
    }

    public static void addBitbucketCommentIfNotExists(SourceCode sourceCode, IPullRequest pullRequest, String key, ITicket ticket, String workspace, String repository) throws IOException {
        String ticketUrl = ticket.getTicketLink();
        String message = "[" + key + "]("+ticketUrl+")" + " " + ticket.getTicketTitle().replaceAll("\"", "") + " ";
        String pullRequestId = pullRequest.getId().toString();
        List<IComment> comments = sourceCode.pullRequestComments(workspace, repository, pullRequestId);
        if (IComment.Impl.checkCommentStartedWith(comments, message) != null) {
            sourceCode.addPullRequestComment(workspace, repository, pullRequestId, message);
        }
    }

}
