# CLI DMTools MCP Methods

DMTools provides a comprehensive CLI interface for Model Context Protocol (MCP) tools, enabling seamless interaction with Jira, Confluence, Figma, and AI services through command-line operations.

## 🚀 First Commands
```bash
# List all available MCP tools (105+ total)
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

## 📋 Available MCP Tools

### Jira Tools (45 tools)

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
| `jira_update_field` | `field`, `value`, `key` | Update a specific field value in a Jira ticket |
| `jira_update_ticket` | `params`, `key` | Update a Jira ticket using JSON parameters following the standard Jira REST API format |
| `jira_update_ticket_parent` | `key`, `parentKey` | Update the parent of a Jira ticket |

### Jira Xray Tools (10 tools)

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

### Figma Tools (12 tools)

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

### Microsoft Teams Tools (25 tools)

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

### File Tools (3 tools)

| Tool | Parameters | Description |
|------|------------|-------------|
| `file_read` | `targetFile`, `offset`, `limit` | Read content from a file with optional offset and limit |
| `file_validate_json` | `jsonContent` | Validate JSON content and return formatted result |
| `file_validate_json_file` | `filePath` | Validate JSON file and return formatted result |
| `file_write` | `filePath`, `contents` | Write content to a file |

### CLI Tools (1 tool)

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
| `bedrock_ai_chat` | `message` | Send a text message to AWS Bedrock AI and get response |
| `bedrock_ai_chat_with_files` | `message`, `filePaths` | Send a text message to AWS Bedrock AI with file attachments |

**Note**: AI tools require proper API key configuration to be available. If you don't see these tools in `dmtools list`, ensure the appropriate environment variables are set (OLLAMA_BASE_PATH, ANTHROPIC_API_KEY, GEMINI_API_KEY, DIAL_API_KEY, AWS credentials, etc.).

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
