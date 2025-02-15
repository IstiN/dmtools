package com.github.istin.dmtools.common.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class MarkdownToJiraConverterTest {

    @Test
    public void testHtmlInput() {
        String html = "<p>This is <strong>bold</strong> and <em>italic</em></p>" +
                "<pre><code class=\"java\">public class Test {}</code></pre>";
        String expected = "This is *bold* and _italic_\n\n" +
                "{code:java}\npublic class Test {}\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(html));
    }

    @Test
    public void testMarkdownInput() {
        String markdown = "This is **bold** and _italic_\n\n" +
                "```java\npublic class Test {}\n```";
        String expected = "This is *bold* and _italic_\n\n" +
                "{code:java}\npublic class Test {}\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(markdown));
    }

    @Test
    public void testMixedInput() {
        String input = "# Heading\n\n<p>This is <strong>bold</strong></p>\n\n* List item\n\n```java\ncode\n```";
        String expected = "h1. Heading\n\nThis is *bold*\n\n* List item\n\n{code:java}\ncode\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));
    }

    @Test
    public void testComplexHtmlInput() {
        String input = "<h1>Title</h1><p><strong>Bold text</strong> and <em>italic text</em></p>" +
                "<pre><code class=\"java\">public class Test {}</code></pre>" +
                "<ul><li>Item 1</li><li>Item 2</li></ul>" +
                "<a href=\"http://example.com\">Link</a>" +
                "<code>inline code</code>";
        String expected = "h1. Title\n\n" +
                "*Bold text* and _italic text_\n\n" +
                "{code:java}\npublic class Test {}\n{code}\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link|http://example.com]\n\n" +
                "{{inline code}}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));
    }

    @Test
    public void testComplexMarkdownInput() {
        String markdown = "# Title\n\n" +
                "**Bold text** and _italic text_\n\n" +
                "```java\npublic class Test {}\n```\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link](http://example.com)\n\n" +
                "`inline code`";

        String expected = "h1. Title\n\n" +
                "*Bold text* and _italic text_\n\n" +
                "{code:java}\npublic class Test {}\n{code}\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link|http://example.com]\n\n" +
                "{{inline code}}";

        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(markdown));
    }

    @Test
    public void testExtensiveHTML() {
        // Empty input
        String input = "<p>Based on the provided context and code snippets, here's a comprehensive overview of JiraClient functionality in DMTools:</p>\n" +
                "<p><strong>1. Core Authentication and Setup</strong></p>\n" +
                "<code class=\"java\">\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "// OR with custom config\n" +
                "JiraClient client = new JiraClient(basePath, authorization);\n" +
                "</code>\n" +
                "<p><strong>2. Issue Management Methods</strong></p>\n" +
                "<table>\n" +
                "    <tr>\n" +
                "        <th>Method</th>\n" +
                "        <th>Description</th>\n" +
                "        <th>Example</th>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>createTicketInProject</td>\n" +
                "        <td>Creates new Jira ticket</td>\n" +
                "        <td>\n" +
                "            <code class=\"java\">\n" +
                "client.createTicketInProject(\"PROJECT\", \"Bug\", \"Summary\", \"Description\");\n" +
                "            </code>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>updateTicket</td>\n" +
                "        <td>Updates existing ticket</td>\n" +
                "        <td>\n" +
                "            <code class=\"java\">\n" +
                "client.updateTicket(\"KEY-123\", new FieldsInitializer()\n" +
                "    .addField(\"summary\", \"Updated Summary\"));\n" +
                "            </code>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>moveToStatus</td>\n" +
                "        <td>Changes ticket status</td>\n" +
                "        <td>\n" +
                "            <code class=\"java\">\n" +
                "client.moveToStatus(\"KEY-123\", \"In Progress\");\n" +
                "            </code>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "<p><strong>3. Comments and Attachments</strong></p>\n" +
                "<code class=\"java\">\n" +
                "// Add comment\n" +
                "client.postComment(\"KEY-123\", \"New comment\");\n" +
                "// Attach file\n" +
                "client.attachFileToTicket(\"KEY-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file\"));\n" +
                "// Get comments\n" +
                "List<IComment> comments = client.getComments(\"KEY-123\");\n" +
                "</code>\n" +
                "<p><strong>4. Search and Query</strong></p>\n" +
                "<code class=\"java\">\n" +
                "// Search with JQL\n" +
                "client.searchAndPerform(ticket -> {\n" +
                "    // Process each ticket\n" +
                "}, \"project = PROJECT AND status = 'In Progress'\", \n" +
                "   client.getDefaultQueryFields());\n" +
                "// Get default fields for queries\n" +
                "String[] fields = client.getDefaultQueryFields();\n" +
                "</code>\n" +
                "<p><strong>5. Issue Linking</strong></p>\n" +
                "<code class=\"java\">\n" +
                "// Link related issues\n" +
                "client.linkIssueWithRelationship(\"KEY-123\", \"KEY-456\", \"relates to\");\n" +
                "</code>\n" +
                "<p><strong>6. Cache Management</strong></p>\n" +
                "<code class=\"java\">\n" +
                "// Enable/disable cache\n" +
                "client.setCacheGetRequestsEnabled(true);\n" +
                "// Clear cache for specific request\n" +
                "client.clearCache(jiraRequest);\n" +
                "</code>\n" +
                "<p><em>Note: All methods support error handling and should be wrapped in try-catch blocks for production use.</em></p>\n" +
                "<p><strong>Common Use Case Example:</strong></p>\n" +
                "<code class=\"java\">\n" +
                "try {\n" +
                "    JiraClient client = BasicJiraClient.getInstance();\n" +
                "    \n" +
                "    // Create ticket\n" +
                "    String ticketKey = client.createTicketInProject(\"PROJECT\", \"Story\", \n" +
                "        \"Implement new feature\", \"Detailed description\");\n" +
                "    \n" +
                "    // Add attachment\n" +
                "    client.attachFileToTicket(ticketKey, \"spec.pdf\", \n" +
                "        \"application/pdf\", new File(\"specification.pdf\"));\n" +
                "    \n" +
                "    // Add comment\n" +
                "    client.postComment(ticketKey, \"Implementation started\");\n" +
                "    \n" +
                "    // Move to In Progress\n" +
                "    client.moveToStatus(ticketKey, \"In Progress\");\n" +
                "    \n" +
                "} catch (IOException e) {\n" +
                "    logger.error(\"Error working with Jira\", e);\n" +
                "}\n" +
                "</code>";

        String expected = "Based on the provided context and code snippets, here's a comprehensive overview of JiraClient functionality in DMTools:\n\n" +
                "*1. Core Authentication and Setup*\n\n" +
                "{code:java}\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "// OR with custom config\n" +
                "JiraClient client = new JiraClient(basePath, authorization);\n" +
                "{code}\n\n" +
                "*2. Issue Management Methods*\n\n" +
                "||Method||Description||Example||\n" +
                "|createTicketInProject|Creates new Jira ticket|{code:java}\n" +
                "client.createTicketInProject(\"PROJECT\", \"Bug\", \"Summary\", \"Description\");\n" +
                "{code}|\n" +
                "|updateTicket|Updates existing ticket|{code:java}\n" +
                "client.updateTicket(\"KEY-123\", new FieldsInitializer()\n" +
                "    .addField(\"summary\", \"Updated Summary\"));\n" +
                "{code}|\n" +
                "|moveToStatus|Changes ticket status|{code:java}\n" +
                "client.moveToStatus(\"KEY-123\", \"In Progress\");\n" +
                "{code}|\n\n" +
                "*3. Comments and Attachments*\n\n" +
                "{code:java}\n" +
                "// Add comment\n" +
                "client.postComment(\"KEY-123\", \"New comment\");\n" +
                "// Attach file\n" +
                "client.attachFileToTicket(\"KEY-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file\"));\n" +
                "// Get comments\n" +
                "List<IComment> comments = client.getComments(\"KEY-123\");\n" +
                "{code}\n\n" +
                "*4. Search and Query*\n\n" +
                "{code:java}\n" +
                "// Search with JQL\n" +
                "client.searchAndPerform(ticket -> {\n" +
                "    // Process each ticket\n" +
                "}, \"project = PROJECT AND status = 'In Progress'\", \n" +
                "   client.getDefaultQueryFields());\n" +
                "// Get default fields for queries\n" +
                "String[] fields = client.getDefaultQueryFields();\n" +
                "{code}\n\n" +
                "*5. Issue Linking*\n\n" +
                "{code:java}\n" +
                "// Link related issues\n" +
                "client.linkIssueWithRelationship(\"KEY-123\", \"KEY-456\", \"relates to\");\n" +
                "{code}\n\n" +
                "*6. Cache Management*\n\n" +
                "{code:java}\n" +
                "// Enable/disable cache\n" +
                "client.setCacheGetRequestsEnabled(true);\n" +
                "// Clear cache for specific request\n" +
                "client.clearCache(jiraRequest);\n" +
                "{code}\n\n" +
                "_Note: All methods support error handling and should be wrapped in try-catch blocks for production use._\n\n" +
                "*Common Use Case Example:*\n\n" +
                "{code:java}\n" +
                "try {\n" +
                "    JiraClient client = BasicJiraClient.getInstance();\n" +
                "    \n" +
                "    // Create ticket\n" +
                "    String ticketKey = client.createTicketInProject(\"PROJECT\", \"Story\", \n" +
                "        \"Implement new feature\", \"Detailed description\");\n" +
                "    \n" +
                "    // Add attachment\n" +
                "    client.attachFileToTicket(ticketKey, \"spec.pdf\", \n" +
                "        \"application/pdf\", new File(\"specification.pdf\"));\n" +
                "    \n" +
                "    // Add comment\n" +
                "    client.postComment(ticketKey, \"Implementation started\");\n" +
                "    \n" +
                "    // Move to In Progress\n" +
                "    client.moveToStatus(ticketKey, \"In Progress\");\n" +
                "    \n" +
                "} catch (IOException e) {\n" +
                "    logger.error(\"Error working with Jira\", e);\n" +
                "}\n" +
                "{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));

    }

    @Test
    public void testEdgeCases() {
        // Empty input
        assertEquals("", MarkdownToJiraConverter.convertToJiraMarkdown(""));

        // Only whitespace
        assertEquals("", MarkdownToJiraConverter.convertToJiraMarkdown("  \n  \t  "));

        // HTML entities
        assertEquals("< > & \"",
                MarkdownToJiraConverter.convertToJiraMarkdown("&lt; &gt; &amp; &quot;"));
    }
}