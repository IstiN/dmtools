package com.github.istin.dmtools.reporting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;

public class ReportConfig {
    private String reportName;
    private String startDate;
    private String endDate;

    @JsonProperty("dataSources")
    private List<DataSourceConfig> dataSources;

    @JsonProperty("timeGrouping")
    @JsonDeserialize(using = TimeGroupingDeserializer.class)
    private List<TimeGroupingConfig> timeGroupings;

    @JsonProperty("aggregation")
    private AggregationConfig aggregation;

    @JsonProperty("output")
    private OutputConfig output;

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<DataSourceConfig> getDataSources() { return dataSources; }
    public void setDataSources(List<DataSourceConfig> dataSources) { this.dataSources = dataSources; }

    /**
     * Returns the list of time groupings. For backward compatibility,
     * a single-object JSON value is deserialized as a list with one element.
     */
    @JsonIgnore
    public List<TimeGroupingConfig> getTimeGroupings() {
        return timeGroupings != null ? timeGroupings : Collections.emptyList();
    }

    @JsonIgnore
    public void setTimeGroupings(List<TimeGroupingConfig> timeGroupings) {
        this.timeGroupings = timeGroupings;
    }

    /**
     * Backward-compatible getter. Returns the first time grouping or null.
     */
    @JsonIgnore
    public TimeGroupingConfig getTimeGrouping() {
        return timeGroupings != null && !timeGroupings.isEmpty() ? timeGroupings.get(0) : null;
    }

    /**
     * Backward-compatible setter. Sets a single time grouping as a list of one.
     */
    @JsonIgnore
    public void setTimeGrouping(TimeGroupingConfig timeGrouping) {
        this.timeGroupings = timeGrouping != null ? Collections.singletonList(timeGrouping) : null;
    }

    /**
     * Returns true if multiple time groupings are configured.
     */
    @JsonIgnore
    public boolean isMultiGrouping() {
        return timeGroupings != null && timeGroupings.size() > 1;
    }

    public AggregationConfig getAggregation() { return aggregation; }
    public void setAggregation(AggregationConfig aggregation) { this.aggregation = aggregation; }
    public OutputConfig getOutput() { return output; }
    public void setOutput(OutputConfig output) { this.output = output; }
}
