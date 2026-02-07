package com.github.istin.dmtools.reporting.datasource;

import com.github.istin.dmtools.report.model.KeyTime;
import org.json.JSONObject;
import java.util.List;

@FunctionalInterface
public interface KeyTimeCollector {
    void collect(List<KeyTime> keyTimes, JSONObject rawMetadata, String itemKey);
}
