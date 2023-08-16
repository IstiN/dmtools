package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateInterval;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

public class Changelog extends JSONModel {

    private static final String MAX_RESULTS = "maxResults";
    private static final String TOTAL = "total";
    public static final String HISTORIES = "histories";
    public static final String ERROR_MESSAGES = "errorMessages";


    public Changelog() {
    }

    public Changelog(String json) throws JSONException {
        super(json);
    }

    public Changelog(JSONObject json) {
        super(json);
    }

    public JSONArray getErrorMessages() {
        return getJSONArray(ERROR_MESSAGES);
    }

    public int getMaxResults() {
        return getInt(MAX_RESULTS);
    }

    public int getTotal() {
        return getInt(TOTAL);
    }

    public List<History> getHistories() {
        return getModels(History.class, HISTORIES);
    }

    public DateInterval dateIntervalBetweenTwoStatuses(String firstStatus, String secondStatus) {
        boolean shouldHandleFirstStatus = true;
        boolean shouldHandleSecondStatus = true;
        Calendar firstStatusDate = null;
        Calendar secondStatusDate = null;

        for (History history: getHistories()) {
            for (HistoryItem historyItem: history.getItems()) {
                if (historyItem.getToString() == null) {
                    continue;
                }
                String toString = historyItem.getToString().toLowerCase();
                if (toString.equals(firstStatus) && shouldHandleFirstStatus) {
                    shouldHandleFirstStatus = false;
                    firstStatusDate = history.getCreated();
                }
                if (toString.equals(secondStatus) && shouldHandleSecondStatus) {
                    shouldHandleSecondStatus = false;
                    secondStatusDate = history.getCreated();
                }
            }
        }

        if (firstStatusDate == null || secondStatusDate == null) {
            return null;
        }

        return new DateInterval(firstStatusDate, secondStatusDate);
    }

}