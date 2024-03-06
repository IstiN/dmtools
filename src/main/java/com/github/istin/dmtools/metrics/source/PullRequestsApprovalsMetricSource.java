package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.Activity;
import com.github.istin.dmtools.atlassian.bitbucket.model.Approval;
import com.github.istin.dmtools.atlassian.bitbucket.model.BitbucketResult;
import com.github.istin.dmtools.atlassian.bitbucket.model.PullRequest;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PullRequestsApprovalsMetricSource extends CommonSourceCollector {

    private final String workspace;
    private final String repo;
    private final Bitbucket bitbucket;

    public PullRequestsApprovalsMetricSource(String workspace, String repo, Bitbucket bitbucket, IEmployees employees) {
        super(employees);
        this.workspace = workspace;
        this.repo = repo;
        this.bitbucket = bitbucket;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> data = new ArrayList<>();
        List<PullRequest> pullRequests = bitbucket.pullRequests(workspace, repo, Bitbucket.PullRequestState.STATE_MERGED, true);
        for (PullRequest pullRequest : pullRequests) {

            String displayName = pullRequest.getAuthor().getDisplayName();

            displayName = getEmployees().transformName(displayName);

            String pullRequestIdAsString = pullRequest.getId().toString();
            BitbucketResult bitbucketResult = bitbucket.pullRequestActivities(workspace, repo, pullRequestIdAsString);
            List<Activity> activities = bitbucketResult.getActivities();
            for (Activity activity : activities) {
                String action = null;

                Approval approval = activity.getApproval();
                if (approval != null) {
                    displayName = getEmployees().transformName(approval.getUser().getDisplayName());
                    action = "Approvals";
                }

                if (action != null) {
                    Calendar pullRequestClosedDateAsCalendar = pullRequest.getClosedDateAsCalendar();
                    String keyTimeOwner = isPersonalized ? displayName : metricName;

                    KeyTime keyTime = new KeyTime(pullRequestIdAsString, pullRequestClosedDateAsCalendar, keyTimeOwner);
                    data.add(keyTime);
                }
            }
        }
        return data;
    }

}