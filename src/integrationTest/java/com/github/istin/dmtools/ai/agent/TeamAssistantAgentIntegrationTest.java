package com.github.istin.dmtools.ai.agent;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TeamAssistantAgentIntegrationTest {

    private RequestDecompositionAgent.Result createRequest(String aiRole, String request, String[] questions,
                                                           String[] tasks, String[] instructions, String knownInfo) {
        return new RequestDecompositionAgent.Result(aiRole, request, questions, tasks, instructions, knownInfo);
    }

    @Test
    public void testTeamAssistantWithCodeRequest() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Java Developer",
                "How to implement proper error handling for REST endpoints?",
                new String[]{"How to handle validation errors?", "What about logging?"},
                new String[]{"Implement global error handling"},
                new String[]{"Provide code examples", "Include best practices"},
                "Project uses Spring Boot with REST APIs. Ticket: TICKET-123"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain Java code block", response.contains("<code class=\"java\">"));
        assertTrue("Response should contain paragraph", response.contains("<p>"));
        assertFalse("Response should not contain html tag", response.contains("<html>"));
    }

    @Test
    public void testTeamAssistantWithArchitectureQuestion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Software Architect",
                "What's the best way to handle service communication?",
                new String[]{"Should we use synchronous or asynchronous?", "What about latency requirements?"},
                new String[]{"Design service communication pattern"},
                new String[]{"Consider different patterns", "Address scalability concerns"},
                "Microservices architecture with Kafka. Ticket: TICKET-456"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain unordered list", response.contains("<ul>"));
        assertTrue("Response should contain list items", response.contains("<li>"));
        assertFalse("Response should not contain class attributes except in code tags",
                response.replaceAll("<code class=\".*?\">", "").contains("class="));
    }

    @Test
    public void testTeamAssistantWithTechnicalDebtDiscussion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Technical Lead",
                "How should we approach refactoring the authentication module?",
                new String[]{"What are the security implications?", "How to handle deprecated methods?"},
                new String[]{"Plan authentication module refactoring"},
                new String[]{"Consider security aspects", "Provide migration strategy"},
                "Legacy monolithic application. Ticket: TICKET-789"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain table structure", response.contains("<table>"));
        assertTrue("Response should contain emphasized text", response.contains("<strong>"));
        assertFalse("Response should not contain body tag", response.contains("<body>"));
    }

    @Test
    public void testTeamAssistantWithEmptyParams() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "",
                "",
                new String[]{},
                new String[]{},
                new String[]{},
                ""
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain guidance message", response.contains("<p>"));
        assertTrue("Response should contain list of capabilities", response.contains("<ul>"));
    }

    @Test
    public void testTeamAssistantWithOnlyTicketContent() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Performance Engineer",
                "",
                new String[]{},
                new String[]{"Investigate memory leak"},
                new String[]{"Analyze OOM errors", "Provide investigation steps"},
                "TICKET-101: Memory Leak Investigation - OOM errors in production"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should acknowledge ticket content", response.contains("<p>"));
        assertTrue("Response should offer relevant assistance", response.contains("<ul>"));
    }

    @Test
    public void testTeamAssistantWithAuthenticationFlowQuestion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Security Architect",
                "How does our current authentication system work?",
                new String[]{"How is JWT implemented?", "How do refresh tokens work?"},
                new String[]{"Document authentication flow"},
                new String[]{"Explain security measures", "Include flow diagrams"},
                "Spring Security based system with JWT support. Ticket: TICKET-123"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain ordered list", response.contains("<ol>"));
        assertTrue("Response should contain Java code", response.contains("<code class=\"java\">"));
        assertTrue("Response should mention JWT", response.contains("JWT"));
    }

    @Test
    public void testTeamAssistantWithCodeReview() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Senior Java Developer",
                "Review this code: userRepository.findById(id).map(User::getName).orElse(null)",
                new String[]{"Are there any potential issues?", "How can we improve error handling?"},
                new String[]{"Review code", "Suggest improvements"},
                new String[]{"Check for null safety", "Consider Optional usage"},
                "Java Spring Boot application. Ticket: TICKET-456"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain code analysis", response.contains("<ul>"));
        assertTrue("Response should contain Java code example", response.contains("<code class=\"java\">"));
        assertTrue("Response should contain strong emphasis", response.contains("<strong>"));
    }

    @Test
    public void testTeamAssistantWithDatabaseQuery() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Database Expert",
                "How to optimize this query: SELECT u FROM User u WHERE u.status = 'ACTIVE'",
                new String[]{"Would an index help?", "Should we use pagination?"},
                new String[]{"Optimize database query"},
                new String[]{"Consider indexing strategy", "Evaluate query plan"},
                "PostgreSQL with JPA. Ticket: TICKET-303"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain SQL code", response.contains("<code class=\"sql\">"));
        assertTrue("Response should contain optimization tips", response.contains("<ul>"));
        assertTrue("Response should contain explanation", response.contains("<p>"));
    }

    @Test
    public void testTeamAssistantResponseFormat() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        RequestDecompositionAgent.Result decomposedRequest = createRequest(
                "Technical Writer",
                "Document API endpoints",
                new String[]{"What format should we use?"},
                new String[]{"Create API documentation"},
                new String[]{"Use standard format", "Include examples"},
                "Test Project. Ticket: TEST-001"
        );
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(decomposedRequest, null);

        // Act
        String response = agent.run(params);

        // Assert
        assertFalse("Response should not contain HTML tag", response.contains("<html>"));
        assertFalse("Response should not contain HEAD tag", response.contains("<head>"));
        assertFalse("Response should not contain BODY tag", response.contains("<body>"));
        assertTrue("Response should contain at least one paragraph", response.contains("<p>"));
    }
}