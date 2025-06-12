package com.github.istin.dmtools.job;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.documentation.DocumentationGeneratorParams;
import com.github.istin.dmtools.estimations.JEstimatorParams;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class JobParams extends JSONModel {

    public static String NAME = "name";
    public static String PARAMS = "params";

    public JobParams() {

    }

    public JobParams(String json) throws JSONException {
        super(json);
    }

    public JobParams(JSONObject json) {
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

    public Object getParamsByClass(Class clazz) {
        if (JSONModel.class.isAssignableFrom(clazz)) {
            return getModel(clazz, PARAMS);
        } else {
            Gson gson = new Gson();
            return gson.fromJson(Objects.requireNonNull(getJSONObject(PARAMS)).toString(), clazz);
        }
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
