---
name: jira-dmtools-ticket-delete
description: Use when the user needs to delete a JIRA ticket. Covers jira_delete_ticket. Run dmtools in the terminal.
---

# JIRA delete tickets

## When to use this skill

Use when the user wants to:
- Delete a JIRA ticket by key (irreversible)

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_delete_ticket` | Delete ticket (irreversible) | `key` (required) |

## Steps

1. Confirm the ticket key and that delete is intended (no undo).
2. Run `dmtools jira_delete_ticket PROJ-123`.
3. Check stdout/stderr and exit code.

## Examples

```bash
dmtools jira_delete_ticket PROJ-123
```

## Notes

- `jira_delete_ticket` cannot be undone; confirm before running.
- This command uses **positional** args only (`dmtools jira_delete_ticket PROJ-123`); no `--data`, so URL-mangling from JSON is not a concern.
