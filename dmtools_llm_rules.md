# DMTools CLI - LLM Usage Rules

### List Available Tools
```bash
# List all tools
dmtools list

# Filter by integration
dmtools list jira
dmtools list confluence
dmtools list figma
```

### Execute Tools

#### Simple Parameters (Positional)
```bash
dmtools jira_get_ticket DMC-100
dmtools jira_get_ticket DMC-100 summary,description
dmtools confluence_get_content 12345
```

#### Named Parameters (JSON)
```bash
dmtools jira_get_ticket --data '{"key": "DMC-100"}'
dmtools jira_get_ticket --data '{"key": "DMC-100", "fields": ["summary", "description"]}'
dmtools confluence_get_content --data '{"id": "12345"}'
```

#### Complex JSON (Heredoc)
```bash
dmtools jira_create_ticket_with_json <<EOF
{
  "summary": "New ticket",
  "description": "Ticket description",
  "issueType": "Task",
  "project": "DMC"
}
EOF
```

## Common Patterns

### Get Jira Ticket
```bash
dmtools jira_get_ticket DMC-100
dmtools jira_get_ticket DMC-100 summary,description,status
```

### Search Jira
```bash
dmtools jira_search_by_jql --data '{"jql": "project = DMC AND status = Open"}'
```

### Create Jira Ticket
```bash
dmtools jira_create_ticket_basic "Bug Title" "Bug description" "Bug" "DMC"
```

### Update Jira Ticket
```bash
dmtools jira_update_ticket --data '{"key": "DMC-100", "summary": "Updated title"}'
```

### Get Confluence Page
```bash
dmtools confluence_get_content 12345
dmtools confluence_find_content_by_title_and_space "Page Title" "SPACE"
```

## Output Format

- **JSON Response**: All commands return JSON or File path
- **Clean Output**: No debug logs in production
- **Error Handling**: Errors returned as JSON with error details

## **IMPORTANT** Tips for LLMs

1. **Always use `dmtools list` first** to see available tools and format of parameters
2. **Use positional args for simple cases**: `dmtools jira_get_ticket DMC-100`
3. **Use `--data` for complex parameters**: `--data '{"key": "value"}'`
4. **Use `Heredoc` for complex JSONs**: `--data '{"key": "value"}'`