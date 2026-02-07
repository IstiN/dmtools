package com.github.istin.dmtools.reporting.metrics;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.TrackerRule;
import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.metrics.rules.BugsCreatorsRule;
import com.github.istin.dmtools.metrics.rules.TicketMovedToStatusRule;
import com.github.istin.dmtools.metrics.rules.TicketCreatorsRule;
import com.github.istin.dmtools.metrics.source.PullRequestsMetricSource;
import com.github.istin.dmtools.team.Employees;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating Metric instances from configuration
 */
public class MetricFactory {
    private final TrackerClient trackerClient;
    private final SourceCode sourceCode;

    public MetricFactory(TrackerClient trackerClient, SourceCode sourceCode) {
        this.trackerClient = trackerClient;
        this.sourceCode = sourceCode;
    }

    public Metric createMetric(String metricName, Map<String, Object> params, String dataSourceType) throws Exception {
        String label = (String) params.getOrDefault("label", metricName);
        boolean isWeight = (boolean) params.getOrDefault("isWeight", false);
        boolean isPersonalized = (boolean) params.getOrDefault("isPersonalized", false);

        if ("tracker".equals(dataSourceType)) {
            TrackerRule rule = createTrackerRule(metricName, params);
            return new Metric(label, isWeight, rule);
        } else if ("pullRequests".equals(dataSourceType) || "commits".equals(dataSourceType)) {
            SourceCollector collector = createSourceCollector(metricName, params);
            return new Metric(label, isWeight, isPersonalized, collector);
        }

        throw new IllegalArgumentException("Unknown data source type: " + dataSourceType);
    }

    private TrackerRule createTrackerRule(String metricName, Map<String, Object> params) {
        Employees employees = Employees.getInstance();

        switch (metricName) {
            case "BugsCreatorsRule":
                String project = (String) params.get("project");
                return new BugsCreatorsRule(project, employees);

            case "TicketMovedToStatusRule":
                Object statusesObj = params.get("statuses");
                String[] statuses;
                if (statusesObj instanceof List) {
                    List<String> statusList = (List<String>) statusesObj;
                    statuses = statusList.toArray(new String[0]);
                } else if (statusesObj instanceof String[]) {
                    statuses = (String[]) statusesObj;
                } else {
                    statuses = new String[]{statusesObj.toString()};
                }
                return new TicketMovedToStatusRule(statuses);

            case "TicketCreatorsRule":
                String creatorProject = (String) params.getOrDefault("project", null);
                return new TicketCreatorsRule(creatorProject, employees);

            default:
                throw new IllegalArgumentException("Unknown tracker rule: " + metricName);
        }
    }

    private SourceCollector createSourceCollector(String metricName, Map<String, Object> params) {
        Employees employees = Employees.getInstance();

        if (sourceCode == null) {
            throw new IllegalArgumentException("SourceCode is not configured");
        }

        String workspace = (String) params.getOrDefault("workspace", sourceCode.getDefaultWorkspace());
        String repository = (String) params.getOrDefault("repository", sourceCode.getDefaultRepository());

        switch (metricName) {
            case "PullRequestsMetricSource":
                Calendar prStartDate = parseDateParam(params.get("since"));
                return new PullRequestsMetricSource(workspace, repository, sourceCode, employees, prStartDate);

            default:
                throw new IllegalArgumentException("Unknown source collector: " + metricName + ". Note: CommitsMetricSource not yet supported.");
        }
    }

    private Calendar parseDateParam(Object dateParam) {
        if (dateParam == null) {
            return null;
        }

        if (dateParam instanceof Calendar) {
            return (Calendar) dateParam;
        }

        String dateStr = dateParam.toString();
        Calendar cal = Calendar.getInstance();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            cal.setTime(sdf.parse(dateStr));
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateStr);
            return null;
        }
        return cal;
    }
}
