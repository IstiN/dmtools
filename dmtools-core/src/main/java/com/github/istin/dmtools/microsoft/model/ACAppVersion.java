package com.github.istin.dmtools.microsoft.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class ACAppVersion extends JSONModel implements AppVersion {

    private static final String VERSION = "version";

    private static final String ID = "id";

    private static final String TITLE = "version";

    private static final String SHORT_VERSION = "short_version";

    public ACAppVersion() {
    }

    public ACAppVersion(String json) throws JSONException {
        super(json);
    }

    public ACAppVersion(JSONObject json) {
        super(json);
    }

    @Override
    public Long getId() {
        return getLong(ID);
    }

    @Override
    public String getVersion() {
        return getString(VERSION);
    }

    public String getShortVersion() {
        return getString(SHORT_VERSION);
    }

    @Override
    public String getTitle() {
        return getString(TITLE);
    }

}