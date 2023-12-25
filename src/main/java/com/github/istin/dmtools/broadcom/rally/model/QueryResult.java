package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class QueryResult extends JSONModel {

    private static final String RESULTS = "Results";

    public QueryResult() {
    }

    public QueryResult(String json) throws JSONException {
        super(json);
    }

    public QueryResult(JSONObject json) {
        super(json);
    }

    public List<RallyIssue> getIssues() {
        return getModels(RallyIssue.class, RESULTS);
    }

    public JSONArray getErrors() {
        return getJSONArray("Errors");
    }

    public int getTotalResultCount() {
        return getInt("TotalResultCount");
    }

    public int getPageSize() {
        return getInt("PageSize");
    }
}
