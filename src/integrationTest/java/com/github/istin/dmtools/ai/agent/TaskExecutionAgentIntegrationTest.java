package com.github.istin.dmtools.ai.agent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskExecutionAgentIntegrationTest {

    @Test
    public void testBrowserSearch() throws Exception {
        TaskExecutionAgent agent = new TaskExecutionAgent();
        TaskExecutionAgent.Result result = agent.run(new TaskExecutionAgent.Params(
                "Go to Wikipedia and search for Albert Einstein"
        ));

        JSONArray steps = result.getSteps();
        JSONObject knownData = result.getKnownData();

        assertNotNull(steps);
        assertNotNull(knownData);
        assertTrue(steps.length() > 0);
        // Verify steps are strings
        for (int i = 0; i < steps.length(); i++) {
            assertTrue(steps.get(i) instanceof String);
            assertFalse(steps.getString(i).isEmpty());
        }
        assertTrue(knownData.has("url"));
        assertTrue(knownData.has("searchTerm") || knownData.has("searchQuery"));
    }

    @Test
    public void testFileOperation() throws Exception {
        TaskExecutionAgent agent = new TaskExecutionAgent();
        TaskExecutionAgent.Result result = agent.run(new TaskExecutionAgent.Params(
                "Create a new text file on desktop named 'notes.txt' and write 'Remember meeting' in it"
        ));

        JSONArray steps = result.getSteps();
        JSONObject knownData = result.getKnownData();

        assertNotNull(steps);
        assertNotNull(knownData);
        assertTrue(steps.length() > 0);
        // Verify steps format
        for (int i = 0; i < steps.length(); i++) {
            String step = steps.getString(i);
            assertTrue(step instanceof String);
            assertTrue(step.length() > 5);
        }
        assertTrue(knownData.has("fileName"));
        assertTrue(knownData.has("content") || knownData.has("fileContent"));
    }

    @Test
    public void testMultiApplicationTask() throws Exception {
        TaskExecutionAgent agent = new TaskExecutionAgent();
        TaskExecutionAgent.Result result = agent.run(new TaskExecutionAgent.Params(
                "Take a screenshot and upload it to Google Drive"
        ));

        JSONArray steps = result.getSteps();
        JSONObject knownData = result.getKnownData();

        assertNotNull(steps);
        assertNotNull(knownData);
        assertTrue(steps.length() > 0);
        // Verify steps are clear and actionable
        for (int i = 0; i < steps.length(); i++) {
            String step = steps.getString(i);
            assertTrue(step instanceof String);
            assertFalse(step.contains("{"));
            assertFalse(step.contains("}"));
        }
        assertTrue(knownData.has("applications") || knownData.has("application"));
    }
}