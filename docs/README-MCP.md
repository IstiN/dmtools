# CLI DMTools MCP Methods

DMTools provides a comprehensive CLI interface for Model Context Protocol (MCP) tools, enabling seamless interaction with Jira, Confluence, Figma, and AI services through command-line operations.

## 🚀 First Commands
```bash
# List all available MCP tools
dmtools list

# Get a Jira ticket
dmtools jira_get_ticket PROJ-123

# Search Jira tickets
dmtools jira_search_by_jql "project = PROJ AND status = Open" "summary,status,assignee"

# Extract Figma design data
dmtools figma_get_layers "https://www.figma.com/file/abc123/Design"

# Send Teams message
dmtools teams_send_message "Hello from dmtools!" "chat@example.com"
```

## 📞 Format of Calls

DMTools supports multiple input methods with this priority:

### 1. Positional Arguments (Simplest)
```bash
dmtools jira_get_ticket PROJ-123 summary,description,status
```

### 2. JSON Data Flag
```bash
dmtools jira_get_ticket --data '{"key": "PROJ-123", "fields": ["summary"]}'
```

### 3. File Input
```bash
dmtools jira_get_ticket --file ticket-params.json
```

### 4. Heredoc (Multi-line JSON)
```bash
dmtools jira_create_ticket_basic <<EOF
{
  "project": "PROJ",
  "issueType": "Task",
  "summary": "New task",
  "description": "Task description"
}
EOF
```

### 5. STDIN/Pipe
```bash
echo '{"key": "PROJ-123"}' | dmtools jira_get_ticket
```

## 🚀 JavaScript Actions (JSRunner)

DMTools supports running JavaScript actions via the `jsrunner` job type. This allows you to execute custom JavaScript code that can interact with MCP tools, process data, and perform complex workflows.

### Basic JSRunner Job

```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "my-script.js",
    "jobParams": {
      "ticketKey": "PROJ-123",
      "outputPath": "./output"
    }
  }
}
```

**Execute:**
```bash
dmtools run my-job.json
```

### Example JavaScript Script

Create a file `my-script.js`:

```javascript
/**
 * Example JavaScript action demonstrating MCP tool usage
 * Shows how to call different MCP tools from JavaScript:
 * - Jira: ticket management and search
 * - ADO: work item management
 * - Confluence: page content and search
 * - Figma: design analysis
 * - AI: intelligent analysis
 * - Teams: messaging
 * - File: data persistence
 * - CLI: system commands
 */
function action(params) {
    try {
        console.log('🚀 Starting JavaScript action...');

        const ticketKey = params.jobParams?.ticketKey || 'PROJ-123';

        // 1. Get Jira ticket information
        console.log('📋 Getting Jira ticket...');
        const ticket = jira_get_ticket(ticketKey);
        console.log('Ticket title:', ticket.fields?.summary);

        // 2. Get ticket comments
        console.log('💬 Getting ticket comments...');
        const comments = jira_get_comments(ticketKey);
        console.log('Found', comments.length, 'comments');

        // 3. Search for related tickets
        console.log('🔍 Searching for related tickets...');
        const relatedTickets = jira_search_by_jql(
            'project = PROJ AND status = Open',
            'summary,status'
        );
        console.log('Found', relatedTickets.length, 'related tickets');

        // 4. Get Confluence page content
        console.log('📄 Getting Confluence page...');
        const page = confluence_content_by_title('API Documentation');
        if (page) {
            console.log('Page found with ID:', page.id);
        }

        // Search Confluence content
        console.log('🔍 Searching Confluence...');
        const searchResults = confluence_search_content_by_text('user authentication', 5);
        console.log('Found', searchResults.length, 'search results');

        // 5. Extract Figma design data
        console.log('🎨 Getting Figma design layers...');
        const layers = figma_get_layers('https://www.figma.com/file/abc123/MyDesign');
        if (layers) {
            console.log('Found', layers.length, 'design layers');
        }

        // 6. Work with Azure DevOps work items
        console.log('🔧 Working with Azure DevOps...');
        try {
            // Get ADO work item
            const workItem = ado_get_work_item(12345);
            console.log('ADO work item title:', workItem.fields?.['System.Title']);

            // Update work item status
            ado_move_to_state(12345, 'In Progress');
            console.log('Work item moved to In Progress');

        } catch (adoError) {
            console.log('ADO tools not configured, skipping...');
        }

        // 7. Use AI for analysis (if configured)
        console.log('🤖 Analyzing with AI...');
        try {
            const aiResponse = gemini_ai_chat('Summarize the ticket: ' + ticket.fields?.summary);
            console.log('AI Summary:', aiResponse.substring(0, 100) + '...');
        } catch (aiError) {
            console.log('AI tools not configured, skipping...');
        }

        // 8. Send Teams message (if configured)
        console.log('💬 Sending Teams notification...');
        try {
            teams_send_message('Ticket processed: ' + ticketKey, 'user@company.com');
            console.log('Teams message sent');
        } catch (teamsError) {
            console.log('Teams not configured, skipping...');
        }

        // 9. File operations - read and write files
        console.log('📁 Performing file operations...');
        try {
            // Write processed data to file
            const outputData = {
                ticketKey: ticketKey,
                summary: ticket.fields?.summary,
                commentsCount: comments.length,
                processedAt: new Date().toISOString()
            };

            file_write('./output/ticket-report.json', JSON.stringify(outputData, null, 2));
            console.log('Report saved to ticket-report.json');

            // Validate the written file
            const isValid = file_validate_json_file('./output/ticket-report.json');
            console.log('File validation:', isValid ? '✅ Valid JSON' : '❌ Invalid JSON');

        } catch (fileError) {
            console.log('File operations not available, skipping...');
        }

        // 10. Execute CLI commands
        console.log('💻 Executing CLI commands...');
        try {
            // Run git status to check repository state
            const gitResult = cli_execute_command('git status --porcelain');
            console.log('Git status:', gitResult || 'No changes');

        } catch (cliError) {
            console.log('CLI commands not available, skipping...');
        }

        console.log('✅ JavaScript action completed successfully');

        return {
            success: true,
            ticketProcessed: ticketKey,
            commentsCount: comments.length,
            relatedTicketsCount: relatedTickets.length,
            confluencePageFound: !!page,
            confluenceSearchResults: searchResults.length,
            figmaLayersFound: layers ? layers.length : 0,
            adoWorkItemProcessed: true
        };

    } catch (error) {
        console.error('❌ Error in JavaScript action:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

### MCP Tools Available in JavaScript

| Category | Example Tools | Description |
|----------|---------------|-------------|
| **Jira** | `jira_get_ticket()`, `jira_search_by_jql()`, `jira_post_comment()` | Ticket management and queries |
| **ADO** | `ado_get_work_item()`, `ado_move_to_state()`, `ado_assign_work_item()` | Azure DevOps work item management |
| **Confluence** | `confluence_content_by_title()`, `confluence_search_content_by_text()`, `confluence_create_page()` | Page content, search, and management |
| **Figma** | `figma_get_layers()`, `figma_get_icons()` | Design file analysis |
| **Teams** | `teams_send_message()`, `teams_get_messages()` | Chat and messaging |
| **TestRail** | `testrail_get_all_cases()`, `testrail_search_cases()`, `testrail_create_case()` | Test case management |
| **AI** | `gemini_ai_chat()`, `ollama_ai_chat()`, `anthropic_ai_chat()`, `openai_ai_chat()` | AI-powered analysis |
| **File** | `file_read()`, `file_write()`, `file_validate_json()` | File system operations |
| **CLI** | `cli_execute_command()` | Execute system commands |

### Running Custom JavaScript Actions

```bash
# Create your script
cat > my-action.js << 'EOF'
function action(params) {
    console.log('Hello from custom JS action!');
    return { success: true };
}
EOF

# Create job configuration
cat > job.json << 'EOF'
{
  "name": "jsrunner",
  "params": {
    "jsPath": "my-action.js"
  }
}
EOF

# Run the job
dmtools run job.json
```

## 📋 Available MCP Tools

### ⚠️ Important: Duplicate Field Names

Jira can have multiple custom fields with the same name. DMTools handles this intelligently:

- **Field Updates** (using field name): Updates **ALL** fields with that name
  ```bash
  # Updates both customfield_10551 and customfield_10186 if both are named "Dependencies"
  dmtools jira_update_field PROJ-123 Dependencies "value"
  ```

- **Field Updates** (using field ID): Updates **only that specific field**
  ```bash
  # Updates only customfield_10551
  dmtools jira_update_field PROJ-123 customfield_10551 "value"
  ```

- **JQL Searches**: Returns data from **ALL** fields with that name
  ```bash
  # Retrieves data from all "Acceptance Criteria" fields
  dmtools jira_search_by_jql "project = PROJ" "Acceptance Criteria,summary"
  ```

See [JIRA_DUPLICATE_FIELDS_GUIDE.md](JIRA_DUPLICATE_FIELDS_GUIDE.md) for complete details.

### Jira Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `jira_add_fix_version` | `key`, `fixVersion` | Add a fix version to a Jira ticket (without removing existing ones) |
| `jira_add_label` | `key`, `label` | Adding label to specific ticket key |
| `jira_assign_ticket_to` | `accountId`, `key` | Assigns a Jira ticket to user |
| `jira_attach_file_to_ticket` | `name`, `ticketKey`, `contentType`, `filePath` | Attach a file to a Jira ticket from a local file path |
| `jira_clear_field` | `field`, `key` | Clear (delete value) a specific field value in a Jira ticket |
| `jira_create_ticket_basic` | `issueType`, `summary`, `project`, `description` | Create a new Jira ticket with basic fields (project, issue type, summary, description) |
| `jira_create_ticket_with_json` | `project`, `fieldsJson` | Create a new Jira ticket with custom fields using JSON configuration |
| `jira_create_ticket_with_parent` | `issueType`, `summary`, `project`, `description`, `parentKey` | Create a new Jira ticket with a parent relationship |
| `jira_delete_ticket` | `key` | Delete a Jira ticket by key |
| `jira_download_attachment` | `href` | Download a Jira attachment by URL and save it as a file |
| `jira_execute_request` | `url` | Execute a custom HTTP GET request to Jira API with auth |
| `jira_get_account_by_email` | `email` | Gets account details by email |
| `jira_get_comments` | `ticket` | Get all comments for a specific Jira ticket |
| `jira_get_components` | `project` | Get all components for a specific Jira project |
| `jira_get_field_custom_code` | `project`, `fieldName` | Get the custom field code for a human friendly field name in a Jira project |
| `jira_get_fields` | `project` | Get all available fields for a Jira project |
| `jira_get_fix_versions` | `project` | Get all fix versions for a specific Jira project |
| `jira_get_issue_link_types` | - | Get all available issue link types/relationships in Jira |
| `jira_get_issue_types` | `project` | Get all available issue types for a specific Jira project |
| `jira_get_my_profile` | - | Get the current user's profile information from Jira |
| `jira_get_project_statuses` | `project` | Get all statuses for a specific Jira project |
| `jira_get_subtasks` | `key` | Get all subtasks of a specific Jira ticket using jql |
| `jira_get_ticket` | `fields`, `key` | Get a specific Jira ticket by key with optional field filtering |
| `jira_get_transitions` | `key` | Get all available transitions(statuses, workflows) for a Jira ticket |
| `jira_get_user_profile` | `userId` | Get a specific user's profile information from Jira |
| `jira_link_issues` | `sourceKey`, `relationship`, `anotherKey` | Link two Jira issues with a specific relationship type |
| `jira_move_to_status` | `statusName`, `key` | Move a Jira ticket to a specific status (workflow, transition) |
| `jira_move_to_status_with_resolution` | `statusName`, `resolution`, `key` | Move a Jira ticket to a specific status (workflow, transition) with resolution |
| `jira_post_comment` | `key`, `comment` | Post a comment to a Jira ticket |
| `jira_post_comment_if_not_exists` | `key`, `comment` | Post a comment to a Jira ticket only if it doesn't already exist |
| `jira_remove_fix_version` | `fixVersion`, `key` | Remove a fix version from a Jira ticket |
| `jira_search_by_jql` | `fields`, `searchQueryJQL` | Search for Jira tickets using JQL and returns all results |
| `jira_search_by_page` | `jql`, `nextPageToken`, `fields` | Search for Jira tickets using JQL with paging support |
| `jira_search_with_pagination` | `jql`, `fields`, `startAt` | Search for Jira tickets using JQL with pagination support |
| `jira_set_fix_version` | `fixVersion`, `key` | Set the fix version for a Jira ticket |
| `jira_set_priority` | `priority`, `key` | Set the priority for a Jira ticket |
| `jira_update_description` | `description`, `key` | Update the description of a Jira ticket |
| `jira_update_field` | `field`, `value`, `key` | Update field(s) in a ticket. Using field name updates ALL matching fields, using customfield_ID updates only that field |
| `jira_update_all_fields_with_name` | `key`, `fieldName`, `value` | Explicitly update ALL fields with the same name (same as jira_update_field with name) |
| `jira_get_all_fields_with_name` | `project`, `fieldName` | Get all custom field IDs that have the same display name |
| `jira_update_ticket` | `params`, `key` | Update a Jira ticket using JSON parameters following the standard Jira REST API format |
| `jira_update_ticket_parent` | `key`, `parentKey` | Update the parent of a Jira ticket |

### Jira Xray Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `jira_xray_add_precondition_to_test` | `preconditionKey`, `testKey` | Add a single precondition to a test |
| `jira_xray_add_preconditions_to_test` | `preconditionKeys`, `testKey` | Add multiple preconditions to a test |
| `jira_xray_add_test_step` | `testKey`, `step` | Add a single test step to a test |
| `jira_xray_add_test_steps` | `testKey`, `steps` | Add multiple test steps to a test |
| `jira_xray_create_precondition` | `project`, `summary`, `definition` | Create a new precondition in Xray |
| `jira_xray_get_precondition_details` | `preconditionKey` | Get detailed information about a specific precondition |
| `jira_xray_get_preconditions` | `project` | Get all preconditions for a project |
| `jira_xray_get_test_details` | `testKey` | Get detailed information about a specific test |
| `jira_xray_get_test_steps` | `testKey` | Get all test steps for a specific test |
| `jira_xray_search_tickets` | `jql`, `fields` | Search for Xray-related tickets using JQL |

### Confluence Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `confluence_contents_by_urls` | `urlStrings` | Get Confluence content by multiple URLs |
| `confluence_search_content_by_text` | `query`, `limit` | Search content by text query using CQL |
| `confluence_content_by_id` | `contentId` | Get content by its unique content ID |
| `confluence_content_by_title_and_space` | `title`, `space` | Get content by title and space key |
| `confluence_content_by_title` | `title` | Get content by title in the default space |
| `confluence_get_current_user_profile` | - | Get the current user's profile information |
| `confluence_get_user_profile_by_id` | `userId` | Get a specific user's profile by user ID |
| `confluence_get_content_attachments` | `contentId` | Get all attachments for a specific content |
| `confluence_find_content_by_title_and_space` | `title`, `space` | Find content by title and space key |
| `confluence_find_content` | `title` | Find content by title in the default space |
| `confluence_find_or_create` | `title`, `body`, `parentId` | Find content by title or create it if not found |
| `confluence_create_page` | `title`, `parentId`, `body`, `space` | Create a new page with title, parent, body, and space |
| `confluence_update_page` | `contentId`, `title`, `body`, `parentId`, `space` | Update an existing page |
| `confluence_update_page_with_history` | `contentId`, `title`, `body`, `parentId`, `space`, `historyComment` | Update page and add history comment |
| `confluence_get_children_by_id` | `contentId` | Get child pages by content ID |
| `confluence_get_children_by_name` | `spaceKey`, `contentName` | Get child pages by space key and content name |
| `confluence_download_attachment` | `href` | Download attachment by URL |

### ADO Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `ado_get_work_item` | `id` | Get work item details by ID |
| `ado_get_work_items` | `ids` | Get multiple work items by IDs |
| `ado_create_work_item` | `project`, `type`, `title`, `description` | Create a new work item |
| `ado_update_work_item` | `id`, `fields` | Update work item fields |
| `ado_delete_work_item` | `id` | Delete a work item |
| `ado_get_work_item_revisions` | `id` | Get revision history of a work item |
| `ado_add_comment` | `id`, `comment` | Add a comment to a work item |
| `ado_get_comments` | `id` | Get all comments for a work item |
| `ado_assign_work_item` | `id`, `email` | Assign work item to a user by email |
| `ado_move_to_state` | `id`, `state` | Move work item to a specific state |
| `ado_add_tag` | `id`, `tag` | Add a tag to a work item |
| `ado_remove_tag` | `id`, `tag` | Remove a tag from a work item |
| `ado_get_work_item_types` | `project` | Get available work item types for a project |
| `ado_get_states` | `project`, `type` | Get available states for a work item type |
| `ado_query_work_items` | `query` | Execute a work item query (WIQL) |
| `ado_get_attachment` | `id` | Download work item attachment |
| `ado_add_attachment` | `id`, `filePath`, `comment` | Add attachment to work item |
| `ado_get_work_item_links` | `id` | Get work item links and relationships |
| `ado_add_link` | `sourceId`, `targetId`, `linkType` | Add link between work items |
| `ado_remove_link` | `sourceId`, `targetId`, `linkType` | Remove link between work items |
| `ado_get_iteration` | `project`, `iteration` | Get iteration details |
| `ado_get_area` | `project`, `area` | Get area details |
| `ado_set_work_item_parent` | `id`, `parentId` | Set parent work item |
| `ado_get_work_item_parent` | `id` | Get parent work item |
| `ado_get_work_item_children` | `id` | Get child work items |

### Figma Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `figma_download_image_as_file` | `format`, `nodeId`, `href` | Download image as file by node ID and format. Use this after figma_get_icons to download actual icon files. |
| `figma_download_image_of_file` | `href` | Download image by URL as File type. Converts Figma design URL to downloadable image file. |
| `figma_download_node_image` | `format`, `scale`, `href`, `nodeId` | Download image of specific node/component. Useful for visual preview of design pieces before processing structure. |
| `figma_get_icons` | `href` | Find and extract all exportable visual elements (vectors, shapes, graphics, text) from Figma design by URL. |
| `figma_get_layers` | `href` | Get first-level layers (direct children) to understand structure. Returns layer names, IDs, types, sizes. Essential first step before getting details. |
| `figma_get_layers_batch` | `nodeIds`, `href` | Get layers for multiple nodes at once. More efficient for analyzing multiple screens/containers. Returns map of nodeId to layers. |
| `figma_get_node_children` | `href` | Get immediate children IDs and basic info for a node. Non-recursive, returns only direct children. |
| `figma_get_node_details` | `nodeIds`, `href` | Get detailed properties for specific node(s) including colors, fonts, text, dimensions, and styles. Returns small focused response. |
| `figma_get_screen_source` | `url` | Get screen source content by URL. Returns the image URL for the specified Figma design node. |
| `figma_get_styles` | `href` | Get design tokens (colors, text styles) defined in Figma file. |
| `figma_get_svg_content` | `nodeId`, `href` | Get SVG content as text by node ID. Use this after figma_get_icons to get SVG code for vector icons. |
| `figma_get_text_content` | `nodeIds`, `href` | Extract text content from text nodes. Returns map of nodeId to text content. |

### Microsoft Teams Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `teams_chat_by_name_raw` | `chatName` | Find a chat by topic or participant name and return raw JSON |
| `teams_chats` | `limit` | List chats showing only chat/contact names, last message (truncated to 100 chars), and date |
| `teams_chats_raw` | `limit` | List chats for the current user with topic, type, and participant information (returns raw JSON) |
| `teams_download_file` | `fileUrl`, `fileName` | Download a file from Teams/SharePoint by URL |
| `teams_download_recording_transcript` | `recordingUrl` | Download transcript for a Teams meeting recording |
| `teams_extract_transcript_from_sharepoint` | `sharepointUrl` | Extract transcript content from SharePoint URL |
| `teams_find_channel_by_name_raw` | `teamName`, `channelName` | Find a channel by team name and channel name, return raw JSON |
| `teams_find_team_by_name_raw` | `teamName` | Find a team by name and return raw JSON |
| `teams_get_call_transcripts` | `callId` | Get transcripts for a specific Teams call |
| `teams_get_channel_messages_by_name_raw` | `teamName`, `channelName`, `limit` | Get messages from a channel by team and channel name, return raw JSON |
| `teams_get_joined_teams_raw` | - | Get all teams the user has joined, return raw JSON |
| `teams_get_message_hosted_contents` | `messageId`, `chatId` | Get hosted content (files, images) from a specific message |
| `teams_get_recording_transcripts` | `recordingId` | Get transcripts for a meeting recording |
| `teams_get_team_channels_raw` | `teamId` | Get all channels for a specific team, return raw JSON |
| `teams_list_recording_transcripts` | - | List all available meeting recording transcripts |
| `teams_messages` | `limit` | Get recent messages from all chats showing sender, content, and date |
| `teams_messages_by_chat_id_raw` | `chatId`, `limit`, `filter` | Get messages from a chat by ID with optional server-side filtering. Use $filter syntax with lastModifiedDateTime |
| `teams_messages_raw` | `limit` | Get recent messages from all chats, return raw JSON |
| `teams_messages_since` | `sinceDate`, `limit` | Get messages since a specific date across all chats |
| `teams_messages_since_by_id` | `chatId`, `sinceDate`, `limit` | Get messages since a specific date from a specific chat |
| `teams_myself_messages` | `limit` | Get messages sent by the current user across all chats |
| `teams_myself_messages_raw` | `limit` | Get messages sent by the current user across all chats, return raw JSON |
| `teams_recent_chats` | `limit`, `chatType` | Get recent chats sorted by last activity showing chat/contact names, last message with author, and date |
| `teams_search_user_drive_files` | `query`, `limit` | Search for files in the user's OneDrive |
| `teams_send_message` | `message`, `chatName` | Send a message to a chat by participant name or topic |
| `teams_send_message_by_id` | `message`, `chatId` | Send a message to a chat by ID |
| `teams_send_myself_message` | `message` | Send a message to yourself (personal notes) |

### TestRail Tools

TestRail integration for test management. Configure via `TESTRAIL_BASE_PATH`, `TESTRAIL_USERNAME`, and `TESTRAIL_API_KEY` environment variables.

| Tool | Parameters | Description |
|------|------------|-------------|
| `testrail_get_projects` | - | Get list of all projects in TestRail instance |
| `testrail_get_case` | `case_id` | Get a test case by ID (numeric, without 'C' prefix) |
| `testrail_get_all_cases` | `project_name` | Get ALL test cases in a project (uses automatic pagination to retrieve all cases) |
| `testrail_search_cases` | `project_name`, `suite_id`, `section_id` | Search test cases by project and optional filters. Both suite_id and section_id are optional |
| `testrail_get_cases_by_refs` | `refs`, `project_name` | Get test cases linked to a requirement/story via refs field (e.g., JIRA ticket key) |
| `testrail_create_case` | `project_name`, `title`, `description`, `priority_id`, `refs` | Create a basic test case. description saved to preconditions field. priority_id (1=Low, 2=Medium, 3=High, 4=Critical) and refs are optional |
| `testrail_create_case_detailed` | `project_name`, `title`, `preconditions`, `steps`, `expected`, `priority_id`, `type_id`, `refs`, `label_ids` | Create test case using **Text template** (template_id=1). Single text fields for steps and expected. Markdown tables auto-converted to TestRail `|||` format. |
| `testrail_create_case_steps` | `project_name`, `title`, `steps_json`, `preconditions`, `priority_id`, `type_id`, `refs`, `label_ids` | Create test case using **Steps template** (template_id=2). `steps_json` is a JSON array `[{"content":"step","expected":"result"},...]`. Markdown tables in content/expected auto-converted to HTML. |
| `testrail_update_case` | `case_id`, `title`, `priority_id`, `refs` | Update a test case. All parameters except case_id are optional |
| `testrail_link_to_requirement` | `case_id`, `requirement_key` | Link a test case to a requirement by adding requirement key to refs field |
| `testrail_get_labels` | `project_name` | Get all labels for a project (returns array of {id, title, created_by, created_on}) |
| `testrail_get_label` | `label_id` | Get a single label by ID |
| `testrail_update_label` | `label_id`, `project_name`, `title` | Update a label title (max 20 characters) |
| `testrail_get_case_types` | - | Get all available case types (e.g., Automated, Functionality, Other) |
| `testrail_delete_case` | `case_id` | Delete a test case by ID (numeric, without 'C' prefix) |

**Example Usage:**
```bash
# Get list of all projects
dmtools testrail_get_projects

# Get a test case
dmtools testrail_get_case 123

# Get ALL test cases in a project (with automatic pagination)
dmtools testrail_get_all_cases "My Project"

# Search test cases in a project
dmtools testrail_search_cases "My Project"

# Find test cases linked to a Jira ticket
dmtools testrail_get_cases_by_refs "PROJ-456" "My Project"

# Create a basic test case
dmtools testrail_create_case "My Project" "Verify login" "User has valid credentials" 3 "PROJ-456"

# Create a detailed test case with type_id and labels
dmtools testrail_create_case_detailed --data '{
  "project_name": "My Project",
  "title": "Update User Profile",
  "preconditions": "User is logged in",
  "steps": "1. Navigate to profile page\n\n2. Click Edit\n\n3. Update username\n\n4. Click Save",
  "expected": "Profile updated with success message",
  "priority_id": "3",
  "type_id": "1",
  "refs": "PROJ-789",
  "label_ids": "7,8"
}'

# Link test case to a requirement
dmtools testrail_link_to_requirement 123 "PROJ-789"

# Get all labels for a project
dmtools testrail_get_labels "My Project"

# Get a single label
dmtools testrail_get_label 7

# Update a label title
dmtools testrail_update_label --data '{"label_id": "7", "project_name": "My Project", "title": "Release 2.0"}'

# Get available case types
dmtools testrail_get_case_types
```

**Table Format Note:**

TestRail uses its own table format, not standard Markdown tables. When using `testrail_create_case_detailed`, standard Markdown tables are automatically converted to TestRail format:

```
Markdown (input):                    TestRail (auto-converted):
| Col 1 | Col 2 | Col 3 |          |||:Col 1|:Col 2|:Col 3
|-------|-------|-------|           ||val1|val2|val3
| val1  | val2  | val3  |          ||val4|val5|val6
| val4  | val5  | val6  |
```

You can also write in TestRail format directly — it will be passed through as-is.

**JavaScript Access:**

All TestRail MCP tools are available as direct function calls in JavaScript agents via GraalJS integration.

```javascript
/**
 * Example JavaScript agent using TestRail MCP tools
 */
function action(params) {
    // 1. Get list of all projects
    const projectsJson = testrail_get_projects();
    const projects = JSON.parse(projectsJson);
    console.log(`Found ${projects.length} projects`);

    // 2. Get specific test case
    const testCaseJson = testrail_get_case('123');
    const testCase = JSON.parse(testCaseJson);
    console.log(`Test case: ${testCase.title}`);

    // 3. Get ALL test cases in a project (with automatic pagination)
    const allCasesJson = testrail_get_all_cases('My Project');
    const allCases = JSON.parse(allCasesJson);
    console.log(`Found ${allCases.length} total test cases in project`);

    // 4. Search test cases in a project
    const casesJson = testrail_search_cases('My Project');
    const cases = JSON.parse(casesJson);
    console.log(`Found ${cases.length} test cases`);

    // 4. Find test cases linked to JIRA ticket
    const linkedCasesJson = testrail_get_cases_by_refs('PROJ-456', 'My Project');
    const linkedCases = JSON.parse(linkedCasesJson);

    // 5. Create new test case
    const newCaseJson = testrail_create_case(
        'My Project',
        'Verify user logout',
        'Steps:\n1. Click logout button\n2. Verify redirect to login page',
        '3',  // High priority
        'PROJ-456'
    );
    const newCase = JSON.parse(newCaseJson);
    console.log(`Created test case C${newCase.id}`);

    // 6. Update test case
    testrail_update_case('123', 'Updated title', '4', 'PROJ-789');

    // 7. Link test case to requirement
    testrail_link_to_requirement('123', 'PROJ-999');

    return {
        success: true,
        projectsCount: projects.length,
        testCasesCount: cases.length
    };
}
```

**Running TestRail JavaScript Agent:**
```bash
# Create job configuration
cat > testrail_job.json << 'EOF'
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/testrail_example.js",
    "jobParams": {
      "projectName": "My Project",
      "caseId": "123",
      "jiraKey": "PROJ-456"
    }
  }
}
EOF

# Execute the job
dmtools run testrail_job.json
```

See `examples/testrail_example.js` for a complete working example.

### File Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `file_read` | `targetFile`, `offset`, `limit` | Read content from a file with optional offset and limit |
| `file_validate_json` | `jsonContent` | Validate JSON content and return formatted result |
| `file_validate_json_file` | `filePath` | Validate JSON file and return formatted result |
| `file_write` | `filePath`, `contents` | Write content to a file |

### CLI Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `cli_execute_command` | `workingDirectory`, `command` | Execute CLI commands (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az) |

### AI Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `ollama_ai_chat` | `message` | Send a text message to Ollama AI and get response |
| `ollama_ai_chat_with_files` | `message`, `filePaths` | Send a text message to Ollama AI with file attachments |
| `anthropic_ai_chat` | `message` | Send a text message to Anthropic Claude AI and get response |
| `anthropic_ai_chat_with_files` | `message`, `filePaths` | Send a text message to Anthropic Claude AI with file attachments |
| `gemini_ai_chat` | `message` | Send a text message to Gemini AI and get response |
| `gemini_ai_chat_with_files` | `message`, `filePaths` | Send a text message to Gemini AI with file attachments |
| `dial_ai_chat` | `message` | Send a text message to Dial AI and get response |
| `dial_ai_chat_with_files` | `message`, `filePaths` | Send a text message to Dial AI with file attachments |
| `bedrock_ai_chat` | `message` | Send a text message to AWS Bedrock AI and get response |
| `bedrock_ai_chat_with_files` | `message`, `filePaths` | Send a text message to AWS Bedrock AI with file attachments |
| `openai_ai_chat` | `message` | Send a text message to OpenAI and get response |
| `openai_ai_chat_with_files` | `message`, `filePaths` | Send a text message to OpenAI with file attachments (images for vision models) |
| `vertex_ai_gemini_chat` | `message` | Send a text message to Google Vertex AI Gemini (service account auth) |
| `vertex_ai_gemini_chat_with_files` | `message`, `filePaths` | Send message with file attachments to Vertex AI Gemini |

**Note**: AI tools require proper API key configuration to be available. If you don't see these tools in `dmtools list`, ensure the appropriate environment variables are set (OLLAMA_BASE_PATH, ANTHROPIC_API_KEY, GEMINI_API_KEY, DIAL_API_KEY, OPENAI_API_KEY, AWS credentials, etc.). For Vertex AI Gemini, set GEMINI_VERTEX_ENABLED=true and provide either GEMINI_VERTEX_CREDENTIALS_PATH or GEMINI_VERTEX_CREDENTIALS_JSON along with GEMINI_VERTEX_PROJECT_ID and GEMINI_VERTEX_LOCATION.

## 💻 Input Methods

DMTools supports multiple input methods with this priority:

### 1. Positional Arguments (Simplest)
```bash
dmtools jira_get_ticket PROJ-123 summary,description,status
```

### 2. JSON Data Flag
```bash
dmtools jira_get_ticket --data '{"key": "PROJ-123", "fields": ["summary"]}'
```

### 3. File Input
```bash
dmtools jira_get_ticket --file ticket-params.json
```

### 4. Heredoc (Multi-line JSON)
```bash
dmtools jira_create_ticket_basic <<EOF
{
  "project": "PROJ",
  "issueType": "Task",
  "summary": "New task",
  "description": "Task description"
}
EOF
```

### 5. STDIN/Pipe
```bash
echo '{"key": "PROJ-123"}' | dmtools jira_get_ticket
```

## 📤 Output Format

### Clean JSON Output (Default)
```json
{
  "key": "PROJ-123",
  "fields": {
    "summary": "Hello World",
    "status": {"name": "Backlog"}
  }
}
```

### Verbose Debug Output
```bash
dmtools jira_get_ticket PROJ-123 --verbose
```

### Error Response
```json
{
  "error": true,
  "message": "Ticket not found: PROJ-123"
}
```

## 🔧 Advanced Usage

### Shell Scripting Integration
```bash
#!/bin/bash
TICKET_KEY="PROJ-123"
TICKET_DATA=$(dmtools jira_get_ticket "$TICKET_KEY")
STATUS=$(echo "$TICKET_DATA" | jq -r '.fields.status.name')
echo "Ticket status: $STATUS"
```

### Python Integration
```python
import subprocess
import json

def get_jira_ticket(key):
    result = subprocess.run([
        'dmtools', 'jira_get_ticket',
        '--data', json.dumps({"key": key})
    ], capture_output=True, text=True)

    return json.loads(result.stdout)

ticket = get_jira_ticket("PROJ-123")
print(f"Status: {ticket['fields']['status']['name']}")
```

### CI/CD Pipeline Integration
```yaml
- name: Get Jira Ticket
  run: |
    TICKET_JSON=$(dmtools jira_get_ticket "${{ env.TICKET_KEY }}")
    echo "ticket-data=$TICKET_JSON" >> $GITHUB_OUTPUT
```

## 🐛 Troubleshooting

### Common Issues

**Command not found**
```bash
# Ensure dmtools is in PATH
echo $PATH | grep dmtools
source ~/.zshrc  # or ~/.bashrc
```

**Authentication errors**
```bash
# Check environment variables
echo $JIRA_BASE_PATH
echo $JIRA_API_TOKEN
echo $JIRA_EMAIL
```

**Missing AI tools**
```bash
# AI tools require API keys to be configured
echo $OLLAMA_BASE_PATH
echo $ANTHROPIC_API_KEY
echo $GEMINI_API_KEY
echo $DIAL_API_KEY
# For AWS Bedrock:
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
```

**Invalid JSON**
```bash
# Validate JSON using jq
echo '{"key": "PROJ-123"}' | jq '.'
```

### Debug Mode
Enable verbose logging for troubleshooting:
```bash
dmtools jira_get_ticket PROJ-123 --verbose
```

## 📚 Complete Documentation

For detailed usage examples, parameter descriptions, and advanced features, see:
- **[MCP Tools Reference](cli-usage/mcp-tools.md)** - Complete guide with all MCP tools
- **[First Steps](getting-started/first-steps.md)** - Getting started guide
- **[Configuration](getting-started/configuration.md)** - Environment setup
- **[Figma MCP Integration](mcp/figma_mcp.md)** - Figma-specific usage

## 🚀 Performance & Security

- **Cold start**: ~2-3 seconds (JVM startup)
- **Warm execution**: ~500ms-1s (cached connections)
- **Memory usage**: ~200-500MB
- **Security**: API tokens via environment variables, TLS encryption, local caching

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/IstiN/dmtools/issues)
- **Discussions**: [GitHub Discussions](https://github.com/IstiN/dmtools/discussions)
- **Documentation**: [Complete Docs](docs/README.md)
