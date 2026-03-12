# Default Integration Aliases for MCP Tools

DMtools supports **cross-integration aliases** — universal tool names that map to
platform-specific implementations. Instead of calling `github_list_prs` or
`gitlab_list_mrs` directly, you can call `source_code_list_prs` and DMtools routes
the call to the correct integration based on environment variables.

---

## How It Works

1. You call a generic alias, e.g. `source_code_list_prs`.
2. DMtools looks up all implementations registered for that alias.
3. If only one implementation exists, it is used automatically.
4. If multiple implementations exist (e.g., GitHub **and** GitLab), the routing env
   var is consulted:
   - `DEFAULT_SOURCE_CODE` → `github` or `gitlab`
   - `DEFAULT_TRACKER` → `jira` or `ado`
5. If no env var is set, the first registered implementation is used (with a warning).

---

## Configuration

```bash
# Use GitHub for all source_code_* calls
DEFAULT_SOURCE_CODE=github

# Use GitLab for all source_code_* calls
DEFAULT_SOURCE_CODE=gitlab

# Use Jira for all tracker_* calls
DEFAULT_TRACKER=jira

# Use Azure DevOps for all tracker_* calls
DEFAULT_TRACKER=ado
```

Add these to your `dmtools.env` file or export them as shell environment variables.

---

## Source Code Aliases (`source_code_*`)

These aliases abstract over **GitHub** and **GitLab** PR/MR operations.

| Alias | GitHub Implementation | GitLab Implementation |
|-------|-----------------------|-----------------------|
| `source_code_list_prs` | `github_list_prs` | `gitlab_list_mrs` |
| `source_code_get_pr` | `github_get_pr` | `gitlab_get_mr` |
| `source_code_get_pr_comments` | `github_get_pr_comments` | `gitlab_get_mr_comments` |
| `source_code_add_pr_comment` | `github_add_pr_comment` | `gitlab_add_mr_comment` |
| `source_code_get_pr_activities` | `github_get_pr_activities` | `gitlab_get_mr_activities` |
| `source_code_get_pr_discussions` | `github_get_pr_conversations` | `gitlab_get_mr_discussions` |
| `source_code_reply_to_pr_thread` | `github_reply_to_pr_thread` | `gitlab_reply_to_mr_thread` |
| `source_code_add_inline_comment` | `github_add_inline_comment` | `gitlab_add_inline_comment` |
| `source_code_resolve_pr_thread` | `github_resolve_pr_thread` | `gitlab_resolve_mr_thread` |
| `source_code_merge_pr` | `github_merge_pr` | `gitlab_merge_mr` |
| `source_code_get_pr_diff` | `github_get_pr_diff` | `gitlab_get_mr_diff` |

### Aligned Parameter Names (source_code_*)

| Alias Parameter | GitHub Parameter | GitLab Parameter |
|----------------|-----------------|-----------------|
| `workspace` | `workspace` | `workspace` |
| `repository` | `repository` | `repository` |
| `pullRequestId` | `pullRequestId` | `pullRequestId` |
| `text` | `text` | `text` |
| `filePath` | `path` (alias: `filePath`) | `filePath` |
| `threadId` | `inReplyToId` (alias: `threadId`) | `discussionId` (alias: `threadId`) |

---

## Tracker Aliases (`tracker_*`)

These aliases abstract over **Jira** and **Azure DevOps (ADO)** issue-tracking operations.

| Alias | Jira Implementation | ADO Implementation |
|-------|---------------------|--------------------|
| `tracker_search` | `jira_search_by_jql` | `ado_search_by_wiql` |
| `tracker_get_ticket` | `jira_get_ticket` | `ado_get_work_item` |
| `tracker_get_comments` | `jira_get_comments` | `ado_get_comments` |
| `tracker_post_comment` | `jira_post_comment` | `ado_post_comment` |
| `tracker_assign_ticket` | `jira_assign_ticket_to` | `ado_assign_work_item` |
| `tracker_move_to_status` | `jira_move_to_status` | `ado_move_to_state` |
| `tracker_get_my_profile` | `jira_get_my_profile` | `ado_get_my_profile` |
| `tracker_get_user_by_email` | `jira_get_account_by_email` | `ado_get_account_by_email` |
| `tracker_link_tickets` | `jira_link_issues` | `ado_link_work_items` |
| `tracker_create_ticket` | `jira_create_ticket_basic` | `ado_create_work_item` |
| `tracker_download_attachment` | `jira_download_attachment` | `ado_download_attachment` |

### Aligned Parameter Names (tracker_*)

| Alias Parameter | Jira Parameter | ADO Parameter |
|----------------|---------------|--------------|
| `key` | `key` | `id` (alias: `key`) |
| `query` | `jql` (alias: `query`) | `wiql` (aliases: `jql`, `query`) |
| `statusName` | `statusName` | `state` (alias: `statusName`) |
| `accountId` | `accountId` | `userEmail` (alias: `accountId`) |
| `issueType` | `issueType` | `workItemType` (alias: `issueType`) |
| `summary` | `summary` | `title` (alias: `summary`) |
| `sourceKey` | `sourceKey` | `sourceId` (alias: `sourceKey`) |
| `anotherKey` | `anotherKey` | `targetId` (alias: `anotherKey`) |

---

## Usage Examples

### CLI

```bash
# List PRs/MRs using the generic alias (routes via DEFAULT_SOURCE_CODE)
./dmtools.sh source_code_list_prs workspace=myorg repository=myrepo

# Search issues (routes via DEFAULT_TRACKER)
./dmtools.sh tracker_search query="status = In Progress"

# Get a ticket by key
./dmtools.sh tracker_get_ticket key=PROJ-123
```

### JavaScript Agents

```javascript
// Works for both GitHub and GitLab depending on DEFAULT_SOURCE_CODE
const prs = source_code_list_prs("myorg", "myrepo", "open");

// Works for both Jira and ADO depending on DEFAULT_TRACKER
const issues = tracker_search("status = In Progress AND assignee = currentUser()");
const ticket = tracker_get_ticket("PROJ-123");

// Post a comment using the alias
tracker_post_comment("PROJ-123", "This has been reviewed.");
```

---

## Adding New Aliases

To add a new alias to any `@MCPTool`-annotated method:

```java
@MCPTool(
    name = "github_list_prs",
    description = "List pull requests in a repository",
    integration = "github",
    category = "pull_requests",
    aliases = {"source_code_list_prs"}   // ← add aliases here
)
public List<PullRequest> listPullRequests(
        @MCPParam(name = "workspace", ...) String workspace,
        @MCPParam(name = "repository", ...) String repository) { ... }
```

After adding aliases, rebuild: `./gradlew :dmtools-core:compileJava`.
The annotation processor will update `MCPToolRegistry.ALIAS_TO_TOOL_NAMES` automatically.
