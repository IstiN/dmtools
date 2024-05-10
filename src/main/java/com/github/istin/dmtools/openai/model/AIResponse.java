package com.github.istin.dmtools.openai.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AIResponse extends JSONModel {

    public AIResponse() {
    }

    public AIResponse(String json) throws JSONException {
        super(json);
    }

    public AIResponse(JSONObject json) {
        super(json);
    }

    public List<Choice> getChoices() {
        return getModels(Choice.class, "choices");
    }

}
