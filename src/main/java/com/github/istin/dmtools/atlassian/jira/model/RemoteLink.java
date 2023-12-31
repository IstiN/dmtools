package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.IBlocker;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemoteLink extends JSONModel implements IBlocker {

    private static String GLOBAL_ID = "globalId";

    public RemoteLink() {
    }

    public RemoteLink(String json) throws JSONException {
        super(json);
    }

    public RemoteLink(JSONObject json) {
        super(json);
    }

    public String getRelationship() {
        return getString("relationship");
    }

    public String getUrl() {
        JSONObject object = getJSONObject("object");
        if (object == null) {
            return null;
        }
        return object.getString("url");
    }

    public String getTitle() {
        JSONObject object = getJSONObject("object");
        if (object == null) {
            return "Can't get Title";
        }
        return object.getString("title");
    }

    public boolean isBlocker() {
        return Relationship.IS_BLOCKED_BY.equalsIgnoreCase(getRelationship());
    }

    @Override
    public String getStatus() throws IOException {
        Status statusModel = getStatusModel();
        if (statusModel == null) {
            return null;
        }
        return statusModel.getName();
    }

    @Override
    public String getPriority() throws IOException {
        return null;
    }

    @Override
    public Status getStatusModel() throws IOException {
        return null;
    }

    @Override
    public String getTicketKey() {
        return getTitle();
    }

    @Override
    public String getIssueType() throws IOException {
        return "Unknown";
    }

    @Override
    public String getTicketLink() {
        return getUrl();
    }

    public String getHtmlTicketLink() {
        return "<a href=\""+getTicketLink()+"\">" + getTicketKey() + "</a>";
    }

    public String getGlobalId() {
        return getString(GLOBAL_ID);
    }

    @Override
    public String getTicketTitle() throws IOException {
        return "Unknown";
    }

    @Override
    public String getTicketDependenciesDescription() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IBlocker) {
            return getTicketKey().equals(((IBlocker) obj).getTicketKey());
        }
        return super.equals(obj);
    }

    @Override
    public double getWeight() {
        return 1;
    }

    @Override
    public String getKey() {
        return getTicketKey();
    }
}