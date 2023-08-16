package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class PullRequest extends JSONModel {

    public PullRequest() {
    }

    public PullRequest(String json) throws JSONException {
        super(json);
    }

    public PullRequest(JSONObject json) {
        super(json);
    }

    public String getTitle() {
        return getString("title");
    }

    public String getDescription() {
        return getString("description");
    }

    public boolean isWIP() {
        return getTitle().toLowerCase().contains("[wip]");
    }

    public Integer getId() {
        return getInt("id");
    }

    public Integer getVersion() {
        return getInt("version");
    }

    public String fromRefTitle() {
        return getJSONObject().getJSONObject("fromRef").getString("displayId");
    }

    public String toRefTitle() {
        return getJSONObject().getJSONObject("toRef").getString("displayId");
    }

    public Assignee getAuthor() {
        return new Assignee(getJSONObject().getJSONObject("author").getJSONObject("user"));
    }

    public Calendar getCreatedDateAsCalendar() {
        Long createdDate = getCreatedDate();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(createdDate);
        return instance;
    }

    public Calendar getClosedDateAsCalendar() {
        Long closedDate = getClosedDate();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(closedDate);
        return instance;
    }

    public int getWorkingHoursOpened() {
        Integer hoursDuration = DateUtils.getHoursDuration(getClosedDate(), getCreatedDate());
        int weekendDaysBetweenTwoDates = DateUtils.getWeekendDaysBetweenTwoDates(getCreatedDate(), getClosedDate());
        hoursDuration = hoursDuration - weekendDaysBetweenTwoDates * 24;
        if (hoursDuration < 0) {
            hoursDuration = 0;
        }
        if (hoursDuration > 24) {
            hoursDuration = hoursDuration - (hoursDuration / 24) * 8;
        }
        return hoursDuration;
    }


    public Long getCreatedDate() {
        return getLong("createdDate");
    }

    public Long getClosedDate() {
        return getLong("closedDate");
    }

    public Calendar getUpdatedDateAsCalendar() {
        Long updatedDate = getUpdatedDate();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(updatedDate);
        return instance;
    }

    public Long getUpdatedDate() {
        return getLong("updatedDate");
    }
}