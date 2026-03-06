---
name: jira-dmtools-search
description: Use when the user needs to search or list JIRA tickets by JQL. Covers jira_search_by_jql, jira_search_by_page, jira_search_with_pagination. Run dmtools in the terminal.
---

# JIRA search (JQL) with dmtools

## When to use this skill

Use when the user wants to:
- Find tickets by project, status, assignee, or other JQL criteria
- List issues matching a JQL query
- Page through large result sets

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_search_by_jql` | Search by JQL, return all results | `jql` (required), `fields` (array, optional) |
| `jira_search_by_page` | Search with paging (nextPageToken) | `jql`, `fields` (required), `nextPageToken` (required) |
| `jira_search_with_pagination` | [Deprecated] Search with startAt | `jql`, `fields`, `startAt` (required) |

## Steps

1. Build a JQL string (e.g. `project = PROJ`, `project = PROJ AND status = Open`, `parent = PROJ-123 OR key = PROJ-123`).
2. Choose fields to return (optional). Omit or use `["key","summary","status"]` etc.
3. Run in terminal:
   - **All results:** `dmtools jira_search_by_jql "project = PROJ" "key,summary,status"` or `dmtools jira_search_by_jql --data '{"jql":"project = PROJ ORDER BY key ASC","fields":["key","summary","status"]}'`
   - **Paged:** `dmtools jira_search_by_page --data '{"jql":"project = PROJ","fields":["key","summary"],"nextPageToken":""}'` (use `nextPageToken` from response for next page).
4. Parse JSON stdout for `issues` array or equivalent; check for `error` on failure.

## Examples

```bash
# All open tickets in project
dmtools jira_search_by_jql "project = PROJ AND status = Open" "key,summary,status"

# With JSON (e.g. for complex JQL or optional fields)
dmtools jira_search_by_jql --data '{"jql": "project = PROJ ORDER BY key ASC", "fields": ["key", "summary", "status", "assignee"]}'

# Parent and children (context for a story)
dmtools jira_search_by_jql --data '{"jql": "parent = PROJ-123 OR key = PROJ-123", "fields": ["key", "summary", "status"]}'
```

## Notes

- JQL is case-sensitive for keywords (e.g. `AND`, `OR`). Field names and values depend on your JIRA schema.
- Large result sets: use `jira_search_by_page` and loop with `nextPageToken`.
- Response may be an array of issues, an object with `issues`, or an object with `result` (array of issues); handle all when parsing.

## Avoiding malformed API URLs

Prefer **positional** form when you only have JQL and optional fields: `dmtools jira_search_by_jql "project = PROJ" "key,summary,status"`. Use `--data` only for complex JQL or when needed. Ensure `--data` JSON is valid and passed as a single argument (correct shell quoting); bad quoting can produce mangled URLs and 404s. If you get a 404 or "dead link", retry with the positional form.
