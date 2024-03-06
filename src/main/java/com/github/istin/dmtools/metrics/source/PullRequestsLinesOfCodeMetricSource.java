package com.github.istin.dmtools.metrics.source;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.*;
import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;

import java.util.ArrayList;
import java.util.List;

public class PullRequestsLinesOfCodeMetricSource extends CommonSourceCollector {

    private final String workspace;
    private final String repo;
    private final Bitbucket bitbucket;

    private final String branchName;

    public PullRequestsLinesOfCodeMetricSource(String workspace, String repo, Bitbucket bitbucket, String branchName, IEmployees employees) {
        super(employees);
        this.workspace = workspace;
        this.repo = repo;
        this.bitbucket = bitbucket;
        this.branchName = branchName;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> data = new ArrayList<>();
        bitbucket.performCommitsFromBranch(workspace, repo, branchName, new AtlassianRestClient.Performer<Commit>() {
            @Override
            public boolean perform(Commit model) throws Exception {
                String displayName = model.getAuthor().getDisplayName();

                if (isNameIgnored(displayName)) {
                    return false;
                }

                displayName = transformName(displayName);
                KeyTime keyTime = new KeyTime(model.getId(), model.getCommitterDate(), isPersonalized ? displayName : metricName);
                BitbucketResult commitDiff = bitbucket.getCommitDiff(workspace, repo, model.getId());
                List<Diff> diffs = commitDiff.getDiffs();
                int amountOfLines = 0;
                for (Diff diff : diffs) {
                    if (!isValidFileCounted(diff.getSource())) {
                        continue;
                    }

                    List<Hunk> hunks = diff.getHunks();
                    for (Hunk hunk : hunks) {
                        List<Segment> segments = hunk.getSegments();
                        for (Segment segment : segments) {
                            amountOfLines = amountOfLines + segment.getLines().size();
                        }
                    }
                }
                keyTime.setWeight(amountOfLines/1000d);
                data.add(keyTime);
                return false;
            }
        });
        return data;
    }

    public static boolean isValidFileCounted(String source) {
        if (source.endsWith(".g.dart")) {
            return false;
        }
        if (source.endsWith(".freezed.dart")) {
            return false;
        }
        if (source.endsWith(".config.dart")) {
            return false;
        }
        boolean isSwaggerGenFile = source.startsWith("packages/horizon_services/lib/swagger_generated_code/");
        if (isSwaggerGenFile && source.endsWith("swagger.chopper.dart")) {
            return false;
        }
        if (isSwaggerGenFile && source.endsWith("swagger.dart")) {
            return false;
        }

        return true;
    }
}