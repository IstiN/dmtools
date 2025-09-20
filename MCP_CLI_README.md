# DMTools MCP CLI

This document describes the MCP (Multi-Cloud Platform) CLI support for DMTools, which allows executing MCP tools directly from the command line.

## Overview

The MCP CLI provides a command-line interface to execute DMTools MCP tools without running the full server. It supports various input methods and returns clean JSON responses suitable for automation and scripting.

## Architecture

### Core Components

1. **McpCliHandler** - Handles MCP command parsing and execution
2. **JobRunner** - Extended to support MCP CLI commands
3. **dmtools.sh** - User-friendly wrapper script (via installation)
4. **Fat JAR** - Self-contained executable with all dependencies

### Flow Diagram

```
CLI Input → dmtools → JobRunner → McpCliHandler → MCPToolExecutor → JSON Output
```

## Installation

### Prerequisites

- Java 17 or higher
- Gradle (for building)

### Building

```bash
# Build the fat JAR
./gradlew :dmtools-core:shadowJar

# The fat JAR will be created at:
# dmtools-core/build/libs/dmtools-v1.7.16-all.jar
```

## Usage

### Direct JAR Usage

```bash
# List available tools
java -cp dmtools-core/build/libs/dmtools-v1.7.16-all.jar com.github.istin.dmtools.job.JobRunner mcp list

# Execute a tool
java -cp dmtools-core/build/libs/dmtools-v1.7.16-all.jar com.github.istin.dmtools.job.JobRunner mcp jira_get_ticket --data '{"key": "PROJ-123"}'
```

### Wrapper Script Usage (Recommended)

The `dmtools_mcp.sh` wrapper script provides a more user-friendly interface:

```bash
# List available tools
./dmtools_mcp.sh list

# Execute with inline JSON (clean output by default)
./dmtools_mcp.sh jira_get_ticket --data '{"key": "PROJ-123"}'

# Execute with JSON file
./dmtools_mcp.sh jira_get_ticket --file params.json

# Execute with heredoc (multi-line JSON)
./dmtools_mcp.sh jira_get_ticket <<EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}
EOF

# Execute with verbose debug output
./dmtools_mcp.sh jira_get_ticket --data '{"key": "PROJ-123"}' --verbose

# Execute with positional arguments
./dmtools_mcp.sh jira_get_ticket PROJ-123

# Show help
./dmtools_mcp.sh help
```

## Input Methods

The CLI supports multiple input methods with the following priority:

### 1. STDIN/Heredoc (Highest Priority)
```bash
./dmtools_mcp.sh jira_get_ticket <<EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description"]
}
EOF
```

### 2. Inline JSON Data
```bash
./dmtools_mcp.sh jira_get_ticket --data '{"key": "PROJ-123"}'
```

### 3. JSON File
```bash
./dmtools_mcp.sh jira_get_ticket --file params.json
```

### 4. Positional Arguments (Lowest Priority)
```bash
./dmtools_mcp.sh jira_get_ticket PROJ-123
```

### 5. Key-Value Arguments
```bash
./dmtools_mcp.sh jira_get_ticket key=PROJ-123 fields=summary,description
```

## Output Format

All tools return clean JSON results by default. Use `--verbose` for debug output:

**Default Clean Output:**
```bash
./dmtools_mcp.sh jira_get_ticket --data '{"key": "MAPC-1"}'
# Returns only:
{"key":"MAPC-1","fields":{"summary":"Hello World","status":{"name":"Backlog"}}}
```

**Verbose Debug Output:**
```bash
./dmtools_mcp.sh jira_get_ticket --data '{"key": "MAPC-1"}' --verbose
# Shows debug logs + final result
```

All tools return results in JSON format:

### Successful Execution
```json
{
  "key": "PROJ-123",
  "fields": {
    "summary": "Example ticket",
    "status": {"name": "In Progress"}
  }
}
```

### Error Response
```json
{
  "error": true,
  "message": "Ticket not found: PROJ-123"
}
```

## Configuration

The CLI uses environment variables for configuration:

### Jira Integration
```bash
export JIRA_BASE_PATH="https://your-domain.atlassian.net"
export JIRA_TOKEN="your-api-token"
```

### Confluence Integration
```bash
export CONFLUENCE_BASE_PATH="https://your-domain.atlassian.net"
export CONFLUENCE_TOKEN="your-api-token"
```

### Figma Integration
```bash
export FIGMA_TOKEN="your-figma-token"
```

### AI Integration
```bash
export GEMINI_API_KEY="your-gemini-api-key"
```

## Available Tools

Use `./dmtools_mcp.sh list` to see all available tools. Common tools include:

### Jira Tools
- `jira_get_ticket` - Get ticket details
- `jira_search_by_jql` - Search tickets using JQL
- `jira_create_ticket_basic` - Create a new ticket
- `jira_update_ticket` - Update ticket fields
- `jira_post_comment` - Add comment to ticket

### Confluence Tools
- `confluence_search_content_by_text` - Search content
- `confluence_content_by_id` - Get page by ID
- `confluence_create_page` - Create new page
- `confluence_update_page` - Update existing page

### Figma Tools
- `figma_get_file_structure` - Get file structure
- `figma_download_image_of_file` - Download image
- `figma_get_icons` - Extract icons

### AI Tools
- `gemini_ai_chat` - Chat with Gemini AI
- `dial_ai_chat` - Chat with DIAL AI

## Examples

### Get Jira Ticket
```bash
./dmtools_mcp.sh jira_get_ticket --data '{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status", "assignee"]
}'
```

### Search Jira Tickets
```bash
./dmtools_mcp.sh jira_search_by_jql --data '{
  "jql": "project = PROJ AND status = \"In Progress\"",
  "maxResults": 50
}'
```

### Create Confluence Page
```bash
./dmtools_mcp.sh confluence_create_page --data '{
  "spaceKey": "DEV",
  "title": "API Documentation",
  "content": "<h1>API Documentation</h1><p>Content here...</p>"
}'
```

### Chat with AI
```bash
./dmtools_mcp.sh gemini_ai_chat --data '{
  "message": "Explain the MCP architecture",
  "context": "DMTools project"
}'
```

## Troubleshooting

### Common Issues

1. **JAR file not found**
   ```bash
   Error: JAR file not found: dmtools-core/build/libs/dmtools-v1.7.16-all.jar
   ```
   **Solution:** Build the project first: `./gradlew :dmtools-core:shadowJar`

2. **Permission denied**
   ```bash
   ./dmtools_mcp.sh: Permission denied
   ```
   **Solution:** Make script executable: `chmod +x dmtools_mcp.sh`

3. **Authentication errors**
   ```bash
   {"error": true, "message": "Unauthorized"}
   ```
   **Solution:** Check environment variables for API tokens

4. **Invalid JSON**
   ```bash
   {"error": true, "message": "Invalid JSON format"}
   ```
   **Solution:** Validate JSON syntax using online tools

### Debug Mode

Use `--verbose` flag to see detailed logs:

```bash
./dmtools_mcp.sh jira_get_ticket --data '{"key": "PROJ-123"}' --verbose
```

### Log Files

Check application logs in:
- `dmtools-core/cacheBasicJiraClient/` - Jira client logs
- `dmtools-core/cacheBasicConfluence/` - Confluence client logs
- `dmtools-core/cacheBasicDialAI/` - AI client logs

## Integration Examples

### Shell Scripting
```bash
#!/bin/bash
TICKET_KEY="PROJ-123"
TICKET_DATA=$(./dmtools_mcp.sh jira_get_ticket --data "{\"key\": \"$TICKET_KEY\"}")
echo "Ticket status: $(echo "$TICKET_DATA" | jq -r '.fields.status.name')"
```

### CI/CD Pipeline
```yaml
- name: Get Jira Ticket
  run: |
    TICKET_JSON=$(./dmtools_mcp.sh jira_get_ticket --data '{"key": "${{ env.TICKET_KEY }}"}')
    echo "ticket-data=$TICKET_JSON" >> $GITHUB_OUTPUT
```

### Python Integration
```python
import subprocess
import json

def get_jira_ticket(key):
    result = subprocess.run([
        './dmtools_mcp.sh', 'jira_get_ticket', 
        '--data', json.dumps({"key": key})
    ], capture_output=True, text=True)
    
    return json.loads(result.stdout)

ticket = get_jira_ticket("PROJ-123")
print(f"Status: {ticket['fields']['status']['name']}")
```

## Performance

- **Cold start:** ~2-3 seconds (JVM startup + initialization)
- **Warm execution:** ~500ms-1s (cached connections)
- **Memory usage:** ~200-500MB (depending on tool complexity)

## Security

- API tokens are passed via environment variables (not command line)
- No sensitive data is logged in quiet mode
- All HTTP requests use TLS encryption
- Local caching respects API rate limits

## Limitations

- Requires Java runtime environment
- No interactive mode (single command execution)
- Limited to tools exposed via MCP annotations
- File uploads require full file paths

## Contributing

To add new MCP tools:

1. Create tool method with `@MCPTool` annotation
2. Rebuild with `./gradlew :dmtools-core:shadowJar`
3. Test with `./dmtools_mcp.sh list`
4. Add documentation and examples

## Support

For issues and questions:
- Check troubleshooting section above
- Review logs with `--verbose` flag
- Validate JSON input format
- Verify environment variables are set correctly