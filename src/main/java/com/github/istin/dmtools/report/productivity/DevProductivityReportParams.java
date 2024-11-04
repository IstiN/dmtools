package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.excel.model.ExcelMetricConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DevProductivityReportParams extends ProductivityJobParams {

    public static String REPORT_NAME = "report_name";
    public static String IS_WEIGHT = "is_weight";
    public static String STATUSES_READY_FOR_TESTING = "statuses_ready_for_testing";
    public static String STATUSES_IN_TESTING = "statuses_in_testing";
    public static String STATUSES_IN_DEVELOPMENT = "statuses_in_development";
    public static String INITIAL_STATUS = "initial_status";
    public static final String SOURCES = "sources";
    public static final String START_DATE = "start_date";
    public static final String CALC_WEIGHT_TYPE = "calc_weight_type";
    public static final String EXCEL_METRICS_PARAMS = "excel_metrics_params";

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

    public String getInitialStatus() {
        return getString(INITIAL_STATUS);
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

    public void setExcelMetricsParams(ExcelMetricConfig ... configs) {
        JSONArray array = new JSONArray();
        for (ExcelMetricConfig excelMetricConfig : configs) {
            array.put(excelMetricConfig.getJSONObject());
        }
        set(EXCEL_METRICS_PARAMS, array);
    }

    public List<ExcelMetricConfig> getExcelMetricsParams() {
        return getModels(ExcelMetricConfig.class, EXCEL_METRICS_PARAMS);
    }

}
