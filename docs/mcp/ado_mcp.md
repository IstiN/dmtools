# Azure DevOps MCP Integration Guide

## Overview

This guide explains how to use Azure DevOps (ADO) MCP (Model Context Protocol) tools to manage work items, track progress, and automate DevOps workflows. The integration provides a comprehensive set of methods for work item management, search, comments, linking, and user profile operations.

## Table of Contents

1. [Available MCP Methods](#available-mcp-methods)
2. [Quick Start](#quick-start)
3. [Work Item Management Workflow](#work-item-management-workflow)
4. [Method Details](#method-details)
5. [Practical Examples](#practical-examples)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## Available MCP Methods

### Work Item Operations (4 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_get_work_item` ⭐ | Get work item by ID with optional field filtering | Work item ID, optional fields array | WorkItem object with all fields |
| `ado_create_work_item` ⭐ | Create a new work item | Project, type, title, description, optional fields | Work item ID (in response JSON) |
| `ado_update_description` | Update work item description | Work item ID, new description (HTML) | Update response |
| `ado_move_to_state` | Change work item state/status | Work item ID, target state name | Update response |

### Search & Query (1 method)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_search_by_wiql` ⭐ | Search work items using WIQL query language | WIQL query string, optional fields | List of WorkItem objects |

### Assignment & Linking (2 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_assign_work_item` | Assign work item to a user | Work item ID, user email/name | Update response |
| `ado_link_work_items` | Link two work items with relationship | Source ID, target ID, relationship type | Link response |

### Comments (2 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_get_comments` | Get all comments for a work item | Work item ID | List of Comment objects |
| `ado_post_comment` | Post a comment to a work item | Work item ID, comment text | Success response |

### History & Profile (2 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_get_changelog` | Get work item history/changelog | Work item ID | Changelog with history entries |
| `ado_get_my_profile` | Get current user profile information | None | User profile object |

### File Management (1 method)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `ado_download_attachment` | Download attachment by URL | Attachment URL | File object |

**Total: 12 MCP methods**

---

## Quick Start

### Problem: Create and Manage Work Items in Azure DevOps

Given an Azure DevOps organization and project, you need to:
1. Create work items (User Stories, Tasks, Bugs, Test Cases)
2. Search for work items
3. Update work items
4. Link work items together
5. Track changes and history

### Solution: Use ADO MCP Tools

```java
// Step 1: Create a User Story
String storyResponse = adoClient.createTicketInProject(
    "MyProject",
    "User Story",
    "As a user, I want to login",
    "<p>User story description</p>",
    null
);
JSONObject storyJson = new JSONObject(storyResponse);
String storyId = storyJson.getString("id");

// Step 2: Create a Task linked to the story
String taskResponse = adoClient.createTicketInProject(
    "MyProject",
    "Task",
    "Implement login API",
    "<p>Task description</p>",
    null
);
JSONObject taskJson = new JSONObject(taskResponse);
String taskId = taskJson.getString("id");

// Step 3: Link task to story as child
adoClient.linkIssueWithRelationship(taskId, storyId, "child");

// Step 4: Assign task to user
adoClient.assignTo(taskId, "user@company.com");

// Step 5: Move task to Active state
adoClient.moveToStatus(taskId, "Active");

// Step 6: Add a comment
adoClient.postComment(taskId, "Started working on this task");

// Step 7: Search for all active tasks
String wiql = "SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'Task' AND [System.State] = 'Active'";
List<WorkItem> activeTasks = adoClient.searchAndPerform(wiql, adoClient.getDefaultQueryFields());
```

---

## Work Item Management Workflow

### Typical Workflow: Story → Tasks → Test Cases

```
1. Create User Story
   → ado_create_work_item(type: "User Story")
   → Returns: Story ID

2. Create Tasks for the Story
   → ado_create_work_item(type: "Task")
   → Link each task to story: ado_link_work_items(taskId, storyId, "child")

3. Create Test Cases
   → ado_create_work_item(type: "Test Case")
   → Link test case to story: ado_link_work_items(testCaseId, storyId, "tested by")

4. Assign Tasks
   → ado_assign_work_item(taskId, userEmail)

5. Track Progress
   → ado_move_to_state(taskId, "Active")
   → ado_move_to_state(taskId, "Resolved")
   → ado_move_to_state(taskId, "Closed")

6. Add Comments
   → ado_post_comment(taskId, "Progress update")

7. Review History
   → ado_get_changelog(taskId)
   → See all state changes, field updates, comments
```

### State Transition Flow

```
New → Active → Resolved → Closed
  ↓       ↓
Removed  Removed (if supported)
```

**Note**: Not all work item types support all states. Test Cases typically don't support "Removed" state.

---

## Method Details

### 1. ado_get_work_item ⭐

**Purpose**: Retrieve a specific work item by ID with optional field filtering.

**Signature**:
```java
public WorkItem performTicket(
    String workItemId,
    String[] fields  // Optional: specific fields to include
)
```

**Parameters**:
- `id` (required) - Work item ID (numeric string, e.g., "12345")
- `fields` (optional) - Array of field names to include
  - Friendly names: `["title", "state", "priority"]`
  - ADO field names: `["System.Title", "System.State", "Microsoft.VSTS.Common.Priority"]`
  - If null, returns all fields

**Returns**: `WorkItem` object with:
- `getTicketKey()` - Work item ID
- `getTicketTitle()` - Title
- `getTicketDescription()` - Description (HTML)
- `getStatus()` - Current state
- `getIssueType()` - Work item type (Bug, Task, User Story, etc.)
- `getPriority()` - Priority value
- `getCreator()` - User who created it
- `getAssignee()` - Assigned user
- `getCreated()` - Creation date
- `getUpdated()` - Last update date

**When to use**:
- ✅ Get full work item details
- ✅ Check current state/status
- ✅ Retrieve specific fields only (performance optimization)
- ✅ Verify work item exists

**Example**:
```java
// Get all fields
WorkItem workItem = adoClient.performTicket("12345", null);

// Get specific fields only (faster)
WorkItem workItem = adoClient.performTicket("12345", 
    new String[]{"title", "state", "priority", "assignedTo"});

// Use friendly field names
WorkItem workItem = adoClient.performTicket("12345", 
    new String[]{"title", "state"});  // Automatically resolves to System.Title, System.State
```

---

### 2. ado_create_work_item ⭐

**Purpose**: Create a new work item in Azure DevOps.

**Signature**:
```java
public String createTicketInProject(
    String projectName,
    String workItemType,
    String title,
    String description,  // Optional, HTML format
    FieldsInitializer fieldsInitializer  // Optional, for custom fields
)
```

**Parameters**:
- `project` (required) - Project name
- `workItemType` (required) - Type: "Bug", "Task", "User Story", "Test Case", "Epic", etc.
- `title` (required) - Work item title
- `description` (optional) - Description in HTML format
- `fieldsInitializer` (optional) - Lambda for setting custom fields

**Returns**: `String` - JSON response containing:
- `id` - Created work item ID
- `rev` - Revision number
- `fields` - All field values
- `url` - Work item API URL

**When to use**:
- ✅ Create new work items
- ✅ Initialize work items with custom fields
- ✅ Bulk creation of work items

**Example**:
```java
// Simple creation
String response = adoClient.createTicketInProject(
    "MyProject",
    "Task",
    "Fix login bug",
    "<p>User cannot login with email</p>",
    null
);

// With custom fields
String response = adoClient.createTicketInProject(
    "MyProject",
    "User Story",
    "User login feature",
    "<p>As a user, I want to login</p>",
    fields -> {
        fields.set("Microsoft.VSTS.Scheduling.StoryPoints", 5);
        fields.set("Microsoft.VSTS.Common.Priority", 1);
        fields.set("System.Tags", "feature;authentication");
    }
);

// Parse response to get ID
JSONObject json = new JSONObject(response);
String workItemId = json.getString("id");
```

**Supported Work Item Types**:
- Bug
- Task
- User Story
- Test Case
- Epic
- Feature
- Issue
- (And any custom types defined in your project)

---

### 3. ado_search_by_wiql ⭐

**Purpose**: Search for work items using WIQL (Work Item Query Language).

**Signature**:
```java
public List<WorkItem> searchAndPerform(
    String wiqlQuery,
    String[] fields  // Optional: fields to include in results
)
```

**Parameters**:
- `wiql` (required) - WIQL query string
- `fields` (optional) - Fields to include in results

**Returns**: `List<WorkItem>` - Matching work items

**When to use**:
- ✅ Find work items by criteria
- ✅ Search across multiple projects
- ✅ Complex queries with multiple conditions
- ✅ Bulk retrieval of work items

**WIQL Syntax Examples**:
```sql
-- Find all active bugs
SELECT [System.Id] FROM WorkItems 
WHERE [System.WorkItemType] = 'Bug' 
  AND [System.State] = 'Active'

-- Find tasks assigned to specific user
SELECT [System.Id] FROM WorkItems 
WHERE [System.WorkItemType] = 'Task' 
  AND [System.AssignedTo] = 'user@company.com'

-- Find work items created in last 7 days
SELECT [System.Id] FROM WorkItems 
WHERE [System.CreatedDate] >= @today - 7

-- Find work items with specific tag
SELECT [System.Id] FROM WorkItems 
WHERE [System.Tags] CONTAINS 'urgent'

-- Find work items by title
SELECT [System.Id] FROM WorkItems 
WHERE [System.Title] CONTAINS 'login'
```

**Example**:
```java
// Search for active bugs
String wiql = "SELECT [System.Id] FROM WorkItems " +
              "WHERE [System.WorkItemType] = 'Bug' " +
              "AND [System.State] = 'Active'";
List<WorkItem> bugs = adoClient.searchAndPerform(wiql, adoClient.getDefaultQueryFields());

// Search with specific fields
String wiql = "SELECT [System.Id] FROM WorkItems " +
              "WHERE [System.Title] CONTAINS 'login'";
List<WorkItem> results = adoClient.searchAndPerform(wiql, 
    new String[]{"title", "state", "priority", "assignedTo"});

// Process results
for (WorkItem item : results) {
    System.out.println("ID: " + item.getTicketKey());
    System.out.println("Title: " + item.getTicketTitle());
    System.out.println("State: " + item.getStatus());
}
```

**Performance Notes**:
- ADO supports up to 200 work items per batch
- Large result sets are automatically paginated
- Use field filtering to reduce response size

---

### 4. ado_update_description

**Purpose**: Update the description of a work item.

**Signature**:
```java
public String updateDescription(
    String workItemId,
    String description  // HTML format
)
```

**Parameters**:
- `id` (required) - Work item ID
- `description` (required) - New description in HTML format

**Returns**: `String` - Update response JSON

**When to use**:
- ✅ Update work item description
- ✅ Add detailed information
- ✅ Format description with HTML

**Example**:
```java
String newDescription = "<p>Updated description with:</p>" +
                       "<ul>" +
                       "<li>Point 1</li>" +
                       "<li>Point 2</li>" +
                       "</ul>";
adoClient.updateDescription("12345", newDescription);
```

---

### 5. ado_move_to_state

**Purpose**: Change the state/status of a work item.

**Signature**:
```java
public String moveToStatus(
    String workItemId,
    String stateName
)
```

**Parameters**:
- `id` (required) - Work item ID
- `state` (required) - Target state name (e.g., "Active", "Resolved", "Closed", "Removed")

**Returns**: `String` - Update response JSON

**When to use**:
- ✅ Move work items through workflow states
- ✅ Close completed work items
- ✅ Mark work items as removed

**Common States**:
- **New** - Initial state
- **Active** - Work in progress
- **Resolved** - Completed, pending verification
- **Closed** - Verified and closed
- **Removed** - Deleted (not all types support this)

**Example**:
```java
// Move to Active
adoClient.moveToStatus("12345", "Active");

// Move to Resolved
adoClient.moveToStatus("12345", "Resolved");

// Close work item
adoClient.moveToStatus("12345", "Closed");
```

**Note**: State names are case-sensitive and must match your project's workflow configuration.

---

### 6. ado_assign_work_item

**Purpose**: Assign a work item to a user.

**Signature**:
```java
public String assignTo(
    String workItemId,
    String userIdentity  // Email or display name
)
```

**Parameters**:
- `id` (required) - Work item ID
- `userEmail` (required) - User email address or display name

**Returns**: `String` - Update response JSON

**When to use**:
- ✅ Assign work items to team members
- ✅ Reassign work items
- ✅ Clear assignment (set to null/empty)

**Example**:
```java
// Assign by email
adoClient.assignTo("12345", "user@company.com");

// Assign by display name
adoClient.assignTo("12345", "John Doe");

// Unassign (clear assignment)
adoClient.assignTo("12345", "");
```

---

### 7. ado_link_work_items

**Purpose**: Create a relationship/link between two work items.

**Signature**:
```java
public String linkIssueWithRelationship(
    String sourceWorkItemId,
    String targetWorkItemId,
    String relationshipType
)
```

**Parameters**:
- `sourceId` (required) - Source work item ID
- `targetId` (required) - Target work item ID
- `relationshipType` (required) - Relationship type:
  - `"parent"` / `"child"` - Parent-child relationship
  - `"related"` - Related work items
  - `"blocks"` / `"blocked by"` - Dependency relationship
  - `"tests"` / `"tested by"` - Test relationship
  - `"duplicate"` / `"duplicate of"` - Duplicate relationship

**Returns**: `String` - Link response JSON

**When to use**:
- ✅ Link tasks to user stories
- ✅ Link test cases to stories (tested by)
- ✅ Create dependency relationships
- ✅ Mark duplicate work items

**Example**:
```java
// Link task to story as child
adoClient.linkIssueWithRelationship(taskId, storyId, "child");

// Link test case to story (story is tested by test case)
adoClient.linkIssueWithRelationship(storyId, testCaseId, "tested by");

// Create dependency (task blocks another task)
adoClient.linkIssueWithRelationship(blockingTaskId, blockedTaskId, "blocks");
```

**Supported Relationship Types**:
- `parent` / `child` → System.LinkTypes.Hierarchy-Forward
- `related` → System.LinkTypes.Related
- `blocks` / `blocked by` → System.LinkTypes.Dependency-Forward / Dependency-Reverse
- `tests` / `tested by` → Microsoft.VSTS.Common.TestedBy-Forward / TestedBy-Reverse
- `duplicate` / `duplicate of` → System.LinkTypes.Duplicate-Forward / Duplicate-Reverse

---

### 8. ado_get_comments

**Purpose**: Retrieve all comments for a work item.

**Signature**:
```java
public List<? extends IComment> getComments(
    String workItemId,
    ITicket ticket  // Optional, can be null
)
```

**Parameters**:
- `id` (required) - Work item ID
- `ticket` (optional) - Can be null

**Returns**: `List<IComment>` - List of comments with:
- `getId()` - Comment ID
- `getBody()` - Comment text
- `getAuthor()` - User who posted the comment
- `getCreated()` - Creation date

**When to use**:
- ✅ Review discussion on work item
- ✅ Check for updates/comments
- ✅ Extract comment history

**Example**:
```java
List<? extends IComment> comments = adoClient.getComments("12345", null);

for (IComment comment : comments) {
    System.out.println("Author: " + comment.getAuthor().getFullName());
    System.out.println("Date: " + comment.getCreated());
    System.out.println("Comment: " + comment.getBody());
    System.out.println("---");
}
```

---

### 9. ado_post_comment

**Purpose**: Post a comment to a work item.

**Signature**:
```java
public void postComment(
    String workItemId,
    String comment
)
```

**Parameters**:
- `id` (required) - Work item ID
- `comment` (required) - Comment text (plain text or HTML)

**Returns**: `void` (success if no exception)

**When to use**:
- ✅ Add progress updates
- ✅ Provide context/notes
- ✅ Respond to questions
- ✅ Document decisions

**Example**:
```java
// Simple text comment
adoClient.postComment("12345", "Started working on this task");

// HTML comment with formatting
adoClient.postComment("12345", 
    "<p>Progress update:</p>" +
    "<ul>" +
    "<li>Completed API implementation</li>" +
    "<li>Pending: Unit tests</li>" +
    "</ul>");
```

**Note**: ADO comments API uses `api-version=7.0-preview`.

---

### 10. ado_get_changelog

**Purpose**: Get the complete history/changelog of a work item.

**Signature**:
```java
public IChangelog getChangeLog(
    String workItemId,
    ITicket ticket  // Optional, can be null
)
```

**Parameters**:
- `id` (required) - Work item ID
- `ticket` (optional) - Can be null

**Returns**: `IChangelog` with:
- `getHistories()` - List of history entries
  - Each entry contains:
    - `getAuthor()` - User who made the change
    - `getCreated()` - Date/time of change
    - `getHistoryItems()` - List of field changes
      - `getField()` - Field name that changed
      - `getOldValue()` - Previous value
      - `getNewValue()` - New value

**When to use**:
- ✅ Track all changes to work item
- ✅ Audit trail
- ✅ Understand work item evolution
- ✅ Debug state transitions

**Example**:
```java
IChangelog changelog = adoClient.getChangeLog("12345", null);
List<? extends IHistory> histories = changelog.getHistories();

for (IHistory history : histories) {
    System.out.println("Date: " + history.getCreated().getTime());
    System.out.println("Author: " + history.getAuthor().getFullName());
    
    List<? extends IHistoryItem> items = history.getHistoryItems();
    for (IHistoryItem item : items) {
        System.out.println("  Field: " + item.getField());
        System.out.println("  Old: " + item.getOldValue());
        System.out.println("  New: " + item.getNewValue());
    }
    System.out.println("---");
}
```

**Common Field Changes**:
- `System.State` - State transitions
- `System.Description` - Description updates
- `System.AssignedTo` - Assignment changes
- `System.Title` - Title changes
- `Microsoft.VSTS.Common.Priority` - Priority changes
- Comments are also tracked in history

---

### 11. ado_get_my_profile

**Purpose**: Get the current authenticated user's profile information.

**Signature**:
```java
public IUser getMyProfile()
```

**Parameters**: None

**Returns**: `IUser` with:
- `getID()` - User ID (GUID)
- `getFullName()` - Display name
- `getEmailAddress()` - Email address

**When to use**:
- ✅ Verify authentication
- ✅ Get current user information
- ✅ Use for assignment operations
- ✅ Personalize workflows

**Example**:
```java
IUser user = adoClient.getMyProfile();
System.out.println("User ID: " + user.getID());
System.out.println("Name: " + user.getFullName());
System.out.println("Email: " + user.getEmailAddress());
```

**Implementation Note**: Uses ADO Profile API endpoint (`https://app.vssps.visualstudio.com/_apis/profile/profiles/me`) which works with PAT tokens.

---

### 12. ado_download_attachment

**Purpose**: Download an attachment file from a work item.

**Signature**:
```java
public File convertUrlToFile(
    String href  // Attachment URL
)
```

**Parameters**:
- `href` (required) - Full URL to the attachment

**Returns**: `File` - Downloaded file (cached)

**When to use**:
- ✅ Download work item attachments
- ✅ Process attached files
- ✅ Extract data from attachments

**Example**:
```java
// Get work item with attachments
WorkItem workItem = adoClient.performTicket("12345", 
    new String[]{"System.Id"});

// Download attachment (if work item has attachment URL)
// Note: Requires attachment URL from work item
String attachmentUrl = "..."; // From work item attachment
File downloadedFile = adoClient.convertUrlToFile(attachmentUrl);
```

**Note**: Currently, ADO attachment upload is not yet implemented. This method is for downloading existing attachments.

---

## Practical Examples

### Example 1: Create User Story with Tasks and Test Cases

```java
// 1. Create User Story
String storyResponse = adoClient.createTicketInProject(
    "MyProject",
    "User Story",
    "User Authentication",
    "<p>As a user, I want to login to the system</p>",
    fields -> {
        fields.set("Microsoft.VSTS.Scheduling.StoryPoints", 8);
        fields.set("Microsoft.VSTS.Common.Priority", 1);
    }
);
JSONObject storyJson = new JSONObject(storyResponse);
String storyId = storyJson.getString("id");

// 2. Create Tasks
String[] taskTitles = {
    "Implement login API",
    "Create login UI",
    "Add password reset"
};

for (String taskTitle : taskTitles) {
    String taskResponse = adoClient.createTicketInProject(
        "MyProject",
        "Task",
        taskTitle,
        "<p>Task for user authentication story</p>",
        null
    );
    JSONObject taskJson = new JSONObject(taskResponse);
    String taskId = taskJson.getString("id");
    
    // Link task to story
    adoClient.linkIssueWithRelationship(taskId, storyId, "child");
}

// 3. Create Test Case
String testCaseResponse = adoClient.createTicketInProject(
    "MyProject",
    "Test Case",
    "Test user login",
    "<p>Verify user can login with valid credentials</p>",
    null
);
JSONObject testCaseJson = new JSONObject(testCaseResponse);
String testCaseId = testCaseJson.getString("id");

// Link test case to story (story is tested by test case)
adoClient.linkIssueWithRelationship(storyId, testCaseId, "tested by");
```

### Example 2: Search and Update Work Items

```java
// Search for all active bugs
String wiql = "SELECT [System.Id] FROM WorkItems " +
              "WHERE [System.WorkItemType] = 'Bug' " +
              "AND [System.State] = 'Active'";
List<WorkItem> activeBugs = adoClient.searchAndPerform(wiql, 
    new String[]{"title", "state", "priority", "assignedTo"});

// Update each bug
for (WorkItem bug : activeBugs) {
    String bugId = bug.getTicketKey();
    
    // Add comment
    adoClient.postComment(bugId, "Reviewing this bug");
    
    // Update description
    String newDescription = bug.getTicketDescription() + 
        "<p><strong>Update:</strong> Investigating root cause</p>";
    adoClient.updateDescription(bugId, newDescription);
    
    // Move to Resolved if priority is low
    if (bug.getPriority() != null && bug.getPriority() > 2) {
        adoClient.moveToStatus(bugId, "Resolved");
    }
}
```

### Example 3: Track Work Item History

```java
// Get work item
WorkItem workItem = adoClient.performTicket("12345", null);

// Get changelog
IChangelog changelog = adoClient.getChangeLog("12345", null);
List<? extends IHistory> histories = changelog.getHistories();

// Analyze changes
for (IHistory history : histories) {
    Calendar changeDate = history.getCreated();
    IUser author = history.getAuthor();
    List<? extends IHistoryItem> items = history.getHistoryItems();
    
    System.out.println("Change on " + changeDate.getTime() + 
                      " by " + author.getFullName());
    
    for (IHistoryItem item : items) {
        if ("System.State".equals(item.getField())) {
            System.out.println("  State: " + item.getOldValue() + 
                             " → " + item.getNewValue());
        }
    }
}
```

---

## Best Practices

### 1. Use Field Filtering for Performance

**Inefficient**:
```java
// Gets all fields (slow for large work items)
WorkItem item = adoClient.performTicket("12345", null);
```

**Efficient**:
```java
// Gets only needed fields (faster)
WorkItem item = adoClient.performTicket("12345", 
    new String[]{"title", "state", "priority"});
```

### 2. Batch Operations with WIQL

Instead of multiple `get_work_item` calls, use `search_by_wiql`:

```java
// ❌ Inefficient
for (String id : workItemIds) {
    WorkItem item = adoClient.performTicket(id, null);
}

// ✅ Efficient
String wiql = "SELECT [System.Id] FROM WorkItems " +
              "WHERE [System.Id] IN (" + String.join(",", workItemIds) + ")";
List<WorkItem> items = adoClient.searchAndPerform(wiql, null);
```

### 3. Handle State Transitions Properly

Not all work item types support all states:

```java
try {
    adoClient.moveToStatus(workItemId, "Removed");
} catch (Exception e) {
    if (e.getMessage().contains("not in the list of supported values")) {
        // Try alternative state
        adoClient.moveToStatus(workItemId, "Closed");
    }
}
```

### 4. Use Friendly Field Names

The client automatically resolves friendly names to ADO field names:

```java
// ✅ Use friendly names (easier to read)
WorkItem item = adoClient.performTicket("12345", 
    new String[]{"title", "state", "priority"});

// Also works with full ADO field names
WorkItem item = adoClient.performTicket("12345", 
    new String[]{"System.Title", "System.State", "Microsoft.VSTS.Common.Priority"});
```

### 5. Error Handling

Always handle exceptions for network and API errors:

```java
try {
    WorkItem item = adoClient.performTicket("12345", null);
    if (item == null) {
        System.out.println("Work item not found");
    }
} catch (IOException e) {
    System.err.println("Error retrieving work item: " + e.getMessage());
}
```

---

## Troubleshooting

### Issue: Authentication Errors

**Symptoms**: 401 Unauthorized errors

**Solutions**:
1. Verify PAT token is valid and not expired
2. Check token has "Work Items: Read & Write" scope
3. Ensure organization name is correct
4. Verify token format: `Basic {base64(:token)}`

### Issue: Work Item Not Found

**Symptoms**: 404 Not Found or null response

**Solutions**:
1. Verify work item ID exists
2. Check PAT token has access to the project
3. Ensure project name is correct
4. Verify work item hasn't been deleted

### Issue: State Transition Fails

**Symptoms**: "not in the list of supported values" error

**Solutions**:
1. Check which states are valid for the work item type
2. Some types (like Test Case) don't support "Removed"
3. Use "Closed" as alternative for cleanup
4. Check project's workflow configuration

### Issue: Field Name Not Found

**Symptoms**: Field returns null or empty

**Solutions**:
1. Use friendly field names (auto-resolved)
2. Check field exists in your project
3. Verify field is included in work item type
4. Use `getDefaultQueryFields()` for common fields

### Issue: WIQL Query Fails

**Symptoms**: Query returns no results or error

**Solutions**:
1. Verify WIQL syntax is correct
2. Check field names in WHERE clause
3. Ensure date comparisons use `@today` syntax
4. Test query in ADO web interface first

---

## Configuration

### Required Setup

1. **Azure DevOps Organization**: Set in environment
   ```bash
   ADO_ORGANIZATION=yourorganization
   ```

2. **Project Name**: Set in environment
   ```bash
   ADO_PROJECT=YourProject
   ```

3. **PAT Token**: Create and set in environment
   ```bash
   ADO_PAT_TOKEN=your-pat-token-here
   ```

4. **Base Path** (optional): Defaults to `https://dev.azure.com`
   ```bash
   ADO_BASE_PATH=https://dev.azure.com
   ```

### Creating PAT Token

1. Go to: `https://dev.azure.com/{organization}/_usersSettings/tokens`
2. Click "New Token"
3. Name: "DMTools Integration"
4. Expiration: Set as needed
5. Scopes:
   - **Work Items**: Read, write, & manage
6. Click "Create" and copy token

---

## Integration Testing

All methods are covered by integration tests in:
```
dmtools-core/src/integrationTest/java/com/github/istin/dmtools/ado/AzureDevOpsClientMcpToolsIntegrationTest.java
```

**19 tests, all passing ✅**

To run tests:
```bash
./gradlew :dmtools-core:integrationTest --tests "*.AzureDevOpsClientMcpToolsIntegrationTest"
```

**Test Coverage**:
- ✅ Work item creation
- ✅ Work item retrieval
- ✅ Field filtering
- ✅ WIQL search
- ✅ Description updates
- ✅ State transitions
- ✅ Assignment
- ✅ Comments (get/post)
- ✅ Work item linking
- ✅ Tags/labels
- ✅ Batch operations
- ✅ Error handling
- ✅ Metadata extraction
- ✅ Story/Test Case linking
- ✅ User profile
- ✅ Changelog/history
- ✅ Attachment download

---

## Field Name Reference

### Common Fields (Friendly → ADO)

| Friendly Name | ADO Field Name |
|--------------|----------------|
| `id` | `System.Id` |
| `title` | `System.Title` |
| `description` | `System.Description` |
| `state` | `System.State` |
| `assignedTo` | `System.AssignedTo` |
| `createdBy` | `System.CreatedBy` |
| `createdDate` | `System.CreatedDate` |
| `changedDate` | `System.ChangedDate` |
| `workItemType` | `System.WorkItemType` |
| `priority` | `Microsoft.VSTS.Common.Priority` |
| `tags` | `System.Tags` |
| `areaPath` | `System.AreaPath` |
| `iterationPath` | `System.IterationPath` |
| `storyPoints` | `Microsoft.VSTS.Scheduling.StoryPoints` |
| `effort` | `Microsoft.VSTS.Scheduling.Effort` |

---

## Limitations & Future Improvements

### Current Limitations

1. **Attachment Upload**: Not yet implemented
   - Workaround: Use ADO web interface or REST API directly

2. **Work Item Deletion**: ADO doesn't support true deletion
   - Workaround: Move to "Removed" state (if supported) or "Closed"

3. **Batch Updates**: No batch update method yet
   - Workaround: Update work items individually

### Future Improvements

Potential additions:
- `ado_upload_attachment` - Upload files to work items
- `ado_batch_update` - Update multiple work items at once
- `ado_get_work_item_types` - List available work item types
- `ado_get_states` - List valid states for work item type
- `ado_get_projects` - List projects in organization
- Enhanced relationship management

---

## Summary

### ✅ Complete Azure DevOps MCP Integration

**12 MCP methods** covering:
- ✅ Work item CRUD operations
- ✅ Advanced search with WIQL
- ✅ Comments and history
- ✅ Work item linking and relationships
- ✅ User profile management
- ✅ File attachment download

**Production Ready**:
- ✅ All methods tested (19 integration tests)
- ✅ Documented with examples
- ✅ Error handling and validation
- ✅ Field name resolution
- ✅ State transition support

### Key Features

- **WIQL Search** - Powerful query language for complex searches
- **Relationship Management** - Link work items with various relationship types
- **Changelog Tracking** - Complete history of all changes
- **User Profile** - Get current user information
- **Field Resolution** - Automatic mapping of friendly names to ADO fields

**Result**: Complete toolkit for Azure DevOps work item management! 🚀

---

## See Also

- [AzureDevOpsClient.java](../../dmtools-core/src/main/java/com/github/istin/dmtools/microsoft/ado/AzureDevOpsClient.java) - Implementation
- [AzureDevOpsClientMcpToolsIntegrationTest.java](../../dmtools-core/src/integrationTest/java/com/github/istin/dmtools/ado/AzureDevOpsClientMcpToolsIntegrationTest.java) - Tests
- [ADO README.md](../../dmtools-core/src/main/java/com/github/istin/dmtools/microsoft/ado/README.md) - Additional documentation
- [Model Classes](../../dmtools-core/src/main/java/com/github/istin/dmtools/microsoft/ado/model/) - Data models

