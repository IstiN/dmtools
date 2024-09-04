package com.github.istin.dmtools.report.productivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DevProductivityReportParams extends ProductivityJobParams {

    public static String FORMULA = "formula";
    public static String EMPLOYEES = "employees";
    public static String REPORT_NAME = "report_name";
    public static String IS_WEIGHT = "is_weight";
    public static String STATUSES_READY_FOR_TESTING = "statuses_ready_for_testing";
    public static String STATUSES_IN_TESTING = "statuses_in_testing";
    public static String STATUSES_IN_DEVELOPMENT = "statuses_in_development";
    public static String INITIAL_STATUS = "initial_status";
    public static final String SOURCES = "sources";
    public static final String START_DATE = "start_date";
    public static final String CALC_WEIGHT_TYPE = "calc_weight_type";

    public enum CalcWeightType {
        TIME_SPENT, STORY_POINTS
    }

    public DevProductivityReportParams() {
    }

    public DevProductivityReportParams(String json) throws JSONException {
        super(json);
    }

    public DevProductivityReportParams(JSONObject json) {
        super(json);
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

    public String getInitialStatus() {
        return getString(INITIAL_STATUS);
    }

    public String getStartDate() {
        return getString(START_DATE);
    }

    public Boolean isWeight() {
        return getBoolean(IS_WEIGHT);
    }

    public CalcWeightType getCalcWeightType() {
        return CalcWeightType.valueOf(getString(CALC_WEIGHT_TYPE));
    }

    public String[] getStatusesReadyForTesting() {
        return getStringArray(STATUSES_READY_FOR_TESTING);
    }

    public String[] getStatusesInDevelopment() {
        return getStringArray(STATUSES_IN_DEVELOPMENT);
    }

    public String[] getStatusesInTesting() {
        return getStringArray(STATUSES_IN_TESTING);
    }

    public JSONArray getSources() {
        return getJSONArray(SOURCES);
    }
}
