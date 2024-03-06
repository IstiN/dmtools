package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.metrics.source.PullRequestsCommentsMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class PullRequestsCommentsMetric extends CommonSourceCodeMetric {

    public static final String NAME = "Pull Requests Comments";
    private Runnable runnable;

    public PullRequestsCommentsMetric(boolean isPersonlized, String workspace, String repo, Bitbucket bitbucket, IEmployees employees) {
        super(NAME, isPersonlized, workspace, repo, bitbucket, employees, new PullRequestsCommentsMetricSource(workspace, repo, bitbucket, employees));
    }

    @Override
    public String getName() {
        return NAME;
    }

}