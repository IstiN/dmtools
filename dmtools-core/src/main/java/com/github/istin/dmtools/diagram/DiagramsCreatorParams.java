package com.github.istin.dmtools.diagram;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class DiagramsCreatorParams extends JSONModel {
    public static final String STORIES_JQL = "storiesJql";
    public static final String LABEL_NAME_TO_MARK_AS_REVIEWED = "labelNameToMarkAsReviewed";
    public static final String ROLE_SPECIFIC = "roleSpecific";
    public static final String PROJECT_SPECIFIC = "projectSpecific";

    public DiagramsCreatorParams() {
    }

    public DiagramsCreatorParams(String json) throws JSONException {
        super(json);
    }

    public DiagramsCreatorParams(JSONObject json) {
        super(json);
    }

    public String getStoriesJql() {
        return getString(STORIES_JQL);
    }

    public String getLabelNameToMarkAsReviewed() {
        return getString(LABEL_NAME_TO_MARK_AS_REVIEWED);
    }

    public String getRoleSpecific() {
        return getString(ROLE_SPECIFIC);
    }

    public String getProjectSpecific() {
        return getString(PROJECT_SPECIFIC);
    }

}