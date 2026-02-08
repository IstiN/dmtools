package com.github.istin.dmtools.reporting.datasource;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import org.json.JSONObject;
import java.util.*;

public class PullRequestsDataSource extends DataSource {
    private final SourceCode sourceCode;
    private final Map<String, Object> params;

    public PullRequestsDataSource(SourceCode sourceCode, Map<String, Object> params) {
        this.sourceCode = sourceCode;
        this.params = params;
    }

    @Override
    public void performMetricCollection(Metric metric, KeyTimeCollector collector) throws Exception {
        SourceCollector sourceCollector = metric.getSourceCollector();
        if (sourceCollector == null || sourceCode == null) {
            return;
        }

        // Build base URL for PR links from params
        String prBaseUrl = buildPrBaseUrl();

        List<KeyTime> keyTimes = sourceCollector.performSourceCollection(
            metric.isPersonalized(),
            metric.getName()
        );

        for (KeyTime kt : keyTimes) {
            JSONObject metadata = new JSONObject();
            metadata.put("key", kt.getKey());
            metadata.put("who", kt.getWho());
            if (kt.getSummary() != null) {
                metadata.put("summary", kt.getSummary());
            }
            // Build link: use KeyTime link if set, otherwise build from params
            String link = kt.getLink();
            if (link == null && prBaseUrl != null) {
                link = prBaseUrl + "/pull/" + kt.getKey();
            }
            if (link != null) {
                metadata.put("link", link);
            }
            collector.collect(Arrays.asList(kt), metadata, kt.getKey());
        }
    }

    private String buildPrBaseUrl() {
        if (params == null) return null;
        String sourceType = (String) params.getOrDefault("sourceType", "github");
        String workspace = (String) params.get("workspace");
        String repository = (String) params.get("repository");
        if (workspace == null || repository == null) return null;

        switch (sourceType.toLowerCase()) {
            case "gitlab":
                return "https://gitlab.com/" + workspace + "/" + repository;
            case "bitbucket":
                return "https://bitbucket.org/" + workspace + "/" + repository;
            default:
                return "https://github.com/" + workspace + "/" + repository;
        }
    }

    @Override
    public JSONObject extractRawMetadata(Object item) {
        if (item instanceof JSONModel) {
            return ((JSONModel) item).getJSONObject();
        }
        return new JSONObject();
    }

    @Override
    public String getSourceName() {
        return "pullRequests";
    }
}
