package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import org.junit.Before;
import org.junit.Test;
import org.json.JSONArray;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FinalStatusDetectionAgentTest {

    private FinalStatusDetectionAgent agent;
    private AI mockAI;
    private IPromptTemplateReader mockPromptReader;

    @Before
    public void setUp() throws Exception {
        agent = new FinalStatusDetectionAgent();
        
        mockAI = mock(AI.class);
        mockPromptReader = mock(IPromptTemplateReader.class);

        // Use reflection to inject mocks
        java.lang.reflect.Field aiField = FinalStatusDetectionAgent.class.getSuperclass().getDeclaredField("ai");
        aiField.setAccessible(true);
        aiField.set(agent, mockAI);

        java.lang.reflect.Field promptField = FinalStatusDetectionAgent.class.getSuperclass().getDeclaredField("promptTemplateReader");
        promptField.setAccessible(true);
        promptField.set(agent, mockPromptReader);
    }

    @Test
    public void testTransformAIResponse() throws Exception {
        String aiResponse = "[\"Done\", \"Closed\", \"Resolved\"]";
        FinalStatusDetectionAgent.Params params = new FinalStatusDetectionAgent.Params("TEST", "{}");

        JSONArray result = agent.transformAIResponse(params, aiResponse);

        assertNotNull(result);
        assertEquals(3, result.length());
        assertEquals("Done", result.getString(0));
        assertEquals("Closed", result.getString(1));
        assertEquals("Resolved", result.getString(2));
    }

    @Test
    public void testParamsConstructor() {
        FinalStatusDetectionAgent.Params params = new FinalStatusDetectionAgent.Params("TEST", "metadata");
        assertEquals("TEST", params.getProjectKey());
        assertEquals("metadata", params.getWorkflowMetadata());
    }
}
