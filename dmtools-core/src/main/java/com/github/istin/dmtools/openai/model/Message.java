package com.github.istin.dmtools.openai.model;

import com.github.istin.dmtools.broadcom.rally.model.RallyFields;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Message extends JSONModel {

    public Message() {
    }

    public Message(String json) throws JSONException {
        super(json);
    }

    public Message(JSONObject json) {
        super(json);
    }

    public String getContent() {
        return getString("content");
    }

}