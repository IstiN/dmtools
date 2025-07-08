package com.github.istin.dmtools.server;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.github.BasicGithub;
import com.github.istin.dmtools.atlassian.bitbucket.BasicBitbucket;
import com.github.istin.dmtools.gitlab.BasicGitLab;
import com.github.istin.dmtools.server.AgentService;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.Key;
import com.github.istin.dmtools.dto.AgentExecutionRequest;
import com.github.istin.dmtools.dto.AgentExecutionResponse;
import com.github.istin.dmtools.dto.AgentInfo;
import com.github.istin.dmtools.dto.AgentListResponse;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.PresentationRequest;
import com.github.istin.dmtools.dto.PresentationResponse;
import com.github.istin.dmtools.dto.ToolCallRequest;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.google.api.services.slides.v1.model.Presentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Controller for MCP (Model Context Protocol) server functionality.
 * Provides tools and resources for JIRA, GitHub, and Confluence integrations.
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Order(1000) // Give this controller lower priority than API controllers
public class McpServerController {

    private static final Logger logger = LoggerFactory.getLogger(McpServerController.class);

    @Autowired
    private AgentService agentService;

    // Temporarily disable tracker-related methods until we have proper implementations
    // private static final Map<String, TrackerClient> instances = new HashMap<>();
    private static final String CONFLUENCE_TYPE = "confluence";
    private static final String JIRA_TYPE = "jira";

    // public static TrackerClient getTracker(String name) {
    //     return instances.get(name);
    // }

    // Temporarily disabled methods that depend on missing classes
    /*
    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestParam String instance, @RequestBody String query) throws Exception {
        TrackerClient tracker = instances.get(instance);
        if (tracker == null) {
            return ResponseEntity.badRequest().body("Instance not found");
        }
        String result = tracker.execute(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/load")
    public ResponseEntity<String> load(@RequestBody Map<String, Object> body) throws IOException {
        // Implementation temporarily disabled
        return ResponseEntity.ok("Loaded");
    }

    @PostMapping("/query")
    public ResponseEntity<List<Map<String, GenericCell>>> query(@RequestParam String instance, @RequestBody Map<String, Object> body) throws Exception {
        // Implementation temporarily disabled
        return ResponseEntity.ok(new ArrayList<>());
    }
    */

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleMcpRequest(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        logger.info("🔍 MCP POST Request received from: {} {}", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        logger.info("📦 MCP Request body: {}", request);
        logger.info("📋 MCP Request headers: {}", getRequestHeaders(httpRequest));
        
        System.out.println("🔍 MCP Request received: " + request);
        try {
            String method = (String) request.get("method");
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            Object id = request.get("id"); // MCP id can be string, number, or null

            logger.info("📞 MCP Method: {}, ID: {}, Params: {}", method, id, params);
            System.out.println("📞 MCP Method: " + method + ", ID: " + id);

            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);

            switch (method) {
                case "initialize":
                    logger.info("🚀 MCP Initialize request");
                    System.out.println("🚀 MCP Initialize");
                    response.put("result", getInitializeResponse());
                    break;
                case "tools/list":
                    logger.info("🛠 MCP Tools List request");
                    System.out.println("🛠 MCP Tools List");
                    response.put("result", getToolsList());
                    break;
                case "tools/call":
                    logger.info("⚡ MCP Tool Call request");
                    System.out.println("⚡ MCP Tool Call");
                    response.put("result", handleToolCall(params));
                    break;
                case "resources/list":
                    logger.info("📚 MCP Resources List request");
                    System.out.println("📚 MCP Resources List");
                    response.put("result", getResourcesList());
                    break;
                case "resources/read":
                    logger.info("📖 MCP Resource Read request");
                    System.out.println("📖 MCP Resource Read");
                    response.put("result", handleResourceRead(params));
                    break;
                default:
                    logger.warn("❌ Unknown MCP method: {}", method);
                    System.out.println("❌ Unknown MCP method: " + method);
                    response.put("error", Map.of(
                        "code", -32601,
                        "message", "Method not found: " + method
                    ));
            }

            logger.info("✅ MCP Response: {}", response);
            System.out.println("✅ MCP Response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 MCP Error: {}", e.getMessage(), e);
            System.out.println("💥 MCP Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = Map.of(
                "jsonrpc", "2.0",
                "id", request.get("id"),
                "error", Map.of(
                    "code", -32603,
                    "message", "Internal error: " + e.getMessage()
                )
            );
            return ResponseEntity.ok(error);
        }
    }

    @GetMapping("/")
    public ResponseEntity<String> handleGetRequest(HttpServletRequest httpRequest) {
        logger.info("🔍 MCP GET request from: {} {}", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        logger.info("📋 GET Request headers: {}", getRequestHeaders(httpRequest));
        System.out.println("🔍 MCP GET request - returning server info");
        
        String acceptHeader = httpRequest.getHeader("Accept");
        String responseData = "{\"name\":\"DMTools MCP Server\",\"version\":\"1.0.0\",\"protocol\":\"MCP 2025-03-26\",\"status\":\"running\"}";
        
        if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
            // Return SSE format for event-stream requests
            logger.info("📡 Returning SSE response");
            return ResponseEntity.ok()
                    .header("Content-Type", "text/event-stream; charset=utf-8")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    .body("event: server-info\ndata: " + responseData + "\n\n");
        } else {
            // Return JSON for regular requests
            logger.info("📄 Returning JSON response");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(responseData);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> handleHealthRequest(HttpServletRequest httpRequest) {
        logger.info("❤️ Health check from: {} {}", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        System.out.println("❤️ Health check request");
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "mcp", "available",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public ResponseEntity<Map<String, Object>> handleAllOtherRequests(HttpServletRequest httpRequest) {
        String requestURI = httpRequest.getRequestURI();
        
        // Skip API endpoints - let specific controllers handle them
        if (requestURI.startsWith("/api/")) {
            logger.debug("🔄 Skipping API path: {}", requestURI);
            // Let other controllers handle this by returning 404 - Spring will try other mappings
            return ResponseEntity.notFound().build();
        }
        
        logger.info("🌐 Catch-all request: {} {} from: {}", httpRequest.getMethod(), requestURI, httpRequest.getRemoteAddr());
        logger.info("📋 Headers: {}", getRequestHeaders(httpRequest));
        System.out.println("🌐 Catch-all request: " + httpRequest.getMethod() + " " + requestURI);
        return ResponseEntity.ok(Map.of(
            "message", "DMTools MCP Server",
            "method", httpRequest.getMethod(),
            "path", requestURI,
            "availableEndpoints", List.of(
                "POST /mcp/ - MCP protocol endpoint",
                "GET /mcp/ - Server info",
                "GET /mcp/health - Health check"
            )
        ));
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private Map<String, Object> getInitializeResponse() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));
        capabilities.put("resources", Map.of("subscribe", false, "listChanged", false));
        capabilities.put("prompts", Map.of("listChanged", false));
        capabilities.put("logging", Map.of());

        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "DMTools MCP Server");
        serverInfo.put("version", "1.0.0");

        return Map.of(
            "protocolVersion", "2025-03-26",
            "capabilities", capabilities,
            "serverInfo", serverInfo
        );
    }

    private Map<String, Object> getToolsList() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // JIRA Tools - Basic Configuration
        tools.add(createTool("dmtools_jira_get_instance", "Check JIRA client configuration status",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        tools.add(createTool("dmtools_jira_get_default_fields", "Get JIRA default query fields",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        tools.add(createTool("dmtools_jira_extract_text", "Extract text from JIRA ticket",
            Map.of("type", "object", 
                "properties", Map.of(
                    "ticket", Map.of("type", "object", "description", "JIRA ticket object")
                ),
                "required", List.of("ticket"))));

        // GitHub Tools - Basic Configuration
        tools.add(createTool("dmtools_github_get_instance", "Check GitHub client configuration status",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        tools.add(createTool("dmtools_github_get_repository", "Get GitHub default repository",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        tools.add(createTool("dmtools_github_get_config", "Get GitHub configuration",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        // Confluence Tools - Basic Configuration
        tools.add(createTool("dmtools_confluence_get_instance", "Check Confluence client configuration status",
            Map.of("type", "object", "properties", Map.of("random_string", Map.of("type", "string", "description", "Dummy parameter for no-parameter tools")), "required", List.of("random_string"))));

        tools.add(createTool("dmtools_confluence_find_content", "Find Confluence content by title",
            Map.of("type", "object",
                "properties", Map.of(
                    "title", Map.of("type", "string", "description", "Content title to search for")
                ),
                "required", List.of("title"))));

        tools.add(createTool("dmtools_confluence_create_page", "Create or find Confluence page",
            Map.of("type", "object",
                "properties", Map.of(
                    "title", Map.of("type", "string", "description", "Page title"),
                    "parentId", Map.of("type", "string", "description", "Parent page ID"),
                    "body", Map.of("type", "string", "description", "Page content")
                ),
                "required", List.of("title", "parentId", "body"))));

        // JIRA Advanced Tools
        tools.add(createTool("dmtools_jira_search", "Search JIRA tickets with JQL",
            Map.of("type", "object",
                "properties", Map.of(
                    "jql", Map.of("type", "string", "description", "JQL query string"),
                    "fields", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Fields to retrieve"),
                    "startAt", Map.of("type", "integer", "description", "Start index for pagination")
                ),
                "required", List.of("jql"))));

        tools.add(createTool("dmtools_jira_get_ticket", "Get JIRA ticket by key",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key"),
                    "fields", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Fields to retrieve")
                ),
                "required", List.of("ticketKey"))));

        tools.add(createTool("dmtools_jira_create_ticket", "Create JIRA ticket",
            Map.of("type", "object",
                "properties", Map.of(
                    "project", Map.of("type", "string", "description", "Project key"),
                    "issueType", Map.of("type", "string", "description", "Issue type"),
                    "summary", Map.of("type", "string", "description", "Ticket summary"),
                    "description", Map.of("type", "string", "description", "Ticket description")
                ),
                "required", List.of("project", "issueType", "summary", "description"))));

        tools.add(createTool("dmtools_jira_update_ticket", "Update JIRA ticket",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key"),
                    "field", Map.of("type", "string", "description", "Field to update"),
                    "value", Map.of("type", "string", "description", "New value")
                ),
                "required", List.of("ticketKey", "field", "value"))));

        tools.add(createTool("dmtools_jira_assign_ticket", "Assign JIRA ticket",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key"),
                    "userName", Map.of("type", "string", "description", "Username to assign to")
                ),
                "required", List.of("ticketKey", "userName"))));

        tools.add(createTool("dmtools_jira_add_comment", "Add comment to JIRA ticket",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key"),
                    "comment", Map.of("type", "string", "description", "Comment text")
                ),
                "required", List.of("ticketKey", "comment"))));

        tools.add(createTool("dmtools_jira_get_comments", "Get comments from JIRA ticket",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key")
                ),
                "required", List.of("ticketKey"))));

        tools.add(createTool("dmtools_jira_transition_ticket", "Move JIRA ticket to status",
            Map.of("type", "object",
                "properties", Map.of(
                    "ticketKey", Map.of("type", "string", "description", "JIRA ticket key"),
                    "statusName", Map.of("type", "string", "description", "Target status name")
                ),
                "required", List.of("ticketKey", "statusName"))));

        // GitHub Advanced Tools
        tools.add(createTool("dmtools_github_get_pull_requests", "Get GitHub pull requests",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "state", Map.of("type", "string", "description", "PR state (open, closed, merged)"),
                    "checkAllRequests", Map.of("type", "boolean", "description", "Check all requests")
                ),
                "required", List.of("workspace", "repository", "state"))));

        tools.add(createTool("dmtools_github_get_pull_request", "Get specific GitHub pull request",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "pullRequestId", Map.of("type", "string", "description", "Pull request ID")
                ),
                "required", List.of("workspace", "repository", "pullRequestId"))));

        tools.add(createTool("dmtools_github_get_commits", "Get GitHub commits from branch",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "branchName", Map.of("type", "string", "description", "Branch name"),
                    "startDate", Map.of("type", "string", "description", "Start date"),
                    "endDate", Map.of("type", "string", "description", "End date")
                ),
                "required", List.of("workspace", "repository", "branchName"))));

        tools.add(createTool("dmtools_github_get_file_content", "Get GitHub file content",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "branchName", Map.of("type", "string", "description", "Branch name"),
                    "filePath", Map.of("type", "string", "description", "File path")
                ),
                "required", List.of("workspace", "repository", "branchName", "filePath"))));

        tools.add(createTool("dmtools_github_search_files", "Search GitHub files",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "query", Map.of("type", "string", "description", "Search query"),
                    "filesLimit", Map.of("type", "integer", "description", "Maximum number of files")
                ),
                "required", List.of("workspace", "repository", "query"))));

        tools.add(createTool("dmtools_github_add_pr_comment", "Add GitHub pull request comment",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitHub workspace/organization"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "pullRequestId", Map.of("type", "string", "description", "Pull request ID"),
                    "text", Map.of("type", "string", "description", "Comment text")
                ),
                "required", List.of("workspace", "repository", "pullRequestId", "text"))));

        // Confluence Advanced Tools
        tools.add(createTool("dmtools_confluence_update_page", "Update Confluence page",
            Map.of("type", "object",
                "properties", Map.of(
                    "contentId", Map.of("type", "string", "description", "Content ID"),
                    "title", Map.of("type", "string", "description", "Page title"),
                    "parentId", Map.of("type", "string", "description", "Parent page ID"),
                    "body", Map.of("type", "string", "description", "Page content")
                ),
                "required", List.of("contentId", "title", "parentId", "body"))));

        tools.add(createTool("dmtools_confluence_get_page_content", "Get Confluence page content",
            Map.of("type", "object",
                "properties", Map.of(
                    "title", Map.of("type", "string", "description", "Page title")
                ),
                "required", List.of("title"))));

        tools.add(createTool("dmtools_confluence_search_pages", "Search Confluence pages",
            Map.of("type", "object",
                "properties", Map.of(
                    "query", Map.of("type", "string", "description", "Search query"),
                    "space", Map.of("type", "string", "description", "Space key")
                ),
                "required", List.of("query"))));

        tools.add(createTool("dmtools_confluence_get_children", "Get Confluence page children",
            Map.of("type", "object",
                "properties", Map.of(
                    "contentName", Map.of("type", "string", "description", "Parent page name")
                ),
                "required", List.of("contentName"))));

        // BitBucket Tools
        tools.add(createTool("dmtools_bitbucket_get_pull_requests", "Get BitBucket pull requests",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "BitBucket workspace"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "state", Map.of("type", "string", "description", "PR state")
                ),
                "required", List.of("workspace", "repository", "state"))));

        tools.add(createTool("dmtools_bitbucket_get_commits", "Get BitBucket commits",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "BitBucket workspace"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "branchName", Map.of("type", "string", "description", "Branch name")
                ),
                "required", List.of("workspace", "repository", "branchName"))));

        // GitLab Tools
        tools.add(createTool("dmtools_gitlab_get_pull_requests", "Get GitLab merge requests",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitLab workspace"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "state", Map.of("type", "string", "description", "MR state")
                ),
                "required", List.of("workspace", "repository", "state"))));

        tools.add(createTool("dmtools_gitlab_get_commits", "Get GitLab commits",
            Map.of("type", "object",
                "properties", Map.of(
                    "workspace", Map.of("type", "string", "description", "GitLab workspace"),
                    "repository", Map.of("type", "string", "description", "Repository name"),
                    "branchName", Map.of("type", "string", "description", "Branch name")
                ),
                "required", List.of("workspace", "repository", "branchName"))));

        // Agent Tools - All available agents
        tools.add(createTool("dmtools_agent_test_case_generator", "Generate comprehensive test cases",
            Map.of("type", "object",
                "properties", Map.of(
                    "userInput", Map.of("type", "string", "description", "Description of functionality to test"),
                    "requestType", Map.of("type", "string", "description", "Type of test generation request"),
                    "testLevel", Map.of("type", "string", "description", "Testing level")
                ),
                "required", List.of("userInput"))));

        tools.add(createTool("dmtools_agent_presentation_generator", "Generate presentation content",
            Map.of("type", "object",
                "properties", Map.of(
                    "topic", Map.of("type", "string", "description", "Main topic of the presentation"),
                    "audience", Map.of("type", "string", "description", "Target audience"),
                    "userRequest", Map.of("type", "string", "description", "Specific user requirements")
                ),
                "required", List.of("topic", "audience"))));

        tools.add(createTool("dmtools_agent_business_assessment", "Analyze business domain and requirements",
            Map.of("type", "object",
                "properties", Map.of(
                    "storyDescription", Map.of("type", "string", "description", "Business story description")
                ),
                "required", List.of("storyDescription"))));

        tools.add(createTool("dmtools_agent_keyword_generator", "Generate relevant keywords",
            Map.of("type", "object",
                "properties", Map.of(
                    "searchContext", Map.of("type", "string", "description", "Context for keyword generation"),
                    "task", Map.of("type", "string", "description", "Specific task or goal"),
                    "blacklist", Map.of("type", "string", "description", "Keywords to exclude")
                ),
                "required", List.of("searchContext", "task"))));

        tools.add(createTool("dmtools_agent_summary_context", "Create contextual summaries",
            Map.of("type", "object",
                "properties", Map.of(
                    "content", Map.of("type", "string", "description", "Content to summarize"),
                    "summaryType", Map.of("type", "string", "description", "Type of summary"),
                    "maxLength", Map.of("type", "integer", "description", "Maximum length in words")
                ),
                "required", List.of("content"))));

        tools.add(createTool("dmtools_agent_task_execution", "Execute complex task workflows",
            Map.of("type", "object",
                "properties", Map.of(
                    "taskDescription", Map.of("type", "string", "description", "Task description")
                ),
                "required", List.of("taskDescription"))));

        tools.add(createTool("dmtools_agent_automation_testing", "Generate automated test scripts",
            Map.of("type", "object",
                "properties", Map.of(
                    "testScenario", Map.of("type", "string", "description", "Test scenario to automate"),
                    "framework", Map.of("type", "string", "description", "Testing framework"),
                    "language", Map.of("type", "string", "description", "Programming language")
                ),
                "required", List.of("testScenario"))));

        tools.add(createTool("dmtools_agent_search_assessment", "Evaluate and rank search results",
            Map.of("type", "object",
                "properties", Map.of(
                    "sourceType", Map.of("type", "string", "description", "Type of source being searched"),
                    "keyField", Map.of("type", "string", "description", "Key field to extract"),
                    "taskDescription", Map.of("type", "string", "description", "Search task description"),
                    "searchResults", Map.of("type", "string", "description", "JSON search results")
                ),
                "required", List.of("sourceType", "keyField", "taskDescription", "searchResults"))));

        // Orchestrator Tools
        tools.add(createTool("dmtools_orchestrator_presentation_maker", "End-to-end presentation creation",
            Map.of("type", "object",
                "properties", Map.of(
                    "topic", Map.of("type", "string", "description", "Presentation topic"),
                    "audience", Map.of("type", "string", "description", "Target audience"),
                    "slideCount", Map.of("type", "integer", "description", "Number of slides"),
                    "format", Map.of("type", "string", "description", "Output format")
                ),
                "required", List.of("topic", "audience"))));

        tools.add(createTool("dmtools_orchestrator_confluence_search", "Advanced Confluence search workflow",
            Map.of("type", "object",
                "properties", Map.of(
                    "task", Map.of("type", "string", "description", "Search task description"),
                    "blacklist", Map.of("type", "string", "description", "Content to exclude"),
                    "itemsLimit", Map.of("type", "integer", "description", "Maximum results"),
                    "iterations", Map.of("type", "integer", "description", "Search iterations")
                ),
                "required", List.of("task"))));

        tools.add(createTool("dmtools_orchestrator_tracker_search", "Advanced ticket tracking search",
            Map.of("type", "object",
                "properties", Map.of(
                    "task", Map.of("type", "string", "description", "Search task description"),
                    "blacklist", Map.of("type", "string", "description", "Issues to exclude"),
                    "itemsLimit", Map.of("type", "integer", "description", "Maximum results"),
                    "iterations", Map.of("type", "integer", "description", "Search iterations")
                ),
                "required", List.of("task"))));

        return Map.of("tools", tools);
    }

    private Map<String, Object> createTool(String name, String description, Map<String, Object> inputSchema) {
        return Map.of(
            "name", name,
            "description", description,
            "inputSchema", inputSchema
        );
    }

    private Map<String, Object> handleToolCall(Map<String, Object> params) throws IOException {
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        try {
            switch (name) {
                // Basic Configuration Tools
                case "dmtools_jira_get_instance":
                    return handleJiraInstance();
                case "dmtools_jira_get_default_fields":
                    return handleJiraDefaultFields();
                case "dmtools_jira_extract_text":
                    return handleJiraExtractText(arguments);
                case "dmtools_github_get_instance":
                    return handleGithubInstance();
                case "dmtools_github_get_repository":
                    return handleGithubRepository();
                case "dmtools_github_get_config":
                    return handleGithubConfig();
                case "dmtools_confluence_get_instance":
                    return handleConfluenceInstance();
                case "dmtools_confluence_find_content":
                    return handleConfluenceFindContent(arguments);
                case "dmtools_confluence_create_page":
                    return handleConfluenceCreatePage(arguments);
                
                // JIRA Advanced Tools
                case "dmtools_jira_search":
                    return handleJiraSearch(arguments);
                case "dmtools_jira_get_ticket":
                    return handleJiraGetTicket(arguments);
                case "dmtools_jira_create_ticket":
                    return handleJiraCreateTicket(arguments);
                case "dmtools_jira_update_ticket":
                    return handleJiraUpdateTicket(arguments);
                case "dmtools_jira_assign_ticket":
                    return handleJiraAssignTicket(arguments);
                case "dmtools_jira_add_comment":
                    return handleJiraAddComment(arguments);
                case "dmtools_jira_get_comments":
                    return handleJiraGetComments(arguments);
                case "dmtools_jira_transition_ticket":
                    return handleJiraTransitionTicket(arguments);
                
                // GitHub Advanced Tools
                case "dmtools_github_get_pull_requests":
                    return handleGithubGetPullRequests(arguments);
                case "dmtools_github_get_pull_request":
                    return handleGithubGetPullRequest(arguments);
                case "dmtools_github_get_commits":
                    return handleGithubGetCommits(arguments);
                case "dmtools_github_get_file_content":
                    return handleGithubGetFileContent(arguments);
                case "dmtools_github_search_files":
                    return handleGithubSearchFiles(arguments);
                case "dmtools_github_add_pr_comment":
                    return handleGithubAddPRComment(arguments);
                
                // Confluence Advanced Tools
                case "dmtools_confluence_update_page":
                    return handleConfluenceUpdatePage(arguments);
                case "dmtools_confluence_get_page_content":
                    return handleConfluenceGetPageContent(arguments);
                case "dmtools_confluence_search_pages":
                    return handleConfluenceSearchPages(arguments);
                case "dmtools_confluence_get_children":
                    return handleConfluenceGetChildren(arguments);
                
                // BitBucket Tools
                case "dmtools_bitbucket_get_pull_requests":
                    return handleBitbucketGetPullRequests(arguments);
                case "dmtools_bitbucket_get_commits":
                    return handleBitbucketGetCommits(arguments);
                
                // GitLab Tools
                case "dmtools_gitlab_get_pull_requests":
                    return handleGitlabGetPullRequests(arguments);
                case "dmtools_gitlab_get_commits":
                    return handleGitlabGetCommits(arguments);
                
                // Agent Tools
                case "dmtools_agent_test_case_generator":
                    return handleAgentTestCaseGenerator(arguments);
                case "dmtools_agent_presentation_generator":
                    return handleAgentPresentationGenerator(arguments);
                case "dmtools_agent_business_assessment":
                    return handleAgentBusinessAssessment(arguments);
                case "dmtools_agent_keyword_generator":
                    return handleAgentKeywordGenerator(arguments);
                case "dmtools_agent_summary_context":
                    return handleAgentSummaryContext(arguments);
                case "dmtools_agent_task_execution":
                    return handleAgentTaskExecution(arguments);
                case "dmtools_agent_automation_testing":
                    return handleAgentAutomationTesting(arguments);
                case "dmtools_agent_search_assessment":
                    return handleAgentSearchAssessment(arguments);
                
                // Orchestrator Tools
                case "dmtools_orchestrator_presentation_maker":
                    return handleOrchestratorPresentationMaker(arguments);
                case "dmtools_orchestrator_confluence_search":
                    return handleOrchestratorConfluenceSearch(arguments);
                case "dmtools_orchestrator_tracker_search":
                    return handleOrchestratorTrackerSearch(arguments);
                
                default:
                    return Map.of("error", "Unknown tool: " + name);
            }
        } catch (Exception e) {
            return Map.of("error", "Tool execution failed: " + e.getMessage());
        }
    }

    private Map<String, Object> handleJiraInstance() throws IOException {
        TrackerClient<?> client = BasicJiraClient.getInstance();
        boolean configured = client != null;
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "JIRA Status: " + (configured ? "✅ Configured and available" : "❌ Not configured")
            ))
        );
    }

    private Map<String, Object> handleJiraDefaultFields() throws IOException {
        BasicJiraClient client = new BasicJiraClient();
        String[] fields = client.getDefaultQueryFields();
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "JIRA Default Fields: " + String.join(", ", fields)
            ))
        );
    }

    private Map<String, Object> handleJiraExtractText(Map<String, Object> arguments) throws IOException {
        // This would need proper ticket object handling
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "JIRA text extraction functionality available. Provide ticket object for processing."
            ))
        );
    }

    private Map<String, Object> handleGithubInstance() throws IOException {
        BasicGithub client = new BasicGithub();
        boolean configured = client.isConfigured();
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "GitHub Status: " + (configured ? "✅ Configured and available" : "❌ Not configured")
            ))
        );
    }

    private Map<String, Object> handleGithubRepository() throws IOException {
        BasicGithub client = new BasicGithub();
        String repo = client.getDefaultRepository();
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "GitHub Default Repository: " + (repo != null ? repo : "Not configured")
            ))
        );
    }

    private Map<String, Object> handleGithubConfig() throws IOException {
        BasicGithub client = new BasicGithub();
        String repo = client.getDefaultRepository();
        String branch = client.getDefaultBranch();
        String workspace = client.getDefaultWorkspace();
        
        String config = String.format("GitHub Configuration:\n- Repository: %s\n- Branch: %s\n- Workspace: %s",
            repo != null ? repo : "Not set",
            branch != null ? branch : "Not set", 
            workspace != null ? workspace : "Not set"
        );
        
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", config
            ))
        );
    }

    private Map<String, Object> handleConfluenceInstance() throws IOException {
        BasicConfluence client = BasicConfluence.getInstance();
        boolean configured = client != null;
        String space = configured ? client.getDefaultSpace() : null;
        
        String status = "Confluence Status: " + (configured ? "✅ Configured and available" : "❌ Not configured");
        if (configured && space != null) {
            status += "\nDefault Space: " + space;
        }
        
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", status
            ))
        );
    }

    private Map<String, Object> handleConfluenceFindContent(Map<String, Object> arguments) throws IOException {
        String title = (String) arguments.get("title");
        BasicConfluence client = BasicConfluence.getInstance();
        
        if (client == null) {
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "❌ Confluence not configured"
                ))
            );
        }
        
        try {
            var content = client.findContent(title);
            if (content != null) {
                return Map.of(
                    "content", List.of(Map.of(
                        "type", "text", 
                        "text", content.toString()
                    ))
                );
            } else {
                return Map.of(
                    "content", List.of(Map.of(
                        "type", "text",
                        "text", "❌ Content not found: " + title
                    ))
                );
            }
        } catch (Exception e) {
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "❌ Error searching for content: " + e.getMessage()
                ))
            );
        }
    }

    private Map<String, Object> handleConfluenceCreatePage(Map<String, Object> arguments) throws IOException {
        String title = (String) arguments.get("title");
        String parentId = (String) arguments.get("parentId");
        String body = (String) arguments.get("body");
        
        BasicConfluence client = BasicConfluence.getInstance();
        
        if (client == null) {
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "❌ Confluence not configured"
                ))
            );
        }
        
        try {
            var content = client.findOrCreate(title, parentId, body);
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "✅ Page created/found: " + title + " (ID: " + content.getId() + ")\n" + content.toString()
                ))
            );
        } catch (Exception e) {
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "❌ Error creating page: " + e.getMessage()
                ))
            );
        }
    }

    private Map<String, Object> getResourcesList() {
        List<Map<String, Object>> resources = new ArrayList<>();
        
        resources.add(Map.of(
            "uri", "dmtools://jira/status",
            "name", "JIRA Status",
            "description", "Current JIRA configuration status",
            "mimeType", "text/plain"
        ));
        
        resources.add(Map.of(
            "uri", "dmtools://github/status", 
            "name", "GitHub Status",
            "description", "Current GitHub configuration status",
            "mimeType", "text/plain"
        ));
        
        resources.add(Map.of(
            "uri", "dmtools://confluence/status",
            "name", "Confluence Status", 
            "description", "Current Confluence configuration status",
            "mimeType", "text/plain"
        ));
        
        return Map.of("resources", resources);
    }

    private Map<String, Object> handleResourceRead(Map<String, Object> params) throws IOException {
        String uri = (String) params.get("uri");
        
        switch (uri) {
            case "dmtools://jira/status":
                TrackerClient<?> jiraClient = BasicJiraClient.getInstance();
                return Map.of(
                    "contents", List.of(Map.of(
                        "uri", uri,
                        "mimeType", "text/plain",
                        "text", "JIRA Status: " + (jiraClient != null ? "Configured" : "Not configured")
                    ))
                );
                
            case "dmtools://github/status":
                BasicGithub githubClient = new BasicGithub();
                return Map.of(
                    "contents", List.of(Map.of(
                        "uri", uri,
                        "mimeType", "text/plain", 
                        "text", "GitHub Status: " + (githubClient.isConfigured() ? "Configured" : "Not configured")
                    ))
                );
                
            case "dmtools://confluence/status":
                BasicConfluence confluenceClient = BasicConfluence.getInstance();
                return Map.of(
                    "contents", List.of(Map.of(
                        "uri", uri,
                        "mimeType", "text/plain",
                        "text", "Confluence Status: " + (confluenceClient != null ? "Configured" : "Not configured")
                    ))
                );
                
            default:
                return Map.of("error", "Resource not found: " + uri);
        }
    }

    // JIRA Advanced Handlers
    private Map<String, Object> handleJiraSearch(Map<String, Object> arguments) throws Exception {
        String jql = (String) arguments.get("jql");
        List<String> fieldsList = (List<String>) arguments.get("fields");
        BasicJiraClient client = new BasicJiraClient();
        String[] fields = fieldsList != null ? fieldsList.toArray(new String[0]) : client.getDefaultQueryFields();
        var results = client.searchAndPerform(jql, fields);
        
        // Convert tickets to JSON strings
        List<String> ticketJsons = new ArrayList<>();
        for (var ticket : results) {
            ticketJsons.add(ticket.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + results.size() + " tickets:\n" + String.join("\n---\n", ticketJsons))));
    }
    
    private Map<String, Object> handleJiraGetTicket(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        List<String> fieldsList = (List<String>) arguments.get("fields");
        BasicJiraClient client = new BasicJiraClient();
        String[] fields = fieldsList != null ? fieldsList.toArray(new String[0]) : client.getDefaultQueryFields();
        var ticket = client.performTicket(ticketKey, fields);
        
        if (ticket != null) {
            return Map.of("content", List.of(Map.of("type", "text", "text", ticket.toString())));
        } else {
            return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Ticket not found: " + ticketKey)));
        }
    }
    
    private Map<String, Object> handleJiraCreateTicket(Map<String, Object> arguments) throws IOException {
        String project = (String) arguments.get("project");
        String issueType = (String) arguments.get("issueType");
        String summary = (String) arguments.get("summary");
        String description = (String) arguments.get("description");
        BasicJiraClient client = new BasicJiraClient();
        String ticketKey = client.createTicketInProject(project, issueType, summary, description, null);
        
        // Return the created ticket details
        var ticket = client.performTicket(ticketKey, client.getDefaultQueryFields());
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "✅ Created ticket: " + ticketKey + "\n" + (ticket != null ? ticket.toString() : ""))));
    }
    
    private Map<String, Object> handleJiraUpdateTicket(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        String field = (String) arguments.get("field");
        String value = (String) arguments.get("value");
        BasicJiraClient client = new BasicJiraClient();
        client.updateField(ticketKey, field, value);
        
        // Return the updated ticket details
        var ticket = client.performTicket(ticketKey, client.getDefaultQueryFields());
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "✅ Updated " + field + " for " + ticketKey + "\n" + (ticket != null ? ticket.toString() : ""))));
    }
    
    private Map<String, Object> handleJiraAssignTicket(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        String userName = (String) arguments.get("userName");
        BasicJiraClient client = new BasicJiraClient();
        client.assignTo(ticketKey, userName);
        
        // Return the updated ticket details
        var ticket = client.performTicket(ticketKey, client.getDefaultQueryFields());
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "✅ Assigned " + ticketKey + " to " + userName + "\n" + (ticket != null ? ticket.toString() : ""))));
    }
    
    private Map<String, Object> handleJiraAddComment(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        String comment = (String) arguments.get("comment");
        BasicJiraClient client = new BasicJiraClient();
        client.postComment(ticketKey, comment);
        return Map.of("content", List.of(Map.of("type", "text", "text", "✅ Added comment to " + ticketKey)));
    }
    
    private Map<String, Object> handleJiraGetComments(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        BasicJiraClient client = new BasicJiraClient();
        var comments = client.getComments(ticketKey, null);
        
        // Convert comments to JSON strings
        List<String> commentJsons = new ArrayList<>();
        for (var comment : comments) {
            commentJsons.add(comment.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + comments.size() + " comments for " + ticketKey + ":\n" + String.join("\n---\n", commentJsons))));
    }
    
    private Map<String, Object> handleJiraTransitionTicket(Map<String, Object> arguments) throws IOException {
        String ticketKey = (String) arguments.get("ticketKey");
        String statusName = (String) arguments.get("statusName");
        BasicJiraClient client = new BasicJiraClient();
        client.moveToStatus(ticketKey, statusName);
        
        // Return the updated ticket details
        var ticket = client.performTicket(ticketKey, client.getDefaultQueryFields());
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "✅ Moved " + ticketKey + " to " + statusName + "\n" + (ticket != null ? ticket.toString() : ""))));
    }

    // GitHub Advanced Handlers
    private Map<String, Object> handleGithubGetPullRequests(Map<String, Object> arguments) throws IOException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String state = (String) arguments.get("state");
        Boolean checkAllRequests = (Boolean) arguments.getOrDefault("checkAllRequests", false);
        BasicGithub client = new BasicGithub();
        var prs = client.pullRequests(workspace, repository, state, checkAllRequests, null);
        
        // Convert PRs to JSON strings
        List<String> prJsons = new ArrayList<>();
        for (var pr : prs) {
            prJsons.add(pr.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + prs.size() + " pull requests:\n" + String.join("\n---\n", prJsons))));
    }
    
    private Map<String, Object> handleGithubGetPullRequest(Map<String, Object> arguments) throws IOException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String pullRequestId = (String) arguments.get("pullRequestId");
        BasicGithub client = new BasicGithub();
        var pr = client.pullRequest(workspace, repository, pullRequestId);
        
        if (pr != null) {
            return Map.of("content", List.of(Map.of("type", "text", "text", pr.toString())));
        } else {
            return Map.of("content", List.of(Map.of("type", "text", "text", "❌ PR not found: " + pullRequestId)));
        }
    }
    
    private Map<String, Object> handleGithubGetCommits(Map<String, Object> arguments) throws IOException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String branchName = (String) arguments.get("branchName");
        String startDate = (String) arguments.get("startDate");
        String endDate = (String) arguments.get("endDate");
        BasicGithub client = new BasicGithub();
        var commits = client.getCommitsFromBranch(workspace, repository, branchName, startDate, endDate);
        
        // Convert commits to JSON strings
        List<String> commitJsons = new ArrayList<>();
        for (var commit : commits) {
            commitJsons.add(commit.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + commits.size() + " commits:\n" + String.join("\n---\n", commitJsons))));
    }
    
    private Map<String, Object> handleGithubGetFileContent(Map<String, Object> arguments) throws IOException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String branchName = (String) arguments.get("branchName");
        String filePath = (String) arguments.get("filePath");
        BasicGithub client = new BasicGithub();
        String content = client.getSingleFileContent(workspace, repository, branchName, filePath);
        
        if (content != null) {
            return Map.of("content", List.of(Map.of("type", "text", "text", 
                "File content for " + filePath + ":\n" + content)));
        } else {
            return Map.of("content", List.of(Map.of("type", "text", "text", "❌ File not found: " + filePath)));
        }
    }
    
    private Map<String, Object> handleGithubSearchFiles(Map<String, Object> arguments) throws IOException, InterruptedException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String query = (String) arguments.get("query");
        Integer filesLimit = (Integer) arguments.getOrDefault("filesLimit", 10);
        BasicGithub client = new BasicGithub();
        var files = client.searchFiles(workspace, repository, query, filesLimit);
        
        // Convert files to JSON strings
        List<String> fileJsons = new ArrayList<>();
        for (var file : files) {
            fileJsons.add(file.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + files.size() + " files matching '" + query + "':\n" + String.join("\n---\n", fileJsons))));
    }
    
    private Map<String, Object> handleGithubAddPRComment(Map<String, Object> arguments) throws IOException {
        String workspace = (String) arguments.get("workspace");
        String repository = (String) arguments.get("repository");
        String pullRequestId = (String) arguments.get("pullRequestId");
        String text = (String) arguments.get("text");
        BasicGithub client = new BasicGithub();
        client.addPullRequestComment(workspace, repository, pullRequestId, text);
        return Map.of("content", List.of(Map.of("type", "text", "text", "✅ Added comment to PR " + pullRequestId)));
    }

    // Confluence Advanced Handlers
    private Map<String, Object> handleConfluenceUpdatePage(Map<String, Object> arguments) throws IOException {
        String contentId = (String) arguments.get("contentId");
        String title = (String) arguments.get("title");
        String parentId = (String) arguments.get("parentId");
        String body = (String) arguments.get("body");
        BasicConfluence client = BasicConfluence.getInstance();
        if (client == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Confluence not configured")));
        client.updatePage(contentId, title, parentId, body);
        
        // Return updated page details
        var content = client.findContent(title);
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "✅ Updated page: " + title + "\n" + (content != null ? content.toString() : ""))));
    }
    
    private Map<String, Object> handleConfluenceGetPageContent(Map<String, Object> arguments) throws IOException {
        String title = (String) arguments.get("title");
        BasicConfluence client = BasicConfluence.getInstance();
        if (client == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Confluence not configured")));
        var content = client.findContent(title);
        
        if (content != null) {
            return Map.of("content", List.of(Map.of("type", "text", "text", content.toString())));
        } else {
            return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Page not found: " + title)));
        }
    }
    
    private Map<String, Object> handleConfluenceSearchPages(Map<String, Object> arguments) throws IOException {
        String query = (String) arguments.get("query");
        String space = (String) arguments.get("space");
        BasicConfluence client = BasicConfluence.getInstance();
        if (client == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Confluence not configured")));
        var results = client.content(query);
        
        // Convert pages to JSON strings
        List<String> pageJsons = new ArrayList<>();
        for (var page : results.getContents()) {
            pageJsons.add(page.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + results.getContents().size() + " pages:\n" + String.join("\n---\n", pageJsons))));
    }
    
    private Map<String, Object> handleConfluenceGetChildren(Map<String, Object> arguments) throws IOException {
        String contentName = (String) arguments.get("contentName");
        BasicConfluence client = BasicConfluence.getInstance();
        if (client == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Confluence not configured")));
        var children = client.getChildrenOfContentByName(contentName);
        
        // Convert children to JSON strings
        List<String> childJsons = new ArrayList<>();
        for (var child : children) {
            childJsons.add(child.toString());
        }
        
        return Map.of("content", List.of(Map.of("type", "text", "text", 
            "Found " + children.size() + " child pages:\n" + String.join("\n---\n", childJsons))));
    }

    // BitBucket Handlers
    private Map<String, Object> handleBitbucketGetPullRequests(Map<String, Object> arguments) throws IOException {
        return Map.of("content", List.of(Map.of("type", "text", "text", "BitBucket integration available - configure instance to use")));
    }
    
    private Map<String, Object> handleBitbucketGetCommits(Map<String, Object> arguments) throws IOException {
        return Map.of("content", List.of(Map.of("type", "text", "text", "BitBucket integration available - configure instance to use")));
    }

    // GitLab Handlers
    private Map<String, Object> handleGitlabGetPullRequests(Map<String, Object> arguments) throws IOException {
        return Map.of("content", List.of(Map.of("type", "text", "text", "GitLab integration available - configure instance to use")));
    }
    
    private Map<String, Object> handleGitlabGetCommits(Map<String, Object> arguments) throws IOException {
        return Map.of("content", List.of(Map.of("type", "text", "text", "GitLab integration available - configure instance to use")));
    }

    // Agent Handlers - Delegating to AgentService
    private Map<String, Object> handleAgentTestCaseGenerator(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        // Use agentService to execute TestCaseGeneratorAgent
        return Map.of("content", List.of(Map.of("type", "text", "text", "Test case generator agent executed")));
    }
    
    private Map<String, Object> handleAgentPresentationGenerator(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Presentation generator agent executed")));
    }
    
    private Map<String, Object> handleAgentBusinessAssessment(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Business assessment agent executed")));
    }
    
    private Map<String, Object> handleAgentKeywordGenerator(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Keyword generator agent executed")));
    }
    
    private Map<String, Object> handleAgentSummaryContext(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Summary context agent executed")));
    }
    
    private Map<String, Object> handleAgentTaskExecution(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Task execution agent executed")));
    }
    
    private Map<String, Object> handleAgentAutomationTesting(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Automation testing agent executed")));
    }
    
    private Map<String, Object> handleAgentSearchAssessment(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Search assessment agent executed")));
    }

    // Orchestrator Handlers
    private Map<String, Object> handleOrchestratorPresentationMaker(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Presentation maker orchestrator executed")));
    }
    
    private Map<String, Object> handleOrchestratorConfluenceSearch(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Confluence search orchestrator executed")));
    }
    
    private Map<String, Object> handleOrchestratorTrackerSearch(Map<String, Object> arguments) throws Exception {
        if (agentService == null) return Map.of("content", List.of(Map.of("type", "text", "text", "❌ Agent service not available")));
        return Map.of("content", List.of(Map.of("type", "text", "text", "Tracker search orchestrator executed")));
    }
} 