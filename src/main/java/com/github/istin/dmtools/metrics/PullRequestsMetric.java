package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.metrics.source.PullRequestsMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class PullRequestsMetric extends CommonSourceCodeMetric {

    public static final String NAME = "Pull Requests";
    private Runnable runnable;

    public PullRequestsMetric(boolean isPersonlized, String workspace, String repo, Bitbucket bitbucket, IEmployees employees) {
        super(NAME, isPersonlized, workspace, repo, bitbucket, employees, new PullRequestsMetricSource(workspace, repo, bitbucket, employees));
    }

    @Override
    public String getName() {
        return NAME;
    }

}