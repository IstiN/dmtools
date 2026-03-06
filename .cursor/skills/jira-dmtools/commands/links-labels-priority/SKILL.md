---
name: jira-dmtools-links-labels-priority
description: Link issues, add labels, or set priority. jira_get_issue_link_types, jira_link_issues, jira_add_label, jira_set_priority.
---

# JIRA links, labels, priority

Use when linking two issues, adding a label, or setting priority.

**Commands:** `jira_get_issue_link_types` (no args), `jira_link_issues` (sourceKey, relationship, anotherKey), `jira_add_label` (key, label), `jira_set_priority` (key, priority).

**Examples:**
```bash
dmtools jira_get_issue_link_types
dmtools jira_link_issues --data '{"sourceKey":"PROJ-123","relationship":"blocks","anotherKey":"PROJ-456"}'
dmtools jira_add_label PROJ-123 "mylabel"
dmtools jira_set_priority --data '{"key":"PROJ-123","priority":"High"}'
```

Get valid relationship names from the first command. Priority values depend on project.

**Avoiding malformed API URLs:** Prefer **positional** for add_label: `dmtools jira_add_label PROJ-123 "mylabel"`. For link_issues and set_priority use `--data`; ensure JSON is valid and passed as a single argument to avoid mangled URLs and 404s.
