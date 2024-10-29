package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.job.BaseJobParams;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductivityJobParams extends BaseJobParams {

    public static String FORMULA = "formula";
    public static String EMPLOYEES = "employees";
    public static String IGNORE_TICKET_PREFIXES = "ignore_ticket_prefixes";
    public static String REPORT_NAME = "report_name";
    public static String IS_WEIGHT = "is_weight";
    public static final String START_DATE = "start_date";

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

    public String getFormula() {
        return getString(FORMULA);
    }

    public String getEmployees() {
        return getString(EMPLOYEES);
    }

    public String getReportName() {
        return getString(REPORT_NAME);
    }

    public String getStartDate() {
        return getString(START_DATE);
    }

    public Boolean isWeight() {
        return getBoolean(IS_WEIGHT);
    }
}
