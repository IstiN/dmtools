---
name: jira-dmtools-ticket-create
description: Use when the user needs to create a JIRA ticket (basic, with JSON, or with parent). Covers jira_create_ticket_basic, jira_create_ticket_with_json, jira_create_ticket_with_parent. Run dmtools in the terminal.
---

# JIRA create tickets

## When to use this skill

Use when the user wants to:
- Create a new ticket with basic fields (issue type, summary, project, description)
- Create a ticket with custom fields (JSON)
- Create a ticket as child of a parent (e.g. subtask)

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_create_ticket_basic` | Create with type, summary, project, description | `issueType`, `summary`, `project`, `description` (all required) |
| `jira_create_ticket_with_json` | Create with custom fields | `project`, `fieldsJson` (required) |
| `jira_create_ticket_with_parent` | Create with parent key | `issueType`, `summary`, `project`, `description`, `parentKey` (all required) |

## Steps

1. Choose basic, JSON, or with-parent.
2. For basic: `dmtools jira_create_ticket_basic "Task" "Title" "PROJ" "Description"`.
3. For JSON or parent use `--data` with the required keys.
4. Parse stdout for created `key`; on error check stderr.

## Examples

```bash
# Create basic
dmtools jira_create_ticket_basic "Task" "Fix login bug" "PROJ" "Users cannot log in."

# Create with parent (subtask)
dmtools jira_create_ticket_with_parent --data '{"issueType":"Subtask","summary":"Implement validation","project":"PROJ","description":"...","parentKey":"PROJ-123"}'

# Create with custom fields
dmtools jira_create_ticket_with_json --data '{"project":"PROJ","fieldsJson":{"summary":"New feature","description":"...","issuetype":{"name":"Story"},"priority":{"name":"High"}}}'
```

## Notes

- Descriptions and summaries support Jira markup: `h2.`, `*bold*`, `{code}...{code}`, `*` lists.
- For custom field IDs use `jira_get_field_custom_code` or `jira_get_fields` (see fieldsdata subskill).

## Avoiding malformed API URLs

Prefer **positional** for basic create: `dmtools jira_create_ticket_basic "Task" "Title" "PROJ" "Description"`. For `jira_create_ticket_with_json` and `jira_create_ticket_with_parent` use `--data`; ensure the JSON is valid and passed as a single argument. Bad quoting can cause mangled URLs and 404s.
