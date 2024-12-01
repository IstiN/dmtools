package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.job.BaseJobParams;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpertParams extends BaseJobParams {

    public static final String OUTPUT_TYPE_COMMENT = "comment";
    public static final String OUTPUT_TYPE_FIELD = "field";

    public static final String PROJECT_CONTEXT = "projectContext";
    public static final String REQUEST = "request";
    public static final String OUTPUT_TYPE = "outputType";
    public static final String FIELD_NAME = "fieldName";

    public ExpertParams() {
    }

    public ExpertParams(String json) throws JSONException {
        super(json);
    }

    public ExpertParams(JSONObject json) {
        super(json);
    }

    public String getProjectContext() {
        return getString(PROJECT_CONTEXT);
    }

    public String getRequest() {
        return getString(REQUEST);
    }

    public ExpertParams setProjectContext(String projectContext) {
        set(PROJECT_CONTEXT, projectContext);
        return this;
    }

    public ExpertParams setRequest(String request) {
        set(REQUEST, request);
        return this;
    }

    public String getOutputType() {
        String outputType = getString(OUTPUT_TYPE);
        if (outputType == null) {
            return OUTPUT_TYPE_COMMENT;
        }
        return outputType;
    }

    public ExpertParams setOutputType(String outputType) {
        set(OUTPUT_TYPE, outputType);
        return this;
    }

    public String getFieldName() {
        return getString(FIELD_NAME);
    }

    public ExpertParams setFieldName(String fieldName) {
        set(FIELD_NAME, fieldName);
        return this;
    }

}