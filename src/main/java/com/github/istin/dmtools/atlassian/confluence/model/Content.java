package com.github.istin.dmtools.atlassian.confluence.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Content extends JSONModel {

    public static final String ID = "id";
    public static final String TITLE = "title";

    public static final String STORAGE = "storage";

    public static final String VERSION = "version";

    public Content() {
    }

    public Content(String json) throws JSONException {
        super(json);
    }

    public Content(JSONObject json) {
        super(json);
    }


    public String getId() {
        return getString(ID);
    }

    public String getTitle() {
        return getString(TITLE);
    }

    public Storage getStorage() {
        JSONObject body = getJSONObject("body");
        if (body == null) {
            return null;
        }
        return new Storage(body.getJSONObject(STORAGE));
    }

    public int getVersionNumber() {
        return getJSONObject(VERSION).getInt("number");
    }

    public String getViewUrl(String basePath) {
        return basePath + getJSONObject("_links").getString("webui");
    }

    public String getParentId() {
        List<Content> ancestors = getModels(Content.class, "ancestors");
        if (ancestors != null && !ancestors.isEmpty()) {
            return ancestors.get(ancestors.size()-1).getId();
        }
        return null;
    }
}
