---
name: jira-dmtools-xray
description: Xray test and precondition operations. All jira_xray_* tools. Run dmtools in terminal.
---

# JIRA Xray

Use when working with Xray tests or preconditions (search, get details/steps, create precondition, add steps/preconditions). Requires JIRA with Xray.

**Tools:** jira_xray_search_tickets, jira_xray_get_test_details, jira_xray_get_test_steps, jira_xray_get_preconditions, jira_xray_get_precondition_details, jira_xray_create_precondition, jira_xray_add_precondition_to_test, jira_xray_add_preconditions_to_test, jira_xray_add_test_step, jira_xray_add_test_steps.

**Examples:**
```bash
dmtools jira_xray_search_tickets --data '{"searchQueryJQL":"project=TP AND issueType=Test"}'
dmtools jira_xray_get_test_details --data '{"testKey":"TP-909"}'
dmtools jira_xray_create_precondition --data '{"summary":"...","project":"TP"}'
dmtools jira_xray_add_test_step --data '{"issueId":"12345","action":"...","result":"..."}'
```

Use issue IDs (not keys) for add-precondition and add-step tools when required by the API.

**Avoiding malformed API URLs:** All Xray commands use `--data`. Ensure the JSON is valid and passed as a **single argument** (correct shell quoting); bad quoting can cause mangled URLs and 404s.
