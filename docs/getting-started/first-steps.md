# First Steps

Your first commands with DMTools CLI.

## Prerequisites

- ✅ DMTools installed ([Installation Guide](installation.md))
- ✅ Configuration set up ([Configuration Guide](configuration.md))

---

## Your First Command

Let's start with a simple Jira query:

```bash
# Get a Jira ticket
dmtools jira_get_ticket YOUR-123
```

**Expected output:** JSON with ticket details
```json
{
  "key": "YOUR-123",
  "fields": {
    "summary": "Example ticket",
    "status": {"name": "In Progress"},
    "assignee": {"displayName": "John Doe"}
  }
}
```

---

## Basic Commands

### List Available Tools

```bash
# See all 67 available MCP tools
dmtools list
```

Output shows all tools organized by category:
- **Jira** (45 tools)
- **Confluence** (16 tools)
- **Figma** (6 tools)

### Get Help

```bash
# Show version
dmtools --version

# Show help
dmtools --help

# List available jobs
dmtools --list-jobs
```

---

## Common Jira Operations

### Get Ticket Details

```bash
# Get full ticket details
dmtools jira_get_ticket PROJ-123

# Get specific fields only
dmtools jira_get_ticket --data '{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}'
```

### Search Tickets

```bash
# Search with JQL
dmtools jira_search_by_jql \
  "project = PROJ AND status = 'In Progress'" \
  "summary,status,assignee"

# Using heredoc for complex JQL
dmtools jira_search_by_jql <<EOF
{
  "jql": "project = PROJ AND created >= -7d ORDER BY created DESC",
  "fields": ["summary", "status", "created", "assignee"]
}
EOF
```

### Post Comment

```bash
# Simple comment
dmtools jira_post_comment PROJ-123 "This is a test comment"

# Comment with Jira markup
dmtools jira_post_comment PROJ-123 "h2. Status Update

*Progress:* Implementation completed
*Next steps:* Code review

{code}
function example() {
  return true;
}
{code}"
```

### Create Ticket

```bash
# Basic ticket creation
dmtools jira_create_ticket_basic \
  PROJ \
  Task \
  "Fix login bug" \
  "Users cannot log in with special characters in password"

# With JSON parameters
dmtools jira_create_ticket_with_json <<EOF
{
  "project": "PROJ",
  "fieldsJson": {
    "summary": "Implement new feature",
    "description": "Feature description here",
    "issuetype": {"name": "Story"},
    "priority": {"name": "High"},
    "labels": ["feature", "high-priority"]
  }
}
EOF
```

---

## Common Confluence Operations

### Get Page Content

```bash
# By title (in default space)
dmtools confluence_content_by_title "My Page Title"

# By title and space
dmtools confluence_content_by_title_and_space \
  "Architecture Overview" \
  "TECH"

# By content ID
dmtools confluence_content_by_id "123456"
```

### Search Content

```bash
# Text search
dmtools confluence_search_content_by_text "API documentation" 10
```

### Create/Update Page

```bash
# Create new page
dmtools confluence_create_page <<EOF
{
  "title": "New Documentation Page",
  "parentId": "123456",
  "body": "<p>This is the page content in Confluence storage format.</p>",
  "space": "TECH"
}
EOF

# Update existing page
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

---

## Common Figma Operations

### Get Design File

```bash
# Get file structure
dmtools figma_get_file_structure \
  "https://www.figma.com/file/abc123/MyDesign"

# Get icons/graphics from file
dmtools figma_get_icons \
  "https://www.figma.com/file/abc123/MyDesign"

# Download as PNG
dmtools figma_download_image_as_file \
  "https://www.figma.com/file/abc123/MyDesign" \
  "123:456" \
  "png"
```

---

## Running Jobs

DMTools includes 20 automation jobs. Here's how to run them:

### Simple Job Execution

```bash
# Create a job configuration file
cat > my-job.json << EOF
{
  "name": "expert",
  "params": {
    "question": "What is the current status of the project?",
    "outputFormat": "markdown"
  }
}
EOF

# Run the job
dmtools run my-job.json
```

### Job with Overrides

You can override parameters using base64 or URL-encoded JSON:

```bash
# With base64 encoded override
dmtools run my-job.json "$(echo '{"params":{"question":"New question"}}' | base64)"

# With URL-encoded override
dmtools run my-job.json "%7B%22params%22%3A%7B%22question%22%3A%22New%20question%22%7D%7D"
```

See **[Jobs Documentation](../jobs/README.md)** for all 20 available jobs.

---

## Input Methods

DMTools supports multiple input methods:

### 1. Positional Arguments

```bash
dmtools jira_get_ticket PROJ-123 summary,description
```

### 2. JSON Data Flag

```bash
dmtools jira_get_ticket --data '{"key": "PROJ-123", "fields": ["summary"]}'
```

### 3. File Input

```bash
# Create params file
cat > params.json << EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}
EOF

# Use file
dmtools jira_get_ticket --file params.json
```

### 4. Heredoc (Multi-line)

```bash
dmtools jira_get_ticket <<EOF
{
  "key": "PROJ-123",
  "fields": ["summary", "description", "status"]
}
EOF
```

### 5. Pipe/STDIN

```bash
echo '{"key": "PROJ-123"}' | dmtools jira_get_ticket
```

---

## Debugging

### Verbose Mode

```bash
# See detailed logs and execution info
dmtools jira_get_ticket --verbose PROJ-123
```

### Debug Mode

```bash
# Show all logs including errors
dmtools jira_get_ticket --debug PROJ-123
```

---

## Output Handling

### Save Output to File

```bash
# JSON output
dmtools jira_get_ticket PROJ-123 > ticket.json

# Pretty print with jq
dmtools jira_get_ticket PROJ-123 | jq '.'

# Extract specific field
dmtools jira_get_ticket PROJ-123 | jq -r '.fields.summary'
```

### Process Multiple Tickets

```bash
# Get multiple tickets
for ticket in PROJ-123 PROJ-124 PROJ-125; do
  echo "Processing $ticket..."
  dmtools jira_get_ticket "$ticket" | jq '.fields.summary'
done

# Search and process results
dmtools jira_search_by_jql "project = PROJ AND status = Open" "key" \
  | jq -r '.issues[].key' \
  | while read ticket; do
      echo "Commenting on $ticket..."
      dmtools jira_post_comment "$ticket" "Automated comment"
    done
```

---

## Common Workflows

### Workflow 1: Ticket Status Report

```bash
#!/bin/bash
# Generate status report for all tickets

JQL="project = PROJ AND sprint in openSprints()"

dmtools jira_search_by_jql "$JQL" "key,summary,status,assignee" \
  | jq -r '.issues[] | "\(.key): \(.fields.summary) [\(.fields.status.name)]"' \
  > sprint-report.txt

echo "Report saved to sprint-report.txt"
```

### Workflow 2: Bulk Update Labels

```bash
#!/bin/bash
# Add label to all tickets in a sprint

TICKETS=$(dmtools jira_search_by_jql \
  "project = PROJ AND sprint = 42" \
  "key" | jq -r '.issues[].key')

for ticket in $TICKETS; do
  echo "Adding label to $ticket..."
  dmtools jira_update_ticket <<EOF
{
  "key": "$ticket",
  "params": {
    "update": {
      "labels": [{"add": "sprint-42-automated"}]
    }
  }
}
EOF
done
```

### Workflow 3: Documentation Sync

```bash
#!/bin/bash
# Sync Jira ticket details to Confluence

TICKET="PROJ-123"

# Get ticket details
TICKET_DATA=$(dmtools jira_get_ticket "$TICKET")
SUMMARY=$(echo "$TICKET_DATA" | jq -r '.fields.summary')
DESCRIPTION=$(echo "$TICKET_DATA" | jq -r '.fields.description')

# Create Confluence page
dmtools confluence_create_page <<EOF
{
  "title": "$TICKET: $SUMMARY",
  "parentId": "123456",
  "body": "<h1>$SUMMARY</h1><p>$DESCRIPTION</p>",
  "space": "DOCS"
}
EOF
```

---

## Environment-Specific Configuration

### Use Different Configurations

```bash
# Development environment
JIRA_BASE_PATH=https://dev.company.atlassian.net \
  dmtools jira_get_ticket DEV-123

# Production environment  
JIRA_BASE_PATH=https://prod.company.atlassian.net \
  dmtools jira_get_ticket PROD-123

# Use specific env file
cd /path/to/project
dmtools jira_get_ticket PROJ-123  # Uses local dmtools.env
```

---

## Next Steps

✅ You now know the basics of DMTools CLI!

### Deep Dive:
- **[CLI Usage Overview](../cli-usage/overview.md)** - Command structure and advanced usage
- **[MCP Tools Reference](../cli-usage/mcp-tools.md)** - All 67 tools documented
- **[Jobs Documentation](../jobs/README.md)** - Explore 20 automation jobs

### Automation:
- **[AI Teammate Workflows](../ai-teammate/README.md)** - GitHub Actions automation
- **[CI/CD Integration](../configuration/ci-cd.md)** - Use DMTools in pipelines

### Examples:
- **[CLI Examples](../cli-usage/examples.md)** - Real-world use cases
- **[Workflow Examples](../ai-teammate/examples.md)** - Automation patterns
