package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.job.BaseJobParams;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductivityJobParams extends BaseJobParams {

    public static String IGNORE_TICKET_PREFIXES = "ignore_ticket_prefixes";

    public ProductivityJobParams() {
    }

    public ProductivityJobParams(String json) throws JSONException {
        super(json);
    }

    public ProductivityJobParams(JSONObject json) {
        super(json);
    }

    public String[] getIgnoreTicketPrefixes() {
        return getStringArray(IGNORE_TICKET_PREFIXES);
    }
}
