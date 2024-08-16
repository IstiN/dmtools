package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.IPullRequest;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.List;

public class PullRequestsMetricSource extends CommonSourceCollector {

    private final String workspace;
    private final String repo;
    private final SourceCode sourceCode;

    public PullRequestsMetricSource(String workspace, String repo, SourceCode sourceCode, IEmployees employees) {
        super(employees);
        this.workspace = workspace;
        this.repo = repo;
        this.sourceCode = sourceCode;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> data = new ArrayList<>();
        List<IPullRequest> pullRequests = sourceCode.pullRequests(workspace, repo, Bitbucket.PullRequestState.STATE_MERGED, true);
        for (IPullRequest pullRequest : pullRequests) {
            String displayName = transformName(pullRequest.getAuthor().getFullName());
            if (!isTeamContainsTheName(displayName)) {
                displayName = IEmployees.UNKNOWN;
            }
            String keyTimeOwner = isPersonalized ? displayName : metricName;
            KeyTime keyTime = new KeyTime(pullRequest.getId().toString(), IPullRequest.Utils.getClosedDateAsCalendar(pullRequest), keyTimeOwner);
            data.add(keyTime);
        }
        return data;
    }

}
