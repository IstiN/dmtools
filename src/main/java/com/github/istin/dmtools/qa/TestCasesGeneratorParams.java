package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.job.BaseJobParams;
import org.json.JSONException;
import org.json.JSONObject;

public class TestCasesGeneratorParams extends BaseJobParams {

    public static final String STORIES_JQL = "storiesJql";
    public static final String EXISTING_TEST_CASES_JQL = "existingTestCasesJql";
    public static final String CONFLUENCE_ROOT_PAGE = "confluenceRootPage";
    public static final String EACH_PAGE_PREFIX = "eachPagePrefix";
    public static final String OUTPUT_TYPE = "outputType";
    public static final String TEST_CASES_PRIORITIES = "testCasesPriorities";

    public static final String OUTPUT_TYPE_TRACKER_COMMENT = "trackerComment";
    public static final String OUTPUT_TYPE_TRACKER_TESTCASES_CREATION = "creation";

    public TestCasesGeneratorParams() {
    }

    public TestCasesGeneratorParams(String json) throws JSONException {
        super(json);
    }

    public TestCasesGeneratorParams(JSONObject json) {
        super(json);
    }

    public String getConfluenceRootPage() {
        return getString(CONFLUENCE_ROOT_PAGE);
    }

    public String getEachPagePrefix() {
        return getString(EACH_PAGE_PREFIX);
    }

    public String getStoriesJQL() {
        return getString(STORIES_JQL);
    }

    public String getExistingTestCasesJQL() {
        return getString(EXISTING_TEST_CASES_JQL);
    }

    public String getOutputType() {
        return getString(OUTPUT_TYPE);
    }

    public String getTestCasesPriorities() {
        return getString(TEST_CASES_PRIORITIES);
    }
}