package com.github.istin.dmtools.report.productivity;

import org.json.JSONException;
import org.json.JSONObject;

public class QAProductivityReportParams extends ProductivityJobParams {

    public static String REPORT_NAME = "report_name";
    public static String IS_WEIGHT = "is_weight";
    public static final String START_DATE = "start_date";
    public static final String BUGS_PROJECT_CODE = "bugs_project_code";
    public static final String TEST_CASES_PROJECT_CODE = "test_cases_project_code";
    public static String STATUSES_DONE = "statuses_done";

    public static String STATUSES_IN_TESTING = "statuses_in_testing";
    public static String STATUSES_IN_DEVELOPMENT = "statuses_in_development";

    public QAProductivityReportParams() {
    }

    public QAProductivityReportParams(String json) throws JSONException {
        super(json);
    }

    public QAProductivityReportParams(JSONObject json) {
        super(json);
    }

    public String getReportName() {
        return getString(REPORT_NAME);
    }

    public String getStartDate() {
        return getString(START_DATE);
    }

    public String getBugsProjectCode() {
        return getString(BUGS_PROJECT_CODE);
    }

    public String getTestCasesProjectCode() {
        return getString(TEST_CASES_PROJECT_CODE);
    }

    public String[] getStatusesDone() {
        return getStringArray(STATUSES_DONE);
    }

    public Boolean isWeight() {
        return getBoolean(IS_WEIGHT);
    }

    public String[] getStatusesInTesting() {
        return getStringArray(STATUSES_IN_TESTING);
    }

    public String[] getStatusesInDevelopment() {
        return getStringArray(STATUSES_IN_DEVELOPMENT);
    }


}