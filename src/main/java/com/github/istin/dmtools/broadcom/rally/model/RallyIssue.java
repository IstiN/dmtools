package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.Resolution;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.tracker.model.Status;
import com.github.istin.dmtools.common.tracker.model.Workflow;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RallyIssue extends JSONModel implements ITicket, Comparable<RallyIssue> {

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

    public Iteration getIteration() {
        return getModel(Iteration.class, RallyFields.ITERATION);
    }

    public String getFormattedId() {
        return getString(RallyFields.FORMATTED_ID);
    }

    @Override
    public String getStatus() throws IOException {
        Status statusModel = getStatusModel();
        if (statusModel == null) {
            return null;
        }
        return statusModel.getName();
    }

    public String getIterationName() {
        Iteration iteration = getIteration();
        if (iteration != null) {
            return iteration.getName();
        }
        return null;
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
        return getRef().replace("/slm/webservice/v2.x/","/#/?detail=/");
    }

    @Override
    public int compareTo(@NotNull RallyIssue o) {
        Integer comparedProgressResult = ITicketProgress.compare(this, o);
        if (comparedProgressResult != null) return comparedProgressResult;

        String iterationName = getIterationName();
        String secondIterationName = o.getIterationName();

        Integer compareResult = sortByTwoStrings(iterationName, secondIterationName);
        if (compareResult != null) return compareResult;

        Integer typeCompare = sortByTwoStrings(getType(), o.getType());
        if (typeCompare != null) return typeCompare;

        int workFlowComparison = compareWorkflow(o);
        if (workFlowComparison != 0) {
            return workFlowComparison;
        } else {
            return comparePriority(o);
        }
    }

    private int comparePriority(@NotNull RallyIssue o) {
        Priority priorityAsEnum = this.getPriorityAsEnum();
        Priority secondObjectPriority = o.getPriorityAsEnum();
        return priorityAsEnum.compareTo(secondObjectPriority);
    }

    private int compareWorkflow(@NotNull RallyIssue o) {
        try {
            Workflow workflow = Workflow.safeValueOf(this.getStatus());
            Workflow secondaryWorkflow = Workflow.safeValueOf(o.getStatus());
            return secondaryWorkflow.compareTo(workflow);
        } catch (IOException ignored) {
            return 0;
        }
    }

    @Nullable
    private static Integer sortByTwoStrings(String firstString, String secondString) {
        // Null check for iterationName and secondIterationName
        if (firstString !=null && secondString !=null) {
            // Sort by iterationName first
            int nameCompare = firstString.compareTo(secondString);
            if(nameCompare != 0) {
                return nameCompare;
            }
        } else if (firstString != null) {
            return -1;
        } else if (secondString != null) {
            return 1;
        }
        return null;
    }


    public static enum Priority {
        High_Attention,

        High,
        Medium,

        Normal,
        Low,
        NotSet;

        public static Priority byName(String name) {
            if (name == null) {
                return NotSet;
            }
            try {
                return valueOf(name.replace(" ", "_"));
            } catch (Exception ignored) {
                return NotSet;
            }
        }
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

    public Priority getPriorityAsEnum() {
        try {
            return Priority.byName(getPriority());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
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

    public Calendar getCreatedAsCalendar() {
        Date created = getCreated();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(created);
        return calendar;
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
    public IUser getCreator() {
        return null;
    }

    @Override
    public Resolution getResolution() {
        return null;
    }

    @Override
    public Fields getFields() {
        return null;
    }

    @Override
    public double getProgress() throws IOException {
        return new ITicket.ITicketProgress.Impl().calc(this);
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

    public RevisionHistory getRevisionHistory() {
        return getModel(RevisionHistory.class, RallyFields.REVISION_HISTORY);
    }
}
