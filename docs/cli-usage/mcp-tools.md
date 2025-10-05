# MCP Tools Reference

DMTools provides 67 built-in MCP (Model Context Protocol) tools for Jira, Confluence, Figma, and AI integrations.

## What are MCP Tools?

MCP tools are command-line accessible functions that allow you to interact with various integrations directly from the CLI. Each tool accepts JSON input and returns JSON output.

## Architecture

```
CLI Input → dmtools → JobRunner → McpCliHandler → MCPToolExecutor → JSON Output
```

**Core Components:**
1. **McpCliHandler** - Handles MCP command parsing and execution
2. **JobRunner** - Extended to support MCP CLI commands
3. **dmtools** command - User-friendly CLI wrapper
4. **Fat JAR** - Self-contained executable with all dependencies

---

## List Available Tools

```bash
# See all 67 tools
dmtools list

# Output shows tools grouped by integration:
# - Jira (45 tools)
# - Confluence (16 tools)
# - Figma (6 tools)
```

---

## Input Methods

DMTools CLI supports multiple input methods with the following priority:

### 1. Positional Arguments (Simplest)
```bash
dmtools jira_get_ticket PROJ-123
dmtools jira_get_ticket PROJ-123 summary,description,status
```

### 2. JSON Data Flag
```bash
dmtools jira_get_ticket --data '{"key": "PROJ-123", "fields": ["summary"]}'
```

### 3. File Input
```bash
cat > params.json << EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}
EOF

dmtools jira_get_ticket --file params.json
```

### 4. Heredoc (Multi-line JSON)
```bash
dmtools jira_get_ticket <<EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}
EOF
```

### 5. STDIN/Pipe
```bash
echo '{"key": "PROJ-123"}' | dmtools jira_get_ticket

# Or from command output
cat ticket.json | dmtools jira_get_ticket
```

---

## Output Format

### Clean JSON Output (Default)
```bash
dmtools jira_get_ticket PROJ-123
# Returns:
{"key":"PROJ-123","fields":{"summary":"Hello World","status":{"name":"Backlog"}}}
```

### Verbose Debug Output
```bash
dmtools jira_get_ticket PROJ-123 --verbose
# Shows logs + result
```

### Error Response
```json
{
  "error": true,
  "message": "Ticket not found: PROJ-123"
}
```

---

## Jira Tools (45 tools)

### Ticket Management

#### `jira_get_ticket`
Get ticket details with optional field filtering.

**Parameters:**
- `key` (required): Ticket key
- `fields` (optional): Array of fields to return

**Examples:**
```bash
# Get all fields
dmtools jira_get_ticket PROJ-123

# Get specific fields
dmtools jira_get_ticket PROJ-123 summary,description,status

# With JSON
dmtools jira_get_ticket --data '{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status", "assignee"]
}'
```

#### `jira_create_ticket_basic`
Create a new ticket with basic fields.

**Parameters:**
- `project`: Project key
- `issueType`: Issue type (e.g., "Task", "Bug", "Story")
- `summary`: Ticket title
- `description`: Ticket description

**Example:**
```bash
dmtools jira_create_ticket_basic \
  PROJ \
  Task \
  "Fix login bug" \
  "Users cannot log in with special characters in password"
```

#### `jira_create_ticket_with_json`
Create ticket with custom fields using JSON.

**Example:**
```bash
dmtools jira_create_ticket_with_json <<EOF
{
  "project": "PROJ",
  "fieldsJson": {
    "summary": "Implement new feature",
    "description": "Feature description",
    "issuetype": {"name": "Story"},
    "priority": {"name": "High"},
    "labels": ["feature", "high-priority"]
  }
}
EOF
```

#### `jira_update_ticket`
Update ticket using JSON parameters.

**Example:**
```bash
dmtools jira_update_ticket <<EOF
{
  "key": "PROJ-123",
  "params": {
    "fields": {
      "assignee": {"id": "user-account-id"},
      "labels": ["updated", "reviewed"]
    }
  }
}
EOF
```

#### `jira_delete_ticket`
Delete a ticket (cannot be undone).

**Example:**
```bash
dmtools jira_delete_ticket PROJ-123
```

### Search & Query

#### `jira_search_by_jql`
Search tickets using JQL and return all results.

**Parameters:**
- `jql`: JQL query string
- `fields`: Array of fields to return

**Examples:**
```bash
# Simple search
dmtools jira_search_by_jql \
  "project = PROJ AND status = Open" \
  "summary,status,assignee"

# Complex search with JSON
dmtools jira_search_by_jql <<EOF
{
  "searchQueryJQL": "project = PROJ AND created >= -7d ORDER BY created DESC",
  "fields": ["summary", "status", "created", "assignee", "priority"]
}
EOF
```

#### `jira_search_by_page`
Search with pagination support using nextPageToken.

**Example:**
```bash
dmtools jira_search_by_page <<EOF
{
  "jql": "project = PROJ AND status = Open",
  "nextPageToken": "",
  "fields": ["summary", "status"]
}
EOF
```

### Comments

#### `jira_post_comment`
Add a comment to a ticket.

**Example:**
```bash
# Simple comment
dmtools jira_post_comment PROJ-123 "This is a test comment"

# With Jira markup
dmtools jira_post_comment PROJ-123 "h2. Status Update

*Progress:* Implementation completed
*Next steps:* Code review

{code:java}
function example() {
  return true;
}
{code}"
```

#### `jira_post_comment_if_not_exists`
Post comment only if it doesn't already exist.

**Example:**
```bash
dmtools jira_post_comment_if_not_exists <<EOF
{
  "key": "PROJ-123",
  "comment": "Automated weekly reminder"
}
EOF
```

#### `jira_get_comments`
Get all comments for a ticket.

**Example:**
```bash
dmtools jira_get_comments PROJ-123
```

### Fields & Properties

#### `jira_get_fields`
Get all available fields for a project.

**Example:**
```bash
dmtools jira_get_fields PROJ
```

#### `jira_get_field_custom_code`
Get custom field code for a human-readable field name.

**Example:**
```bash
dmtools jira_get_field_custom_code <<EOF
{
  "project": "PROJ",
  "fieldName": "Story Points"
}
EOF
```

#### `jira_clear_field`
Clear (delete) a field value in a ticket.

**Example:**
```bash
dmtools jira_clear_field <<EOF
{
  "key": "PROJ-123",
  "field": "customfield_10001"
}
EOF
```

### Workflow & Status

#### `jira_get_transitions`
Get all available transitions (statuses) for a ticket.

**Example:**
```bash
dmtools jira_get_transitions PROJ-123
```

#### `jira_move_to_status`
Move ticket to a specific status.

**Example:**
```bash
dmtools jira_move_to_status <<EOF
{
  "key": "PROJ-123",
  "statusName": "In Review"
}
EOF
```

#### `jira_move_to_status_with_resolution`
Move to status and set resolution.

**Example:**
```bash
dmtools jira_move_to_status_with_resolution <<EOF
{
  "key": "PROJ-123",
  "statusName": "Done",
  "resolution": "Fixed"
}
EOF
```

### Versions

#### `jira_get_fix_versions`
Get all fix versions for a project.

**Example:**
```bash
dmtools jira_get_fix_versions PROJ
```

#### `jira_set_fix_version`
Set fix version (replaces existing).

**Example:**
```bash
dmtools jira_set_fix_version <<EOF
{
  "key": "PROJ-123",
  "fixVersion": "1.0.0"
}
EOF
```

#### `jira_add_fix_version`
Add fix version without removing existing ones.

**Example:**
```bash
dmtools jira_add_fix_version <<EOF
{
  "key": "PROJ-123",
  "fixVersion": "1.1.0"
}
EOF
```

#### `jira_remove_fix_version`
Remove a fix version.

**Example:**
```bash
dmtools jira_remove_fix_version <<EOF
{
  "key": "PROJ-123",
  "fixVersion": "1.0.0"
}
EOF
```

### Issue Links

#### `jira_get_issue_link_types`
Get all available issue link types.

**Example:**
```bash
dmtools jira_get_issue_link_types
```

#### `jira_link_issues`
Link two issues with a relationship type.

**Example:**
```bash
dmtools jira_link_issues <<EOF
{
  "sourceKey": "PROJ-123",
  "anotherKey": "PROJ-456",
  "relationship": "blocks"
}
EOF
```

### User Management

#### `jira_get_my_profile`
Get the current user's profile.

**Example:**
```bash
dmtools jira_get_my_profile
```

#### `jira_assign_ticket_to`
Assign ticket to a user.

**Example:**
```bash
dmtools jira_assign_ticket_to <<EOF
{
  "key": "PROJ-123",
  "userName": "user@company.com"
}
EOF
```

### Components & Priority

#### `jira_get_components`
Get all components for a project.

**Example:**
```bash
dmtools jira_get_components PROJ
```

#### `jira_set_priority`
Set ticket priority.

**Example:**
```bash
dmtools jira_set_priority <<EOF
{
  "key": "PROJ-123",
  "priority": "High"
}
EOF
```

### Advanced

#### `jira_get_subtasks`
Get all subtasks of a parent ticket.

**Example:**
```bash
dmtools jira_get_subtasks PROJ-123
```

#### `jira_download_attachment`
Download an attachment by URL.

**Example:**
```bash
dmtools jira_download_attachment <<EOF
{
  "href": "https://company.atlassian.net/attachments/12345/file.pdf"
}
EOF
```

---

## Confluence Tools (16 tools)

### Content Retrieval

#### `confluence_content_by_id`
Get page content by ID.

**Example:**
```bash
dmtools confluence_content_by_id 123456
```

#### `confluence_content_by_title`
Get page by title (in default space).

**Example:**
```bash
dmtools confluence_content_by_title "API Documentation"
```

#### `confluence_content_by_title_and_space`
Get page by title in specific space.

**Example:**
```bash
dmtools confluence_content_by_title_and_space \
  "Architecture Overview" \
  "TECH"
```

### Search

#### `confluence_search_content_by_text`
Search content using text query.

**Parameters:**
- `query`: Search query text
- `limit`: Maximum number of results

**Example:**
```bash
dmtools confluence_search_content_by_text "API documentation" 10
```

### Page Management

#### `confluence_create_page`
Create a new page.

**Example:**
```bash
dmtools confluence_create_page <<EOF
{
  "title": "New Documentation Page",
  "parentId": "123456",
  "body": "<p>This is the page content in Confluence storage format.</p>",
  "space": "TECH"
}
EOF
```

#### `confluence_update_page`
Update an existing page.

**Example:**
```bash
dmtools confluence_update_page <<EOF
{
  "contentId": "789012",
  "title": "Updated Title",
  "parentId": "123456",
  "body": "<p>Updated content here.</p>",
  "space": "TECH"
}
EOF
```

#### `confluence_update_page_with_history`
Update page and add history comment.

**Example:**
```bash
dmtools confluence_update_page_with_history <<EOF
{
  "contentId": "789012",
  "title": "API Documentation",
  "parentId": "123456",
  "body": "<p>Updated API docs</p>",
  "space": "TECH",
  "historyComment": "Added new endpoints documentation"
}
EOF
```

### Navigation

#### `confluence_get_children_by_id`
Get child pages by content ID.

**Example:**
```bash
dmtools confluence_get_children_by_id 123456
```

#### `confluence_get_children_by_name`
Get child pages by parent page name.

**Example:**
```bash
dmtools confluence_get_children_by_name <<EOF
{
  "spaceKey": "TECH",
  "contentName": "Parent Page Name"
}
EOF
```

### Attachments

#### `confluence_get_content_attachments`
Get all attachments for a page.

**Example:**
```bash
dmtools confluence_get_content_attachments 123456
```

### User Profile

#### `confluence_get_current_user_profile`
Get current user's profile.

**Example:**
```bash
dmtools confluence_get_current_user_profile
```

---

## Figma Tools (6 tools)

### File Structure

#### `figma_get_file_structure`
Get JSON structure of a Figma file.

**Example:**
```bash
dmtools figma_get_file_structure \
  "https://www.figma.com/file/abc123/MyDesign"
```

#### `figma_get_screen_source`
Get screen/image URL for a design node.

**Example:**
```bash
dmtools figma_get_screen_source \
  "https://www.figma.com/file/abc123/Design?node-id=1%3A2"
```

### Icons & Graphics

#### `figma_get_icons`
Find and extract all exportable visual elements.

**Example:**
```bash
dmtools figma_get_icons \
  "https://www.figma.com/file/abc123/Design"
```

#### `figma_get_svg_content`
Get SVG content as text by node ID.

**Example:**
```bash
dmtools figma_get_svg_content <<EOF
{
  "href": "https://www.figma.com/file/abc123/Design",
  "nodeId": "123:456"
}
EOF
```

### Image Download

#### `figma_download_image_of_file`
Download entire design as image.

**Example:**
```bash
dmtools figma_download_image_of_file \
  "https://www.figma.com/file/abc123/Design?node-id=1%3A2"
```

#### `figma_download_image_as_file`
Download specific node as image file.

**Parameters:**
- `href`: Figma file URL
- `nodeId`: Node ID to export
- `format`: Export format (png, jpg, svg, pdf)

**Example:**
```bash
dmtools figma_download_image_as_file <<EOF
{
  "href": "https://www.figma.com/file/abc123/Design",
  "nodeId": "123:456",
  "format": "png"
}
EOF
```

---

## AI Tools

### Gemini AI

#### `gemini_ai_chat`
Chat with Google Gemini AI.

**Example:**
```bash
dmtools gemini_ai_chat <<EOF
{
  "message": "Explain the MCP architecture",
  "context": "DMTools project"
}
EOF
```

#### `gemini_ai_chat_with_files`
Chat with Gemini and attach files (supports images).

**Example:**
```bash
dmtools gemini_ai_chat_with_files <<EOF
{
  "message": "Analyze this design mockup",
  "filePaths": ["/path/to/design.png"]
}
EOF
```

---

## Troubleshooting

### Common Issues

**1. Command not found**
```bash
# Solution: Ensure dmtools is in PATH
echo $PATH | grep dmtools
source ~/.zshrc  # or ~/.bashrc
```

**2. Authentication errors**
```json
{"error": true, "message": "Unauthorized"}
```
**Solution:** Check environment variables:
```bash
echo $JIRA_BASE_PATH
echo $JIRA_API_TOKEN
```

**3. Invalid JSON**
```json
{"error": true, "message": "Invalid JSON format"}
```
**Solution:** Validate JSON using `jq`:
```bash
echo '{"key": "PROJ-123"}' | jq '.'
```

**4. Missing fields in response**
```bash
# Request specific fields
dmtools jira_get_ticket PROJ-123 summary,description,status
```

### Debug Mode

Enable verbose logging:
```bash
dmtools jira_get_ticket PROJ-123 --verbose
```

---

## Integration Examples

### Shell Scripting
```bash
#!/bin/bash
TICKET_KEY="PROJ-123"
TICKET_DATA=$(dmtools jira_get_ticket "$TICKET_KEY")
STATUS=$(echo "$TICKET_DATA" | jq -r '.fields.status.name')
echo "Ticket status: $STATUS"
```

### CI/CD Pipeline (GitHub Actions)
```yaml
- name: Get Jira Ticket
  run: |
    TICKET_JSON=$(dmtools jira_get_ticket "${{ env.TICKET_KEY }}")
    echo "ticket-data=$TICKET_JSON" >> $GITHUB_OUTPUT
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

---

## Performance

- **Cold start:** ~2-3 seconds (JVM startup)
- **Warm execution:** ~500ms-1s (cached connections)
- **Memory usage:** ~200-500MB

## Security

- ✅ API tokens passed via environment variables
- ✅ No sensitive data logged in quiet mode
- ✅ All HTTP requests use TLS encryption
- ✅ Local caching respects API rate limits

---

## See Also

- **[First Steps](../getting-started/first-steps.md)** - Getting started guide
- **[Jobs Overview](../jobs/README.md)** - Run automation jobs
- **[Configuration](../getting-started/configuration.md)** - Environment setup
- **[AI Teammate](../ai-teammate/README.md)** - Automation workflows
