package com.github.istin.dmtools.job;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.documentation.DocumentationGeneratorParams;
import com.github.istin.dmtools.estimations.JEstimatorParams;
import org.json.JSONException;
import org.json.JSONObject;

public class Job extends JSONModel {

    public static String NAME = "name";
    public static String PARAMS = "params";

    public Job() {

    }

    public Job(String json) throws JSONException {
        super(json);
    }

    public Job(JSONObject json) {
        super(json);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        set(NAME, name);
    }

    public JSONObject getParams() {
        return getJSONObject(PARAMS);
    }

    public void setParams(JSONModel model) {
        set(PARAMS, model.getJSONObject());
    }

    public JEstimatorParams getJEstimatorParams() {
        return getModel(JEstimatorParams.class, PARAMS);
    }

    public DocumentationGeneratorParams getDocumentationGeneratorParams() {
        return getModel(DocumentationGeneratorParams.class, PARAMS);
    }

}
