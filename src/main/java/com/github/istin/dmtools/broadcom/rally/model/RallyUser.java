package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class RallyUser extends JSONModel implements IUser {

    public RallyUser() {
    }

    public RallyUser(String json) throws JSONException {
        super(json);
    }

    public RallyUser(JSONObject json) {
        super(json);
    }

    @Override
    public String getFullName() {
        return getString(RallyFields._REF_OBJECT_NAME);
    }

}
