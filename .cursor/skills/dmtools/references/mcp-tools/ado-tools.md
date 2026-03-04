# ADO MCP Tools

**Total Tools**: 14

## Quick Reference

```bash
# List all ado tools
dmtools list | jq '.tools[] | select(.name | startswith("ado_"))'

# Example usage
dmtools ado_get_work_item [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for ado tools
const result = ado_get_work_item(...);
const result = ado_search_by_wiql(...);
const result = ado_get_comments(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `ado_assign_work_item` | Assign a work item to a user | `userEmail` (string, **required**)<br>`id` (string, **required**) |
| `ado_create_work_item` | Create a new work item in Azure DevOps | `workItemType` (string, **required**)<br>`project` (string, **required**)<br>`description` (string, optional)<br>`fieldsJson` (object, optional)<br>`title` (string, **required**) |
| `ado_download_attachment` | Download an ADO work item attachment by URL and save it as a file | `href` (string, **required**) |
| `ado_get_changelog` | Get the complete history/changelog of a work item | `ticket` (object, optional)<br>`id` (string, **required**) |
| `ado_get_comments` | Get all comments for a work item | `ticket` (object, **required**)<br>`id` (string, **required**) |
| `ado_get_my_profile` | Get the current user's profile information from Azure DevOps | None |
| `ado_get_user_by_email` | Get user information by email address in Azure DevOps | `email` (string, **required**) |
| `ado_get_work_item` | Get a specific Azure DevOps work item by ID with optional field filtering | `fields` (array, optional)<br>`id` (string, **required**) |
| `ado_link_work_items` | Link two work items with a relationship (e.g., Parent-Child, Related, Tested By) | `sourceId` (string, **required**)<br>`targetId` (string, **required**)<br>`relationship` (string, **required**) |
| `ado_move_to_state` | Move a work item to a specific state | `id` (string, **required**)<br>`state` (string, **required**) |
| `ado_post_comment` | Post a comment to a work item | `comment` (string, **required**)<br>`id` (string, **required**) |
| `ado_search_by_wiql` | Search for work items using WIQL (Work Item Query Language) | `fields` (array, optional)<br>`wiql` (string, **required**) |
| `ado_update_description` | Update the description of a work item | `description` (string, **required**)<br>`id` (string, **required**) |
| `ado_update_tags` | Update the tags of a work item (semicolon-separated string) | `id` (string, **required**)<br>`tags` (string, **required**) |

## Detailed Parameter Information

### `ado_assign_work_item`

Assign a work item to a user

**Parameters:**

- **`userEmail`** (string) 🔴 Required
  - The user email or display name

- **`id`** (string) 🔴 Required
  - The work item ID

**Example:**
```bash
dmtools ado_assign_work_item "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_assign_work_item("userEmail", "id");
```

---

### `ado_create_work_item`

Create a new work item in Azure DevOps

**Parameters:**

- **`workItemType`** (string) 🔴 Required
  - The work item type (Bug, Task, User Story, etc.)

- **`project`** (string) 🔴 Required
  - The project name

- **`description`** (string) ⚪ Optional
  - The work item description (HTML)

- **`fieldsJson`** (object) ⚪ Optional
  - Additional fields as JSON object (e.g., {"Microsoft.VSTS.Common.Priority": 1})

- **`title`** (string) 🔴 Required
  - The work item title

**Example:**
```bash
dmtools ado_create_work_item "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_create_work_item("workItemType", "project");
```

---

### `ado_download_attachment`

Download an ADO work item attachment by URL and save it as a file

**Parameters:**

- **`href`** (string) 🔴 Required
  - The attachment URL to download

**Example:**
```bash
dmtools ado_download_attachment "value"
```

```javascript
// In JavaScript agent
const result = ado_download_attachment("href");
```

---

### `ado_get_changelog`

Get the complete history/changelog of a work item

**Parameters:**

- **`ticket`** (object) ⚪ Optional
  - Optional work item object (can be null)

- **`id`** (string) 🔴 Required
  - The work item ID

**Example:**
```bash
dmtools ado_get_changelog "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_get_changelog("ticket", "id");
```

---

### `ado_get_comments`

Get all comments for a work item

**Parameters:**

- **`ticket`** (object) 🔴 Required
  - Parameter ticket

- **`id`** (string) 🔴 Required
  - The work item ID

**Example:**
```bash
dmtools ado_get_comments "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_get_comments("ticket", "id");
```

---

### `ado_get_my_profile`

Get the current user's profile information from Azure DevOps

**Parameters:** None

**Example:**
```bash
dmtools ado_get_my_profile
```

```javascript
// In JavaScript agent
const result = ado_get_my_profile();
```

---

### `ado_get_user_by_email`

Get user information by email address in Azure DevOps

**Parameters:**

- **`email`** (string) 🔴 Required
  - User email address

**Example:**
```bash
dmtools ado_get_user_by_email "value"
```

```javascript
// In JavaScript agent
const result = ado_get_user_by_email("email");
```

---

### `ado_get_work_item`

Get a specific Azure DevOps work item by ID with optional field filtering

**Parameters:**

- **`fields`** (array) ⚪ Optional
  - Optional array of fields to include in the response

- **`id`** (string) 🔴 Required
  - The work item ID (numeric)
  - Example: `12345`

**Example:**
```bash
dmtools ado_get_work_item "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_get_work_item("fields", "id");
```

---

### `ado_link_work_items`

Link two work items with a relationship (e.g., Parent-Child, Related, Tested By)

**Parameters:**

- **`sourceId`** (string) 🔴 Required
  - The source work item ID

- **`targetId`** (string) 🔴 Required
  - The target work item ID to link to

- **`relationship`** (string) 🔴 Required
  - Relationship type (e.g., 'parent', 'child', 'related', 'tested by', 'tests')
  - Example: `parent`

**Example:**
```bash
dmtools ado_link_work_items "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_link_work_items("sourceId", "targetId");
```

---

### `ado_move_to_state`

Move a work item to a specific state

**Parameters:**

- **`id`** (string) 🔴 Required
  - The work item ID

- **`state`** (string) 🔴 Required
  - The target state name
  - Example: `Active`

**Example:**
```bash
dmtools ado_move_to_state "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_move_to_state("id", "state");
```

---

### `ado_post_comment`

Post a comment to a work item

**Parameters:**

- **`comment`** (string) 🔴 Required
  - The comment text

- **`id`** (string) 🔴 Required
  - The work item ID

**Example:**
```bash
dmtools ado_post_comment "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_post_comment("comment", "id");
```

---

### `ado_search_by_wiql`

Search for work items using WIQL (Work Item Query Language)

**Parameters:**

- **`fields`** (array) ⚪ Optional
  - Optional array of fields to include

- **`wiql`** (string) 🔴 Required
  - WIQL query string
  - Example: `SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'Bug'`

**Example:**
```bash
dmtools ado_search_by_wiql "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_search_by_wiql("fields", "wiql");
```

---

### `ado_update_description`

Update the description of a work item

**Parameters:**

- **`description`** (string) 🔴 Required
  - The new description (HTML format)

- **`id`** (string) 🔴 Required
  - The work item ID

**Example:**
```bash
dmtools ado_update_description "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_update_description("description", "id");
```

---

### `ado_update_tags`

Update the tags of a work item (semicolon-separated string)

**Parameters:**

- **`id`** (string) 🔴 Required
  - The work item ID

- **`tags`** (string) 🔴 Required
  - Tags as semicolon-separated string (e.g., 'tag1;tag2;tag3')

**Example:**
```bash
dmtools ado_update_tags "value" "value"
```

```javascript
// In JavaScript agent
const result = ado_update_tags("id", "tags");
```

---

