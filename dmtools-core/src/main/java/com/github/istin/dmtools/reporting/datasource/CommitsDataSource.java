package com.github.istin.dmtools.reporting.datasource;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import org.json.JSONObject;
import java.util.*;

public class CommitsDataSource extends DataSource {
    private final SourceCode sourceCode;
    private final Map<String, Object> params;

    public CommitsDataSource(SourceCode sourceCode, Map<String, Object> params) {
        this.sourceCode = sourceCode;
        this.params = params;
    }

    @Override
    public void performMetricCollection(Metric metric, KeyTimeCollector collector) throws Exception {
        SourceCollector sourceCollector = metric.getSourceCollector();
        if (sourceCollector == null || sourceCode == null) {
            return;
        }

        List<KeyTime> keyTimes = sourceCollector.performSourceCollection(
            metric.isPersonalized(),
            metric.getName()
        );

        for (KeyTime kt : keyTimes) {
            JSONObject metadata = new JSONObject();
            metadata.put("hash", kt.getKey());
            metadata.put("when", kt.getWhen().getTime());
            metadata.put("who", kt.getWho());
            collector.collect(Arrays.asList(kt), metadata, kt.getKey());
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
        return "commits";
    }
}
