# DMTools MCP Setup for Cursor

This guide explains how to set up DMTools as an MCP (Model Context Protocol) server for use with Cursor and other LLM tools.

## Quick Start

### 1. Download and Run DMTools

**Option A: Download JAR (Recommended)**
1. Download `dmtools-server.jar` from releases
2. Double-click the JAR file to start the server
3. The server will start on `http://localhost:8080`

**Option B: Build from Source**
```bash
# In the DMTools directory
./gradlew build
java -jar build/libs/dmtools-server.jar
```

### 2. Configure Cursor

Add this to your Cursor MCP configuration file (`~/.cursor/mcp.json`):

```json
{
  "mcpServers": {
    "dmtools": {
      "url": "http://localhost:8080/mcp/"
    }
  }
}
```

### 3. Restart Cursor

Restart Cursor to load the new MCP server configuration.

## Configuration

### DMTools Configuration

When you first run the JAR, it will create a `config.properties` file. Edit it with your credentials:

```properties
# JIRA Configuration
jira.base.path=https://your-company.atlassian.net
jira.login.pass.token=your-jira-token
jira.auth.type=basic

# GitHub Configuration  
github.token=your-github-token
github.repository=your-repo
github.workspace=your-org
github.branch=main

# Confluence Configuration
confluence.base.path=https://your-company.atlassian.net/wiki
confluence.login.pass.token=your-confluence-token
confluence.default.space=YOUR_SPACE
```

### Cursor MCP Configuration

1. **Find your Cursor MCP config file:**
   - macOS: `~/.cursor/mcp.json`
   - Windows: `%APPDATA%\Cursor\mcp.json`
   - Linux: `~/.config/cursor/mcp.json`

2. **Add DMTools configuration:**
```json
{
  "mcpServers": {
    "dmtools": {
      "url": "http://localhost:8080/mcp/"
    }
  }
}
```

3. **Restart Cursor** to load the new configuration.

## Available MCP Tools

Once configured, Cursor will have access to these DMTools functions:

### JIRA Tools
- `dmtools_jira_get_instance` - Check JIRA configuration status
- `dmtools_jira_get_default_fields` - Get default JIRA query fields
- `dmtools_jira_search` - Search JIRA tickets with JQL
- `dmtools_jira_get_ticket` - Get specific JIRA ticket
- `dmtools_jira_create_ticket` - Create new JIRA ticket
- `dmtools_jira_add_comment` - Add comment to JIRA ticket

### GitHub Tools
- `dmtools_github_get_instance` - Check GitHub configuration status
- `dmtools_github_get_repository` - Get default repository name
- `dmtools_github_get_config` - Get complete GitHub configuration
- `dmtools_github_get_pull_requests` - Get GitHub pull requests
- `dmtools_github_get_commits` - Get commits from branch
- `dmtools_github_search_files` - Search files in repository

### Confluence Tools
- `dmtools_confluence_get_instance` - Check Confluence configuration status
- `dmtools_confluence_find_content` - Find Confluence pages by title
- `dmtools_confluence_create_page` - Create or find Confluence pages
- `dmtools_confluence_search_pages` - Search Confluence pages

### AI Agent Tools
- `dmtools_agent_test_case_generator` - Generate comprehensive test cases
- `dmtools_agent_presentation_generator` - Generate presentation content
- `dmtools_agent_business_assessment` - Analyze business requirements
- `dmtools_agent_summary_context` - Create contextual summaries

## Usage Examples in Cursor

### Check Integration Status
```
@dmtools Can you check the status of all my integrations?
```

### JIRA Operations
```
@dmtools Search for JIRA tickets assigned to me
@dmtools Create a new bug ticket for the login issue
@dmtools What are the default JIRA query fields I can use?
```

### GitHub Operations
```
@dmtools Show me my GitHub configuration details
@dmtools Get the latest pull requests for my repository
@dmtools Search for files containing "authentication" in my repo
```

### Confluence Operations
```
@dmtools Find the Confluence page titled "API Documentation"
@dmtools Search for pages about "deployment" in our space
```

### AI Agent Tasks
```
@dmtools Generate test cases for user authentication
@dmtools Create a presentation about our new API features
@dmtools Summarize this long document
```

## Troubleshooting

### Server Won't Start

1. **Check Java version:**
   ```bash
   java -version  # Should be 21+
   ```

2. **Check ports:**
   ```bash
   # Windows
   netstat -an | findstr 8080
   
   # macOS/Linux
   lsof -i :8080
   ```

3. **Check configuration:**
   - Ensure `config.properties` exists and is properly configured
   - Verify authentication tokens are valid

### Cursor Can't Connect

1. **Verify server is running:**
   ```bash
   curl http://localhost:8080/mcp/health
   ```

2. **Check MCP configuration:**
   - Ensure `mcp.json` syntax is valid
   - Verify the URL is correct: `http://localhost:8080/mcp/`

3. **Restart Cursor** after configuration changes

### Tools Not Working

1. **Check integration status:**
   Ask Cursor: `@dmtools Check the status of my JIRA/GitHub/Confluence integration`

2. **Verify authentication:**
   - Check that your JIRA/GitHub/Confluence tokens are valid
   - Ensure network connectivity to the services

## Advanced Configuration

### Custom Port

Create `application.properties` next to the JAR:

```properties
server.port=9090
```

Then update your Cursor configuration:
```json
{
  "mcpServers": {
    "dmtools": {
      "url": "http://localhost:9090/mcp/"
    }
  }
}
```

### Environment Variables

You can use environment variables instead of config file:

```bash
# Windows
set JIRA_BASE_PATH=https://your-company.atlassian.net
set JIRA_TOKEN=your-token
set GITHUB_TOKEN=your-token
java -jar dmtools-server.jar

# macOS/Linux
export JIRA_BASE_PATH=https://your-company.atlassian.net
export JIRA_TOKEN=your-token
export GITHUB_TOKEN=your-token
java -jar dmtools-server.jar
```

### Running as Local Development

Set the environment to enable local development features:

```bash
java -Denv=local -jar dmtools-server.jar
```

This enables:
- Automatic browser opening to settings page
- Local "Stop Server" button in the web interface

## Testing the Setup

### 1. Test Server Health
```bash
curl http://localhost:8080/mcp/health
```

### 2. Test in Cursor
Simply ask: `@dmtools Are you working?`

### 3. Check Integration Status
Ask: `@dmtools Check my JIRA, GitHub, and Confluence status`

## Security Considerations

1. **Local Use Only**: The MCP server is designed for local development use
2. **Token Security**: Keep your API tokens secure and don't commit them to version control
3. **Network Access**: Only exposes to localhost by default
4. **Configuration**: Store sensitive data in `config.properties`, not in the JAR

## Example Workflow

1. **Download and run:**
   - Download `dmtools-server.jar`
   - Double-click to start

2. **Configure:**
   - Edit the generated `config.properties`
   - Add your API tokens

3. **Configure Cursor:**
   - Edit `~/.cursor/mcp.json`
   - Add the DMTools MCP server URL

4. **Use in Cursor:**
   - Type `@dmtools` to invoke DMTools functions
   - Ask questions about your JIRA tickets, GitHub repos, or Confluence pages
   - Use AI agents for complex tasks

Your DMTools MCP server is now ready for use with Cursor! 