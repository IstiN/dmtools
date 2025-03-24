package com.github.istin.dmtools.ai.agent;

import org.json.JSONArray;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TaskProgressAgentIntegrationTest {

    @Test
    public void testBrowserNavigation() throws Exception {
        TaskProgressAgent agent = new TaskProgressAgent();

        // Create test steps
        JSONArray steps = new JSONArray()
                .put("Open web browser")
                .put("Navigate to google.com")
                .put("Type 'Java programming'")
                .put("Click search button");

        // Load test screenshot

        TaskProgressAgent.Result result = agent.run(new TaskProgressAgent.Params(steps.toString(), "",true, Arrays.asList(new File("src/test/resources/browser_google_homepage.png"))));

        assertNotNull(result.getCompletedSteps());
        assertNotNull(result.getNextSteps());
        assertTrue(result.getCompletedSteps().length() + result.getNextSteps().length() == steps.length());
    }

    @Test
    public void testFileOperations() throws Exception {
        TaskProgressAgent agent = new TaskProgressAgent();

        JSONArray steps = new JSONArray()
                .put("Open File Explorer")
                .put("Navigate to Documents folder")
                .put("Create new text file")
                .put("Rename file to 'notes.txt'");


        TaskProgressAgent.Result result = agent.run(new TaskProgressAgent.Params(steps.toString(), "",true, Arrays.asList(new File("src/test/resources/file_explorer_documents.png"))));

        assertNotNull(result.getCompletedSteps());
        assertNotNull(result.getNextSteps());
        // Verify all steps are accounted for
        assertEquals(steps.length(),
                result.getCompletedSteps().length() + result.getNextSteps().length());
    }

    @Test
    public void testPartialCompletion() throws Exception {
        TaskProgressAgent agent = new TaskProgressAgent();

        JSONArray steps = new JSONArray()
                .put("Open Settings")
                .put("Click on System")
                .put("Click on Display")
                .put("Change resolution");

        TaskProgressAgent.Result result = agent.run(new TaskProgressAgent.Params(steps.toString(),"",true, Arrays.asList(new File("src/test/resources/settings_display.png"))));

        // Verify specific steps are in the correct arrays
        assertTrue(result.getCompletedSteps().toString().contains("Open Settings"));
        assertTrue(result.getNextSteps().toString().contains("Click on System"));
        assertTrue(result.getNextSteps().toString().contains("Change resolution"));
    }
}