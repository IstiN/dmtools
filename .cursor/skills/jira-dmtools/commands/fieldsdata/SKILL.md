---
name: jira-dmtools-fieldsdata
description: Use when the user needs JIRA field definitions or custom field IDs. Covers jira_get_fields, jira_get_field_custom_code, jira_get_all_fields_with_name. Run dmtools in the terminal.
---

# JIRA fields data

## When to use this skill

Use when the user wants to:
- List all fields for a project
- Resolve a human-readable field name to custom field ID (e.g. "Story Points" -> customfield_10021)
- Get all field IDs that share a display name

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_get_fields` | All fields for project | `project` (required) |
| `jira_get_field_custom_code` | Custom field ID for a name | `project`, `fieldName` (required) |
| `jira_get_all_fields_with_name` | All field IDs with same name | `project`, `fieldName` (required) |

## Steps

1. Have the project key (e.g. `PROJ`).
2. Run the needed command: `dmtools jira_get_fields PROJ`, or `jira_get_field_custom_code` / `jira_get_all_fields_with_name` with `--data`.
3. Use the returned IDs in update/create/clear operations (e.g. customfield_10021 in `jira_update_field` or `jira_clear_field`).

## Examples

```bash
dmtools jira_get_fields PROJ
dmtools jira_get_field_custom_code --data '{"project":"PROJ","fieldName":"Story Points"}'
dmtools jira_get_all_fields_with_name --data '{"project":"PROJ","fieldName":"Dependencies"}'
```

## Notes

- Use `jira_get_field_custom_code` before updating or clearing a custom field by ID.
- Some projects have multiple custom fields with the same display name; `jira_get_all_fields_with_name` returns all; `jira_update_all_fields_with_name` (ticket-update) updates all of them.
- **Cached in jira.cfg:** Field data (e.g. from `jira_get_fields`, `jira_get_field_custom_code`) can be stored in `.dmtools/jira.cfg` per project. The verification script (run with `--project PROJ`) can populate this; agents may read `jira.cfg` for known field IDs when appropriate to avoid repeated API calls.

## Avoiding malformed API URLs

Prefer **positional** for single-param: `dmtools jira_get_fields PROJ`. For `jira_get_field_custom_code` and `jira_get_all_fields_with_name` use `--data`; ensure the JSON is valid and passed as a single argument (correct shell quoting). Bad quoting can cause mangled URLs and 404s.
