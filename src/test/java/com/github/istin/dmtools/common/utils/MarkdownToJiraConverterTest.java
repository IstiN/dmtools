package com.github.istin.dmtools.common.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class MarkdownToJiraConverterTest {

    @Test
    public void testGherkinStyleWithLineBreaks() {
        String html = "<p><strong>Given</strong> the user is on the Google search page<br>" +
                "<strong>When</strong> the user clicks the 'Search' button<br>" +
                "<strong>Then</strong> the results page should appear<br>" +
                "<strong>And</strong> the page should contain 'Search results' and 'Filter options'</p>";

        String expected = "*Given* the user is on the Google search page\n" +
                "*When* the user clicks the 'Search' button\n" +
                "*Then* the results page should appear\n" +
                "*And* the page should contain 'Search results' and 'Filter options'";

        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(html));
    }

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
    public void testComplexMarkdownInput2() {
        String markdown = "# JiraClient Usage Guide\n\n" +
                "Main methods available in JiraClient:\n\n" +
                "1. **Authentication** and _Configuration_:\n" +
                "Reference: Constructor and authentication methods\n" +
                "```java\n" +
                "public JiraClient(String basePath, String authorization)\n" +
                "public void setAuthType(String authType)\n" +
                "```\n\n" +
                "2. Ticket Operations:\n" +
                "* Create ticket\n" +
                "* Update ticket\n" +
                "* Move status\n\n" +
                "Example usage:\n" +
                "```java\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "String key = client.createTicketInProject(\"PROJECT\", \"Bug\", \n" +
                "    \"Summary\", \"Description\");\n" +
                "```\n\n" +
                "For more information, visit [Jira API](https://docs.atlassian.com/)\n\n" +
                "Common types used:\n" +
                "* `ITicket` - ticket interface\n" +
                "* `List<IComment>` - list of comments\n" +
                "* `TrackerClient<T>` - generic client\n\n" +
                "Note: Make sure to handle exceptions:\n" +
                "```java\n" +
                "try {\n" +
                "    client.updateTicket(key, fields);\n" +
                "} catch (Exception e) {\n" +
                "    logger.error(\"Failed\", e);\n" +
                "}\n" +
                "```";

        String expected = "h1. JiraClient Usage Guide\n\n" +
                "Main methods available in JiraClient:\n\n" +
                "*Authentication* and _Configuration_:\n" +
                "Reference: Constructor and authentication methods\n\n" +
                "{code:java}\n" +
                "public JiraClient(String basePath, String authorization)\n" +
                "public void setAuthType(String authType)\n" +
                "{code}\n\n" +
                "2. Ticket Operations:\n" +
                "* Create ticket\n" +
                "* Update ticket\n" +
                "* Move status\n\n" +
                "Example usage:\n\n" +
                "{code:java}\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "String key = client.createTicketInProject(\"PROJECT\", \"Bug\", \n" +
                "    \"Summary\", \"Description\");\n" +
                "{code}\n\n" +
                "For more information, visit [Jira API|https://docs.atlassian.com/]\n\n" +
                "Common types used:\n" +
                "* {{ITicket}} - ticket interface\n" +
                "* {{List<IComment>}} - list of comments\n" +
                "* {{TrackerClient<T>}} - generic client\n\n" +
                "Note: Make sure to handle exceptions:\n\n" +
                "{code:java}\n" +
                "try {\n" +
                "    client.updateTicket(key, fields);\n" +
                "} catch (Exception e) {\n" +
                "    logger.error(\"Failed\", e);\n" +
                "}\n" +
                "{code}";

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
                "client.updateTicket(\"KEY-123\", new FieldsInitializer().addField(\"summary\", \"Updated Summary\"));\n" +
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

        String expected = "Based on the provided context and code snippets, here's a comprehensive overview of JiraClient functionality in DMTools:\n" +
                "\n" +
                "*1. Core Authentication and Setup*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "// OR with custom config\n" +
                "JiraClient client = new JiraClient(basePath, authorization);\n" +
                "{code}\n" +
                "\n" +
                "*2. Issue Management Methods*\n" +
                "\n" +
                "||Method||Description||Example||\n" +
                "|createTicketInProject|Creates new Jira ticket|{code:java}\n" +
                "\n" +
                "client.createTicketInProject(\"PROJECT\", \"Bug\", \"Summary\", \"Description\");\n" +
                "{code}|\n" +
                "|updateTicket|Updates existing ticket|{code:java}\n" +
                "\n" +
                "client.updateTicket(\"KEY-123\", new FieldsInitializer().addField(\"summary\", \"Updated Summary\"));\n" +
                "{code}|\n" +
                "|moveToStatus|Changes ticket status|{code:java}\n" +
                "\n" +
                "client.moveToStatus(\"KEY-123\", \"In Progress\");\n" +
                "{code}|\n" +
                "\n" +
                "*3. Comments and Attachments*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "// Add comment\n" +
                "client.postComment(\"KEY-123\", \"New comment\");\n" +
                "// Attach file\n" +
                "client.attachFileToTicket(\"KEY-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file\"));\n" +
                "// Get comments\n" +
                "List<IComment> comments = client.getComments(\"KEY-123\");\n" +
                "{code}\n" +
                "\n" +
                "*4. Search and Query*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "// Search with JQL\n" +
                "client.searchAndPerform(ticket -> {\n" +
                "// Process each ticket\n" +
                "}, \"project = PROJECT AND status = 'In Progress'\", \n" +
                "client.getDefaultQueryFields());\n" +
                "// Get default fields for queries\n" +
                "String[] fields = client.getDefaultQueryFields();\n" +
                "{code}\n" +
                "\n" +
                "*5. Issue Linking*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "// Link related issues\n" +
                "client.linkIssueWithRelationship(\"KEY-123\", \"KEY-456\", \"relates to\");\n" +
                "{code}\n" +
                "\n" +
                "*6. Cache Management*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "// Enable/disable cache\n" +
                "client.setCacheGetRequestsEnabled(true);\n" +
                "// Clear cache for specific request\n" +
                "client.clearCache(jiraRequest);\n" +
                "{code}\n" +
                "\n" +
                "_Note: All methods support error handling and should be wrapped in try-catch blocks for production use._\n" +
                "\n" +
                "*Common Use Case Example:*\n" +
                "\n" +
                "{code:java}\n" +
                "\n" +
                "try {\n" +
                "JiraClient client = BasicJiraClient.getInstance();\n" +
                "\n" +
                "// Create ticket\n" +
                "String ticketKey = client.createTicketInProject(\"PROJECT\", \"Story\", \n" +
                "\"Implement new feature\", \"Detailed description\");\n" +
                "\n" +
                "// Add attachment\n" +
                "client.attachFileToTicket(ticketKey, \"spec.pdf\", \n" +
                "\"application/pdf\", new File(\"specification.pdf\"));\n" +
                "\n" +
                "// Add comment\n" +
                "client.postComment(ticketKey, \"Implementation started\");\n" +
                "\n" +
                "// Move to In Progress\n" +
                "client.moveToStatus(ticketKey, \"In Progress\");\n" +
                "\n" +
                "} catch (IOException e) {\n" +
                "logger.error(\"Error working with Jira\", e);\n" +
                "}\n" +
                "{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));

    }

    @Test
    public void testHTMLWithCodeBlockWithTags() {
        // Empty input
        String input =
                "<p><strong>3. Comments and Attachments</strong></p>\n" +
                "<code class=\"java\">\n" +
                "// Add comment\n" +
                "client.postComment(\"KEY-123\", \"New comment\");\n" +
                "// Attach file\n" +
                "client.attachFileToTicket(\"KEY-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file\"));\n" +
                "// Get comments\n" +
                "List<IComment> comments = client.getComments(\"KEY-123\");\n" +
                "</code>"
                ;

        String expected =
                "*3. Comments and Attachments*\n" +
                        "\n" +
                        "{code:java}\n" +
                        "\n" +
                        "// Add comment\n" +
                        "client.postComment(\"KEY-123\", \"New comment\");\n" +
                        "// Attach file\n" +
                        "client.attachFileToTicket(\"KEY-123\", \"file.txt\", \"text/plain\", new File(\"path/to/file\"));\n" +
                        "// Get comments\n" +
                        "List<IComment> comments = client.getComments(\"KEY-123\");\n" +
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

    @Test
    public void testHTMLWithCodeBlockWithTagsAdvanced() {
        // Empty input
        String input =
                "<p>Based on the provided information, I can summarize the PropertyReader configuration parameters and their usage in DMTools. Here's a comprehensive overview:</p>\n" +
                        "<h3>Main Configuration Groups</h3>\n" +
                        "<ol>\n" +
                        "<li><strong>Jira Configuration</strong>\n" +
                        "<ul>\n" +
                        "    <li>JIRA_BASE_PATH - Base URL for Jira instance</li>\n" +
                        "    <li>JIRA_LOGIN_PASS_TOKEN - Authentication token</li>\n" +
                        "    <li>JIRA_AUTH_TYPE - Authentication type (e.g. \"Bearer\")</li>\n" +
                        "    <li>JIRA_WAIT_BEFORE_PERFORM - Boolean flag for wait operations</li>\n" +
                        "    <li>JIRA_LOGGING_ENABLED - Enable logging</li>\n" +
                        "    <li>JIRA_CLEAR_CACHE - Cache control</li>\n" +
                        "    <li>JIRA_EXTRA_FIELDS_PROJECT - Project specific fields</li>\n" +
                        "    <li>JIRA_EXTRA_FIELDS - Additional fields (comma-separated)</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "<li><strong>Version Control Systems</strong>\n" +
                        "<ul>\n" +
                        "    <li><em>Bitbucket:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        BITBUCKET_TOKEN=Bearer [token]\n" +
                        "        BITBUCKET_BASE_PATH=https://api.bitbucket.org\n" +
                        "        BITBUCKET_API_VERSION=V2\n" +
                        "        BITBUCKET_WORKSPACE=workspace-name\n" +
                        "        BITBUCKET_REPOSITORY=repo-name\n" +
                        "        BITBUCKET_BRANCH=branch-name\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li><em>GitHub:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        SOURCE_GITHUB_TOKEN\n" +
                        "        SOURCE_GITHUB_WORKSPACE\n" +
                        "        SOURCE_GITHUB_REPOSITORY\n" +
                        "        SOURCE_GITHUB_BRANCH\n" +
                        "        SOURCE_GITHUB_BASE_PATH\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li><em>GitLab:</em> (Similar parameters with GITLAB_ prefix)</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "<li><strong>AI Integration</strong>\n" +
                        "<ul>\n" +
                        "    <li><em>OpenAI:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        OPEN_AI_BATH_PATH\n" +
                        "        OPEN_AI_API_KEY\n" +
                        "        OPEN_AI_MODEL\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li><em>CurlAI:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        CURL_AI_BATH_PATH\n" +
                        "        CURL_AI_AUTH\n" +
                        "        CURL_AI_MODEL\n" +
                        "        CURL_AI_RESPONSE_JSON_PATH\n" +
                        "        CURL_AI_URL_TEMPLATE\n" +
                        "        CURL_AI_BODY_TEMPLATE\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li>AI Models: CODE_AI_MODEL, TEST_AI_MODEL</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "<li><strong>Other Tools</strong>\n" +
                        "<ul>\n" +
                        "    <li><em>Confluence:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        CONFLUENCE_BASE_PATH\n" +
                        "        CONFLUENCE_LOGIN_PASS_TOKEN\n" +
                        "        CONFLUENCE_GRAPHQL_PATH\n" +
                        "        CONFLUENCE_DEFAULT_SPACE\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li><em>Figma:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        FIGMA_BASE_PATH\n" +
                        "        FIGMA_TOKEN\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "    <li><em>Firebase:</em>\n" +
                        "        <code class=\"properties\">\n" +
                        "        FIREBASE_PROJECT_ID\n" +
                        "        FIREBASE_SERVICE_ACCOUNT_JSON_AUTH\n" +
                        "        </code>\n" +
                        "    </li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "<li><strong>System Configuration</strong>\n" +
                        "<ul>\n" +
                        "    <li>SLEEP_TIME_REQUEST - Default: 300L</li>\n" +
                        "    <li>LINES_OF_CODE_DIVIDER - For code metrics</li>\n" +
                        "    <li>TIME_SPENT_ON_DIVIDER - For time calculations</li>\n" +
                        "    <li>TICKET_FIELDS_CHANGED_DIVIDER - For ticket field changes</li>\n" +
                        "    <li>IS_READ_PULL_REQUEST_DIFF - Boolean, defaults to true</li>\n" +
                        "</ul>\n" +
                        "</li>\n" +
                        "</ol>\n" +
                        "<p><strong>Usage Example:</strong></p>\n" +
                        "<code class=\"java\">\n" +
                        "PropertyReader propertyReader = new PropertyReader();\n" +
                        "// Configure Confluence\n" +
                        "KnowledgeBaseConfig config = new KnowledgeBaseConfig();\n" +
                        "config.setPath(propertyReader.getConfluenceBasePath());\n" +
                        "config.setAuth(propertyReader.getConfluenceLoginPassToken());\n" +
                        "config.setType(KnowledgeBaseConfig.Type.CONFLUENCE);\n" +
                        "config.setWorkspace(propertyReader.getConfluenceDefaultSpace());\n" +
                        "config.setGraphQLPath(propertyReader.getConfluenceGraphQLPath());\n" +
                        "</code>\n" +
                        "<p><strong>Note:</strong> PropertyReader first checks the properties file located at \"/config.properties\" and falls back to system environment variables if the property is not found or empty.</p>"
                ;

        String expected =
                "Based on the provided information, I can summarize the PropertyReader configuration parameters and their usage in DMTools. Here's a comprehensive overview:\n" +
                        "\n" +
                        "h3. Main Configuration Groups\n" +
                        "\n" +
                        "# *Jira Configuration*\n" +
                        "* JIRA_BASE_PATH - Base URL for Jira instance\n" +
                        "* JIRA_LOGIN_PASS_TOKEN - Authentication token\n" +
                        "* JIRA_AUTH_TYPE - Authentication type (e.g. \"Bearer\")\n" +
                        "* JIRA_WAIT_BEFORE_PERFORM - Boolean flag for wait operations\n" +
                        "* JIRA_LOGGING_ENABLED - Enable logging\n" +
                        "* JIRA_CLEAR_CACHE - Cache control\n" +
                        "* JIRA_EXTRA_FIELDS_PROJECT - Project specific fields\n" +
                        "* JIRA_EXTRA_FIELDS - Additional fields (comma-separated)\n" +
                        "# *Version Control Systems*\n" +
                        "* _Bitbucket:_ {code:bash}\n" +
                        "\n" +
                        "BITBUCKET_TOKEN=Bearer [token]\n" +
                        "BITBUCKET_BASE_PATH=https://api.bitbucket.org\n" +
                        "BITBUCKET_API_VERSION=V2\n" +
                        "BITBUCKET_WORKSPACE=workspace-name\n" +
                        "BITBUCKET_REPOSITORY=repo-name\n" +
                        "BITBUCKET_BRANCH=branch-name\n" +
                        "{code}\n" +
                        "* _GitHub:_ {code:bash}\n" +
                        "\n" +
                        "SOURCE_GITHUB_TOKEN\n" +
                        "SOURCE_GITHUB_WORKSPACE\n" +
                        "SOURCE_GITHUB_REPOSITORY\n" +
                        "SOURCE_GITHUB_BRANCH\n" +
                        "SOURCE_GITHUB_BASE_PATH\n" +
                        "{code}\n" +
                        "* _GitLab:_ (Similar parameters with GITLAB_ prefix)\n" +
                        "# *AI Integration*\n" +
                        "* _OpenAI:_ {code:bash}\n" +
                        "\n" +
                        "OPEN_AI_BATH_PATH\n" +
                        "OPEN_AI_API_KEY\n" +
                        "OPEN_AI_MODEL\n" +
                        "{code}\n" +
                        "* _CurlAI:_ {code:bash}\n" +
                        "\n" +
                        "CURL_AI_BATH_PATH\n" +
                        "CURL_AI_AUTH\n" +
                        "CURL_AI_MODEL\n" +
                        "CURL_AI_RESPONSE_JSON_PATH\n" +
                        "CURL_AI_URL_TEMPLATE\n" +
                        "CURL_AI_BODY_TEMPLATE\n" +
                        "{code}\n" +
                        "* AI Models: CODE_AI_MODEL, TEST_AI_MODEL\n" +
                        "# *Other Tools*\n" +
                        "* _Confluence:_ {code:bash}\n" +
                        "\n" +
                        "CONFLUENCE_BASE_PATH\n" +
                        "CONFLUENCE_LOGIN_PASS_TOKEN\n" +
                        "CONFLUENCE_GRAPHQL_PATH\n" +
                        "CONFLUENCE_DEFAULT_SPACE\n" +
                        "{code}\n" +
                        "* _Figma:_ {code:bash}\n" +
                        "\n" +
                        "FIGMA_BASE_PATH\n" +
                        "FIGMA_TOKEN\n" +
                        "{code}\n" +
                        "* _Firebase:_ {code:bash}\n" +
                        "\n" +
                        "FIREBASE_PROJECT_ID\n" +
                        "FIREBASE_SERVICE_ACCOUNT_JSON_AUTH\n" +
                        "{code}\n" +
                        "# *System Configuration*\n" +
                        "* SLEEP_TIME_REQUEST - Default: 300L\n" +
                        "* LINES_OF_CODE_DIVIDER - For code metrics\n" +
                        "* TIME_SPENT_ON_DIVIDER - For time calculations\n" +
                        "* TICKET_FIELDS_CHANGED_DIVIDER - For ticket field changes\n" +
                        "* IS_READ_PULL_REQUEST_DIFF - Boolean, defaults to true\n" +
                        "\n" +
                        "*Usage Example:*\n" +
                        "\n" +
                        "{code:java}\n" +
                        "\n" +
                        "PropertyReader propertyReader = new PropertyReader();\n" +
                        "// Configure Confluence\n" +
                        "KnowledgeBaseConfig config = new KnowledgeBaseConfig();\n" +
                        "config.setPath(propertyReader.getConfluenceBasePath());\n" +
                        "config.setAuth(propertyReader.getConfluenceLoginPassToken());\n" +
                        "config.setType(KnowledgeBaseConfig.Type.CONFLUENCE);\n" +
                        "config.setWorkspace(propertyReader.getConfluenceDefaultSpace());\n" +
                        "config.setGraphQLPath(propertyReader.getConfluenceGraphQLPath());\n" +
                        "{code}\n" +
                        "\n" +
                        "*Note:* PropertyReader first checks the properties file located at \"/config.properties\" and falls back to system environment variables if the property is not found or empty.";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));
    }

    /*

     */
}