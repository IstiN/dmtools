---
name: jira-dmtools-profile
description: Use when the user needs JIRA user or account info. Covers jira_get_account_by_email, jira_get_my_profile, jira_get_user_profile. Run dmtools in the terminal.
---

# JIRA profile and account

## When to use this skill

Use when the user wants to:
- Get account ID by email (e.g. for assigning tickets)
- Get current user's profile (verify auth)
- Get a specific user's profile by ID

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_get_account_by_email` | Get account ID by email | `email` (required) |
| `jira_get_my_profile` | Current user profile | None |
| `jira_get_user_profile` | User profile by ID | `userId` (required) |

## Steps

1. For account by email: `dmtools jira_get_account_by_email "user@company.com"` (use returned accountId with `jira_assign_ticket_to`).
2. For current user: `dmtools jira_get_my_profile` (e.g. to verify JIRA config).
3. For another user: `dmtools jira_get_user_profile --data '{"userId":"123456:uuid"}'`.
4. Parse JSON stdout; check stderr on failure.

## Examples

```bash
# Resolve account ID for assignment
dmtools jira_get_account_by_email "user@company.com"

# Verify JIRA config
dmtools jira_get_my_profile

# User profile by ID
dmtools jira_get_user_profile --data '{"userId":"123456:uuid"}'
```

## Notes

- Use `jira_get_account_by_email` before `jira_assign_ticket_to` when you only have the assignee's email.

## Avoiding malformed API URLs

`jira_get_account_by_email` and `jira_get_my_profile` use **positional** args only—no `--data`. For `jira_get_user_profile`, `--data` is required (userId). Ensure the JSON is valid and passed as a single argument; bad quoting can produce mangled URLs and 404s.
