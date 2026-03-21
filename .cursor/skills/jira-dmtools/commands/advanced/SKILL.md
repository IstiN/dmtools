---
name: jira-dmtools-advanced
description: Use when the user needs to run a custom GET request to the JIRA API with auth. Covers jira_execute_request. Run dmtools in the terminal.
---

# JIRA custom API request

## When to use this skill

Use when the user needs to:
- Call a JIRA REST API endpoint that is not covered by other jira_* tools
- Perform a custom GET request with the same authentication as dmtools (JIRA_BASE_PATH, JIRA_EMAIL, JIRA_API_TOKEN)

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_execute_request` | Custom HTTP GET to Jira API with auth | `url` (required) |

## Steps

1. Build the full JIRA API URL (e.g. `https://company.atlassian.net/rest/api/3/issue/PROJ-123?expand=changelog`). Base is typically from JIRA_BASE_PATH.
2. Run `dmtools jira_execute_request --data '{"url":"https://..."}'`.
3. Parse JSON (or other) stdout; check stderr on failure.

## Examples

```bash
# Custom GET (e.g. issue with expand)
dmtools jira_execute_request --data '{"url":"https://company.atlassian.net/rest/api/3/issue/PROJ-123?expand=changelog"}'
```

## Notes

- Only GET is supported; no POST/PUT/DELETE. For other operations use the dedicated jira_* tools where possible.
- URL must be reachable with the configured JIRA credentials (same host as JIRA_BASE_PATH typically).

## Avoiding malformed API URLs

`jira_execute_request` uses `--data` with a single `url` field. Ensure the JSON is valid and passed as a **single argument** (correct shell quoting); bad quoting can cause mangled URLs and 404s.
