package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.metrics.source.PullRequestsLinesOfCodeMetricSource;
import com.github.istin.dmtools.team.IEmployees;

public class LinesOfCodeMetric extends Metric {

    public static final String NAME = "Lines Of Code";

    public LinesOfCodeMetric(boolean isPersonalized, String workspace, String repo, String branch, Bitbucket bitbucket, IEmployees employees) {
        super(NAME, true, isPersonalized, new PullRequestsLinesOfCodeMetricSource(workspace, repo, bitbucket, branch, employees));
    }

    @Override
    public String getName() {
        return NAME;
    }


}
