package com.github.istin.dmtools.openai.model;

import com.github.istin.dmtools.broadcom.rally.model.RallyFields;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Choice extends JSONModel {

    public Choice() {
    }

    public Choice(String json) throws JSONException {
        super(json);
    }

    public Choice(JSONObject json) {
        super(json);
    }

    public Message getMessage() {
        return getModel(Message.class, "message");
    }

}