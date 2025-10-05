# JavaScript Actions

JavaScript actions are post-processing scripts that run after AI Teammate completes its task. They can interact with Jira, GitHub, file system, and CLI commands.

## What are JavaScript Actions?

JavaScript actions enable you to:
- ✅ **Access MCP tools** (Jira, Confluence, Figma, etc.)
- ✅ **Execute CLI commands** (git, gh, dmtools)
- ✅ **Read/write files**
- ✅ **Create PRs and branches**
- ✅ **Update Jira tickets**
- ✅ **Make complex decisions**

---

## Quick Start

### Basic Action Structure

```javascript
/**
 * My Custom Action
 * Description of what this action does
 */

function action(params) {
    try {
        // Access parameters
        const ticketKey = params.ticket.key;
        const ticketSummary = params.ticket.fields.summary;
        const response = params.response;  // Output from AI
        const initiator = params.initiator;
        
        // Your logic here
        console.log('Processing ticket:', ticketKey);
        
        // Return result
        return {
            success: true,
            message: 'Action completed successfully'
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

### Using in Agent Configuration

```json
{
  "name": "Teammate",
  "params": {
    "inputJql": "key = PROJ-123",
    "postJSAction": "agents/myCustomAction.js"
  }
}
```

---

## Available APIs

### Parameters Object

The `params` object passed to `action()` contains:

```javascript
{
  ticket: {
    key: "PROJ-123",
    fields: {
      summary: "Ticket summary",
      description: "Ticket description",
      status: { name: "In Progress" },
      assignee: { displayName: "John Doe" },
      // ... all ticket fields
    }
  },
  response: "Output from AI or cursor-agent",
  initiator: "user-account-id",
  jobParams: { /* job configuration */ }
}
```

### MCP Tools (Jira)

Access all Jira MCP tools directly:

```javascript
// Get ticket
const ticketData = jira_get_ticket({
    key: "PROJ-123"
});

// Post comment
jira_post_comment({
    key: "PROJ-123",
    comment: "h2. Update\n\nWork completed"
});

// Update ticket
jira_update_ticket({
    key: "PROJ-123",
    params: {
        fields: {
            assignee: { id: "user-account-id" }
        }
    }
});

// Move to status
jira_move_to_status({
    key: "PROJ-123",
    statusName: "In Review"
});

// Add label
jira_add_label({
    key: "PROJ-123",
    label: "automated"
});

// Search tickets
const results = jira_search_by_jql({
    jql: "parent = PROJ-123",
    fields: ["key", "summary", "status"]
});
```

### CLI Commands

Execute shell commands:

```javascript
// Execute command
const output = cli_execute_command({
    command: "git status"
});

// Create branch
cli_execute_command({
    command: "git checkout -b feature/PROJ-123"
});

// Stage and commit
cli_execute_command({
    command: "git add ."
});

cli_execute_command({
    command: 'git commit -m "PROJ-123 Implementation"'
});

// Push to remote
cli_execute_command({
    command: "git push -u origin feature/PROJ-123"
});

// Create PR with GitHub CLI
const prOutput = cli_execute_command({
    command: 'gh pr create --title "PROJ-123 Fix" --body-file outputs/response.md --base main'
});
```

### File Operations

Read and write files:

```javascript
// Read file
const content = file_read({
    path: "outputs/response.md"
});

// Write file
file_write({
    path: "output.txt",
    content: "Hello, World!"
});

// Check if file exists
try {
    const exists = file_read({ path: "outputs/response.md" });
    if (exists) {
        console.log("File exists");
    }
} catch (error) {
    console.log("File does not exist");
}
```

### Helper Functions

Common helper utilities available in `common/` folder:

**`common/jiraHelpers.js`:**
```javascript
const { assignForReview, extractTicketKey } = require('./common/jiraHelpers.js');

// Assign ticket and move to In Review
assignForReview("PROJ-123", "user-account-id");

// Extract ticket key from text
const key = extractTicketKey("Working on PROJ-123");
```

**`common/aiResponseParser.js`:**
```javascript
const { parseJSONResponse } = require('./common/aiResponseParser.js');

// Parse AI response to JSON
const data = parseJSONResponse(params.response);
```

---

## Complete Examples

### Example 1: Assign for Review

**File:** `agents/assignForReview.js`

```javascript
/**
 * Simple Assign For Review Action
 * Assigns ticket to initiator and moves to "In Review" status
 */

const { assignForReview } = require('./common/jiraHelpers.js');

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const initiatorId = params.initiator;
        
        // Use common assignForReview function
        return assignForReview(ticketKey, initiatorId);
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

### Example 2: Develop Ticket and Create PR

**File:** `agents/developTicketAndCreatePR.js`

```javascript
/**
 * Develop Ticket and Create PR Action
 * Handles git operations, branch creation, commit, push, and PR creation
 */

function extractIssueTypePrefix(summary) {
    if (!summary) return 'feature';
    const match = summary.match(/^\[([^\]]+)\]/);
    if (match && match[1]) {
        return match[1].toLowerCase().replace(/[^a-z0-9]/g, '');
    }
    return 'feature';
}

function generateUniqueBranchName(baseType, ticketKey) {
    const baseBranchName = baseType + '/' + ticketKey;
    
    try {
        const existingBranches = cli_execute_command({
            command: 'git branch --all --list "*' + baseBranchName + '*"'
        }) || '';
        
        if (!existingBranches.trim()) {
            return baseBranchName;
        }
        
        for (let i = 1; i <= 10; i++) {
            const candidateName = baseBranchName + '_' + i;
            if (existingBranches.indexOf(candidateName) === -1) {
                return candidateName;
            }
        }
        
        return baseBranchName + '_' + Date.now();
    } catch (error) {
        console.warn('Error checking branches:', error);
        return baseBranchName;
    }
}

function configureGitAuthor() {
    try {
        cli_execute_command({
            command: 'git config user.name "AI Teammate"'
        });
        cli_execute_command({
            command: 'git config user.email "agent.ai.native@gmail.com"'
        });
        console.log('✅ Configured git author');
        return true;
    } catch (error) {
        console.error('Failed to configure git:', error);
        return false;
    }
}

function performGitOperations(branchName, commitMessage) {
    try {
        console.log('Creating branch:', branchName);
        cli_execute_command({
            command: 'git checkout -b ' + branchName
        });
        
        console.log('Staging changes...');
        cli_execute_command({
            command: 'git add .'
        });
        
        const statusOutput = cli_execute_command({
            command: 'git status --porcelain'
        });
        
        if (!statusOutput || !statusOutput.trim()) {
            console.warn('No changes to commit');
            return {
                success: false,
                error: 'No changes were made'
            };
        }
        
        console.log('Committing changes...');
        cli_execute_command({
            command: 'git commit -m "' + commitMessage.replace(/"/g, '\\"') + '"'
        });
        
        console.log('Pushing to remote...');
        cli_execute_command({
            command: 'git push -u origin ' + branchName
        });
        
        console.log('✅ Git operations completed');
        return {
            success: true,
            branchName: branchName
        };
    } catch (error) {
        console.error('Git operations failed:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

function createPullRequest(title) {
    try {
        console.log('Creating Pull Request...');
        const escapedTitle = title.replace(/"/g, '\\"').replace(/\n/g, ' ');
        const bodyFilePath = 'outputs/response.md';
        
        const output = cli_execute_command({
            command: 'gh pr create --title "' + escapedTitle + '" --body-file "' + bodyFilePath + '" --base main'
        }) || '';
        
        const urlMatch = output.match(/https:\/\/github\.com\/[^\s]+/);
        const prUrl = urlMatch ? urlMatch[0] : null;
        
        console.log('✅ Pull Request created:', prUrl || '(URL not found)');
        
        return {
            success: true,
            prUrl: prUrl,
            output: output
        };
    } catch (error) {
        console.error('Failed to create PR:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

function postPRCommentToJira(ticketKey, prUrl, branchName) {
    try {
        let comment = 'h3. *Development Completed*\n\n';
        comment += '*Branch:* {code}' + branchName + '{code}\n';
        
        if (prUrl) {
            comment += '*Pull Request:* ' + prUrl + '\n';
        }
        
        comment += '\nAI Teammate has completed the implementation.';
        
        jira_post_comment({
            key: ticketKey,
            comment: comment
        });
        
        console.log('✅ Posted PR comment to', ticketKey);
    } catch (error) {
        console.error('Failed to post comment:', error);
    }
}

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const ticketSummary = params.ticket.fields.summary;
        
        console.log('Processing:', ticketKey);
        
        // Configure git
        if (!configureGitAuthor()) {
            return { success: false, error: 'Git config failed' };
        }
        
        // Generate branch name
        const issueType = extractIssueTypePrefix(ticketSummary);
        const branchName = generateUniqueBranchName(issueType, ticketKey);
        console.log('Branch name:', branchName);
        
        // Git operations
        const commitMessage = ticketKey + ' ' + ticketSummary;
        const gitResult = performGitOperations(branchName, commitMessage);
        
        if (!gitResult.success) {
            return {
                success: false,
                error: 'Git operations failed: ' + gitResult.error
            };
        }
        
        // Verify response file exists
        try {
            const responseContent = file_read({
                path: 'outputs/response.md'
            });
            if (!responseContent) {
                return {
                    success: false,
                    error: 'outputs/response.md not found'
                };
            }
        } catch (error) {
            return {
                success: false,
                error: 'Failed to read PR body: ' + error.toString()
            };
        }
        
        // Create PR
        const prTitle = ticketKey + ' ' + ticketSummary;
        const prResult = createPullRequest(prTitle);
        
        if (!prResult.success) {
            return {
                success: false,
                error: 'PR creation failed: ' + prResult.error
            };
        }
        
        // Move ticket to In Review
        try {
            jira_move_to_status({
                key: ticketKey,
                statusName: 'In Review'
            });
            console.log('✅ Moved to In Review');
        } catch (error) {
            console.warn('Failed to move ticket:', error);
        }
        
        // Post comment
        postPRCommentToJira(ticketKey, prResult.prUrl, branchName);
        
        // Add label
        try {
            jira_add_label({
                key: ticketKey,
                label: 'ai_developed'
            });
        } catch (error) {
            console.warn('Failed to add label:', error);
        }
        
        console.log('✅ Workflow completed successfully');
        
        return {
            success: true,
            message: 'Ticket developed and PR created',
            branchName: branchName,
            prUrl: prResult.prUrl
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

### Example 3: Create Solution Design Tickets

**File:** `agents/createSolutionDesignTicketsAndAssignForReview.js`

```javascript
/**
 * Create Solution Design Tickets and Assign for Review
 * Parses AI response to create subtasks and assigns parent for review
 */

const { parseJSONResponse } = require('./common/aiResponseParser.js');
const { assignForReview } = require('./common/jiraHelpers.js');

function action(params) {
    try {
        const parentKey = params.ticket.key;
        const initiatorId = params.initiator;
        const response = params.response;
        
        console.log('Creating solution design tickets for:', parentKey);
        
        // Parse AI response
        const designData = parseJSONResponse(response);
        
        if (!designData || !designData.tasks) {
            console.warn('No tasks found in response');
            return assignForReview(parentKey, initiatorId);
        }
        
        // Create subtasks
        const createdTickets = [];
        for (const task of designData.tasks) {
            console.log('Creating subtask:', task.title);
            
            const result = jira_create_ticket_with_parent({
                project: parentKey.split('-')[0],
                issueType: 'Sub-task',
                summary: task.title,
                description: task.description || '',
                parentKey: parentKey
            });
            
            if (result && result.key) {
                createdTickets.push(result.key);
                console.log('✅ Created:', result.key);
            }
        }
        
        // Post summary comment
        let comment = 'h2. Solution Design Tasks Created\n\n';
        comment += 'Created ' + createdTickets.length + ' subtasks:\n';
        for (const key of createdTickets) {
            comment += '* ' + key + '\n';
        }
        
        jira_post_comment({
            key: parentKey,
            comment: comment
        });
        
        // Assign for review
        return assignForReview(parentKey, initiatorId);
        
    } catch (error) {
        console.error('❌ Error:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

---

## Best Practices

### 1. Error Handling

Always wrap your code in try-catch:

```javascript
function action(params) {
    try {
        // Your logic
        return { success: true };
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

### 2. Logging

Use console.log for progress tracking:

```javascript
console.log('Starting action for ticket:', ticketKey);
console.log('✅ Step completed successfully');
console.warn('⚠️ Warning: Non-critical issue');
console.error('❌ Error:', error);
```

### 3. Use Helper Functions

Extract reusable logic:

```javascript
// common/jiraHelpers.js
function assignForReview(ticketKey, userId) {
    try {
        jira_update_ticket({
            key: ticketKey,
            params: {
                fields: {
                    assignee: { id: userId }
                }
            }
        });
        
        jira_move_to_status({
            key: ticketKey,
            statusName: 'In Review'
        });
        
        return { success: true };
    } catch (error) {
        return { success: false, error: error.toString() };
    }
}

module.exports = { assignForReview };
```

### 4. Validate Inputs

Check parameters before using:

```javascript
function action(params) {
    if (!params || !params.ticket || !params.ticket.key) {
        return {
            success: false,
            error: 'Invalid parameters: ticket key is required'
        };
    }
    
    // Continue with logic
}
```

### 5. Return Meaningful Results

Always return a result object:

```javascript
return {
    success: true,
    message: 'Operation completed',
    data: {
        branchName: 'feature/PROJ-123',
        prUrl: 'https://github.com/...'
    }
};
```

---

## Debugging

### Enable Debug Logging

```javascript
const DEBUG = true;

function debug(message, data) {
    if (DEBUG) {
        console.log('[DEBUG]', message, data ? JSON.stringify(data, null, 2) : '');
    }
}

function action(params) {
    debug('Params received:', params);
    debug('Ticket key:', params.ticket.key);
    // ...
}
```

### Test Locally

Create a test harness:

```javascript
// test.js
const action = require('./agents/myAction.js').action;

const testParams = {
    ticket: {
        key: 'TEST-123',
        fields: {
            summary: 'Test ticket',
            description: 'Test description'
        }
    },
    response: 'AI response here',
    initiator: 'test-user-id'
};

const result = action(testParams);
console.log('Result:', result);
```

---

## See Also

- **[AI Teammate Overview](README.md)** - Understanding the workflow
- **[Agent Configuration](agent-configuration.md)** - Configuring agents
- **[Workflow Examples](examples.md)** - Complete workflow examples
- **[MCP Tools Reference](../cli-usage/mcp-tools.md)** - Available MCP tools
