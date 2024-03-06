package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.PullRequest;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.List;

public class PullRequestsMetricSource extends CommonSourceCollector {

    private final String workspace;
    private final String repo;
    private final Bitbucket bitbucket;

    public PullRequestsMetricSource(String workspace, String repo, Bitbucket bitbucket, IEmployees employees) {
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
            String displayName = transformName(pullRequest.getAuthor().getDisplayName());
            String keyTimeOwner = isPersonalized ? displayName : metricName;
            KeyTime keyTime = new KeyTime(pullRequest.getId().toString(), pullRequest.getClosedDateAsCalendar(), keyTimeOwner);
            data.add(keyTime);
        }
        return data;
    }

}
