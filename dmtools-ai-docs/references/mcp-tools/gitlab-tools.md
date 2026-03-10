# GitLab MCP Tools Reference

**Total tools**: 12
**Integration key**: `gitlab`
**Categories**: `merge_requests`

## Quick Start

```bash
# List available GitLab tools
dmtools list gitlab

# Get MR details (includes diff_refs needed for inline comments)
dmtools gitlab_get_mr workspace=mygroup repository=myrepo pullRequestId=42

# Get all MR comments
dmtools gitlab_get_mr_comments workspace=mygroup repository=myrepo pullRequestId=42
```

## Configuration

```bash
GITLAB_TOKEN=your-personal-access-token
GITLAB_BASE_PATH=https://gitlab.yourcompany.com
```

## Tools

### `gitlab_list_mrs`

List merge requests for a GitLab project.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `state` | String | ✅ | `opened`, `closed`, `merged`, or `all` |

```bash
dmtools gitlab_list_mrs workspace=mygroup repository=myrepo state=opened
```

Returns an array of merge request objects with `iid`, `title`, `state`, `author`, `source_branch`, `target_branch`, `diff_refs`, etc.

---

### `gitlab_get_mr`

Get full details of a specific merge request including `diff_refs` (needed for inline comments).

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID (e.g. `42`) |

```bash
dmtools gitlab_get_mr workspace=mygroup repository=myrepo pullRequestId=42
```

Returns: `iid`, `title`, `description`, `state`, `author`, `source_branch`, `target_branch`, `diff_refs` (`base_sha`, `head_sha`, `start_sha`), `labels`, `assignees`.

The `diff_refs` fields are required for `gitlab_add_inline_mr_comment`.

---

### `gitlab_get_mr_comments`

Get all non-system comments for a merge request. Includes both inline code review comments (`DiffNote`) and general discussion notes.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |

```bash
dmtools gitlab_get_mr_comments workspace=mygroup repository=myrepo pullRequestId=42
```

Returns an array of comment objects with `id`, `body`, `author`, `created_at`, `type` (`DiffNote` for inline, `null` for discussion notes), `system` flag.

---

### `gitlab_get_mr_activities`

Get all activities for a merge request, including approvals and general discussion notes. System-generated notes are excluded.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |

```bash
dmtools gitlab_get_mr_activities workspace=mygroup repository=myrepo pullRequestId=42
```

Returns an array of activity objects with `action` (`APPROVED` or `COMMENTED`) and associated comment details.

---

### `gitlab_add_mr_comment`

Add a general discussion comment to a merge request.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |
| `text` | String | ✅ | Comment text |

```bash
dmtools gitlab_add_mr_comment workspace=mygroup repository=myrepo pullRequestId=42 text="LGTM!"
```

Returns the created note object as JSON.

---

### `gitlab_get_mr_discussions`

Get all discussion threads for a merge request. Each discussion contains one or more notes and a `resolved` status.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |

```bash
dmtools gitlab_get_mr_discussions workspace=mygroup repository=myrepo pullRequestId=42
```

Returns an array of discussion objects:
```json
{
  "id": "6a9c1750b37d57bba1079be3bbd13a...",
  "individual_note": false,
  "notes": [
    { "id": 101, "body": "Please fix this", "author": {...}, "resolvable": true, "resolved": false }
  ]
}
```

Use the `id` field with `gitlab_reply_to_mr_thread` and `gitlab_resolve_mr_thread`.

---

### `gitlab_reply_to_mr_thread`

Reply to an existing discussion thread in a merge request.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |
| `discussionId` | String | ✅ | Discussion thread ID (from `gitlab_get_mr_discussions`) |
| `text` | String | ✅ | Reply text |

```bash
dmtools gitlab_reply_to_mr_thread workspace=mygroup repository=myrepo pullRequestId=42 discussionId=6a9c1750b37d57bba1079be3bbd13a text="Addressed in latest commit"
```

Returns the created note object as JSON.

---

### `gitlab_add_inline_mr_comment`

Create a new inline code review comment on a specific file and line. Requires SHA values from the MR's `diff_refs` (use `gitlab_get_mr` first).

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |
| `filePath` | String | ✅ | Path to the file (e.g. `src/main/Foo.java`) |
| `line` | String | ✅ | Line number in the new version of the file |
| `text` | String | ✅ | Comment text |
| `baseSha` | String | ✅ | `diff_refs.base_sha` from the MR |
| `headSha` | String | ✅ | `diff_refs.head_sha` from the MR |
| `startSha` | String | ✅ | `diff_refs.start_sha` from the MR |

```bash
dmtools gitlab_add_inline_mr_comment workspace=mygroup repository=myrepo pullRequestId=42 \
  filePath=src/main/Foo.java line=42 text="This variable name is confusing" \
  baseSha=abc123 headSha=def456 startSha=abc123
```

Returns the created discussion object with the inline note.

**Workflow example:**
```bash
# 1. Get MR to extract diff_refs
MR=$(dmtools gitlab_get_mr workspace=mygroup repository=myrepo pullRequestId=42)
# Extract diff_refs.base_sha, head_sha, start_sha from MR JSON

# 2. Add inline comment
dmtools gitlab_add_inline_mr_comment workspace=mygroup repository=myrepo pullRequestId=42 \
  filePath=src/Foo.java line=10 text="Potential null pointer" \
  baseSha=<base_sha> headSha=<head_sha> startSha=<start_sha>
```

---

### `gitlab_resolve_mr_thread`

Resolve (close) a review discussion thread in a merge request.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |
| `discussionId` | String | ✅ | Discussion thread ID (from `gitlab_get_mr_discussions`) |

```bash
dmtools gitlab_resolve_mr_thread workspace=mygroup repository=myrepo pullRequestId=42 discussionId=6a9c1750b37d57bba1079be3bbd13a
```

Returns the updated discussion object with `resolved: true`.

---

### `gitlab_approve_mr`

Approve a GitLab merge request. Adds your approval to the MR.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |

```bash
dmtools gitlab_approve_mr workspace=mygroup repository=myrepo pullRequestId=42
```

Returns the approval object with `approved_by` list of approvers.

---

### `gitlab_merge_mr`

Merge a GitLab merge request. Optionally provide a custom merge commit message.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `workspace` | String | ✅ | GitLab group or namespace |
| `repository` | String | ✅ | Repository name |
| `pullRequestId` | String | ✅ | Merge request IID |
| `mergeCommitMessage` | String | ❌ | Optional custom merge commit message |

```bash
dmtools gitlab_merge_mr workspace=mygroup repository=myrepo pullRequestId=42 mergeCommitMessage="Merge feature XYZ"
```

Returns the merged MR object with `state: merged`.

---

## JavaScript Agent Usage

All GitLab tools are available as direct function calls in JS agents:

```javascript
// List open MRs
const mrs = JSON.parse(gitlab_list_mrs("mygroup", "myrepo", "opened"));

// Get MR details including diff_refs
const mr = JSON.parse(gitlab_get_mr("mygroup", "myrepo", "42"));
const { base_sha, head_sha, start_sha } = mr.diff_refs;

// Get all discussions
const discussions = JSON.parse(gitlab_get_mr_discussions("mygroup", "myrepo", "42"));

// Add inline comment
gitlab_add_inline_mr_comment(
    "mygroup", "myrepo", "42",
    "src/main/Foo.java", "15", "This needs a null check",
    base_sha, head_sha, start_sha
);

// Resolve a discussion thread
gitlab_resolve_mr_thread("mygroup", "myrepo", "42", discussions[0].id);

// Reply to a thread
gitlab_reply_to_mr_thread("mygroup", "myrepo", "42", discussions[0].id, "Fixed!");

// Approve an MR
gitlab_approve_mr("mygroup", "myrepo", "42");

// Merge an MR
gitlab_merge_mr("mygroup", "myrepo", "42", "Merge feature XYZ");
```
