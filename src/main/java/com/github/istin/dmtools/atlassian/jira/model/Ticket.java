package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.timeline.ReportIteration;
import com.github.istin.dmtools.common.tracker.model.Status;
import com.github.istin.dmtools.common.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Ticket extends JSONModel implements ITicket {

    private static final String ID = "id";

    public Ticket() {
    }

    public Ticket(String json) throws JSONException {
        super(json);
    }

    public Ticket(JSONObject json) {
        super(json);
    }

    @Override
    public double getWeight() {
        return getFields().getStoryPoints();
    }

    public boolean areStoryPointsSet() {
        return getFields().getJSONObject().optInt(Fields.STORY_POINTS, -1) != -1;
    }

    @Override
    public String getKey() {
        return getString("key");
    }

    public Fields getFields() {
        return getModel(Fields.class, "fields");
    }

    @Override
    public ReportIteration getIteration() {
        List<FixVersion> fixVersions = getFields().getFixVersions();
        if (fixVersions != null && !fixVersions.isEmpty()) {
            return fixVersions.get(0);
        }
        return null;
    }

    public String getIterationName() {
        ReportIteration iteration = getIteration();
        if (iteration != null) {
            return iteration.getIterationName();
        }
        return null;
    }

    @Override
    public double getProgress() throws IOException {
        return new ITicket.ITicketProgress.Impl().calc(this);
    }

    @Override
    public List<? extends IAttachment> getAttachments() {
        Fields fields = getFields();
        return fields.getAttachments();
    }

    @Override
    public TicketPriority getPriorityAsEnum() {
        try {
            return TicketPriority.byName(getPriority());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toText() {
        StringBuilder fieldsAsJSON = StringUtils.transformJSONToText(new StringBuilder(), getFieldsAsJSON(), false);
        return fieldsAsJSON.toString();
    }

    public String getId() {
        return getString(ID);
    }

    @Override
    public String getStatus() throws IOException {
        Status statusModel = getStatusModel();
        if (statusModel == null) {
            return "Unknown";
        }
        return statusModel.getName();
    }

    @Override
    public String getPriority() throws IOException {
        Priority priority = getFields().getPriority();
        if (priority == null) {
            return null;
        }
        return priority.getName();
    }

    @Override
    public Status getStatusModel() throws IOException {
        return getFields().getStatus();
    }

    public Changelog getChangelog() {
        return getModel(Changelog.class, "changelog");
    }

    @Override
    public String getTicketKey() {
        return getKey();
    }

    @Override
    public String getIssueType() {
        Fields fields = getFields();
        if (fields == null) {
            return "";
        }
        return fields.getIssueType().getName();
    }

    @Override
    public String getTicketLink() {
        return getBasePath(getString("self")) + "browse/" + getKey();
    }

    public static String getBasePath(String url) {
        return url.substring(0, url.indexOf("rest"));
    }

    @Override
    public String getTicketTitle() throws IOException {
        return getFields().getSummary();
    }

    @Override
    public String getTicketDescription() {
        return getFields().getDescription();
    }

    @Override
    public String getTicketDependenciesDescription() {
        return null;
    }

    @Override
    public Date getCreated() {
        return getFields().getCreated();
    }

    @Override
    public JSONObject getFieldsAsJSON() {
        return getFields().getJSONObject();
    }

    @Override
    public Long getUpdatedAsMillis() {
        return getFields().getUpdatedAsMillis();
    }

    @Override
    public IUser getCreator() {
        Assignee creator = getFields().getCreator();
        if (creator == null) {
            return getFields().getReporter();
        }
        return creator;
    }

    @Override
    public Resolution getResolution() {
        return getFields().getResolution();
    }

    @Override
    public JSONArray getTicketLabels() {
        return getFields().getLabels();
    }

    public String getHtmlTicketLink() {
        return "<a href=\""+getTicketLink()+"\">" + getKey() + "</a>";
    }

    @Override
    public boolean equals(Object o) {
        return this.getKey().equals(((Key) o).getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public String toString() {
        return getJSONObject().toString();
    }
}