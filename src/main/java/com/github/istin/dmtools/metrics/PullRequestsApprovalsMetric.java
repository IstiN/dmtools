package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.metrics.source.PullRequestsApprovalsMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class PullRequestsApprovalsMetric extends CommonSourceCodeMetric {

    public static final String NAME = "Pull Requests Approvals";

    public PullRequestsApprovalsMetric(boolean isPersonlized, String workspace, String repo, SourceCode sourceCode, IEmployees employees) {
        super(NAME, isPersonlized, workspace, repo, sourceCode, employees, new PullRequestsApprovalsMetricSource(workspace, repo, sourceCode, employees));
    }

    @Override
    public String getName() {
        return NAME;
    }

}