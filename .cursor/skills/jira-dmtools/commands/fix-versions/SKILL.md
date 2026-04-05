---
name: jira-dmtools-fix-versions
description: Use when the user needs to list or change fix versions on JIRA tickets or project. Covers jira_get_fix_versions, jira_set_fix_version, jira_add_fix_version, jira_remove_fix_version. Run dmtools in the terminal.
---

# JIRA fix versions

## When to use this skill

Use when the user wants to:
- List fix versions for a project
- Set or add a fix version on a ticket (set replaces, add keeps existing)
- Remove a fix version from a ticket

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_get_fix_versions` | List fix versions for project | `project` (required) |
| `jira_set_fix_version` | Set fix version (replaces existing) | `key`, `fixVersion` (required) |
| `jira_add_fix_version` | Add fix version (keep existing) | `key`, `fixVersion` (required) |
| `jira_remove_fix_version` | Remove a fix version | `key`, `fixVersion` (required) |

## Steps

1. **List:** `dmtools jira_get_fix_versions PROJ` to see available version names.
2. **Set (replace):** `dmtools jira_set_fix_version --data '{"key":"PROJ-123","fixVersion":"1.0.0"}'`.
3. **Add (keep others):** `dmtools jira_add_fix_version --data '{"key":"PROJ-123","fixVersion":"1.1.0"}'`.
4. **Remove:** `dmtools jira_remove_fix_version --data '{"key":"PROJ-123","fixVersion":"1.0.0"}'`.
5. Parse stdout; check stderr on failure.

## Examples

```bash
# List versions
dmtools jira_get_fix_versions PROJ

# Set single version (replaces any existing)
dmtools jira_set_fix_version --data '{"key":"PROJ-123","fixVersion":"2.0.0"}'

# Add version (multi-version ticket)
dmtools jira_add_fix_version --data '{"key":"PROJ-123","fixVersion":"2.1.0"}'

# Remove
dmtools jira_remove_fix_version --data '{"key":"PROJ-123","fixVersion":"1.0.0"}'
```

## Notes

- Fix version names must exist in the project; use `jira_get_fix_versions` first.
- `set` clears other fix versions on the ticket; use `add` to keep them.

## Avoiding malformed API URLs

Prefer **positional** for list: `dmtools jira_get_fix_versions PROJ`. Set/add/remove use `--data`; ensure the JSON is valid and passed as a single argument. Bad quoting can cause mangled URLs and 404s.
