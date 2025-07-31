package com.github.istin.dmtools.mcp.generated;

import java.util.*;

/**
 * Auto-generated MCP schema generator.
 * Generates MCP protocol-compliant tool schemas.
 */
public class MCPSchemaGenerator {

    public static Map<String, Object> generateToolsListResponse(Set<String> userIntegrations) {
        List<Map<String, Object>> tools = new ArrayList<>();

        if (userIntegrations.contains("confluence")) {
            tools.add(createConfluenceContentsByUrlsTool());
            tools.add(createConfluenceSearchContentByTextTool());
            tools.add(createConfluenceContentByIdTool());
            tools.add(createConfluenceContentByTitleAndSpaceTool());
            tools.add(createConfluenceGetCurrentUserProfileTool());
            tools.add(createConfluenceGetUserProfileByIdTool());
            tools.add(createConfluenceGetContentAttachmentsTool());
            tools.add(createConfluenceFindContentByTitleAndSpaceTool());
            tools.add(createConfluenceCreatePageTool());
            tools.add(createConfluenceUpdatePageTool());
            tools.add(createConfluenceUpdatePageWithHistoryTool());
            tools.add(createConfluenceGetChildrenByNameTool());
            tools.add(createConfluenceGetChildrenByIdTool());
            tools.add(createConfluenceFindContentTool());
            tools.add(createConfluenceFindOrCreateTool());
            tools.add(createConfluenceContentByTitleTool());
        }

        if (userIntegrations.contains("jira")) {
            tools.add(createJiraGetTicketBrowseUrlTool());
            tools.add(createJiraDeleteTicketTool());
            tools.add(createJiraSearchAndPerformTool());
            tools.add(createJiraSearchWithPaginationTool());
            tools.add(createJiraGetMyProfileTool());
            tools.add(createJiraGetUserProfileTool());
            tools.add(createJiraGetTicketTool());
            tools.add(createJiraGetSubtasksTool());
            tools.add(createJiraPostCommentIfNotExistsTool());
            tools.add(createJiraGetCommentsTool());
            tools.add(createJiraPostCommentTool());
            tools.add(createJiraGetFixVersionsTool());
            tools.add(createJiraGetComponentsTool());
            tools.add(createJiraGetProjectStatusesTool());
            tools.add(createJiraCreateTicketWithParentTool());
            tools.add(createJiraCreateTicketBasicTool());
            tools.add(createJiraCreateTicketWithJsonTool());
            tools.add(createJiraGetIssuesInParentByTypeTool());
            tools.add(createJiraUpdateDescriptionTool());
            tools.add(createJiraUpdateTicketParentTool());
            tools.add(createJiraUpdateTicketTool());
            tools.add(createJiraUpdateFieldTool());
            tools.add(createJiraExecuteRequestTool());
            tools.add(createJiraGetTransitionsTool());
            tools.add(createJiraMoveToStatusTool());
            tools.add(createJiraMoveToStatusWithResolutionTool());
            tools.add(createJiraClearFieldTool());
            tools.add(createJiraSetFixVersionTool());
            tools.add(createJiraAddFixVersionTool());
            tools.add(createJiraSetPriorityTool());
            tools.add(createJiraRemoveFixVersionTool());
            tools.add(createJiraGetFieldsTool());
            tools.add(createJiraGetIssueTypesTool());
            tools.add(createJiraGetFieldCustomCodeTool());
            tools.add(createJiraGetIssueLinkTypesTool());
            tools.add(createJiraLinkIssuesTool());
        }

        return Map.of("tools", tools);
    }

    private static Map<String, Object> createJiraGetTicketBrowseUrlTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket_key", Map.of(
            "type", "string",
            "description", "The Jira ticket key to to generate url to"
        ));
        List<String> required = Arrays.asList(
"ticket_key");
        return Map.of(
            "name", "jira_get_ticket_browse_url",
            "description", "return jira ticket url to open in broswer",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraDeleteTicketTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to delete"
        ));
        List<String> required = Arrays.asList(
"ticketKey");
        return Map.of(
            "name", "jira_delete_ticket",
            "description", "Delete a Jira ticket by key",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraSearchAndPerformTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("searchQueryJQL", Map.of(
            "type", "string",
            "description", "Parameter searchQueryJQL"
        ));
        properties.put("fields", Map.of(
            "type", "array",
            "description", "Parameter fields"
        ));
        List<String> required = Arrays.asList(
"searchQueryJQL", "fields");
        return Map.of(
            "name", "jira_search_and_perform",
            "description", "Search for Jira tickets using JQL and perform an action on each ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraSearchWithPaginationTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jql", Map.of(
            "type", "string",
            "description", "JQL query string to search for tickets"
            , "example", "project = PROJ AND status = Open"
        ));
        properties.put("startAt", Map.of(
            "type", "number",
            "description", "Starting index for pagination (0-based)"
            , "example", "0"
        ));
        properties.put("fields", Map.of(
            "type", "array",
            "description", "Array of field names to include in the response"
            , "example", "['summary', 'status', 'assignee']"
        ));
        List<String> required = Arrays.asList(
"jql", "startAt", "fields");
        return Map.of(
            "name", "jira_search_with_pagination",
            "description", "Search for Jira tickets using JQL with pagination support",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetMyProfileTool() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = Arrays.asList(
);
        return Map.of(
            "name", "jira_get_my_profile",
            "description", "Get the current user's profile information from Jira",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetUserProfileTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", Map.of(
            "type", "string",
            "description", "The user ID to get profile for"
        ));
        List<String> required = Arrays.asList(
"userId");
        return Map.of(
            "name", "jira_get_user_profile",
            "description", "Get a specific user's profile information from Jira",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetTicketTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to retrieve"
        ));
        properties.put("fields", Map.of(
            "type", "array",
            "description", "Optional array of fields to include in the response"
        ));
        List<String> required = Arrays.asList(
"ticketKey");
        return Map.of(
            "name", "jira_get_ticket",
            "description", "Get a specific Jira ticket by key with optional field filtering",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetSubtasksTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The parent ticket key to get subtasks for"
        ));
        List<String> required = Arrays.asList(
"ticket");
        return Map.of(
            "name", "jira_get_subtasks",
            "description", "Get all subtasks of a specific Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraPostCommentIfNotExistsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to post comment to"
        ));
        properties.put("comment", Map.of(
            "type", "string",
            "description", "The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        ));
        List<String> required = Arrays.asList(
"ticketKey", "comment");
        return Map.of(
            "name", "jira_post_comment_if_not_exists",
            "description", "Post a comment to a Jira ticket only if it doesn't already exist. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetCommentsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", Map.of(
            "type", "string",
            "description", "The Jira ticket key to get comments for"
        ));
        properties.put("ticket", Map.of(
            "type", "object",
            "description", "Optional ticket object for cache validation"
        ));
        List<String> required = Arrays.asList(
"key");
        return Map.of(
            "name", "jira_get_comments",
            "description", "Get all comments for a specific Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraPostCommentTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to post comment to"
        ));
        properties.put("comment", Map.of(
            "type", "string",
            "description", "The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        ));
        List<String> required = Arrays.asList(
"ticketKey", "comment");
        return Map.of(
            "name", "jira_post_comment",
            "description", "Post a comment to a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetFixVersionsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to get fix versions for"
        ));
        List<String> required = Arrays.asList(
"project");
        return Map.of(
            "name", "jira_get_fix_versions",
            "description", "Get all fix versions for a specific Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetComponentsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to get components for"
        ));
        List<String> required = Arrays.asList(
"project");
        return Map.of(
            "name", "jira_get_components",
            "description", "Get all components for a specific Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetProjectStatusesTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to get statuses for"
        ));
        List<String> required = Arrays.asList(
"project");
        return Map.of(
            "name", "jira_get_project_statuses",
            "description", "Get all statuses for a specific Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraCreateTicketWithParentTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to create the ticket in"
        ));
        properties.put("issueType", Map.of(
            "type", "string",
            "description", "The type of issue to create (e.g., Bug, Story, Task)"
        ));
        properties.put("summary", Map.of(
            "type", "string",
            "description", "The ticket summary/title"
        ));
        properties.put("description", Map.of(
            "type", "string",
            "description", "The ticket description"
        ));
        properties.put("parentKey", Map.of(
            "type", "string",
            "description", "The key of the parent ticket"
        ));
        List<String> required = Arrays.asList(
"project", "issueType", "summary", "description", "parentKey");
        return Map.of(
            "name", "jira_create_ticket_with_parent",
            "description", "Create a new Jira ticket with a parent relationship",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraCreateTicketBasicTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to create the ticket in (e.g., PROJ)"
        ));
        properties.put("issueType", Map.of(
            "type", "string",
            "description", "The type of issue to create (e.g., Bug, Story, Task)"
        ));
        properties.put("summary", Map.of(
            "type", "string",
            "description", "The ticket summary/title (e.g., Fix login issue)"
        ));
        properties.put("description", Map.of(
            "type", "string",
            "description", "The ticket description (e.g., Users are unable to log in with valid credentials)"
        ));
        List<String> required = Arrays.asList(
"project", "issueType", "summary", "description");
        return Map.of(
            "name", "jira_create_ticket_basic",
            "description", "Create a new Jira ticket with basic fields (project, issue type, summary, description)",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraCreateTicketWithJsonTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to create the ticket in (e.g., PROJ)"
        ));
        properties.put("fieldsJson", Map.of(
            "type", "object",
            "description", "JSON object containing ticket fields in Jira format (e.g., {\"summary\": \"Ticket Summary\", \"description\": \"Ticket Description\", \"issuetype\": {\"name\": \"Task\"}, \"priority\": {\"name\": \"High\"}})"
        ));
        List<String> required = Arrays.asList(
"project", "fieldsJson");
        return Map.of(
            "name", "jira_create_ticket_with_json",
            "description", "Create a new Jira ticket with custom fields using JSON configuration",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetIssuesInParentByTypeTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", Map.of(
            "type", "string",
            "description", "The parent key to get issues from"
        ));
        properties.put("type", Map.of(
            "type", "string",
            "description", "The issue type to filter by"
        ));
        properties.put("fields", Map.of(
            "type", "array",
            "description", "Optional array of fields to include"
        ));
        List<String> required = Arrays.asList(
"key", "type");
        return Map.of(
            "name", "jira_get_issues_in_parent_by_type",
            "description", "Get all issues in an parent filtered by issue type",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraUpdateDescriptionTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", Map.of(
            "type", "string",
            "description", "The Jira ticket key to update"
        ));
        properties.put("description", Map.of(
            "type", "string",
            "description", "The new description text (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        ));
        List<String> required = Arrays.asList(
"key", "description");
        return Map.of(
            "name", "jira_update_description",
            "description", "Update the description of a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraUpdateTicketParentTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", Map.of(
            "type", "string",
            "description", "The Jira ticket key to update"
        ));
        properties.put("parentKey", Map.of(
            "type", "string",
            "description", "The key of the new parent ticket"
        ));
        List<String> required = Arrays.asList(
"key", "parentKey");
        return Map.of(
            "name", "jira_update_ticket_parent",
            "description", "Update the parent of a Jira ticket. Can be used for setting up epic relationships and parent-child relationships for subtasks",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraUpdateTicketTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to update"
        ));
        properties.put("params", Map.of(
            "type", "object",
            "description", "JSON object containing update parameters in Jira format (e.g., {\"fields\": {\"summary\": \"New Summary\", \"parent\": {\"key\": \"PROJ-123\"}}})"
        ));
        List<String> required = Arrays.asList(
"ticketKey", "params");
        return Map.of(
            "name", "jira_update_ticket",
            "description", "Update a Jira ticket using JSON parameters following the standard Jira REST API format",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraUpdateFieldTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", Map.of(
            "type", "string",
            "description", "The Jira ticket key to update"
        ));
        properties.put("field", Map.of(
            "type", "string",
            "description", "The field name to update (supports both custom field IDs like 'customfield_10091' and user-friendly names like 'Diagram')"
        ));
        properties.put("value", Map.of(
            "type", "object",
            "description", "The new value for the field"
        ));
        List<String> required = Arrays.asList(
"key", "field", "value");
        return Map.of(
            "name", "jira_update_field",
            "description", "Update a specific field of a Jira ticket. Supports both custom field IDs (e.g., 'customfield_10091') and user-friendly field names (e.g., 'Diagram')",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraExecuteRequestTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("url", Map.of(
            "type", "string",
            "description", "The Jira API URL to execute"
        ));
        List<String> required = Arrays.asList(
"url");
        return Map.of(
            "name", "jira_execute_request",
            "description", "Execute a custom HTTP request to Jira API",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetTransitionsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to get transitions for"
        ));
        List<String> required = Arrays.asList(
"ticket");
        return Map.of(
            "name", "jira_get_transitions",
            "description", "Get all available transitions for a Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraMoveToStatusTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticketKey", Map.of(
            "type", "string",
            "description", "The Jira ticket key to move"
        ));
        properties.put("statusName", Map.of(
            "type", "string",
            "description", "The target status name"
        ));
        List<String> required = Arrays.asList(
"ticketKey", "statusName");
        return Map.of(
            "name", "jira_move_to_status",
            "description", "Move a Jira ticket to a specific status",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraMoveToStatusWithResolutionTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to move"
        ));
        properties.put("statusName", Map.of(
            "type", "string",
            "description", "The target status name"
        ));
        properties.put("resolution", Map.of(
            "type", "string",
            "description", "The resolution to set"
        ));
        List<String> required = Arrays.asList(
"ticket", "statusName", "resolution");
        return Map.of(
            "name", "jira_move_to_status_with_resolution",
            "description", "Move a Jira ticket to a specific status with resolution",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraClearFieldTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to clear field from"
        ));
        properties.put("field", Map.of(
            "type", "string",
            "description", "The field name to clear"
        ));
        List<String> required = Arrays.asList(
"ticket", "field");
        return Map.of(
            "name", "jira_clear_field",
            "description", "Clear a specific field value in a Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraSetFixVersionTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to set fix version for"
        ));
        properties.put("fixVersion", Map.of(
            "type", "string",
            "description", "The fix version name to set"
        ));
        List<String> required = Arrays.asList(
"ticket", "fixVersion");
        return Map.of(
            "name", "jira_set_fix_version",
            "description", "Set the fix version for a Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraAddFixVersionTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to add fix version to"
        ));
        properties.put("fixVersion", Map.of(
            "type", "string",
            "description", "The fix version name to add"
        ));
        List<String> required = Arrays.asList(
"ticket", "fixVersion");
        return Map.of(
            "name", "jira_add_fix_version",
            "description", "Add a fix version to a Jira ticket (without removing existing ones)",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraSetPriorityTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to set priority for"
        ));
        properties.put("priority", Map.of(
            "type", "string",
            "description", "The priority name to set"
        ));
        List<String> required = Arrays.asList(
"ticket", "priority");
        return Map.of(
            "name", "jira_set_priority",
            "description", "Set the priority for a Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraRemoveFixVersionTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket", Map.of(
            "type", "string",
            "description", "The Jira ticket key to remove fix version from"
        ));
        properties.put("fixVersion", Map.of(
            "type", "string",
            "description", "The fix version name to remove"
        ));
        List<String> required = Arrays.asList(
"ticket", "fixVersion");
        return Map.of(
            "name", "jira_remove_fix_version",
            "description", "Remove a fix version from a Jira ticket",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetFieldsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to get fields for"
        ));
        List<String> required = Arrays.asList(
"project");
        return Map.of(
            "name", "jira_get_fields",
            "description", "Get all available fields for a Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetIssueTypesTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key to get issue types for"
        ));
        List<String> required = Arrays.asList(
"project");
        return Map.of(
            "name", "jira_get_issue_types",
            "description", "Get all available issue types for a specific Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetFieldCustomCodeTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", Map.of(
            "type", "string",
            "description", "The Jira project key"
        ));
        properties.put("fieldName", Map.of(
            "type", "string",
            "description", "The human-readable field name"
        ));
        List<String> required = Arrays.asList(
"project", "fieldName");
        return Map.of(
            "name", "jira_get_field_custom_code",
            "description", "Get the custom field code for a field name in a Jira project",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraGetIssueLinkTypesTool() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = Arrays.asList(
);
        return Map.of(
            "name", "jira_get_issue_link_types",
            "description", "Get all available issue link types/relationships in Jira",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createJiraLinkIssuesTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sourceKey", Map.of(
            "type", "string",
            "description", "The source issue key"
        ));
        properties.put("anotherKey", Map.of(
            "type", "string",
            "description", "The target issue key"
        ));
        properties.put("relationship", Map.of(
            "type", "string",
            "description", "The relationship type name"
        ));
        List<String> required = Arrays.asList(
"sourceKey", "anotherKey", "relationship");
        return Map.of(
            "name", "jira_link_issues",
            "description", "Link two Jira issues with a specific relationship type",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceContentsByUrlsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("urlStrings", Map.of(
            "type", "array",
            "description", "Array of Confluence URLs to retrieve content from"
            , "example", "['https://confluence.example.com/wiki/spaces/SPACE/pages/123/Page+Title']"
        ));
        List<String> required = Arrays.asList(
"urlStrings");
        return Map.of(
            "name", "confluence_contents_by_urls",
            "description", "Get Confluence content by multiple URLs. Returns a list of content objects for each valid URL.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceSearchContentByTextTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of(
            "type", "string",
            "description", "Search query text to find in Confluence content"
            , "example", "project documentation"
        ));
        properties.put("limit", Map.of(
            "type", "number",
            "description", "Maximum number of search results to return"
            , "example", "10"
        ));
        List<String> required = Arrays.asList(
"query", "limit");
        return Map.of(
            "name", "confluence_search_content_by_text",
            "description", "Search Confluence content by text query using CQL (Confluence Query Language). Returns search results with content excerpts.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceContentByIdTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("contentId", Map.of(
            "type", "string",
            "description", "The unique content ID of the Confluence page"
            , "example", "123456"
        ));
        List<String> required = Arrays.asList(
"contentId");
        return Map.of(
            "name", "confluence_content_by_id",
            "description", "Get Confluence content by its unique content ID. Returns detailed content information including body, version, and metadata.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceContentByTitleAndSpaceTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "The title of the Confluence page"
            , "example", "Project Documentation"
        ));
        properties.put("space", Map.of(
            "type", "string",
            "description", "The space key where the content is located"
            , "example", "PROJ"
        ));
        List<String> required = Arrays.asList(
"title", "space");
        return Map.of(
            "name", "confluence_content_by_title_and_space",
            "description", "Get Confluence content by title and space key. Returns content result with metadata and body information.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceGetCurrentUserProfileTool() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = Arrays.asList(
);
        return Map.of(
            "name", "confluence_get_current_user_profile",
            "description", "Get the current user's profile information from Confluence. Returns user details for the authenticated user.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceGetUserProfileByIdTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", Map.of(
            "type", "string",
            "description", "The account ID of the user to get profile for"
            , "example", "123456:abcdef-1234-5678-90ab-cdef12345678"
        ));
        List<String> required = Arrays.asList(
"userId");
        return Map.of(
            "name", "confluence_get_user_profile_by_id",
            "description", "Get a specific user's profile information from Confluence by user ID. Returns user details for the specified user.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceGetContentAttachmentsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("contentId", Map.of(
            "type", "string",
            "description", "The content ID to get attachments for"
            , "example", "123456"
        ));
        List<String> required = Arrays.asList(
"contentId");
        return Map.of(
            "name", "confluence_get_content_attachments",
            "description", "Get all attachments for a specific Confluence content. Returns a list of attachment objects with metadata.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceFindContentByTitleAndSpaceTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "The title of the content to find"
            , "example", "Project Documentation"
        ));
        properties.put("space", Map.of(
            "type", "string",
            "description", "The space key where to search for the content"
            , "example", "PROJ"
        ));
        List<String> required = Arrays.asList(
"title", "space");
        return Map.of(
            "name", "confluence_find_content_by_title_and_space",
            "description", "Find Confluence content by title and space key. Returns the first matching content or null if not found.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceCreatePageTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "The title of the new page"
            , "example", "New Project Page"
        ));
        properties.put("parentId", Map.of(
            "type", "string",
            "description", "The ID of the parent page"
            , "example", "123456"
        ));
        properties.put("body", Map.of(
            "type", "string",
            "description", "The body content of the page in Confluence storage format"
            , "example", "<p>This is the page content.</p>"
        ));
        properties.put("space", Map.of(
            "type", "string",
            "description", "The space key where to create the page"
            , "example", "PROJ"
        ));
        List<String> required = Arrays.asList(
"title", "parentId", "body", "space");
        return Map.of(
            "name", "confluence_create_page",
            "description", "Create a new Confluence page with specified title, parent, body content, and space. Returns the created content object.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceUpdatePageTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("contentId", Map.of(
            "type", "string",
            "description", "The ID of the page to update"
            , "example", "123456"
        ));
        properties.put("title", Map.of(
            "type", "string",
            "description", "The new title for the page"
            , "example", "Updated Project Page"
        ));
        properties.put("parentId", Map.of(
            "type", "string",
            "description", "The ID of the new parent page"
            , "example", "123456"
        ));
        properties.put("body", Map.of(
            "type", "string",
            "description", "The new body content of the page in Confluence storage format"
            , "example", "<p>This is the updated page content.</p>"
        ));
        properties.put("space", Map.of(
            "type", "string",
            "description", "The space key where the page is located"
            , "example", "PROJ"
        ));
        List<String> required = Arrays.asList(
"contentId", "title", "parentId", "body", "space");
        return Map.of(
            "name", "confluence_update_page",
            "description", "Update an existing Confluence page with new title, parent, body content, and space. Returns the updated content object.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceUpdatePageWithHistoryTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("contentId", Map.of(
            "type", "string",
            "description", "The ID of the page to update"
            , "example", "123456"
        ));
        properties.put("title", Map.of(
            "type", "string",
            "description", "The new title for the page"
            , "example", "Updated Project Page"
        ));
        properties.put("parentId", Map.of(
            "type", "string",
            "description", "The ID of the new parent page"
            , "example", "123456"
        ));
        properties.put("body", Map.of(
            "type", "string",
            "description", "The new body content of the page in Confluence storage format"
            , "example", "<p>This is the updated page content.</p>"
        ));
        properties.put("space", Map.of(
            "type", "string",
            "description", "The space key where the page is located"
            , "example", "PROJ"
        ));
        properties.put("historyComment", Map.of(
            "type", "string",
            "description", "Comment to add to the page history"
            , "example", "Updated content based on user feedback"
        ));
        List<String> required = Arrays.asList(
"contentId", "title", "parentId", "body", "space", "historyComment");
        return Map.of(
            "name", "confluence_update_page_with_history",
            "description", "Update an existing Confluence page with new content and add a history comment. Returns the updated content object.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceGetChildrenByNameTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spaceKey", Map.of(
            "type", "string",
            "description", "The space key where the parent page is located"
            , "example", "PROJ"
        ));
        properties.put("contentName", Map.of(
            "type", "string",
            "description", "The name/title of the parent page"
            , "example", "Project Documentation"
        ));
        List<String> required = Arrays.asList(
"spaceKey", "contentName");
        return Map.of(
            "name", "confluence_get_children_by_name",
            "description", "Get child pages of a Confluence page by space key and content name. Returns a list of child content objects.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceGetChildrenByIdTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("contentId", Map.of(
            "type", "string",
            "description", "The content ID of the parent page"
            , "example", "123456"
        ));
        List<String> required = Arrays.asList(
"contentId");
        return Map.of(
            "name", "confluence_get_children_by_id",
            "description", "Get child pages of a Confluence page by content ID. Returns a list of child content objects.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceFindContentTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "Title of the Confluence page to find"
            , "example", "Project Documentation"
        ));
        List<String> required = Arrays.asList(
"title");
        return Map.of(
            "name", "confluence_find_content",
            "description", "Find a Confluence page by title in the default space. Returns the page content if found.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceFindOrCreateTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "Title of the page to find or create"
            , "example", "Project Documentation"
        ));
        properties.put("parentId", Map.of(
            "type", "string",
            "description", "ID of the parent page for creation"
            , "example", "123456"
        ));
        properties.put("body", Map.of(
            "type", "string",
            "description", "Body content for the new page (if creation is needed)"
            , "example", "<p>This is the page content.</p>"
        ));
        List<String> required = Arrays.asList(
"title", "parentId", "body");
        return Map.of(
            "name", "confluence_find_or_create",
            "description", "Find a Confluence page by title in the default space, or create it if it doesn't exist. Returns the found or created content.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

    private static Map<String, Object> createConfluenceContentByTitleTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of(
            "type", "string",
            "description", "Title of the Confluence page to get"
            , "example", "Project Documentation"
        ));
        List<String> required = Arrays.asList(
"title");
        return Map.of(
            "name", "confluence_content_by_title",
            "description", "Get Confluence content by title in the default space. Returns content result with metadata and body information.",
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }

}
