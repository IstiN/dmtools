package com.github.istin.dmtools.job;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for JobJavaScriptBridge with mocked MCP tools
 */
class JobJavaScriptBridgeTest {

    @Mock
    private TrackerClient<?> mockTrackerClient;

    @Mock
    private AI mockAI;

    @Mock
    private Confluence mockConfluence;

    @Mock
    private SourceCode mockSourceCode;

    private JobJavaScriptBridge bridge;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bridge = new JobJavaScriptBridge(mockTrackerClient, mockAI, mockConfluence, mockSourceCode);
    }

    @Test
    void testBasicJavaScriptExecution() throws Exception {
        // Given
        String simpleJS = """
            function action(params) {
                return "Hello " + params.name;
            }
            """;
        
        JSONObject params = new JSONObject();
        params.put("name", "World");

        // When
        Object result = bridge.executeJavaScript(simpleJS, params);

        // Then
        assertNotNull(result);
        assertEquals("Hello World", result.toString());
    }

    @Test
    void testAIToolExecution() throws Exception {
        // Given
        String jsWithAI = """
            function action(params) {
                var response = gemini_ai_chat("Test message");
                return "AI said: " + response;
            }
            """;
        
        JSONObject params = new JSONObject();

        // Mock MCP tool execution
        try (MockedStatic<MCPToolExecutor> mcpMock = mockStatic(MCPToolExecutor.class)) {
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("gemini_ai_chat"), 
                any(Map.class), 
                any(Map.class)
            )).thenReturn("Mocked AI response");

            // When
            Object result = bridge.executeJavaScript(jsWithAI, params);

            // Then
            assertNotNull(result);
            assertEquals("AI said: Mocked AI response", result.toString());
            
            // Verify MCP tool was called
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("gemini_ai_chat"),
                argThat(args -> "Test message".equals(((Map<String, Object>) args).get("message"))),
                any(Map.class)
            ));
        }
    }

    @Test
    void testJiraToolExecution() throws Exception {
        // Given
        String jsWithJira = """
            function action(params) {
                var ticket = jira_create_ticket_basic("TEST", "Story", "Test Title", "Test Description");
                jira_post_comment(ticket.key, "Created successfully");
                return { success: true, ticketKey: ticket.key };
            }
            """;
        
        JSONObject params = new JSONObject();

        // Mock MCP tool execution
        try (MockedStatic<MCPToolExecutor> mcpMock = mockStatic(MCPToolExecutor.class)) {
            // Mock ticket creation
            Map<String, Object> mockTicket = new HashMap<>();
            mockTicket.put("key", "TEST-123");
            
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_create_ticket_basic"), 
                any(Map.class), 
                any(Map.class)
            )).thenReturn(mockTicket);

            // Mock comment posting
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_post_comment"), 
                any(Map.class), 
                any(Map.class)
            )).thenReturn("Comment posted");

            // When
            Object result = bridge.executeJavaScript(jsWithJira, params);

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("TEST-123"));
            
            // Verify both tools were called
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("jira_create_ticket_basic"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("jira_post_comment"), any(Map.class), any(Map.class)
            ));
        }
    }

    @Test
    void testConfluenceToolExecution() throws Exception {
        // Given
        String jsWithConfluence = """
            function action(params) {
                var page = confluence_create_page("Test Page", "123", "Test content", "SPACE");
                var content = confluence_content_by_title("Test Page");
                return { pageId: page.id, content: content };
            }
            """;
        
        JSONObject params = new JSONObject();

        // Mock MCP tool execution
        try (MockedStatic<MCPToolExecutor> mcpMock = mockStatic(MCPToolExecutor.class)) {
            // Mock page creation
            Map<String, Object> mockPage = new HashMap<>();
            mockPage.put("id", "456");
            
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("confluence_create_page"), 
                any(Map.class), 
                any(Map.class)
            )).thenReturn(mockPage);

            // Mock content retrieval
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("confluence_content_by_title"), 
                any(Map.class), 
                any(Map.class)
            )).thenReturn("Page content");

            // When
            Object result = bridge.executeJavaScript(jsWithConfluence, params);

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("456"));
            
            // Verify tools were called
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("confluence_create_page"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("confluence_content_by_title"), any(Map.class), any(Map.class)
            ));
        }
    }

    @Test
    void testMultipleToolChain() throws Exception {
        // Given - Complex scenario with multiple tool calls
        String complexJS = """
            function action(params) {
                // Get AI analysis
                var analysis = dial_ai_chat("Analyze: " + params.input);
                
                // Create ticket based on analysis
                var ticket = jira_create_ticket_basic("PROJ", "Task", "AI Analysis", analysis);
                
                // Update ticket with additional info
                jira_update_field(ticket.key, "priority", {name: "High"});
                
                // Create Confluence page
                var page = confluence_create_page("Analysis " + ticket.key, "123", analysis, "SPACE");
                
                // Link them
                jira_post_comment(ticket.key, "Analysis page: " + page.url);
                
                return {
                    ticketKey: ticket.key,
                    pageId: page.id,
                    analysis: analysis
                };
            }
            """;
        
        JSONObject params = new JSONObject();
        params.put("input", "Complex requirement");

        // Mock MCP tool execution
        try (MockedStatic<MCPToolExecutor> mcpMock = mockStatic(MCPToolExecutor.class)) {
            // Mock AI analysis
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("dial_ai_chat"), any(Map.class), any(Map.class)
            )).thenReturn("Detailed AI analysis result");

            // Mock ticket creation
            Map<String, Object> mockTicket = new HashMap<>();
            mockTicket.put("key", "PROJ-456");
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_create_ticket_basic"), any(Map.class), any(Map.class)
            )).thenReturn(mockTicket);

            // Mock field update
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_update_field"), any(Map.class), any(Map.class)
            )).thenReturn("Field updated");

            // Mock page creation
            Map<String, Object> mockPage = new HashMap<>();
            mockPage.put("id", "789");
            mockPage.put("url", "http://confluence.com/page/789");
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("confluence_create_page"), any(Map.class), any(Map.class)
            )).thenReturn(mockPage);

            // Mock comment posting
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_post_comment"), any(Map.class), any(Map.class)
            )).thenReturn("Comment posted");

            // When
            Object result = bridge.executeJavaScript(complexJS, params);

            // Then
            assertNotNull(result);
            String resultStr = result.toString();
            assertTrue(resultStr.contains("PROJ-456"));
            assertTrue(resultStr.contains("789"));
            
            // Verify all tools were called in sequence
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("dial_ai_chat"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("jira_create_ticket_basic"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("jira_update_field"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("confluence_create_page"), any(Map.class), any(Map.class)
            ));
            mcpMock.verify(() -> MCPToolExecutor.executeTool(
                eq("jira_post_comment"), any(Map.class), any(Map.class)
            ));
        }
    }

    @Test
    void testGitHubUrlLoading() throws Exception {
        // Given
        String githubUrl = "https://github.com/IstiN/dmtools/blob/main/dmtools-core/src/main/resources/js/test.js";
        String mockJSContent = """
            function action(params) {
                return "Loaded from GitHub: " + params.message;
            }
            """;
        
        when(mockSourceCode.getFileContent(githubUrl)).thenReturn(mockJSContent);
        
        JSONObject params = new JSONObject();
        params.put("message", "Hello GitHub");

        // When
        Object result = bridge.executeJavaScript(githubUrl, params);

        // Then
        assertNotNull(result);
        assertEquals("Loaded from GitHub: Hello GitHub", result.toString());
        verify(mockSourceCode).getFileContent(githubUrl);
    }

    @Test
    void testResourcePathLoading() throws Exception {
        // Given - Resource path loading uses classpath resources
        String resourcePath = "testScripts/sample.js";
        JSONObject params = new JSONObject();
        params.put("test", "value");

        // When/Then - This will fail as resource doesn't exist, but tests the path detection
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bridge.executeJavaScript(resourcePath, params);
        });
        
        assertTrue(exception.getMessage().contains("Resource not found") || 
                  exception.getMessage().contains("Failed to load JS resource") ||
                  exception.getMessage().contains("JavaScript file not found"));
    }

    @Test
    void testToolExecutionError() throws Exception {
        // Given
        String jsWithError = """
            function action(params) {
                var result = jira_create_ticket_basic("INVALID", "BadType", "", "");
                return result;
            }
            """;
        
        JSONObject params = new JSONObject();

        // Mock MCP tool execution to throw error
        try (MockedStatic<MCPToolExecutor> mcpMock = mockStatic(MCPToolExecutor.class)) {
            mcpMock.when(() -> MCPToolExecutor.executeTool(
                eq("jira_create_ticket_basic"), any(Map.class), any(Map.class)
            )).thenThrow(new RuntimeException("Invalid project"));

            // When/Then
            Exception exception = assertThrows(RuntimeException.class, () -> {
                bridge.executeJavaScript(jsWithError, params);
            });
            
            assertTrue(exception.getMessage().contains("Tool execution failed") || 
                      exception.getMessage().contains("Invalid project"));
        }
    }

    @Test
    void testJavaScriptParameterPassing() throws Exception {
        // Given
        String jsWithComplexParams = """
            function action(params) {
                return {
                    inputTicket: params.inputTicket.key,
                    priority: params.inputTicket.priority,
                    aiResponse: params.aiResponse,
                    processedItems: params.items.length
                };
            }
            """;
        
        JSONObject params = new JSONObject();
        JSONObject ticket = new JSONObject();
        ticket.put("key", "TEST-789");
        ticket.put("priority", "High");
        params.put("inputTicket", ticket);
        params.put("aiResponse", "AI analysis complete");
        params.put("items", new org.json.JSONArray().put("item1").put("item2"));

        // When
        Object result = bridge.executeJavaScript(jsWithComplexParams, params);

        // Then
        assertNotNull(result);
        String resultStr = result.toString();
        assertTrue(resultStr.contains("TEST-789"));
        assertTrue(resultStr.contains("High"));
        assertTrue(resultStr.contains("2")); // items length
    }

    @Test
    void testCachingBehavior() throws Exception {
        // Given
        String githubUrl = "https://github.com/test/repo/blob/main/script.js";
        String jsContent = "function action(params) { return 'cached'; }";
        
        when(mockSourceCode.getFileContent(githubUrl)).thenReturn(jsContent);
        
        JSONObject params = new JSONObject();

        // When - Execute twice
        bridge.executeJavaScript(githubUrl, params);
        bridge.executeJavaScript(githubUrl, params);

        // Then - Should only load from GitHub once due to caching
        verify(mockSourceCode, times(1)).getFileContent(githubUrl);
    }
}


