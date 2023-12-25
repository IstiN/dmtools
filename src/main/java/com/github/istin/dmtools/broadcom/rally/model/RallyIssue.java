package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.tracker.model.Status;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public class RallyIssue extends JSONModel implements ITicket {

    public RallyIssue() {
    }

    public RallyIssue(String json) throws JSONException {
        super(json);
    }

    public RallyIssue(JSONObject json) {
        super(json);
    }

    public String getType() {
        return getString(RallyFields._TYPE);
    }

    public String getRef() {
        return getString(RallyFields._REF);
    }

    public String getFormattedId() {
        return getString(RallyFields.FORMATTED_ID);
    }

    @Override
    public String getStatus() throws IOException {
        return getStatusModel().getName();
    }

    @Override
    public Status getStatusModel() throws IOException {
        return getModel(Status.class, RallyFields.FLOW_STATE);
    }

    @Override
    public String getTicketKey() {
        return getFormattedId();
    }

    @Override
    public String getIssueType() throws IOException {
        return getType();
    }

    @Override
    public String getTicketLink() {
        return getRef().replace("/slm/webservice/v2.0/","/#/?detail=/");
    }

    @Override
    public String getPriority() throws IOException {
        String priority = getString(RallyFields.PRIORITY);
        if (priority != null && !priority.isEmpty()) {
            return priority;
        }
        PriorityUserStory priorityUserStory = getModel(PriorityUserStory.class, RallyFields.PRIORITY_USER_STORY);
        if (priorityUserStory != null) {
            return priorityUserStory.getPriority();
        }
        return null;
    }

    @Override
    public String getTicketTitle() throws IOException {
        return getString(RallyFields._REF_OBJECT_NAME);
    }

    @Override
    public String getTicketDependenciesDescription() {
        return getString(RallyFields.BLOCKED_REASON);
    }

    @Override
    public Date getCreated() {
        return DateUtils.parseRallyDate(getString(RallyFields.CREATION_DATE));
    }

    @Override
    public JSONObject getFieldsAsJSON() {
        return getJSONObject();
    }

    @Override
    public Long getUpdatedAsMillis() {
        Date date = DateUtils.parseRallyDate(getString(RallyFields.LAST_UPDATE_DATE));
        return date.getTime();
    }

    @Override
    public double getWeight() {
        Double storyPoints = getDouble(RallyFields.PLAN_ESTIMATE);
        if (storyPoints == null) {
            return 1d;
        }
        return storyPoints;
    }

    @Override
    public String getKey() {
        return getTicketKey();
    }
}
