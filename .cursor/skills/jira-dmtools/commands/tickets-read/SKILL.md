---
name: jira-dmtools-tickets-read
description: Use when the user needs to read a JIRA ticket, its subtasks, comments, or available transitions. Covers jira_get_ticket, jira_get_subtasks, jira_get_comments, jira_get_transitions. Run dmtools in the terminal.
---

# JIRA read ticket, subtasks, comments, transitions

## When to use this skill

Use when the user wants to:
- Get full or partial details of a ticket by key
- List subtasks of a parent ticket
- Read comments on a ticket
- List available workflow transitions (next statuses) for a ticket

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_get_ticket` | Get ticket by key, optional fields | `key` (required), `fields` (array, optional) |
| `jira_get_subtasks` | Get subtasks of a parent | `key` (required) |
| `jira_get_comments` | Get all comments | `key` (required), `ticket` (optional) |
| `jira_get_transitions` | Get available transitions | `key` (required) |

## Steps

1. Identify the ticket key (e.g. `PROJ-123`).
2. Run the appropriate command in the terminal:
   - **Ticket:** `dmtools jira_get_ticket PROJ-123` or `dmtools jira_get_ticket PROJ-123 summary,description,status` or `--data '{"key":"PROJ-123","fields":["summary","description","status","assignee"]}'`
   - **Subtasks:** `dmtools jira_get_subtasks PROJ-123`
   - **Comments:** `dmtools jira_get_comments PROJ-123`
   - **Transitions:** `dmtools jira_get_transitions PROJ-123`
3. Parse JSON stdout; on error check stderr and exit code. Some dmtools responses wrap the payload in a `result` property; handle both top-level fields and `result` when parsing (e.g. for attachments).

## Examples

```bash
# Full ticket
dmtools jira_get_ticket PROJ-123

# Selected fields (positional)
dmtools jira_get_ticket PROJ-123 summary,description,status,assignee

# Selected fields (JSON)
dmtools jira_get_ticket --data '{"key": "PROJ-123", "fields": ["summary", "description", "status", "assignee", "priority"]}'

# Subtasks and comments
dmtools jira_get_subtasks PROJ-123
dmtools jira_get_comments PROJ-123
dmtools jira_get_transitions PROJ-123
```

## Notes

- Use `jira_get_transitions` before moving a ticket so you know valid `statusName` values for `jira_move_to_status`.
- Subtask issue types vary by project (e.g. subtask, sub-task); the tool handles common names.

## Avoiding malformed API URLs

Some shells or dmtools versions can build wrong URLs when `--data` JSON is used (e.g. fields or key end up in the path). To reduce this:

1. **Prefer positional arguments for `jira_get_ticket`** when you only need the key and optional fields:
   - `dmtools jira_get_ticket PROJ-123` (full ticket)
   - `dmtools jira_get_ticket PROJ-123 summary,description,status` (comma-separated fields, no spaces)
2. **Keep field list simple:** use a single comma-separated string with no spaces (e.g. `summary,description,status`).
3. **Use `--data` only when necessary** (e.g. many or optional parameters). If you do, ensure JSON is valid and correctly quoted for your shell so the tool receives one argument; bad quoting can cause the CLI to split or mangle the request and produce 404s or malformed URLs.
4. If you get a 404 or "dead link" with a mangled path, retry with the positional form above.
