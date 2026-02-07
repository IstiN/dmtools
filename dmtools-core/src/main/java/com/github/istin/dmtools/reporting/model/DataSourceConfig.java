package com.github.istin.dmtools.reporting.model;
import java.util.List;
import java.util.Map;

public class DataSourceConfig {
    private String name;
    private Map<String, Object> params;
    private List<MetricConfig> metrics;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
    public List<MetricConfig> getMetrics() { return metrics; }
    public void setMetrics(List<MetricConfig> metrics) { this.metrics = metrics; }
}
