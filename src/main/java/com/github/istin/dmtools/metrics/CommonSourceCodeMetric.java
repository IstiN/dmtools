package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;

import java.io.IOException;
import java.util.List;

public class CommonSourceCodeMetric extends Metric {
    private String workspace;
    private String repo;

    private Bitbucket bitbucket;
    private IEmployees employees;

    public CommonSourceCodeMetric(String name, boolean isPersonalized, String workspace, String repo, Bitbucket bitbucket, IEmployees employees, SourceCollector sourceCollector) {
        super(name, true, isPersonalized, new TrackerRule<Ticket>() {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, Ticket ticket) throws IOException, Exception {
                return null;
            }
        }, sourceCollector);
        this.workspace = workspace;
        this.repo = repo;
        this.bitbucket = bitbucket;
        this.employees = employees;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getRepo() {
        return repo;
    }

    public Bitbucket getBitbucket() {
        return bitbucket;
    }

    public IEmployees getEmployees() {
        return employees;
    }

}
