/**
 * Comprehensive Jira MCP Methods Examples
 * 
 * This file demonstrates all available Jira MCP tools with example usage.
 * Use this as a reference for implementing JavaScript post-actions in DMTools jobs.
 * 
 * Available in JavaScript context: All 40+ Jira MCP tools
 * Usage: Copy relevant examples to your postJSAction parameter
 */

function action(params) {
    try {
        console.log("=== JIRA MCP EXAMPLES ===");
        console.log("Processing ticket:", params.ticket.key);
        
        const results = {};
        const ticketKey = params.ticket.key;
        
        // ===== TICKET INFORMATION RETRIEVAL =====
        
        // Get ticket details
        console.log("1. Getting ticket details...");
        const ticketDetails = jira_get_ticket({
            key: ticketKey,
            fields: ["summary", "status", "assignee", "labels"]
        });
        results.ticketDetails = ticketDetails;
        
        // Get my profile
        console.log("2. Getting my profile...");
        const myProfile = jira_get_my_profile({});
        results.myProfile = myProfile;
        
        // Get user profile by ID
        if (myProfile && myProfile.accountId) {
            console.log("3. Getting user profile by ID...");
            const userProfile = jira_get_user_profile({
                userId: myProfile.accountId
            });
            results.userProfile = userProfile;
        }
        
        // ===== PROJECT INFORMATION =====
        
        // Get project components
        console.log("4. Getting project components...");
        const components = jira_get_components({
            project: ticketKey.split('-')[0] // Extract project key
        });
        results.components = components;
        
        // Get project fix versions
        console.log("5. Getting project fix versions...");
        const fixVersions = jira_get_fix_versions({
            project: ticketKey.split('-')[0]
        });
        results.fixVersions = fixVersions;
        
        // Get project statuses
        console.log("6. Getting project statuses...");
        const projectStatuses = jira_get_project_statuses({
            project: ticketKey.split('-')[0]
        });
        results.projectStatuses = projectStatuses;
        
        // Get project fields
        console.log("7. Getting project fields...");
        const projectFields = jira_get_fields({
            project: ticketKey.split('-')[0]
        });
        results.projectFields = projectFields;
        
        // Get issue types
        console.log("8. Getting issue types...");
        const issueTypes = jira_get_issue_types({
            project: ticketKey.split('-')[0]
        });
        results.issueTypes = issueTypes;
        
        // ===== SEARCH AND QUERY =====
        
        // Search by JQL
        console.log("9. Searching tickets by JQL...");
        const searchResults = jira_search_by_jql({
            searchQueryJQL: `project = ${ticketKey.split('-')[0]} AND status != Done ORDER BY created DESC`,
            fields: ["key", "summary", "status"]
        });
        results.searchResults = searchResults;
        
        // Search with pagination
        console.log("10. Searching with pagination...");
        const paginatedResults = jira_search_by_page({
            jql: `project = ${ticketKey.split('-')[0]} ORDER BY created DESC`,
            nextPageToken: "", // Empty for first page
            fields: ["key", "summary", "status"]
        });
        results.paginatedResults = paginatedResults;
        
        // Get subtasks
        console.log("11. Getting subtasks...");
        const subtasks = jira_get_subtasks({
            key: ticketKey
        });
        results.subtasks = subtasks;
        
        // ===== COMMENTS =====
        
        // Get comments
        console.log("12. Getting comments...");
        const comments = jira_get_comments({
            key: ticketKey
        });
        results.comments = comments;
        
        // Post a comment
        console.log("13. Posting comment...");
        const commentResult = jira_post_comment({
            key: ticketKey,
            comment: "This is an automated comment from JavaScript MCP example. Timestamp: " + new Date().toISOString()
        });
        results.commentResult = commentResult;
        
        // Post comment if not exists
        console.log("14. Posting comment if not exists...");
        const conditionalComment = jira_post_comment_if_not_exists({
            key: ticketKey,
            comment: "This comment will only be posted once - JavaScript MCP Example"
        });
        results.conditionalComment = conditionalComment;
        
        // ===== TICKET UPDATES =====
        
        // Update description
        console.log("15. Updating description...");
        const descriptionUpdate = jira_update_description({
            key: ticketKey,
            description: (params.ticket.description || "") + "\n\n*Updated by JavaScript MCP Example at " + new Date().toISOString() + "*"
        });
        results.descriptionUpdate = descriptionUpdate;
        
        // Update field (add label)
        console.log("16. Adding label...");
        const currentLabels = params.ticket.labels || [];
        const newLabels = [...currentLabels, "js-mcp-example"];
        const labelUpdate = jira_update_field({
            key: ticketKey,
            field: "labels",
            value: newLabels
        });
        results.labelUpdate = labelUpdate;
        
        // Set priority
        console.log("17. Setting priority...");
        const priorityUpdate = jira_set_priority({
            key: ticketKey,
            priority: "Medium"
        });
        results.priorityUpdate = priorityUpdate;
        
        // Add fix version (if available)
        if (fixVersions && fixVersions.length > 0) {
            console.log("18. Adding fix version...");
            const fixVersionUpdate = jira_add_fix_version({
                key: ticketKey,
                fixVersion: fixVersions[0].name
            });
            results.fixVersionUpdate = fixVersionUpdate;
        }
        
        // ===== TRANSITIONS AND STATUS =====
        
        // Get available transitions
        console.log("19. Getting available transitions...");
        const transitions = jira_get_transitions({
            key: ticketKey
        });
        results.transitions = transitions;
        
        // Note: Actual status transitions commented out to avoid changing ticket state
        // Uncomment and modify as needed for your use case
        /*
        if (transitions && transitions.length > 0) {
            console.log("20. Moving to status...");
            const statusMove = jira_move_to_status({
                key: ticketKey,
                statusName: transitions[0].name
            });
            results.statusMove = statusMove;
        }
        */
        
        // ===== TICKET CREATION =====
        
        // Create basic ticket
        console.log("21. Creating basic ticket...");
        const newTicket = jira_create_ticket_basic({
            project: ticketKey.split('-')[0],
            issueType: "Task",
            summary: "JavaScript MCP Example - Created at " + new Date().toISOString(),
            description: "This ticket was created by JavaScript MCP example to demonstrate ticket creation capabilities."
        });
        results.newTicket = newTicket;
        
        // Create ticket with parent (if we have a new ticket)
        if (newTicket && newTicket.key) {
            console.log("22. Creating subtask...");
            const subtask = jira_create_ticket_with_parent({
                project: ticketKey.split('-')[0],
                issueType: "Sub-task",
                summary: "JavaScript MCP Example - Subtask",
                description: "This is a subtask created by JavaScript MCP example.",
                parentKey: newTicket.key
            });
            results.subtask = subtask;
        }
        
        // Create ticket with JSON (advanced)
        console.log("23. Creating ticket with JSON...");
        const advancedTicket = jira_create_ticket_with_json({
            project: ticketKey.split('-')[0],
            fieldsJson: {
                summary: "JavaScript MCP Example - Advanced Creation",
                description: "Created with advanced JSON parameters",
                issuetype: { name: "Task" },
                priority: { name: "Low" },
                labels: ["js-mcp-example", "automated"]
            }
        });
        results.advancedTicket = advancedTicket;
        
        // ===== LINKING =====
        
        // Get issue link types
        console.log("24. Getting issue link types...");
        const linkTypes = jira_get_issue_link_types({
            random_string: "dummy" // Required parameter for no-param tools
        });
        results.linkTypes = linkTypes;
        
        // Link issues (if we have created tickets)
        if (newTicket && advancedTicket && linkTypes && linkTypes.length > 0) {
            console.log("25. Linking issues...");
            const linkResult = jira_link_issues({
                sourceKey: newTicket.key,
                anotherKey: advancedTicket.key,
                relationship: linkTypes[0].name
            });
            results.linkResult = linkResult;
        }
        
        // ===== FIELD OPERATIONS =====
        
        // Get field custom code
        console.log("26. Getting field custom code...");
        const fieldCode = jira_get_field_custom_code({
            project: ticketKey.split('-')[0],
            fieldName: "Priority"
        });
        results.fieldCode = fieldCode;
        
        // Clear field (example with a safe field)
        console.log("27. Clearing field example (commented out)...");
        // Uncomment if you want to test field clearing
        /*
        const clearResult = jira_clear_field({
            key: ticketKey,
            field: "environment" // Safe field to clear
        });
        results.clearResult = clearResult;
        */
        
        // ===== ADVANCED OPERATIONS =====
        
        // Execute custom request
        console.log("28. Executing custom request...");
        const customRequest = jira_execute_request({
            url: `/rest/api/2/issue/${ticketKey}/watchers`
        });
        results.customRequest = customRequest;
        
        // Assign ticket
        if (myProfile && myProfile.emailAddress) {
            console.log("29. Assigning ticket...");
            const assignResult = jira_assign_ticket_to({
                key: ticketKey,
                userName: myProfile.emailAddress
            });
            results.assignResult = assignResult;
        }
        
        console.log("=== JIRA MCP EXAMPLES COMPLETED ===");
        
        return {
            success: true,
            message: `Successfully executed Jira MCP examples for ticket ${ticketKey}`,
            results: results,
            totalOperations: Object.keys(results).length
        };
        
    } catch (error) {
        console.error("Error in Jira MCP examples:", error);
        return {
            success: false,
            error: error.toString(),
            message: "Failed to execute Jira MCP examples"
        };
    }
}

