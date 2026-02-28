---
name: jira-dmtools-ticket-update
description: Use when the user needs to update a JIRA ticket or clear a field. Covers jira_update_ticket, jira_update_field, jira_update_description, jira_update_all_fields_with_name, jira_update_ticket_parent, jira_clear_field. Run dmtools in the terminal.
---

# JIRA update tickets and clear fields

## When to use this skill

Use when the user wants to:
- Update ticket fields, description, or parent
- Clear a field value

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_update_ticket` | Update with Jira REST API JSON | `key`, `params` (required) |
| `jira_update_field` | Update one field (by name or customfield_ id) | `key`, `field`, `value` (required) |
| `jira_update_description` | Set description | `key`, `description` (required) |
| `jira_update_all_fields_with_name` | Update all fields with same display name | `key`, `fieldName`, `value` (required) |
| `jira_update_ticket_parent` | Change parent | `key`, `parentKey` (required) |
| `jira_clear_field` | Clear a field | `key`, `field` (required) |

## Steps

1. Use `jira_update_ticket` for bulk REST-style updates, or `jira_update_field` / `jira_update_description` / `jira_update_ticket_parent` for single changes.
2. Pass `key` and the relevant payload via `--data`.
3. For clear: `jira_clear_field` with field name or customfield_ id.
4. Parse stdout; on error check stderr.

## Examples

```bash
# Update field
dmtools jira_update_field --data '{"key":"PROJ-123","field":"priority","value":{"name":"High"}}'

# Update description
dmtools jira_update_description --data '{"key":"PROJ-123","description":"Updated text"}'

# Change parent
dmtools jira_update_ticket_parent --data '{"key":"PROJ-124","parentKey":"PROJ-100"}'

# Clear field
dmtools jira_clear_field --data '{"key":"PROJ-123","field":"customfield_10001"}'
```

## Notes

- Jira markup in descriptions: `h2.`, `*bold*`, `{code}...{code}`, `*` lists.
- For custom field IDs use `jira_get_field_custom_code` or `jira_get_fields` (see fieldsdata subskill). `jira_update_all_fields_with_name` updates all fields with that display name.

## Avoiding malformed API URLs

These commands use `--data` with JSON. Ensure the JSON is valid and passed as a **single argument** (correct shell quoting); bad quoting can cause mangled URLs and 404s.
