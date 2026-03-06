---
name: jira-dmtools
description: Perform JIRA operations via dmtools CLI. Flow: (1) verify JIRA connection via verify script; (2) run script without --verify to create/refresh .dmtools/jira.cfg if missing; (3) select command from Command index by task, read commands/<folder>/SKILL.md, run dmtools. JIRA config in dmtools.env (JIRA_BASE_PATH, JIRA_EMAIL, JIRA_API_TOKEN).
---

# JIRA operations with dmtools CLI

## Agent flow (follow in order)

1. **Verify JIRA connection**  
   Run from repo root: `python .cursor/skills/jira-dmtools/scripts/verify_jira_connection.py --verify`  
   This checks that `.dmtools/jira.cfg` exists and is valid. Connection is driven by **dmtools.env** (or env vars): `JIRA_BASE_PATH`, `JIRA_EMAIL`, `JIRA_API_TOKEN`.

2. **Create or refresh config if missing/invalid**  
   If step 1 fails (no jira.cfg or invalid), run: `python .cursor/skills/jira-dmtools/scripts/verify_jira_connection.py`  
   This uses dmtools.env, calls JIRA, and writes `.dmtools/jira.cfg` on success. Optionally add `--project PROJ` to cache issue types, statuses, components.

3. **Select and run the command**  
   Once jira.cfg is present, match the user's task to the **"When to use"** column in the Command index table below. Open the indicated `commands/<folder>/SKILL.md`, then run the corresponding `dmtools` command (positional args or `--data '<JSON>'`).

**On 401/403/timeout** during a dmtools call: re-run step 2 to refresh config, then retry the command.

**Identifying failures quickly:** For workflow (move/transition) commands, 404 with a mangled URL (e.g. path containing `statusName/:/Partial` with the status name cut off) usually means the shell split the `--data` JSON—use single-quoted JSON. For other commands, see the "Avoiding malformed API URLs" / "Identifying issues faster" section in the relevant `commands/<folder>/SKILL.md`.

## How to invoke

Run `dmtools <tool_name>` in the terminal. Use positional args for simple calls or `--data '<JSON>'` for complex ones. Output is JSON on stdout.

**List all JIRA tools:** `dmtools list | jq '.tools[] | select(.name | startswith("jira_"))'`

**Subskills:** For each group below, read `commands/<folder>/SKILL.md` when the task matches the "When to use" description; that file has the exact command and parameters.

## Command index

| Group | When to use | Tools |
|-------|-------------|-------|
| **Search** | Search or list tickets by JQL, project, status, or with paging. | `commands/search` → `jira_search_by_jql`, `jira_search_by_page`, `jira_search_with_pagination` |
| **Read** | Get one ticket, its subtasks, comments, or available transitions. | `commands/tickets-read` → `jira_get_ticket`, `jira_get_subtasks`, `jira_get_comments`, `jira_get_transitions` |
| **Create** | Create a ticket (basic, with custom fields, or as subtask under parent). | `commands/ticket-create` → `jira_create_ticket_basic`, `jira_create_ticket_with_json`, `jira_create_ticket_with_parent` |
| **Update** | Change fields, description, parent, or clear a field on a ticket. | `commands/ticket-update` → `jira_update_ticket`, `jira_update_field`, `jira_update_description`, `jira_update_all_fields_with_name`, `jira_update_ticket_parent`, `jira_clear_field` |
| **Delete** | Delete a ticket (irreversible). | `commands/ticket-delete` → `jira_delete_ticket` |
| **Workflow** | Move ticket to a status, set resolution, or assign to a user. | `commands/workflow` → `jira_move_to_status`, `jira_move_to_status_with_resolution`, `jira_assign_ticket_to` |
| **Profile** | Resolve account ID by email, get current user or another user's profile. | `commands/profile` → `jira_get_account_by_email`, `jira_get_my_profile`, `jira_get_user_profile` |
| **Comments** | Add a comment or add only if not already present; read comments is under Read. | `commands/comments` → `jira_post_comment`, `jira_post_comment_if_not_exists` |
| **Fix versions** | List, set, add, or remove fix version(s) on a ticket or project. | `commands/fix-versions` → `jira_get_fix_versions`, `jira_set_fix_version`, `jira_add_fix_version`, `jira_remove_fix_version` |
| **Links & labels** | List link types, link two issues, add label, or set priority. | `commands/links-labels-priority` → `jira_get_issue_link_types`, `jira_link_issues`, `jira_add_label`, `jira_set_priority` |
| **Fields data** | Get field definitions or custom field ID(s) by display name for a project. | `commands/fieldsdata` → `jira_get_fields`, `jira_get_field_custom_code`, `jira_get_all_fields_with_name` |
| **Metadata** | Get issue types, statuses, or components for a project. | `commands/metadata` → `jira_get_issue_types`, `jira_get_project_statuses`, `jira_get_components` |
| **Attachments** | Attach a file to a ticket or download an attachment by URL. | `commands/attachments` → `jira_attach_file_to_ticket`, `jira_download_attachment` |
| **Xray** | Xray test/precondition operations: search tests, get steps/preconditions, create or add to tests. | `commands/xray` → `jira_xray_*` (search, get details/steps/preconditions, create precondition, add to test) |
| **Advanced** | Run a custom GET request to the JIRA API with auth. | `commands/advanced` → `jira_execute_request` |
