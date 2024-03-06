package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.cloud.CloudPullRequest;
import com.github.istin.dmtools.atlassian.bitbucket.model.server.ServerPullRequest;
import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public abstract class PullRequest extends JSONModel {

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

    public abstract Assignee getAuthor();

    public abstract String getTargetBranchName();

    public abstract String getSourceBranchName();

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


    public abstract Long getCreatedDate();

    public abstract Long getClosedDate();

    public Calendar getUpdatedDateAsCalendar() {
        Long updatedDate = getUpdatedDate();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(updatedDate);
        return instance;
    }

    public abstract Long getUpdatedDate();

    public static PullRequest create(Bitbucket.ApiVersion apiVersion, String json) {
        PullRequest pr = null;
        if (apiVersion == Bitbucket.ApiVersion.V1) {
            pr = new ServerPullRequest();
        } else {
            pr = new CloudPullRequest();
        }
        if (json != null) {
            pr.setJO(new JSONObject(json));
        }
        return pr;
    }

    public Integer getVersion() {
        return getInt("version");
    }

}