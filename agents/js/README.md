# JavaScript Actions for Teammate Agents

This directory contains JavaScript actions that can be executed before (`preJSAction`) and after (`postJSAction`) agent processing.

## Directory Structure

```
js/
├── common/
│   ├── jiraHelpers.js      # Jira-specific helper functions
│   └── adoHelpers.js        # ADO-specific helper functions
├── config.js                # Shared configuration (statuses, labels)
├── checkWipLabel.js         # Jira: Check for WIP label (pre-action)
├── checkWipTagAdo.js        # ADO: Check for WIP tag (pre-action)
├── assignForReview.js       # Jira: Assign & move to review (post-action)
└── assignForReviewAdo.js    # ADO: Assign & move to review (post-action)
```

## Jira vs ADO Actions

### Key Differences

| Feature | Jira | ADO |
|---------|------|-----|
| **Identifier** | `key` (e.g., "DMC-532") | `id` (numeric, e.g., 755) |
| **User Identifier** | `accountId` (e.g., "712020:2a24...") | `email` (e.g., "user@example.com") |
| **Labels/Tags** | Array: `["label1", "label2"]` | Semicolon-separated string: `"tag1;tag2"` |
| **Comments** | Jira Markdown | HTML |
| **Field Names** | `fields.labels` | `fields['System.Tags']` |

### Pre-Actions (Check WIP)

**Jira** (`checkWipLabel.js`):
```javascript
const wipLabel = metadata.contextId + '_wip';
const labels = ticket.fields.labels || [];
if (labels.includes(wipLabel)) {
    jira_post_comment({
        key: ticketKey,
        comment: 'h3. *Processing Skipped*\n\n...'
    });
    return false; // Stop processing
}
```

**ADO** (`checkWipTagAdo.js`):
```javascript
const wipTag = metadata.contextId + '_wip';
const tagsString = workItem.fields['System.Tags'] || '';
const tags = tagsString.split(';').map(t => t.trim());
if (tags.includes(wipTag)) {
    ado_post_comment({
        id: workItemId,
        comment: '<h3><strong>Processing Skipped</strong></h3>...'
    });
    return false; // Stop processing
}
```

### Post-Actions (Assign for Review)

**Jira** (`assignForReview.js`):
```javascript
const { assignForReview } = require('./common/jiraHelpers.js');

function action(params) {
    const ticketKey = params.ticket.key;
    const initiatorId = params.initiator; // accountId
    const wipLabel = params.metadata.contextId + '_wip';
    
    return assignForReview(ticketKey, initiatorId, wipLabel);
}
```

**ADO** (`assignForReviewAdo.js`):
```javascript
const { assignForReview } = require('./common/adoHelpers.js');

function action(params) {
    const workItemId = params.ticket.id;
    const initiatorEmail = params.initiatorEmail; // email, not accountId
    const wipTag = params.metadata.contextId + '_wip';
    
    // Assigns to initiator and moves to "Resolved" state
    return assignForReview(workItemId, initiatorEmail, wipTag);
}
```

## Helper Functions

### Jira Helpers (`jiraHelpers.js`)

- `assignForReview(ticketKey, initiatorId, wipLabel)` - Assign ticket, move to review, update labels
- `extractTicketKey(result)` - Extract ticket key from API response
- `setTicketPriority(ticketKey, priority)` - Set ticket priority

**MCP Tools Used:**
- `jira_assign_ticket_to`
- `jira_move_to_status`
- `jira_add_label`
- `jira_remove_label`
- `jira_set_priority`

### ADO Helpers (`adoHelpers.js`)

- `assignForReview(workItemId, initiatorEmail, wipTag)` - Assign work item, move to review, update tags
- `extractWorkItemId(result)` - Extract work item ID from API response
- `setWorkItemPriority(workItemId, priority)` - Set work item priority (1-4)
- `addTag(workItemId, tag)` - Add a tag to work item
- `removeTag(workItemId, tag)` - Remove a tag from work item

**MCP Tools Used:**
- `ado_assign_work_item`
- `ado_move_to_state`
- `ado_get_work_item`
- `ado_update_work_item`

## Configuration

### Agent JSON Configuration

**Jira Example:**
```json
{
  "name": "Teammate",
  "params": {
    "metadata": {
      "contextId": "story_description"
    },
    "inputJql": "key = DMC-532",
    "initiator": "712020:2a248756-40e8-49d6-8ddc-6852e518451f",
    "preJSAction": "agents/js/checkWipLabel.js",
    "postJSAction": "agents/js/assignForReview.js"
  }
}
```

**ADO Example:**
```json
{
  "name": "Teammate",
  "params": {
    "metadata": {
      "contextId": "ado_story_description"
    },
    "inputJql": "SELECT [System.Id] FROM WorkItems WHERE [System.Id] = 755",
    "initiatorEmail": "user@example.com",
    "preJSAction": "agents/js/checkWipTagAdo.js",
    "postJSAction": "agents/js/assignForReviewAdo.js"
  }
}
```

## Creating New Actions

### Template for New Pre-Action

```javascript
/**
 * My Custom Pre-Action
 * Description of what this action does
 */

function action(params) {
    try {
        const ticket = params.ticket;
        const metadata = params.metadata;
        
        // Your logic here
        
        // Return true to continue processing, false to stop
        return true;
        
    } catch (error) {
        console.error('❌ Error:', error);
        // On error, decide whether to continue or stop
        return true;
    }
}
```

### Template for New Post-Action

```javascript
/**
 * My Custom Post-Action
 * Description of what this action does
 */

function action(params) {
    try {
        const ticketKey = params.ticket.key; // or params.ticket.id for ADO
        
        // Your logic here
        
        return {
            success: true,
            message: 'Action completed successfully'
        };
        
    } catch (error) {
        console.error('❌ Error:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

## Testing

To test your actions:

1. Create a test ticket/work item
2. Add the WIP label/tag to test pre-actions
3. Run the agent with your JSON configuration
4. Check logs for action execution results
5. Verify ticket/work item state changes

## Debugging

Enable verbose logging in your actions:

```javascript
console.log('🔍 Debug info:', {
    ticketKey: ticket.key,
    metadata: metadata,
    labels: ticket.fields.labels
});
```

Check Teammate logs for action execution details and any errors.
