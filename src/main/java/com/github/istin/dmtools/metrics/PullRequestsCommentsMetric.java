package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.metrics.source.PullRequestsCommentsMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class PullRequestsCommentsMetric extends CommonSourceCodeMetric {

    public static final String NAME_POSITIVE = "Pull Requests Comments Written";
    public static final String NAME_NEGATIVE = "Pull Requests Comments Gotten";
    private final boolean isPositive;

    public PullRequestsCommentsMetric(boolean isPersonlized, boolean isPositive, String workspace, String repo, SourceCode sourceCode, IEmployees employees) {
        super(NAME_POSITIVE, isPersonlized, workspace, repo, sourceCode, employees, new PullRequestsCommentsMetricSource(isPositive, workspace, repo, sourceCode, employees));
        this.isPositive = isPositive;
    }

    @Override
    public String getName() {
        return isPositive ? NAME_POSITIVE : NAME_NEGATIVE;
    }

}