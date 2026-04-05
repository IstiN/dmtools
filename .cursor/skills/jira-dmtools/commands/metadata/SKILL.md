---
name: jira-dmtools-metadata
description: Use when the user needs JIRA project metadata: issue types, statuses, components. Covers jira_get_issue_types, jira_get_project_statuses, jira_get_components. For fields and field IDs see fieldsdata subskill. Run dmtools in the terminal.
---

# JIRA project metadata

## When to use this skill

Use when the user wants to:
- List issue types for a project
- List statuses for a project
- List components for a project

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_get_issue_types` | Issue types for project | `project` (required) |
| `jira_get_project_statuses` | Statuses for project | `project` (required) |
| `jira_get_components` | Components for project | `project` (required) |

## Steps

1. Have the project key (e.g. `PROJ`).
2. Run: `dmtools jira_get_issue_types PROJ`, `dmtools jira_get_project_statuses PROJ`, `dmtools jira_get_components PROJ`.
3. Use the returned names in create/update or workflow (e.g. status names for transitions).

## Examples

```bash
dmtools jira_get_issue_types PROJ
dmtools jira_get_project_statuses PROJ
dmtools jira_get_components PROJ
```

## Notes

- For field definitions and custom field IDs use the fieldsdata subskill.
- These commands use **positional** args only (`dmtools jira_get_issue_types PROJ` etc.); no `--data`, so URL-mangling from JSON is not a concern.
- **Cached in jira.cfg:** Issue types, statuses, and components can be stored in `.dmtools/jira.cfg` per project. The verification script (run with `--project PROJ`) can populate this; agents may read `jira.cfg` for metadata when appropriate to avoid repeated API calls.
