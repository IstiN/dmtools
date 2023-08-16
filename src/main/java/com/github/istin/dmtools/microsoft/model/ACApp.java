package com.github.istin.dmtools.microsoft.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class ACApp extends JSONModel implements App {

    private static final String ID = "id";

    private static final String TITLE = "display_name";

    private static final String PUBLIC_IDENTIFIER = "app_name";
    public static final String NAME = "name";

    public ACApp() {
    }

    public ACApp(String json) throws JSONException {
        super(json);
    }

    public ACApp(JSONObject json) {
        super(json);
    }

    @Override
    public Object getId() {
        return getString(ID);
    }

    @Override
    public String getPublicIdentifier() {
        return getString(NAME);
    }

    @Override
    public String getTitle() {
        return getString(TITLE);
    }

    @Override
    public String getName() {
        return getString(NAME);
    }

}