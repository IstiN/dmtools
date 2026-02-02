# JIRA MCP Tools

**Total Tools**: 52

## Quick Reference

```bash
# List all jira tools
dmtools list | jq '.tools[] | select(.name | startswith("jira_"))'

# Example usage
dmtools jira_xray_create_precondition [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for jira tools
const result = jira_xray_create_precondition(...);
const result = jira_xray_search_tickets(...);
const result = jira_xray_get_test_details(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `jira_add_fix_version` | Add a fix version to a Jira ticket (without removing existing ones) | `fixVersion` (string, **required**)<br>`key` (string, **required**) |
| `jira_add_label` | Adding label to specific ticket key | `key` (string, **required**)<br>`label` (string, **required**) |
| `jira_assign_ticket_to` | Assigns a Jira ticket to user | `accountId` (string, **required**)<br>`key` (string, **required**) |
| `jira_attach_file_to_ticket` | Attach a file to a Jira ticket from a local file path. The file will only be attached if a file with the same name doesn't already exist | `name` (string, **required**)<br>`ticketKey` (string, **required**)<br>`contentType` (string, optional)<br>`filePath` (string, **required**) |
| `jira_clear_field` | Clear (delete value) a specific field value in a Jira ticket | `field` (string, **required**)<br>`key` (string, **required**) |
| `jira_create_ticket_basic` | Create a new Jira ticket with basic fields (project, issue type, summary, description) | `issueType` (string, **required**)<br>`summary` (string, **required**)<br>`project` (string, **required**)<br>`description` (string, **required**) |
| `jira_create_ticket_with_json` | Create a new Jira ticket with custom fields using JSON configuration | `project` (string, **required**)<br>`fieldsJson` (object, **required**) |
| `jira_create_ticket_with_parent` | Create a new Jira ticket with a parent relationship | `issueType` (string, **required**)<br>`summary` (string, **required**)<br>`project` (string, **required**)<br>`description` (string, **required**)<br>`parentKey` (string, **required**) |
| `jira_delete_ticket` | Delete a Jira ticket by key | `key` (string, **required**) |
| `jira_download_attachment` | Download a Jira attachment by URL and save it as a file | `href` (string, **required**) |
| `jira_execute_request` | Execute a custom HTTP GET request to Jira API with auth. Can be used to perform any jira get requests which are required auth. | `url` (string, **required**) |
| `jira_get_account_by_email` | Gets account details by email | `email` (string, **required**) |
| `jira_get_all_fields_with_name` | Get all custom field IDs that have the same display name in a Jira project | `project` (string, **required**)<br>`fieldName` (string, **required**) |
| `jira_get_comments` | Get all comments for a specific Jira ticket | `ticket` (object, optional)<br>`key` (string, **required**) |
| `jira_get_components` | Get all components for a specific Jira project | `project` (string, **required**) |
| `jira_get_field_custom_code` | Get the custom field code for a human friendly field name in a Jira project | `project` (string, **required**)<br>`fieldName` (string, **required**) |
| `jira_get_fields` | Get all available fields for a Jira project | `project` (string, **required**) |
| `jira_get_fix_versions` | Get all fix versions for a specific Jira project | `project` (string, **required**) |
| `jira_get_issue_link_types` | Get all available issue link types/relationships in Jira | None |
| `jira_get_issue_types` | Get all available issue types for a specific Jira project | `project` (string, **required**) |
| `jira_get_my_profile` | Get the current user's profile information from Jira | None |
| `jira_get_project_statuses` | Get all statuses for a specific Jira project | `project` (string, **required**) |
| `jira_get_subtasks` | Get all subtasks of a specific Jira ticket using jql: parent = PRJ-123 and issueType in (subtask, sub-task, 'sub task') | `key` (string, **required**) |
| `jira_get_ticket` | Get a specific Jira ticket by key with optional field filtering | `fields` (array, optional)<br>`key` (string, **required**) |
| `jira_get_transitions` | Get all available transitions(statuses, workflows) for a Jira ticket | `key` (string, **required**) |
| `jira_get_user_profile` | Get a specific user's profile information from Jira | `userId` (string, **required**) |
| `jira_link_issues` | Link two Jira issues with a specific relationship type | `sourceKey` (string, **required**)<br>`relationship` (string, **required**)<br>`anotherKey` (string, **required**) |
| `jira_move_to_status` | Move a Jira ticket to a specific status (workflow, transition) | `statusName` (string, **required**)<br>`key` (string, **required**) |
| `jira_move_to_status_with_resolution` | Move a Jira ticket to a specific status (workflow, transition) with resolution | `statusName` (string, **required**)<br>`resolution` (string, **required**)<br>`key` (string, **required**) |
| `jira_post_comment` | Post a comment to a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists | `key` (string, **required**)<br>`comment` (string, **required**) |
| `jira_post_comment_if_not_exists` | Post a comment to a Jira ticket only if it doesn't already exist. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists | `key` (string, **required**)<br>`comment` (string, **required**) |
| `jira_remove_fix_version` | Remove a fix version from a Jira ticket | `fixVersion` (string, **required**)<br>`key` (string, **required**) |
| `jira_search_by_jql` | Search for Jira tickets using JQL and returns all results | `jql` (string, **required**)<br>`fields` (array, optional) |
| `jira_search_by_page` | Search for Jira tickets using JQL with paging support | `jql` (string, **required**)<br>`fields` (array, **required**)<br>`nextPageToken` (string, **required**) |
| `jira_search_with_pagination` | [Deprecated] Search for Jira tickets using JQL with pagination support | `jql` (string, **required**)<br>`fields` (array, **required**)<br>`startAt` (number, **required**) |
| `jira_set_fix_version` | Set the fix version for a Jira ticket | `fixVersion` (string, **required**)<br>`key` (string, **required**) |
| `jira_set_priority` | Set the priority for a Jira ticket | `priority` (string, **required**)<br>`key` (string, **required**) |
| `jira_update_all_fields_with_name` | Update ALL fields with the same name in a Jira ticket. Useful when there are multiple custom fields with the same display name. | `value` (object, **required**)<br>`key` (string, **required**)<br>`fieldName` (string, **required**) |
| `jira_update_description` | Update the description of a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists | `description` (string, **required**)<br>`key` (string, **required**) |
| `jira_update_field` | Update field(s) in a Jira ticket. When using field names (e.g., 'Dependencies'), updates ALL fields with that name. When using custom field IDs (e.g., 'customfield_10091'), updates only that specific field. | `field` (string, **required**)<br>`value` (object, **required**)<br>`key` (string, **required**) |
| `jira_update_ticket` | Update a Jira ticket using JSON parameters following the standard Jira REST API format | `params` (object, **required**)<br>`key` (string, **required**) |
| `jira_update_ticket_parent` | Update the parent of a Jira ticket. Can be used for setting up epic relationships and parent-child relationships for subtasks | `key` (string, **required**)<br>`parentKey` (string, **required**) |
| `jira_xray_add_precondition_to_test` | Add a single precondition to a test issue using X-ray GraphQL API. Returns JSONObject with result. | `testIssueId` (string, **required**)<br>`preconditionIssueId` (string, **required**) |
| `jira_xray_add_preconditions_to_test` | Add multiple preconditions to a test issue using X-ray GraphQL API. Returns JSONArray of results. | `preconditionIssueIds` (object, **required**)<br>`testIssueId` (string, **required**) |
| `jira_xray_add_test_step` | Add a single test step to a test issue using X-ray GraphQL API. Returns JSONObject with created step details. | `result` (string, optional)<br>`action` (string, **required**)<br>`issueId` (string, **required**)<br>`data` (string, optional) |
| `jira_xray_add_test_steps` | Add multiple test steps to a test issue using X-ray GraphQL API. Returns JSONArray of created step objects. | `steps` (object, **required**)<br>`issueId` (string, **required**) |
| `jira_xray_create_precondition` | Create a Precondition issue in Xray with optional steps (converted to definition). Returns the created ticket key. | `summary` (string, **required**)<br>`project` (string, **required**)<br>`description` (string, optional)<br>`steps` (string, optional) |
| `jira_xray_get_precondition_details` | Get Precondition details including definition using X-ray GraphQL API. Returns JSONObject with precondition details. | `preconditionKey` (string, **required**) |
| `jira_xray_get_preconditions` | Get preconditions for a test issue using X-ray GraphQL API. Returns JSONArray of precondition objects. | `testKey` (string, **required**) |
| `jira_xray_get_test_details` | Get test details including steps and preconditions using X-ray GraphQL API. Returns JSONObject with test details. | `testKey` (string, **required**) |
| `jira_xray_get_test_steps` | Get test steps for a test issue using X-ray GraphQL API. Returns JSONArray of test steps. | `testKey` (string, **required**) |
| `jira_xray_search_tickets` | Search for Jira tickets using JQL query and enrich Test/Precondition issues with X-ray test steps and preconditions. Returns list of tickets with X-ray data. | `fields` (array, optional)<br>`searchQueryJQL` (string, **required**) |

## Detailed Parameter Information

### `jira_add_fix_version`

Add a fix version to a Jira ticket (without removing existing ones)

**Parameters:**

- **`fixVersion`** (string) 🔴 Required
  - The fix version name to add

- **`key`** (string) 🔴 Required
  - The Jira ticket key to add fix version to

**Example:**
```bash
dmtools jira_add_fix_version "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_add_fix_version("fixVersion", "key");
```

---

### `jira_add_label`

Adding label to specific ticket key

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to assign
  - Example: `PRJ-123`

- **`label`** (string) 🔴 Required
  - The label to be added to ticket
  - Example: `custom_label`

**Example:**
```bash
dmtools jira_add_label "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_add_label("key", "label");
```

---

### `jira_assign_ticket_to`

Assigns a Jira ticket to user

**Parameters:**

- **`accountId`** (string) 🔴 Required
  - The Jira account ID to assign to. If you know email use first jira_get_account_by_email tools to get account ID
  - Example: `123457:2a123456-40e8-49d6-8ddc-6852e518451f`

- **`key`** (string) 🔴 Required
  - The Jira ticket key to assign
  - Example: `PRJ-123`

**Example:**
```bash
dmtools jira_assign_ticket_to "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_assign_ticket_to("accountId", "key");
```

---

### `jira_attach_file_to_ticket`

Attach a file to a Jira ticket from a local file path. The file will only be attached if a file with the same name doesn't already exist

**Parameters:**

- **`name`** (string) 🔴 Required
  - The name of the file to attach
  - Example: `document.pdf`

- **`ticketKey`** (string) 🔴 Required
  - The Jira ticket key to attach the file to
  - Example: `PRJ-123`

- **`contentType`** (string) ⚪ Optional
  - The content type of the file (e.g., 'application/pdf', 'image/png'). If not provided, defaults to 'image/*'
  - Example: `application/pdf`

- **`filePath`** (string) 🔴 Required
  - Absolute path to the file on disk
  - Example: `/tmp/document.pdf`

**Example:**
```bash
dmtools jira_attach_file_to_ticket "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_attach_file_to_ticket("name", "ticketKey");
```

---

### `jira_clear_field`

Clear (delete value) a specific field value in a Jira ticket

**Parameters:**

- **`field`** (string) 🔴 Required
  - The field name to clear

- **`key`** (string) 🔴 Required
  - The Jira ticket key to clear field from

**Example:**
```bash
dmtools jira_clear_field "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_clear_field("field", "key");
```

---

### `jira_create_ticket_basic`

Create a new Jira ticket with basic fields (project, issue type, summary, description)

**Parameters:**

- **`issueType`** (string) 🔴 Required
  - The type of issue to create (e.g., Bug, Story, Task)

- **`summary`** (string) 🔴 Required
  - The ticket summary/title (e.g., Fix login issue)

- **`project`** (string) 🔴 Required
  - The Jira project key to create the ticket in (e.g., PROJ)

- **`description`** (string) 🔴 Required
  - The ticket description. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists

**Example:**
```bash
dmtools jira_create_ticket_basic "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_create_ticket_basic("issueType", "summary");
```

---

### `jira_create_ticket_with_json`

Create a new Jira ticket with custom fields using JSON configuration

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to create the ticket in (e.g., PROJ)

- **`fieldsJson`** (object) 🔴 Required
  - JSON object containing ticket fields in Jira format (e.g., {"summary": "Ticket Summary", "description": "Ticket Description", "issuetype": {"name": "Task"}, "priority": {"name": "High"}}), Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists

**Example:**
```bash
dmtools jira_create_ticket_with_json "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_create_ticket_with_json("project", "fieldsJson");
```

---

### `jira_create_ticket_with_parent`

Create a new Jira ticket with a parent relationship

**Parameters:**

- **`issueType`** (string) 🔴 Required
  - The type of issue to create (e.g., Bug, Story, Task)

- **`summary`** (string) 🔴 Required
  - The ticket summary/title

- **`project`** (string) 🔴 Required
  - The Jira project key to create the ticket in

- **`description`** (string) 🔴 Required
  - The ticket description

- **`parentKey`** (string) 🔴 Required
  - The key of the parent ticket

**Example:**
```bash
dmtools jira_create_ticket_with_parent "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_create_ticket_with_parent("issueType", "summary");
```

---

### `jira_delete_ticket`

Delete a Jira ticket by key

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to delete
  - Example: `PRJ-123`

**Example:**
```bash
dmtools jira_delete_ticket "value"
```

```javascript
// In JavaScript agent
const result = jira_delete_ticket("key");
```

---

### `jira_download_attachment`

Download a Jira attachment by URL and save it as a file

**Parameters:**

- **`href`** (string) 🔴 Required
  - The attachment URL to download

**Example:**
```bash
dmtools jira_download_attachment "value"
```

```javascript
// In JavaScript agent
const result = jira_download_attachment("href");
```

---

### `jira_execute_request`

Execute a custom HTTP GET request to Jira API with auth. Can be used to perform any jira get requests which are required auth.

**Parameters:**

- **`url`** (string) 🔴 Required
  - The Jira API URL to execute

**Example:**
```bash
dmtools jira_execute_request "value"
```

```javascript
// In JavaScript agent
const result = jira_execute_request("url");
```

---

### `jira_get_account_by_email`

Gets account details by email

**Parameters:**

- **`email`** (string) 🔴 Required
  - The Jira Email
  - Example: `email@email.com`

**Example:**
```bash
dmtools jira_get_account_by_email "value"
```

```javascript
// In JavaScript agent
const result = jira_get_account_by_email("email");
```

---

### `jira_get_all_fields_with_name`

Get all custom field IDs that have the same display name in a Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key

- **`fieldName`** (string) 🔴 Required
  - The user-friendly field name

**Example:**
```bash
dmtools jira_get_all_fields_with_name "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_get_all_fields_with_name("project", "fieldName");
```

---

### `jira_get_comments`

Get all comments for a specific Jira ticket

**Parameters:**

- **`ticket`** (object) ⚪ Optional
  - Optional ticket object for cache validation

- **`key`** (string) 🔴 Required
  - The Jira ticket key to get comments for

**Example:**
```bash
dmtools jira_get_comments "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_get_comments("ticket", "key");
```

---

### `jira_get_components`

Get all components for a specific Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to get components for

**Example:**
```bash
dmtools jira_get_components "value"
```

```javascript
// In JavaScript agent
const result = jira_get_components("project");
```

---

### `jira_get_field_custom_code`

Get the custom field code for a human friendly field name in a Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key

- **`fieldName`** (string) 🔴 Required
  - The human-readable field name

**Example:**
```bash
dmtools jira_get_field_custom_code "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_get_field_custom_code("project", "fieldName");
```

---

### `jira_get_fields`

Get all available fields for a Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to get fields for

**Example:**
```bash
dmtools jira_get_fields "value"
```

```javascript
// In JavaScript agent
const result = jira_get_fields("project");
```

---

### `jira_get_fix_versions`

Get all fix versions for a specific Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to get fix versions for

**Example:**
```bash
dmtools jira_get_fix_versions "value"
```

```javascript
// In JavaScript agent
const result = jira_get_fix_versions("project");
```

---

### `jira_get_issue_link_types`

Get all available issue link types/relationships in Jira

**Parameters:** None

**Example:**
```bash
dmtools jira_get_issue_link_types
```

```javascript
// In JavaScript agent
const result = jira_get_issue_link_types();
```

---

### `jira_get_issue_types`

Get all available issue types for a specific Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to get issue types for

**Example:**
```bash
dmtools jira_get_issue_types "value"
```

```javascript
// In JavaScript agent
const result = jira_get_issue_types("project");
```

---

### `jira_get_my_profile`

Get the current user's profile information from Jira

**Parameters:** None

**Example:**
```bash
dmtools jira_get_my_profile
```

```javascript
// In JavaScript agent
const result = jira_get_my_profile();
```

---

### `jira_get_project_statuses`

Get all statuses for a specific Jira project

**Parameters:**

- **`project`** (string) 🔴 Required
  - The Jira project key to get statuses for

**Example:**
```bash
dmtools jira_get_project_statuses "value"
```

```javascript
// In JavaScript agent
const result = jira_get_project_statuses("project");
```

---

### `jira_get_subtasks`

Get all subtasks of a specific Jira ticket using jql: parent = PRJ-123 and issueType in (subtask, sub-task, 'sub task')

**Parameters:**

- **`key`** (string) 🔴 Required
  - The parent ticket key to get subtasks for

**Example:**
```bash
dmtools jira_get_subtasks "value"
```

```javascript
// In JavaScript agent
const result = jira_get_subtasks("key");
```

---

### `jira_get_ticket`

Get a specific Jira ticket by key with optional field filtering

**Parameters:**

- **`fields`** (array) ⚪ Optional
  - Optional array of fields to include in the response

- **`key`** (string) 🔴 Required
  - The Jira ticket key to retrieve

**Example:**
```bash
dmtools jira_get_ticket "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_get_ticket("fields", "key");
```

---

### `jira_get_transitions`

Get all available transitions(statuses, workflows) for a Jira ticket

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to get transitions for

**Example:**
```bash
dmtools jira_get_transitions "value"
```

```javascript
// In JavaScript agent
const result = jira_get_transitions("key");
```

---

### `jira_get_user_profile`

Get a specific user's profile information from Jira

**Parameters:**

- **`userId`** (string) 🔴 Required
  - The user ID to get profile for

**Example:**
```bash
dmtools jira_get_user_profile "value"
```

```javascript
// In JavaScript agent
const result = jira_get_user_profile("userId");
```

---

### `jira_link_issues`

Link two Jira issues with a specific relationship type

**Parameters:**

- **`sourceKey`** (string) 🔴 Required
  - The source issue key

- **`relationship`** (string) 🔴 Required
  - The relationship type name

- **`anotherKey`** (string) 🔴 Required
  - The target issue key

**Example:**
```bash
dmtools jira_link_issues "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_link_issues("sourceKey", "relationship");
```

---

### `jira_move_to_status`

Move a Jira ticket to a specific status (workflow, transition)

**Parameters:**

- **`statusName`** (string) 🔴 Required
  - The target status name

- **`key`** (string) 🔴 Required
  - The Jira ticket key to move

**Example:**
```bash
dmtools jira_move_to_status "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_move_to_status("statusName", "key");
```

---

### `jira_move_to_status_with_resolution`

Move a Jira ticket to a specific status (workflow, transition) with resolution

**Parameters:**

- **`statusName`** (string) 🔴 Required
  - The target status name

- **`resolution`** (string) 🔴 Required
  - The resolution to set

- **`key`** (string) 🔴 Required
  - The Jira ticket key to move

**Example:**
```bash
dmtools jira_move_to_status_with_resolution "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_move_to_status_with_resolution("statusName", "resolution");
```

---

### `jira_post_comment`

Post a comment to a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to post comment to

- **`comment`** (string) 🔴 Required
  - The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)

**Example:**
```bash
dmtools jira_post_comment "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_post_comment("key", "comment");
```

---

### `jira_post_comment_if_not_exists`

Post a comment to a Jira ticket only if it doesn't already exist. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to post comment to

- **`comment`** (string) 🔴 Required
  - The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)

**Example:**
```bash
dmtools jira_post_comment_if_not_exists "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_post_comment_if_not_exists("key", "comment");
```

---

### `jira_remove_fix_version`

Remove a fix version from a Jira ticket

**Parameters:**

- **`fixVersion`** (string) 🔴 Required
  - The fix version name to remove

- **`key`** (string) 🔴 Required
  - The Jira ticket key to remove fix version from

**Example:**
```bash
dmtools jira_remove_fix_version "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_remove_fix_version("fixVersion", "key");
```

---

### `jira_search_by_jql`

Search for Jira tickets using JQL and returns all results

**Parameters:**

- **`jql`** (string) 🔴 Required
  - JQL query string to search tickets
  - Example: `project = DEMO AND status = Open`

- **`fields`** (array) ⚪ Optional
  - Optional array of field names to include in response
  - Example: `["summary", "status", "assignee"]`

**Example:**
```bash
dmtools jira_search_by_jql "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_search_by_jql("jql", "fields");
```

---

### `jira_search_by_page`

Search for Jira tickets using JQL with paging support

**Parameters:**

- **`jql`** (string) 🔴 Required
  - JQL query string to search for tickets
  - Example: `project = PROJ AND status = Open`

- **`fields`** (array) 🔴 Required
  - Array of field names to include in the response
  - Example: `['summary', 'status', 'assignee']`

- **`nextPageToken`** (string) 🔴 Required
  - Next Page Token from previous response, empty by default for 1 page
  - Example: `AasvvasasaSASdada`

**Example:**
```bash
dmtools jira_search_by_page "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_search_by_page("jql", "fields");
```

---

### `jira_search_with_pagination`

[Deprecated] Search for Jira tickets using JQL with pagination support

**Parameters:**

- **`jql`** (string) 🔴 Required
  - JQL query string to search for tickets
  - Example: `project = PROJ AND status = Open`

- **`fields`** (array) 🔴 Required
  - Array of field names to include in the response
  - Example: `['summary', 'status', 'assignee']`

- **`startAt`** (number) 🔴 Required
  - Starting index for pagination (0-based)
  - Example: `0`

**Example:**
```bash
dmtools jira_search_with_pagination "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_search_with_pagination("jql", "fields");
```

---

### `jira_set_fix_version`

Set the fix version for a Jira ticket

**Parameters:**

- **`fixVersion`** (string) 🔴 Required
  - The fix version name to set

- **`key`** (string) 🔴 Required
  - The Jira ticket key to set fix version for

**Example:**
```bash
dmtools jira_set_fix_version "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_set_fix_version("fixVersion", "key");
```

---

### `jira_set_priority`

Set the priority for a Jira ticket

**Parameters:**

- **`priority`** (string) 🔴 Required
  - The priority name to set

- **`key`** (string) 🔴 Required
  - The Jira ticket key to set priority for

**Example:**
```bash
dmtools jira_set_priority "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_set_priority("priority", "key");
```

---

### `jira_update_all_fields_with_name`

Update ALL fields with the same name in a Jira ticket. Useful when there are multiple custom fields with the same display name.

**Parameters:**

- **`value`** (object) 🔴 Required
  - The new value for the fields

- **`key`** (string) 🔴 Required
  - The Jira ticket key to update

- **`fieldName`** (string) 🔴 Required
  - The user-friendly field name (e.g., 'Dependencies')

**Example:**
```bash
dmtools jira_update_all_fields_with_name "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_update_all_fields_with_name("value", "key");
```

---

### `jira_update_description`

Update the description of a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists

**Parameters:**

- **`description`** (string) 🔴 Required
  - The new description text (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)

- **`key`** (string) 🔴 Required
  - The Jira ticket key to update

**Example:**
```bash
dmtools jira_update_description "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_update_description("description", "key");
```

---

### `jira_update_field`

Update field(s) in a Jira ticket. When using field names (e.g., 'Dependencies'), updates ALL fields with that name. When using custom field IDs (e.g., 'customfield_10091'), updates only that specific field.

**Parameters:**

- **`field`** (string) 🔴 Required
  - The field to update. Use field name (e.g., 'Dependencies') to update ALL fields with that name, or custom field ID (e.g., 'customfield_10091') to update specific field

- **`value`** (object) 🔴 Required
  - The new value for the field(s)

- **`key`** (string) 🔴 Required
  - The Jira ticket key to update

**Example:**
```bash
dmtools jira_update_field "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_update_field("field", "value");
```

---

### `jira_update_ticket`

Update a Jira ticket using JSON parameters following the standard Jira REST API format

**Parameters:**

- **`params`** (object) 🔴 Required
  - JSON object containing update parameters in Jira format (e.g., {"fields": {"summary": "New Summary", "parent": {"key": "PROJ-123"}}})

- **`key`** (string) 🔴 Required
  - The Jira ticket key to update

**Example:**
```bash
dmtools jira_update_ticket "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_update_ticket("params", "key");
```

---

### `jira_update_ticket_parent`

Update the parent of a Jira ticket. Can be used for setting up epic relationships and parent-child relationships for subtasks

**Parameters:**

- **`key`** (string) 🔴 Required
  - The Jira ticket key to update

- **`parentKey`** (string) 🔴 Required
  - The key of the new parent ticket

**Example:**
```bash
dmtools jira_update_ticket_parent "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_update_ticket_parent("key", "parentKey");
```

---

### `jira_xray_add_precondition_to_test`

Add a single precondition to a test issue using X-ray GraphQL API. Returns JSONObject with result.

**Parameters:**

- **`testIssueId`** (string) 🔴 Required
  - Jira issue ID of the test (e.g., '12345')
  - Example: `12345`

- **`preconditionIssueId`** (string) 🔴 Required
  - Jira issue ID of the precondition (e.g., '12346')
  - Example: `12346`

**Example:**
```bash
dmtools jira_xray_add_precondition_to_test "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_add_precondition_to_test("testIssueId", "preconditionIssueId");
```

---

### `jira_xray_add_preconditions_to_test`

Add multiple preconditions to a test issue using X-ray GraphQL API. Returns JSONArray of results.

**Parameters:**

- **`preconditionIssueIds`** (object) 🔴 Required
  - JSON array string of precondition issue IDs (e.g., '["12346", "12347"]')
  - Example: `["12346", "12347"]`

- **`testIssueId`** (string) 🔴 Required
  - Jira issue ID of the test (e.g., '12345')
  - Example: `12345`

**Example:**
```bash
dmtools jira_xray_add_preconditions_to_test "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_add_preconditions_to_test("preconditionIssueIds", "testIssueId");
```

---

### `jira_xray_add_test_step`

Add a single test step to a test issue using X-ray GraphQL API. Returns JSONObject with created step details.

**Parameters:**

- **`result`** (string) ⚪ Optional
  - Step expected result (e.g., 'Username accepted')
  - Example: `Username accepted`

- **`action`** (string) 🔴 Required
  - Step action (e.g., 'Enter username')
  - Example: `Enter username`

- **`issueId`** (string) 🔴 Required
  - Jira issue ID (e.g., '12345')
  - Example: `12345`

- **`data`** (string) ⚪ Optional
  - Step data (e.g., 'test_user')
  - Example: `test_user`

**Example:**
```bash
dmtools jira_xray_add_test_step "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_add_test_step("result", "action");
```

---

### `jira_xray_add_test_steps`

Add multiple test steps to a test issue using X-ray GraphQL API. Returns JSONArray of created step objects.

**Parameters:**

- **`steps`** (object) 🔴 Required
  - JSON array string of step objects, each with 'action', 'data', and 'result' fields (e.g., '[{"action":"Enter username","data":"test_user","result":"Username accepted"}]')
  - Example: `[{"action":"Enter username","data":"test_user","result":"Username accepted"}]`

- **`issueId`** (string) 🔴 Required
  - Jira issue ID (e.g., '12345')
  - Example: `12345`

**Example:**
```bash
dmtools jira_xray_add_test_steps "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_add_test_steps("steps", "issueId");
```

---

### `jira_xray_create_precondition`

Create a Precondition issue in Xray with optional steps (converted to definition). Returns the created ticket key.

**Parameters:**

- **`summary`** (string) 🔴 Required
  - Precondition summary
  - Example: `System is ready for testing`

- **`project`** (string) 🔴 Required
  - Project key (e.g., 'TP')
  - Example: `TP`

- **`description`** (string) ⚪ Optional
  - Precondition description
  - Example: `All system components are initialized`

- **`steps`** (string) ⚪ Optional
  - Optional JSON array of steps in format [{"action": "...", "data": "...", "result": "..."}]. Will be converted to definition format.

**Example:**
```bash
dmtools jira_xray_create_precondition "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_create_precondition("summary", "project");
```

---

### `jira_xray_get_precondition_details`

Get Precondition details including definition using X-ray GraphQL API. Returns JSONObject with precondition details.

**Parameters:**

- **`preconditionKey`** (string) 🔴 Required
  - Jira ticket key (e.g., 'TP-910')
  - Example: `TP-910`

**Example:**
```bash
dmtools jira_xray_get_precondition_details "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_get_precondition_details("preconditionKey");
```

---

### `jira_xray_get_preconditions`

Get preconditions for a test issue using X-ray GraphQL API. Returns JSONArray of precondition objects.

**Parameters:**

- **`testKey`** (string) 🔴 Required
  - Jira ticket key (e.g., 'TP-909')
  - Example: `TP-909`

**Example:**
```bash
dmtools jira_xray_get_preconditions "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_get_preconditions("testKey");
```

---

### `jira_xray_get_test_details`

Get test details including steps and preconditions using X-ray GraphQL API. Returns JSONObject with test details.

**Parameters:**

- **`testKey`** (string) 🔴 Required
  - Jira ticket key (e.g., 'TP-909')
  - Example: `TP-909`

**Example:**
```bash
dmtools jira_xray_get_test_details "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_get_test_details("testKey");
```

---

### `jira_xray_get_test_steps`

Get test steps for a test issue using X-ray GraphQL API. Returns JSONArray of test steps.

**Parameters:**

- **`testKey`** (string) 🔴 Required
  - Jira ticket key (e.g., 'TP-909')
  - Example: `TP-909`

**Example:**
```bash
dmtools jira_xray_get_test_steps "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_get_test_steps("testKey");
```

---

### `jira_xray_search_tickets`

Search for Jira tickets using JQL query and enrich Test/Precondition issues with X-ray test steps and preconditions. Returns list of tickets with X-ray data.

**Parameters:**

- **`fields`** (array) ⚪ Optional
  - Array of field names to retrieve (e.g., ['summary', 'description', 'status'])
  - Example: `summary,description,status`

- **`searchQueryJQL`** (string) 🔴 Required
  - JQL search query (e.g., 'project = TP AND issueType = Test')
  - Example: `project = TP AND issueType = Test`

**Example:**
```bash
dmtools jira_xray_search_tickets "value" "value"
```

```javascript
// In JavaScript agent
const result = jira_xray_search_tickets("fields", "searchQueryJQL");
```

---

