package com.github.istin.dmtools.qa;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TestCasesGeneratorParamsTest {

    private TestCasesGeneratorParams params;
    private JSONObject mockJson;

    @Before
    public void setUp() throws JSONException {
        mockJson = mock(JSONObject.class);
        when(mockJson.getString(TestCasesGeneratorParams.CONFLUENCE_ROOT_PAGE)).thenReturn("rootPage");
        when(mockJson.getString(TestCasesGeneratorParams.EACH_PAGE_PREFIX)).thenReturn("pagePrefix");
        when(mockJson.getString(TestCasesGeneratorParams.STORIES_JQL)).thenReturn("storiesJql");
        when(mockJson.getString(TestCasesGeneratorParams.EXISTING_TEST_CASES_JQL)).thenReturn("existingJql");
        when(mockJson.getString(TestCasesGeneratorParams.OUTPUT_TYPE)).thenReturn("outputType");
        when(mockJson.getString(TestCasesGeneratorParams.TEST_CASES_PRIORITIES)).thenReturn("priorities");

        params = new TestCasesGeneratorParams(mockJson);
    }

    @Test
    public void testGetConfluenceRootPage() {
        assertEquals("rootPage", params.getConfluenceRootPage());
    }

    @Test
    public void testGetEachPagePrefix() {
        assertEquals("pagePrefix", params.getEachPagePrefix());
    }

    @Test
    public void testGetStoriesJQL() {
        assertEquals("storiesJql", params.getStoriesJQL());
    }

    @Test
    public void testGetExistingTestCasesJQL() {
        assertEquals("existingJql", params.getExistingTestCasesJQL());
    }

    @Test
    public void testGetOutputType() {
        assertEquals("outputType", params.getOutputType());
    }

    @Test
    public void testGetTestCasesPriorities() {
        assertEquals("priorities", params.getTestCasesPriorities());
    }

    @Test
    public void testConstructorWithEmptyJson() throws JSONException {
        JSONObject emptyJson = new JSONObject();
        TestCasesGeneratorParams emptyParams = new TestCasesGeneratorParams(emptyJson);
        assertNull(emptyParams.getConfluenceRootPage());
        assertNull(emptyParams.getEachPagePrefix());
        assertNull(emptyParams.getStoriesJQL());
        assertNull(emptyParams.getExistingTestCasesJQL());
        assertNull(emptyParams.getOutputType());
        assertNull(emptyParams.getTestCasesPriorities());
    }
}