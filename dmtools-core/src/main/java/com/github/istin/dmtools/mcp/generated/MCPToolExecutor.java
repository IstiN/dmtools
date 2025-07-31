package com.github.istin.dmtools.mcp.generated;

import com.github.istin.dmtools.mcp.MCPToolDefinition;
import com.github.istin.dmtools.mcp.MCPParameterDefinition;
import java.util.*;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Auto-generated MCP tool executor.
 * Provides type-safe execution of MCP tools.
 */
public class MCPToolExecutor {

    public static Object executeTool(String toolName, Map<String, Object> arguments,
                                    Map<String, Object> clientInstances) throws Exception {
        switch (toolName) {
            case "jira_get_ticket_browse_url":
                return executeJiraGetTicketBrowseUrl(arguments, clientInstances.get("jira"));
            case "jira_delete_ticket":
                return executeJiraDeleteTicket(arguments, clientInstances.get("jira"));
            case "jira_search_and_perform":
                return executeJiraSearchAndPerform(arguments, clientInstances.get("jira"));
            case "jira_search_with_pagination":
                return executeJiraSearchWithPagination(arguments, clientInstances.get("jira"));
            case "jira_get_my_profile":
                return executeJiraGetMyProfile(arguments, clientInstances.get("jira"));
            case "jira_get_user_profile":
                return executeJiraGetUserProfile(arguments, clientInstances.get("jira"));
            case "jira_get_ticket":
                return executeJiraGetTicket(arguments, clientInstances.get("jira"));
            case "jira_get_subtasks":
                return executeJiraGetSubtasks(arguments, clientInstances.get("jira"));
            case "jira_post_comment_if_not_exists":
                return executeJiraPostCommentIfNotExists(arguments, clientInstances.get("jira"));
            case "jira_get_comments":
                return executeJiraGetComments(arguments, clientInstances.get("jira"));
            case "jira_post_comment":
                return executeJiraPostComment(arguments, clientInstances.get("jira"));
            case "jira_get_fix_versions":
                return executeJiraGetFixVersions(arguments, clientInstances.get("jira"));
            case "jira_get_components":
                return executeJiraGetComponents(arguments, clientInstances.get("jira"));
            case "jira_get_project_statuses":
                return executeJiraGetProjectStatuses(arguments, clientInstances.get("jira"));
            case "jira_create_ticket_with_parent":
                return executeJiraCreateTicketWithParent(arguments, clientInstances.get("jira"));
            case "jira_create_ticket_basic":
                return executeJiraCreateTicketBasic(arguments, clientInstances.get("jira"));
            case "jira_create_ticket_with_json":
                return executeJiraCreateTicketWithJson(arguments, clientInstances.get("jira"));
            case "jira_get_issues_in_parent_by_type":
                return executeJiraGetIssuesInParentByType(arguments, clientInstances.get("jira"));
            case "jira_update_description":
                return executeJiraUpdateDescription(arguments, clientInstances.get("jira"));
            case "jira_update_ticket_parent":
                return executeJiraUpdateTicketParent(arguments, clientInstances.get("jira"));
            case "jira_update_ticket":
                return executeJiraUpdateTicket(arguments, clientInstances.get("jira"));
            case "jira_update_field":
                return executeJiraUpdateField(arguments, clientInstances.get("jira"));
            case "jira_execute_request":
                return executeJiraExecuteRequest(arguments, clientInstances.get("jira"));
            case "jira_get_transitions":
                return executeJiraGetTransitions(arguments, clientInstances.get("jira"));
            case "jira_move_to_status":
                return executeJiraMoveToStatus(arguments, clientInstances.get("jira"));
            case "jira_move_to_status_with_resolution":
                return executeJiraMoveToStatusWithResolution(arguments, clientInstances.get("jira"));
            case "jira_clear_field":
                return executeJiraClearField(arguments, clientInstances.get("jira"));
            case "jira_set_fix_version":
                return executeJiraSetFixVersion(arguments, clientInstances.get("jira"));
            case "jira_add_fix_version":
                return executeJiraAddFixVersion(arguments, clientInstances.get("jira"));
            case "jira_set_priority":
                return executeJiraSetPriority(arguments, clientInstances.get("jira"));
            case "jira_remove_fix_version":
                return executeJiraRemoveFixVersion(arguments, clientInstances.get("jira"));
            case "jira_get_fields":
                return executeJiraGetFields(arguments, clientInstances.get("jira"));
            case "jira_get_issue_types":
                return executeJiraGetIssueTypes(arguments, clientInstances.get("jira"));
            case "jira_get_field_custom_code":
                return executeJiraGetFieldCustomCode(arguments, clientInstances.get("jira"));
            case "jira_get_issue_link_types":
                return executeJiraGetIssueLinkTypes(arguments, clientInstances.get("jira"));
            case "jira_link_issues":
                return executeJiraLinkIssues(arguments, clientInstances.get("jira"));
            case "confluence_contents_by_urls":
                return executeConfluenceContentsByUrls(arguments, clientInstances.get("confluence"));
            case "confluence_search_content_by_text":
                return executeConfluenceSearchContentByText(arguments, clientInstances.get("confluence"));
            case "confluence_content_by_id":
                return executeConfluenceContentById(arguments, clientInstances.get("confluence"));
            case "confluence_content_by_title_and_space":
                return executeConfluenceContentByTitleAndSpace(arguments, clientInstances.get("confluence"));
            case "confluence_get_current_user_profile":
                return executeConfluenceGetCurrentUserProfile(arguments, clientInstances.get("confluence"));
            case "confluence_get_user_profile_by_id":
                return executeConfluenceGetUserProfileById(arguments, clientInstances.get("confluence"));
            case "confluence_get_content_attachments":
                return executeConfluenceGetContentAttachments(arguments, clientInstances.get("confluence"));
            case "confluence_find_content_by_title_and_space":
                return executeConfluenceFindContentByTitleAndSpace(arguments, clientInstances.get("confluence"));
            case "confluence_create_page":
                return executeConfluenceCreatePage(arguments, clientInstances.get("confluence"));
            case "confluence_update_page":
                return executeConfluenceUpdatePage(arguments, clientInstances.get("confluence"));
            case "confluence_update_page_with_history":
                return executeConfluenceUpdatePageWithHistory(arguments, clientInstances.get("confluence"));
            case "confluence_get_children_by_name":
                return executeConfluenceGetChildrenByName(arguments, clientInstances.get("confluence"));
            case "confluence_get_children_by_id":
                return executeConfluenceGetChildrenById(arguments, clientInstances.get("confluence"));
            case "confluence_find_content":
                return executeConfluenceFindContent(arguments, clientInstances.get("confluence"));
            case "confluence_find_or_create":
                return executeConfluenceFindOrCreate(arguments, clientInstances.get("confluence"));
            case "confluence_content_by_title":
                return executeConfluenceContentByTitle(arguments, clientInstances.get("confluence"));
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private static Object executeJiraGetTicketBrowseUrl(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket_key = convertParameter(args.get("ticket_key"), "java.lang.String", "ticket_key");
        if (ticket_key == null) {
            throw new IllegalArgumentException("Required parameter 'ticket_key' is missing");
        }
        return client.getTicketBrowseUrl(ticket_key);
    }

    private static Object executeJiraDeleteTicket(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        return client.deleteTicket(ticketKey);
    }

    private static Object executeJiraSearchAndPerform(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String searchQueryJQL = convertParameter(args.get("searchQueryJQL"), "java.lang.String", "searchQueryJQL");
        if (searchQueryJQL == null) {
            throw new IllegalArgumentException("Required parameter 'searchQueryJQL' is missing");
        }
        java.lang.String[] fields = convertParameter(args.get("fields"), "java.lang.String[]", "fields");
        if (fields == null) {
            throw new IllegalArgumentException("Required parameter 'fields' is missing");
        }
        return client.searchAndPerform(searchQueryJQL, fields);
    }

    private static Object executeJiraSearchWithPagination(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String jql = convertParameter(args.get("jql"), "java.lang.String", "jql");
        if (jql == null) {
            throw new IllegalArgumentException("Required parameter 'jql' is missing");
        }
        int startAt = convertParameter(args.get("startAt"), "int", "startAt");
        if (args.get("startAt") == null) {
            throw new IllegalArgumentException("Required parameter 'startAt' is missing");
        }
        java.lang.String[] fields = convertParameter(args.get("fields"), "java.lang.String[]", "fields");
        if (fields == null) {
            throw new IllegalArgumentException("Required parameter 'fields' is missing");
        }
        return client.search(jql, startAt, fields);
    }

    private static Object executeJiraGetMyProfile(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        return client.performMyProfile();
    }

    private static Object executeJiraGetUserProfile(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String userId = convertParameter(args.get("userId"), "java.lang.String", "userId");
        if (userId == null) {
            throw new IllegalArgumentException("Required parameter 'userId' is missing");
        }
        return client.performProfile(userId);
    }

    private static Object executeJiraGetTicket(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        java.lang.String[] fields = convertParameter(args.get("fields"), "java.lang.String[]", "fields");
        return client.performTicket(ticketKey, fields);
    }

    private static Object executeJiraGetSubtasks(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        return client.performGettingSubtask(ticket);
    }

    private static Object executeJiraPostCommentIfNotExists(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        java.lang.String comment = convertParameter(args.get("comment"), "java.lang.String", "comment");
        if (comment == null) {
            throw new IllegalArgumentException("Required parameter 'comment' is missing");
        }
        client.postCommentIfNotExists(ticketKey, comment);
        return null;
    }

    private static Object executeJiraGetComments(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String key = convertParameter(args.get("key"), "java.lang.String", "key");
        if (key == null) {
            throw new IllegalArgumentException("Required parameter 'key' is missing");
        }
        com.github.istin.dmtools.common.model.ITicket ticket = convertParameter(args.get("ticket"), "com.github.istin.dmtools.common.model.ITicket", "ticket");
        return client.getComments(key, ticket);
    }

    private static Object executeJiraPostComment(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        java.lang.String comment = convertParameter(args.get("comment"), "java.lang.String", "comment");
        if (comment == null) {
            throw new IllegalArgumentException("Required parameter 'comment' is missing");
        }
        client.postComment(ticketKey, comment);
        return null;
    }

    private static Object executeJiraGetFixVersions(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        return client.getFixVersions(project);
    }

    private static Object executeJiraGetComponents(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        return client.getComponents(project);
    }

    private static Object executeJiraGetProjectStatuses(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        return client.getStatuses(project);
    }

    private static Object executeJiraCreateTicketWithParent(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        java.lang.String issueType = convertParameter(args.get("issueType"), "java.lang.String", "issueType");
        if (issueType == null) {
            throw new IllegalArgumentException("Required parameter 'issueType' is missing");
        }
        java.lang.String summary = convertParameter(args.get("summary"), "java.lang.String", "summary");
        if (summary == null) {
            throw new IllegalArgumentException("Required parameter 'summary' is missing");
        }
        java.lang.String description = convertParameter(args.get("description"), "java.lang.String", "description");
        if (description == null) {
            throw new IllegalArgumentException("Required parameter 'description' is missing");
        }
        java.lang.String parentKey = convertParameter(args.get("parentKey"), "java.lang.String", "parentKey");
        if (parentKey == null) {
            throw new IllegalArgumentException("Required parameter 'parentKey' is missing");
        }
        return client.createTicketInProjectWithParent(project, issueType, summary, description, parentKey);
    }

    private static Object executeJiraCreateTicketBasic(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        java.lang.String issueType = convertParameter(args.get("issueType"), "java.lang.String", "issueType");
        if (issueType == null) {
            throw new IllegalArgumentException("Required parameter 'issueType' is missing");
        }
        java.lang.String summary = convertParameter(args.get("summary"), "java.lang.String", "summary");
        if (summary == null) {
            throw new IllegalArgumentException("Required parameter 'summary' is missing");
        }
        java.lang.String description = convertParameter(args.get("description"), "java.lang.String", "description");
        if (description == null) {
            throw new IllegalArgumentException("Required parameter 'description' is missing");
        }
        return client.createTicketInProjectMcp(project, issueType, summary, description);
    }

    private static Object executeJiraCreateTicketWithJson(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        org.json.JSONObject fieldsJson = convertParameter(args.get("fieldsJson"), "org.json.JSONObject", "fieldsJson");
        if (fieldsJson == null) {
            throw new IllegalArgumentException("Required parameter 'fieldsJson' is missing");
        }
        return client.createTicketInProjectWithJson(project, fieldsJson);
    }

    private static Object executeJiraGetIssuesInParentByType(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String key = convertParameter(args.get("key"), "java.lang.String", "key");
        if (key == null) {
            throw new IllegalArgumentException("Required parameter 'key' is missing");
        }
        java.lang.String type = convertParameter(args.get("type"), "java.lang.String", "type");
        if (type == null) {
            throw new IllegalArgumentException("Required parameter 'type' is missing");
        }
        java.lang.String[] fields = convertParameter(args.get("fields"), "java.lang.String[]", "fields");
        return client.issuesInParentByType(key, type, fields);
    }

    private static Object executeJiraUpdateDescription(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String key = convertParameter(args.get("key"), "java.lang.String", "key");
        if (key == null) {
            throw new IllegalArgumentException("Required parameter 'key' is missing");
        }
        java.lang.String description = convertParameter(args.get("description"), "java.lang.String", "description");
        if (description == null) {
            throw new IllegalArgumentException("Required parameter 'description' is missing");
        }
        return client.updateDescription(key, description);
    }

    private static Object executeJiraUpdateTicketParent(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String key = convertParameter(args.get("key"), "java.lang.String", "key");
        if (key == null) {
            throw new IllegalArgumentException("Required parameter 'key' is missing");
        }
        java.lang.String parentKey = convertParameter(args.get("parentKey"), "java.lang.String", "parentKey");
        if (parentKey == null) {
            throw new IllegalArgumentException("Required parameter 'parentKey' is missing");
        }
        return client.updateTicketParent(key, parentKey);
    }

    private static Object executeJiraUpdateTicket(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        org.json.JSONObject params = convertParameter(args.get("params"), "org.json.JSONObject", "params");
        if (params == null) {
            throw new IllegalArgumentException("Required parameter 'params' is missing");
        }
        return client.updateTicket(ticketKey, params);
    }

    private static Object executeJiraUpdateField(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String key = convertParameter(args.get("key"), "java.lang.String", "key");
        if (key == null) {
            throw new IllegalArgumentException("Required parameter 'key' is missing");
        }
        java.lang.String field = convertParameter(args.get("field"), "java.lang.String", "field");
        if (field == null) {
            throw new IllegalArgumentException("Required parameter 'field' is missing");
        }
        java.lang.Object value = convertParameter(args.get("value"), "java.lang.Object", "value");
        if (value == null) {
            throw new IllegalArgumentException("Required parameter 'value' is missing");
        }
        return client.updateField(key, field, value);
    }

    private static Object executeJiraExecuteRequest(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String url = convertParameter(args.get("url"), "java.lang.String", "url");
        if (url == null) {
            throw new IllegalArgumentException("Required parameter 'url' is missing");
        }
        return client.execute(url);
    }

    private static Object executeJiraGetTransitions(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        return client.getTransitions(ticket);
    }

    private static Object executeJiraMoveToStatus(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticketKey = convertParameter(args.get("ticketKey"), "java.lang.String", "ticketKey");
        if (ticketKey == null) {
            throw new IllegalArgumentException("Required parameter 'ticketKey' is missing");
        }
        java.lang.String statusName = convertParameter(args.get("statusName"), "java.lang.String", "statusName");
        if (statusName == null) {
            throw new IllegalArgumentException("Required parameter 'statusName' is missing");
        }
        return client.moveToStatus(ticketKey, statusName);
    }

    private static Object executeJiraMoveToStatusWithResolution(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String statusName = convertParameter(args.get("statusName"), "java.lang.String", "statusName");
        if (statusName == null) {
            throw new IllegalArgumentException("Required parameter 'statusName' is missing");
        }
        java.lang.String resolution = convertParameter(args.get("resolution"), "java.lang.String", "resolution");
        if (resolution == null) {
            throw new IllegalArgumentException("Required parameter 'resolution' is missing");
        }
        return client.moveToStatus(ticket, statusName, resolution);
    }

    private static Object executeJiraClearField(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String field = convertParameter(args.get("field"), "java.lang.String", "field");
        if (field == null) {
            throw new IllegalArgumentException("Required parameter 'field' is missing");
        }
        return client.clearField(ticket, field);
    }

    private static Object executeJiraSetFixVersion(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String fixVersion = convertParameter(args.get("fixVersion"), "java.lang.String", "fixVersion");
        if (fixVersion == null) {
            throw new IllegalArgumentException("Required parameter 'fixVersion' is missing");
        }
        return client.setTicketFixVersion(ticket, fixVersion);
    }

    private static Object executeJiraAddFixVersion(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String fixVersion = convertParameter(args.get("fixVersion"), "java.lang.String", "fixVersion");
        if (fixVersion == null) {
            throw new IllegalArgumentException("Required parameter 'fixVersion' is missing");
        }
        return client.addTicketFixVersion(ticket, fixVersion);
    }

    private static Object executeJiraSetPriority(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String priority = convertParameter(args.get("priority"), "java.lang.String", "priority");
        if (priority == null) {
            throw new IllegalArgumentException("Required parameter 'priority' is missing");
        }
        return client.setTicketPriority(ticket, priority);
    }

    private static Object executeJiraRemoveFixVersion(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String ticket = convertParameter(args.get("ticket"), "java.lang.String", "ticket");
        if (ticket == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        java.lang.String fixVersion = convertParameter(args.get("fixVersion"), "java.lang.String", "fixVersion");
        if (fixVersion == null) {
            throw new IllegalArgumentException("Required parameter 'fixVersion' is missing");
        }
        return client.removeTicketFixVersion(ticket, fixVersion);
    }

    private static Object executeJiraGetFields(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        return client.getFields(project);
    }

    private static Object executeJiraGetIssueTypes(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        return client.getIssueTypes(project);
    }

    private static Object executeJiraGetFieldCustomCode(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String project = convertParameter(args.get("project"), "java.lang.String", "project");
        if (project == null) {
            throw new IllegalArgumentException("Required parameter 'project' is missing");
        }
        java.lang.String fieldName = convertParameter(args.get("fieldName"), "java.lang.String", "fieldName");
        if (fieldName == null) {
            throw new IllegalArgumentException("Required parameter 'fieldName' is missing");
        }
        return client.getFieldCustomCode(project, fieldName);
    }

    private static Object executeJiraGetIssueLinkTypes(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        return client.getRelationships();
    }

    private static Object executeJiraLinkIssues(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.jira.JiraClient client = (com.github.istin.dmtools.atlassian.jira.JiraClient) clientInstance;
        java.lang.String sourceKey = convertParameter(args.get("sourceKey"), "java.lang.String", "sourceKey");
        if (sourceKey == null) {
            throw new IllegalArgumentException("Required parameter 'sourceKey' is missing");
        }
        java.lang.String anotherKey = convertParameter(args.get("anotherKey"), "java.lang.String", "anotherKey");
        if (anotherKey == null) {
            throw new IllegalArgumentException("Required parameter 'anotherKey' is missing");
        }
        java.lang.String relationship = convertParameter(args.get("relationship"), "java.lang.String", "relationship");
        if (relationship == null) {
            throw new IllegalArgumentException("Required parameter 'relationship' is missing");
        }
        return client.linkIssueWithRelationship(sourceKey, anotherKey, relationship);
    }

    private static Object executeConfluenceContentsByUrls(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String[] urlStrings = convertParameter(args.get("urlStrings"), "java.lang.String[]", "urlStrings");
        if (urlStrings == null) {
            throw new IllegalArgumentException("Required parameter 'urlStrings' is missing");
        }
        return client.contentsByUrls(urlStrings);
    }

    private static Object executeConfluenceSearchContentByText(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String query = convertParameter(args.get("query"), "java.lang.String", "query");
        if (query == null) {
            throw new IllegalArgumentException("Required parameter 'query' is missing");
        }
        int limit = convertParameter(args.get("limit"), "int", "limit");
        if (args.get("limit") == null) {
            throw new IllegalArgumentException("Required parameter 'limit' is missing");
        }
        return client.searchContentByText(query, limit);
    }

    private static Object executeConfluenceContentById(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String contentId = convertParameter(args.get("contentId"), "java.lang.String", "contentId");
        if (contentId == null) {
            throw new IllegalArgumentException("Required parameter 'contentId' is missing");
        }
        return client.contentById(contentId);
    }

    private static Object executeConfluenceContentByTitleAndSpace(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String space = convertParameter(args.get("space"), "java.lang.String", "space");
        if (space == null) {
            throw new IllegalArgumentException("Required parameter 'space' is missing");
        }
        return client.content(title, space);
    }

    private static Object executeConfluenceGetCurrentUserProfile(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        return client.profile();
    }

    private static Object executeConfluenceGetUserProfileById(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String userId = convertParameter(args.get("userId"), "java.lang.String", "userId");
        if (userId == null) {
            throw new IllegalArgumentException("Required parameter 'userId' is missing");
        }
        return client.profile(userId);
    }

    private static Object executeConfluenceGetContentAttachments(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String contentId = convertParameter(args.get("contentId"), "java.lang.String", "contentId");
        if (contentId == null) {
            throw new IllegalArgumentException("Required parameter 'contentId' is missing");
        }
        return client.getContentAttachments(contentId);
    }

    private static Object executeConfluenceFindContentByTitleAndSpace(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String space = convertParameter(args.get("space"), "java.lang.String", "space");
        if (space == null) {
            throw new IllegalArgumentException("Required parameter 'space' is missing");
        }
        return client.findContent(title, space);
    }

    private static Object executeConfluenceCreatePage(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String parentId = convertParameter(args.get("parentId"), "java.lang.String", "parentId");
        if (parentId == null) {
            throw new IllegalArgumentException("Required parameter 'parentId' is missing");
        }
        java.lang.String body = convertParameter(args.get("body"), "java.lang.String", "body");
        if (body == null) {
            throw new IllegalArgumentException("Required parameter 'body' is missing");
        }
        java.lang.String space = convertParameter(args.get("space"), "java.lang.String", "space");
        if (space == null) {
            throw new IllegalArgumentException("Required parameter 'space' is missing");
        }
        return client.createPage(title, parentId, body, space);
    }

    private static Object executeConfluenceUpdatePage(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String contentId = convertParameter(args.get("contentId"), "java.lang.String", "contentId");
        if (contentId == null) {
            throw new IllegalArgumentException("Required parameter 'contentId' is missing");
        }
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String parentId = convertParameter(args.get("parentId"), "java.lang.String", "parentId");
        if (parentId == null) {
            throw new IllegalArgumentException("Required parameter 'parentId' is missing");
        }
        java.lang.String body = convertParameter(args.get("body"), "java.lang.String", "body");
        if (body == null) {
            throw new IllegalArgumentException("Required parameter 'body' is missing");
        }
        java.lang.String space = convertParameter(args.get("space"), "java.lang.String", "space");
        if (space == null) {
            throw new IllegalArgumentException("Required parameter 'space' is missing");
        }
        return client.updatePage(contentId, title, parentId, body, space);
    }

    private static Object executeConfluenceUpdatePageWithHistory(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String contentId = convertParameter(args.get("contentId"), "java.lang.String", "contentId");
        if (contentId == null) {
            throw new IllegalArgumentException("Required parameter 'contentId' is missing");
        }
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String parentId = convertParameter(args.get("parentId"), "java.lang.String", "parentId");
        if (parentId == null) {
            throw new IllegalArgumentException("Required parameter 'parentId' is missing");
        }
        java.lang.String body = convertParameter(args.get("body"), "java.lang.String", "body");
        if (body == null) {
            throw new IllegalArgumentException("Required parameter 'body' is missing");
        }
        java.lang.String space = convertParameter(args.get("space"), "java.lang.String", "space");
        if (space == null) {
            throw new IllegalArgumentException("Required parameter 'space' is missing");
        }
        java.lang.String historyComment = convertParameter(args.get("historyComment"), "java.lang.String", "historyComment");
        if (historyComment == null) {
            throw new IllegalArgumentException("Required parameter 'historyComment' is missing");
        }
        return client.updatePage(contentId, title, parentId, body, space, historyComment);
    }

    private static Object executeConfluenceGetChildrenByName(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String spaceKey = convertParameter(args.get("spaceKey"), "java.lang.String", "spaceKey");
        if (spaceKey == null) {
            throw new IllegalArgumentException("Required parameter 'spaceKey' is missing");
        }
        java.lang.String contentName = convertParameter(args.get("contentName"), "java.lang.String", "contentName");
        if (contentName == null) {
            throw new IllegalArgumentException("Required parameter 'contentName' is missing");
        }
        return client.getChildrenOfContentByName(spaceKey, contentName);
    }

    private static Object executeConfluenceGetChildrenById(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String contentId = convertParameter(args.get("contentId"), "java.lang.String", "contentId");
        if (contentId == null) {
            throw new IllegalArgumentException("Required parameter 'contentId' is missing");
        }
        return client.getChildrenOfContentById(contentId);
    }

    private static Object executeConfluenceFindContent(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        return client.findContent(title);
    }

    private static Object executeConfluenceFindOrCreate(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        java.lang.String parentId = convertParameter(args.get("parentId"), "java.lang.String", "parentId");
        if (parentId == null) {
            throw new IllegalArgumentException("Required parameter 'parentId' is missing");
        }
        java.lang.String body = convertParameter(args.get("body"), "java.lang.String", "body");
        if (body == null) {
            throw new IllegalArgumentException("Required parameter 'body' is missing");
        }
        return client.findOrCreate(title, parentId, body);
    }

    private static Object executeConfluenceContentByTitle(Map<String, Object> args, Object clientInstance) throws Exception {
        com.github.istin.dmtools.atlassian.confluence.Confluence client = (com.github.istin.dmtools.atlassian.confluence.Confluence) clientInstance;
        java.lang.String title = convertParameter(args.get("title"), "java.lang.String", "title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        return client.content(title);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertParameter(Object value, String targetType, String paramName) {
        if (value == null) {
            return null;
        }

        // Handle JSONArray to String[] conversion
        if (targetType.equals("java.lang.String[]") && value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            String[] result = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                result[i] = jsonArray.getString(i);
            }
            return (T) result;
        }

        // Handle JSONArray to Object[] conversion
        if (targetType.equals("java.lang.Object[]") && value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            Object[] result = new Object[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                result[i] = jsonArray.get(i);
            }
            return (T) result;
        }

        // Handle direct casting for other types
        try {
            return (T) value;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' cannot be converted to " + targetType + ". Value: " + value + ", Type: " + value.getClass().getSimpleName());
        }
    }

}
