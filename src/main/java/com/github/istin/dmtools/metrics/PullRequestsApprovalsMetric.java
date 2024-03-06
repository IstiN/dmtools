package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.metrics.source.PullRequestsApprovalsMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class PullRequestsApprovalsMetric extends CommonSourceCodeMetric {

    public static final String NAME = "Pull Requests Approvals";

    public PullRequestsApprovalsMetric(boolean isPersonlized, String workspace, String repo, Bitbucket bitbucket, IEmployees employees) {
        super(NAME, isPersonlized, workspace, repo, bitbucket, employees, new PullRequestsApprovalsMetricSource(workspace, repo, bitbucket, employees));
    }

    @Override
    public String getName() {
        return NAME;
    }

}