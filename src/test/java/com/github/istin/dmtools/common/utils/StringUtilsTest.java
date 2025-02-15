package com.github.istin.dmtools.common.utils;

import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StringUtilsTest {

    @Test
    public void testExtractUrls() {
        String text = "Check out this link: https://example.com and this one: http://example.org";
        List<String> urls = StringUtils.extractUrls(text);
        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://example.com"));
        assertTrue(urls.contains("http://example.org"));
    }

    @Test
    public void testExtractUrlsWithNullInput() {
        List<String> urls = StringUtils.extractUrls(null);
        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }

    @Test
    public void testConvertToMarkdown() {
        String htmlInput = "<p>This is <strong>bold</strong> and <em>italic</em> text.</p>";
        String expectedMarkdown = "This is *bold* and _italic_ text.";
        String markdown = StringUtils.convertToMarkdown(htmlInput);
        assertEquals(expectedMarkdown, markdown);
    }

    /*
    <p>Based on the comprehensive analysis of JiraClient functionality in DMTools, here are the key features and usage examples:</p>\n\n<h3>Core Functionality</h3>\n\n<h4>1. Issue Management</h4>\n<ul>\n    <li>Create tickets in projects</li>\n    <li>Update existing issues</li>\n    <li>Retrieve issue details</li>\n    <li>Change issue status</li>\n    <li>Assign issues to users</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">TrackerClient client = BasicJiraClient.getInstance();\n// Create new ticket\nclient.createTicketInProject(\"PROJECT\", \"Bug\", \"Bug summary\", \"Description\");\n// Update ticket\nclient.updateTicket(\"PROJECT-123\", new TrackerClient.FieldsInitializer()\n    .addField(\"summary\", \"Updated summary\"));\n</code></pre>\n\n<h4>2. Search Functionality</h4>\n<ul>\n    <li>JQL search support</li>\n    <li>Search and perform operations</li>\n    <li>Retrieve tickets by query</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">String searchQuery = \"project = PROJECT AND status = 'In Progress'\";\nclient.searchAndPerform(ticket -> {\n    // Process each found ticket\n}, searchQuery, client.getDefaultQueryFields());</code></pre>\n\n<h4>3. Comments & Attachments</h4>\n<ul>\n    <li>Post comments</li>\n    <li>Retrieve comments</li>\n    <li>Attach files</li>\n    <li>Manage existing comments</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">// Add comment\nclient.postComment(\"PROJECT-123\", \"New comment\");\n// Attach file\nclient.attachFileToTicket(\"PROJECT-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file.txt\"));</code></pre>\n\n<h4>4. Field Management</h4>\n<ul>\n    <li>Update fields</li>\n    <li>Clear field values</li>\n    <li>Get field information</li>\n    <li>Custom field support</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">// Get custom field code\nString fieldCode = ((JiraClient)client).getFieldCustomCode(\"PROJECT\", \"customField\");\n// Update field\nclient.updateField(\"PROJECT-123\", fieldCode, \"new value\");</code></pre>\n\n<h4>5. Workflow & Status Management</h4>\n<ul>\n    <li>Move between statuses</li>\n    <li>Handle transitions</li>\n    <li>Support various workflow states</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">// Move ticket to new status\nclient.moveToStatus(\"PROJECT-123\", \"In Progress\");\n// Check status\nif (ticket.getStatus().equals(Status.IN_DEVELOPMENT)) {\n    // Process development status\n}</code></pre>\n\n<h4>6. Sprint & Board Operations</h4>\n<ul>\n    <li>Manage sprints</li>\n    <li>Track sprint capacity</li>\n    <li>Handle release relationships</li>\n</ul>\n\n<p>Example:</p>\n<pre><code class=\"java\">// Get sprint information\nSprint sprint = client.getCurrentSprint(\"BOARD-1\");\n// Check sprint status\nif (sprint.isCurrent()) {\n    // Process current sprint\n}</code></pre>\n\n<h4>Configuration</h4>\n<table>\n    <tr>\n        <th>Property</th>\n        <th>Description</th>\n    </tr>\n    <tr>\n        <td>BASE_PATH</td>\n        <td>Jira instance URL</td>\n    </tr>\n    <tr>\n        <td>TOKEN</td>\n        <td>Authentication token</td>\n    </tr>\n    <tr>\n        <td>AUTH_TYPE</td>\n        <td>Authentication method</td>\n    </tr>\n</table>\n\n<p><em>Note: JiraClient implements TrackerClient interface and supports both Jira Server and Cloud versions.</em></p>
     */

    @Test
    public void testConcatenate() {
        String result = StringUtils.concatenate(", ", "one", "two", "three");
        assertEquals("one, two, three", result);
    }

    @Test
    public void testConcatenateWithSingleValue() {
        String result = StringUtils.concatenate(", ", "single");
        assertEquals("single", result);
    }


    @Test
    public void testTransformJSONToTextWithArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("item1");
        jsonArray.put("item2");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("items", jsonArray);

        StringBuilder textBuilder = new StringBuilder();
        StringUtils.transformJSONToText(textBuilder, jsonObject, false);

        String expectedText = "items: [item1, item2]\n";
        assertEquals(expectedText, textBuilder.toString());
    }
}