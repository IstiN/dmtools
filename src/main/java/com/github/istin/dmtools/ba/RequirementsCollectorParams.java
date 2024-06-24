package com.github.istin.dmtools.ba;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class RequirementsCollectorParams extends JSONModel {

    public static final String STORIES_JQL = "storiesJql";
    public static final String LABEL_NAME_TO_MARK_AS_REVIEWED = "labelNameToMarkAsReviewed";
    public static final String ROLE_SPECIFIC = "roleSpecific";
    public static final String PROJECT_SPECIFIC = "projectSpecific";
    public static final String EACH_PAGE_PREFIX = "eachPagePrefix";
    private static final String EXCLUDE_JQL = "excludeJQL";

    public RequirementsCollectorParams() {
    }

    public RequirementsCollectorParams(String json) throws JSONException {
        super(json);
    }

    public RequirementsCollectorParams(JSONObject json) {
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

    public String getEachPagePrefix() {
        return getString(EACH_PAGE_PREFIX);
    }

    public String getExcludeJQL() {
        return getString(EXCLUDE_JQL);
    }
}
