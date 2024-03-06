package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.Commit;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.List;

public class CommitsMetricSource extends CommonSourceCollector {

    private final String workspace;
    private final String repo;
    private final String branch;
    private final Bitbucket bitbucket;

    public CommitsMetricSource(String workspace, String repo, String branch, Bitbucket bitbucket, IEmployees employees) {
        super(employees);
        this.workspace = workspace;
        this.repo = repo;
        this.branch = branch;
        this.bitbucket = bitbucket;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> data = new ArrayList<>();
        bitbucket.performCommitsFromBranch(workspace, repo, branch, new AtlassianRestClient.Performer<Commit>() {
            @Override
            public boolean perform(Commit model) {
                String displayName = transformName(model.getAuthor().getDisplayName());
                isNameIgnored(displayName);

                KeyTime keyTime = new KeyTime(model.getId(), model.getCommitterDate(), isPersonalized ? displayName : metricName);
                data.add(keyTime);
                return false;
            }

        });
        return data;
    }

}
