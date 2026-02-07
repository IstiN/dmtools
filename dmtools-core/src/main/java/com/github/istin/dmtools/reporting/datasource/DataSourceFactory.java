package com.github.istin.dmtools.reporting.datasource;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating data sources from configuration
 */
public class DataSourceFactory {

    public DataSourceFactory() {
    }

    public DataSource createDataSource(
        String name,
        Map<String, Object> params,
        TrackerClient trackerClient,
        SourceCode sourceCode
    ) {
        switch (name) {
            case "tracker":
                String jql = (String) params.get("jql");
                return new TrackerDataSource(trackerClient, jql);

            case "pullRequests":
                return new PullRequestsDataSource(sourceCode, params);

            case "commits":
                return new CommitsDataSource(sourceCode, params);

            default:
                throw new IllegalArgumentException("Unknown data source: " + name);
        }
    }
}
