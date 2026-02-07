package com.github.istin.dmtools.reporting.model;
import java.util.Map;

public class MetricConfig {
    private String name;
    private Map<String, Object> params;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
}
