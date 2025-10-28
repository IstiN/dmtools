package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for TestCaseGeneratorAgent focusing on prompt template rendering
 * without actual AI calls
 */
class TestCaseGeneratorAgentTest {

    @Mock
    private AI mockAI;

    @Mock
    private IPromptTemplateReader mockPromptTemplateReader;

    private TestCaseGeneratorAgent agent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agent = new TestCaseGeneratorAgent();
        // Inject mocks directly bypassing Dagger
        agent.ai = mockAI;
        agent.promptTemplateReader = mockPromptTemplateReader;
    }

    @Test
    void testConstructor() {
        TestCaseGeneratorAgent newAgent = new TestCaseGeneratorAgent();
        assertNotNull(newAgent);
        assertEquals("agents/test_case_generator", newAgent.getPromptName());
    }

    @Test
    void testPromptTemplateRendering() throws Exception {
        // Arrange
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "High, Medium",
            "Existing test case 1\nExisting test case 2",
            "Story: User login functionality",
            "Extra rule: Focus on edge cases"

        );

        String renderedPrompt = "Generated prompt with params";
        String mockAIResponse = "[{\"priority\":\"High\",\"summary\":\"Test summary\",\"description\":\"Test description\"}]";

        when(mockPromptTemplateReader.read(eq("agents/test_case_generator"), any(PromptContext.class)))
            .thenReturn(renderedPrompt);
        when(mockAI.chat(renderedPrompt))
            .thenReturn(mockAIResponse);

        // Act
        List<TestCaseGeneratorAgent.TestCase> result = agent.run(params);

        // Assert - verify prompt template was called with correct context
        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(PromptContext.class);
        verify(mockPromptTemplateReader).read(eq("agents/test_case_generator"), contextCaptor.capture());
        
        PromptContext capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext);

        // Verify AI was called with the rendered prompt
        verify(mockAI).chat(renderedPrompt);

        // Verify result parsing
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("High", result.get(0).getPriority());
        assertEquals("Test summary", result.get(0).getSummary());
        assertEquals("Test description", result.get(0).getDescription());
    }

    @Test
    void testPromptTemplateRenderingWithDifferentParams() throws Exception {
        // Arrange
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "Low",
            "",
            "Story: New feature implementation",
            null
        );

        String renderedPrompt = "Prompt for new feature";
        String mockAIResponse = "[]";

        when(mockPromptTemplateReader.read(eq("agents/test_case_generator"), any(PromptContext.class)))
            .thenReturn(renderedPrompt);
        when(mockAI.chat(renderedPrompt))
            .thenReturn(mockAIResponse);

        // Act
        List<TestCaseGeneratorAgent.TestCase> result = agent.run(params);

        // Assert - verify prompt template was rendered
        verify(mockPromptTemplateReader).read(eq("agents/test_case_generator"), any(PromptContext.class));
        verify(mockAI).chat(renderedPrompt);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTransformAIResponse() throws Exception {
        // Arrange
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "High", "existing", "story", "rules"
        );
        String aiResponse = "[" +
            "{\"priority\":\"High\",\"summary\":\"Test 1\",\"description\":\"Desc 1\"}," +
            "{\"priority\":\"Medium\",\"summary\":\"Test 2\",\"description\":\"Desc 2\"}" +
            "]";

        // Act
        List<TestCaseGeneratorAgent.TestCase> result = agent.transformAIResponse(params, aiResponse);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("High", result.get(0).getPriority());
        assertEquals("Test 1", result.get(0).getSummary());
        assertEquals("Medium", result.get(1).getPriority());
        assertEquals("Test 2", result.get(1).getSummary());
    }

    @Test
    void testGetParamsClass() {
        Class<TestCaseGeneratorAgent.Params> paramsClass = agent.getParamsClass();
        assertNotNull(paramsClass);
        assertEquals(TestCaseGeneratorAgent.Params.class, paramsClass);
    }

    @Test
    void testParamsGetters() {
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "priorities",
            "existing",
            "description",
            "rules"
        );

        assertEquals("priorities", params.getPriorities());
        assertEquals("existing", params.getExistingTestCases());
        assertEquals("description", params.getStoryDescription());
        assertEquals("rules", params.getExtraRules());
    }

    @Test
    void testTestCaseGetters() {
        TestCaseGeneratorAgent.TestCase testCase = new TestCaseGeneratorAgent.TestCase(
            "High",
            "Summary",
            "Description"
        );

        assertEquals("High", testCase.getPriority());
        assertEquals("Summary", testCase.getSummary());
        assertEquals("Description", testCase.getDescription());
    }

    @Test
    void testTestCaseToString() {
        TestCaseGeneratorAgent.TestCase testCase = new TestCaseGeneratorAgent.TestCase(
            "High",
            "Summary",
            "Description"
        );

        String toString = testCase.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("High"));
        assertTrue(toString.contains("Summary"));
    }

    @Test
    void testParamsWithDefaultExamples() {
        // Test default behavior when isToOverrideExamples is false
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "High, Medium",
            "existing test cases",
            "story description",
            "extra rules"
        );

        assertEquals("High, Medium", params.getPriorities());
        assertEquals("existing test cases", params.getExistingTestCases());
        assertEquals("story description", params.getStoryDescription());
        assertEquals("extra rules", params.getExtraRules());
        assertFalse(params.isOverridePromptExamples());
        assertEquals("", params.getExamples());
    }

    @Test
    void testParamsWithCustomExamples() {
        // Test custom examples behavior
        String customExamples = "<example><human>Test</human><ai>[]</ai></example>";
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "P1, P2",
            "existing",
            "story",
            "rules",
            true,
            customExamples
        );

        assertEquals("P1, P2", params.getPriorities());
        assertEquals("existing", params.getExistingTestCases());
        assertEquals("story", params.getStoryDescription());
        assertEquals("rules", params.getExtraRules());
        assertTrue(params.isOverridePromptExamples());
        assertEquals(customExamples, params.getExamples());
    }

    @Test
    void testPromptTemplateWithCustomExamples() throws Exception {
        // Arrange
        String customExamples = "<example><human>Generate test cases for API</human>" +
            "<ai>[{\"priority\":\"P1\",\"summary\":\"API test\",\"description\":\"Test API endpoint\"}]</ai></example>";
        
        TestCaseGeneratorAgent.Params params = new TestCaseGeneratorAgent.Params(
            "P1, P2",
            "existing test cases",
            "API feature story",
            "focus on security",
            true,
            customExamples
        );

        String renderedPrompt = "Generated prompt with custom examples";
        String mockAIResponse = "[{\"priority\":\"P1\",\"summary\":\"API security test\",\"description\":\"Test API authentication\"}]";

        when(mockPromptTemplateReader.read(eq("agents/test_case_generator"), any(PromptContext.class)))
            .thenReturn(renderedPrompt);
        when(mockAI.chat(renderedPrompt))
            .thenReturn(mockAIResponse);

        // Act
        List<TestCaseGeneratorAgent.TestCase> result = agent.run(params);

        // Assert - verify prompt template was called with correct context
        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(PromptContext.class);
        verify(mockPromptTemplateReader).read(eq("agents/test_case_generator"), contextCaptor.capture());
        
        PromptContext capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext);

        // Verify AI was called
        verify(mockAI).chat(renderedPrompt);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getPriority());
        assertEquals("API security test", result.get(0).getSummary());
    }
}
