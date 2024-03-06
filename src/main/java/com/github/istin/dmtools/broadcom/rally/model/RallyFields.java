package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.common.utils.DateUtils;

public class RallyFields {

    public static final String START_DATE = "StartDate";
    public static final String END_DATE = "EndDate";
    public static final String NAME = "Name";
    public static final String PROJECT = "Project";
    public static final String STATE = "State";
    public static final String REVISION_HISTORY = "RevisionHistory";
    public static final String FORMATTED_ID = "FormattedID";
    public static final String _REF = "_ref";

    public static final String _REF_OBJECT_NAME = "_refObjectName";

    public static final String _TYPE = "_type";

    public static final String PRIORITY = "Priority";

    public static final String PRIORITY_USER_STORY = "c_PriorityUserStory";

    public static final String CREATION_DATE = "CreationDate";
    public static final String LAST_UPDATE_DATE = "LastUpdateDate";

    public static final String FLOW_STATE = "FlowState";

    public static final String BLOCKED = "Blocked";
    public static final String BLOCKED_REASON = "BlockedReason";

    public static final String PLAN_ESTIMATE = "PlanEstimate";

    public static final String DESCRIPTION = "Description";

    public static final String USER = "User";

    public static final String ITERATION = "Iteration";

    public static final String[] DEFAULT = new String[] {
            FORMATTED_ID,NAME,PRIORITY,PRIORITY_USER_STORY,CREATION_DATE,LAST_UPDATE_DATE,FLOW_STATE,BLOCKED,BLOCKED_REASON,PLAN_ESTIMATE, REVISION_HISTORY, ITERATION, START_DATE, END_DATE
    };


}
