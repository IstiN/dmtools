---
name: jira-dmtools-comments
description: Use when the user needs to add or read comments on a JIRA ticket. Covers jira_post_comment, jira_post_comment_if_not_exists, jira_get_comments. Run dmtools in the terminal.
---

# JIRA comments

## When to use this skill

Use when the user wants to:
- Add a comment to a ticket
- Add a comment only if it does not already exist (idempotent)
- Read all comments on a ticket

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_post_comment` | Add comment (Jira markup supported) | `key`, `comment` (required) |
| `jira_post_comment_if_not_exists` | Add only if comment not present | `key`, `comment` (required) |
| `jira_get_comments` | Get all comments | `key` (required), `ticket` (optional) |

## Steps

1. Have the ticket key and comment text ready.
2. To **add:** `dmtools jira_post_comment PROJ-123 "Comment text"` or `dmtools jira_post_comment --data '{"key":"PROJ-123","comment":"Comment text"}'`. Use `jira_post_comment_if_not_exists` with the same args when you want to avoid duplicates.
3. To **read:** `dmtools jira_get_comments PROJ-123`.
4. Parse JSON stdout; on error check stderr.

## Examples

```bash
# Post comment (positional)
dmtools jira_post_comment PROJ-123 "Deployed to staging."

# Post comment (JSON, useful for long or multi-line)
dmtools jira_post_comment --data '{"key":"PROJ-123","comment":"h2. Update\n* Item 1\n* Item 2"}'

# Post only if not already present
dmtools jira_post_comment_if_not_exists --data '{"key":"PROJ-123","comment":"Automated sync completed"}'

# Get comments
dmtools jira_get_comments PROJ-123
```

## Notes

- Jira markup in comments: `h2.` for headings, `*text*` for bold, `{code}...{code}` for code, `*` for bullet lists.
- Escape single quotes in JSON (e.g. `'"'"'` in shell) or use heredoc for complex content.

## Avoiding malformed API URLs

Prefer **positional** form for get and simple post: `dmtools jira_get_comments PROJ-123` and `dmtools jira_post_comment PROJ-123 "Comment text"`. Use `--data` only for long or multi-line comments. Ensure `--data` JSON is valid and passed as a single argument; bad quoting can cause mangled URLs and 404s. If you get a 404 or mangled path, retry with positional.
