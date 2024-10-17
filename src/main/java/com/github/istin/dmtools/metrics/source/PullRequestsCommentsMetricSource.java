package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.IActivity;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IPullRequest;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PullRequestsCommentsMetricSource extends CommonSourceCollector {

    private final boolean isPositive;
    private final String workspace;
    private final String repo;
    private final SourceCode sourceCode;

    public PullRequestsCommentsMetricSource(boolean isPositive, String workspace, String repo, SourceCode sourceCode, IEmployees employees) {
        super(employees);
        this.isPositive = isPositive;
        this.workspace = workspace;
        this.repo = repo;
        this.sourceCode = sourceCode;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> data = new ArrayList<>();
        List<IPullRequest> pullRequests = sourceCode.pullRequests(workspace, repo, IPullRequest.PullRequestState.STATE_MERGED, true);
        for (IPullRequest pullRequest : pullRequests) {

            String pullRequestAuthorDisplayName = pullRequest.getAuthor().getFullName();

            pullRequestAuthorDisplayName = getEmployees().transformName(pullRequestAuthorDisplayName);

            if (!isTeamContainsTheName(pullRequestAuthorDisplayName)) {
                pullRequestAuthorDisplayName = IEmployees.UNKNOWN;
            }

            String pullRequestIdAsString = pullRequest.getId().toString();
            List<IActivity> activities = sourceCode.pullRequestActivities(workspace, repo, pullRequestIdAsString);
            for (IActivity activity : activities) {
                IComment comment = activity.getComment();
                String action = null;
                String commentDisplayName = null;
                if (comment != null) {
                    commentDisplayName = comment.getAuthor().getFullName();

                    commentDisplayName = getEmployees().transformName(commentDisplayName);

                    if (getEmployees().isBot(commentDisplayName)) {
                        continue;
                    }

                    if (!isTeamContainsTheName(commentDisplayName)) {
                        commentDisplayName = IEmployees.UNKNOWN;
                    }

                    if (!pullRequestAuthorDisplayName.equalsIgnoreCase(commentDisplayName)) {
                        action = "Comments";
                    }
                }

                if (action != null) {
                    Calendar pullRequestClosedDateAsCalendar = IPullRequest.Utils.getClosedDateAsCalendar(pullRequest);
                    if (!isPositive) {
                        commentDisplayName = pullRequestAuthorDisplayName;
                    }
                    String keyTimeOwner = isPersonalized ? commentDisplayName : metricName;
                    KeyTime keyTime = new KeyTime(pullRequestIdAsString, pullRequestClosedDateAsCalendar, keyTimeOwner);

                    data.add(keyTime);
                }
            }

        }
        return data;
    }

}