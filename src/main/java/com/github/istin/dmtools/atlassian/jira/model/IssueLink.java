package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.model.IBlocker;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.model.Key;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.github.istin.dmtools.atlassian.jira.model.Relationship.*;

public class IssueLink extends JSONModel implements IBlocker, Key {

    public IssueLink() {
    }

    public IssueLink(String json) throws JSONException {
        super(json);
    }

    public IssueLink(JSONObject json) {
        super(json);
    }

    public String getId() {
        return getString("id");
    }

    public Ticket getInwardIssue() {
        return getModel(Ticket.class, "inwardIssue");
    }

    public Ticket getOutwardIssue() {
        return getModel(Ticket.class, "outwardIssue");
    }

    public String getInwardType() {
        return getJSONObject("type").optString("inward");
    }

    public String getOutwardType() {
        return getJSONObject("type").optString("outward");
    }

    public boolean isBlocker() {
        return inwardRelationshipIn(IS_BLOCKED_BY, IS_BLOCKED_BY_, DEPENDS_UPON, DEPENDS_ON, IS_BROKEN_BY)
                || Relationship.DEPENDS_ON.equalsIgnoreCase(getOutwardType()) && getOutwardIssue() != null;
    }

    public boolean isTest() {
        return inwardRelationshipIn(TESTED_BY) && getInwardIssue().getFields().getIssueType().isTest();
    }

    private boolean inwardRelationshipIn(String ... relationships) {
        for (String relationship : relationships) {
            if (relationship.equalsIgnoreCase(getInwardType()) && getInwardIssue() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlocks() {
        return Relationship.BLOCKS.equalsIgnoreCase(getOutwardType()) && getOutwardIssue() != null
                || Relationship.BLOCKS_.equalsIgnoreCase(getOutwardType()) && getOutwardIssue() != null
                || Relationship.IS_A_PREREQUISITE_OF.equalsIgnoreCase(getOutwardType()) && getOutwardIssue() != null;
    }

    @Override
    public String getStatus() {
        try {
            if (getStatusModel() == null) {
                return "NONE";
            } else {
                return getStatusModel().getName();
            }
        } catch(IOException e){
            return "NONE";
        }
    }

    @Override
    public String getPriority() throws IOException{
        Ticket relatedIssue = getRelatedTicket();
        if (relatedIssue != null) {
            Priority priority = relatedIssue.getFields().getPriority();
            return priority == null ? "none" : priority.getName();
        } else {
            return "none";
        }
    }

    @Override
    public Status getStatusModel() throws IOException {
        Ticket relatedIssue = getRelatedTicket();
        if (relatedIssue != null) {
            return relatedIssue.getFields().getStatus();
        } else {
            return null;
        }
    }

    public Ticket getRelatedTicket() {
        Ticket relatedIssue = getInwardIssue();
        if (relatedIssue == null) {
            relatedIssue = getOutwardIssue();
        }
        return relatedIssue;
    }

    @Override
    public String getTicketKey() {
        Ticket inwardIssue = getInwardIssue();
        if (inwardIssue == null) {
            Ticket outwardIssue = getOutwardIssue();
            if (outwardIssue == null) {
                return "";
            }
            return outwardIssue.getKey();
        }
        return inwardIssue.getKey();
    }

    @Override
    public String getIssueType() {
        return getRelatedTicket().getIssueType();
    }

    @Override
    public String getTicketLink() {
        return Ticket.getBasePath(getString("self")) + "browse/" + getKey();
    }

    @Override
    public String getTicketTitle() throws IOException {
        Ticket inwardIssue = getInwardIssue();
        if (inwardIssue == null) {
            Ticket outwardIssue = getOutwardIssue();
            if (outwardIssue == null) {
                return "Unknown";
            }
            return outwardIssue.getFields().getSummary();
        }
        return inwardIssue.getFields().getSummary();
    }

    @Override
    public String getTicketDependenciesDescription() {
        return null;
    }

    public IssueType getTicketType() {
        Ticket inwardIssue = getInwardIssue();
        if (inwardIssue == null) {
            Ticket outwardIssue = getOutwardIssue();
            if (outwardIssue == null) {
                return null;
            }
            return outwardIssue.getFields().getIssueType();
        }
        return inwardIssue.getFields().getIssueType();
    }

    public String getHtmlTicketLink() {
        return "<a href=\""+getTicketLink()+"\">" + getTicketKey() + "</a>";
    }

    @Override
    public double getWeight() {
        return 1;
    }

    @Override
    public String getKey() {
        return getTicketKey();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Key) {
            return this.getKey().equals(((Key) o).getKey());
        }
        if (o instanceof IBlocker) {
            return getTicketKey().equals(((IBlocker) o).getTicketKey());
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}