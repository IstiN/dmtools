package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.IBlocker;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.model.Key;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Ticket extends JSONModel implements IBlocker {

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
        return 1;
    }

    @Override
    public String getKey() {
        return getString("key");
    }

    public Fields getFields() {
        return getModel(Fields.class, "fields");
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
        return getFields().getPriority().getName();
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
    public String getTicketDependenciesDescription() {
        return null;
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
}