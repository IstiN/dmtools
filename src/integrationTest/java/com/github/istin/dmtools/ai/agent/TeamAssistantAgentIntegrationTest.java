package com.github.istin.dmtools.ai.agent;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TeamAssistantAgentIntegrationTest {

    @Test
    public void testTeamAssistantWithCodeRequest() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Java Spring Boot project with REST APIs",
                "How to implement proper error handling for REST endpoints?",
                "TICKET-123: Implement global error handling",
                "John: We need to consider validation errors\nMary: Don't forget about logging"
        );

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
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Microservices architecture with Kafka",
                "What's the best way to handle service communication?",
                "TICKET-456: Design service communication pattern",
                "Alex: Should we use synchronous or asynchronous?\nSarah: Consider latency requirements"
        );

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
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Legacy monolithic application",
                "How should we approach refactoring the authentication module?",
                "TICKET-789: Technical Debt - Authentication Refactoring",
                "Mike: Current implementation uses deprecated methods\nLisa: Security concerns identified"
        );

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
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params("", "", "", "");

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
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "",  // Empty project context
                "",  // Empty request
                "TICKET-101: Memory Leak Investigation\nWe are seeing OOM errors in production",
                ""   // Empty discussion
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should acknowledge ticket content", response.contains("<p>"));
        assertTrue("Response should offer relevant assistance", response.contains("<ul>"));
    }

    @Test
    public void testTeamAssistantWithOnlyDiscussion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "",  // Empty project context
                "",  // Empty request
                "",  // Empty ticket
                "Dev1: We should implement retry mechanism\nDev2: Agreed, with exponential backoff"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should reference discussion", response.contains("<p>"));
        assertTrue("Response should provide relevant suggestions", response.contains("<ul>"));
    }

    @Test
    public void testTeamAssistantResponseFormat() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Test Project",
                "Simple request",
                "TEST-001: Test ticket",
                "Previous: Test discussion"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertFalse("Response should not contain HTML tag", response.contains("<html>"));
        assertFalse("Response should not contain HEAD tag", response.contains("<head>"));
        assertFalse("Response should not contain BODY tag", response.contains("<body>"));
        assertTrue("Response should contain at least one paragraph", response.contains("<p>"));
    }

    @Test
    public void testTeamAssistantWithAuthenticationFlowQuestion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Spring Security based authentication system",
                "How does our current authentication system work?",
                "TICKET-123: Authentication Flow Documentation",
                "John: We recently added JWT support\nMary: Remember about refresh tokens"
        );

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
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Java Spring Boot application",
                "Review this code: userRepository.findById(id).map(User::getName).orElse(null)",
                "TICKET-456: Code Review",
                "Tom: We need to handle exceptions better\nAna: Consider using Optional"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain code analysis", response.contains("<ul>"));
        assertTrue("Response should contain Java code example", response.contains("<code class=\"java\">"));
        assertTrue("Response should contain strong emphasis", response.contains("<strong>"));
    }

    @Test
    public void testTeamAssistantWithProcessQuestion() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "GitLab CI/CD with Docker",
                "What's our deployment process to production?",
                "TICKET-789: Deployment Process Documentation",
                "DevOps: We use rolling updates\nQA: Need to mention testing gates"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain steps", response.contains("<ol>"));
        assertTrue("Response should contain code block", response.contains("<code"));
    }

    @Test
    public void testTeamAssistantWithBugAnalysis() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Production monitoring system",
                "Why does our memory leak in UserSessionManager?",
                "TICKET-101: Memory Leak Investigation",
                "Support: Seeing OOM errors\nDev: Session cleanup might be incomplete"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain code analysis", response.contains("<code class=\"java\">"));
        assertTrue("Response should contain problem description", response.contains("<p>"));
        assertTrue("Response should contain solution steps", response.contains("<ol>"));
    }

    @Test
    public void testTeamAssistantWithArchitectureExplanation() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "Microservices with Event Sourcing",
                "How does our event processing pipeline work?",
                "TICKET-202: Architecture Documentation",
                "Architect: We use Kafka for events\nDev: Need to explain retry mechanism"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain event flow description", response.contains("<p><strong>Event Flow:</strong></p>"));
        assertTrue("Response should contain code examples", response.contains("<code"));
        assertTrue("Response should contain component list", response.contains("<ul>"));
    }

    @Test
    public void testTeamAssistantWithDatabaseQuery() throws Exception {
        // Arrange
        TeamAssistantAgent agent = new TeamAssistantAgent();
        TeamAssistantAgent.Params params = new TeamAssistantAgent.Params(
                "PostgreSQL with JPA",
                "How to optimize this query: SELECT u FROM User u WHERE u.status = 'ACTIVE'",
                "TICKET-303: Query Optimization",
                "DBA: Index might help\nDev: Consider pagination"
        );

        // Act
        String response = agent.run(params);

        // Assert
        assertTrue("Response should contain SQL code", response.contains("<code class=\"sql\">"));
        assertTrue("Response should contain optimization tips", response.contains("<ul>"));
        assertTrue("Response should contain explanation", response.contains("<p>"));
    }
}