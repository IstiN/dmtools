package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Iteration extends JSONModel {

    public Iteration() {
    }

    public Iteration(String json) throws JSONException {
        super(json);
    }

    public Iteration(JSONObject json) {
        super(json);
    }

    public Calendar getStartDate() {
        return DateUtils.parseRallyCalendar(getString(RallyFields.START_DATE));
    }

    public Calendar getEndDate() {
        return DateUtils.parseRallyCalendar(getString(RallyFields.END_DATE));
    }

    public String getName() {
        return getString(RallyFields.NAME);
    }

    public String getType() {
        return getString(RallyFields._TYPE);
    }

    public String getState() {
        return getString(RallyFields.STATE);
    }

}
