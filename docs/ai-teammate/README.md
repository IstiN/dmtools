# AI Teammate Workflows

**AI Teammate** is a powerful automation pattern that combines DMTools, Cursor AI, and GitHub Actions to create intelligent development workflows.

## What is AI Teammate?

AI Teammate is a workflow automation system that:
- ✅ **Reads Jira tickets** as input
- ✅ **Uses AI (Cursor)** to perform complex tasks
- ✅ **Executes DMTools CLI** commands for integrations
- ✅ **Runs JavaScript** for post-processing
- ✅ **Creates PRs** and updates tickets automatically

### Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         GitHub Actions Workflow                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                                                                │  │
│  │  1. Trigger → 2. Setup → 3. DMTools Run → 4. Post-Processing │  │
│  │                                                                │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Teammate Job                                │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ - Fetch ticket from Jira                                       │  │
│  │ - Prepare input context (ticket + children + attachments)     │  │
│  │ - Execute CLI commands (run cursor-agent)                     │  │
│  │ - Run post-JS action                                          │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Cursor Agent                                │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ - Read input folder (ticket context)                           │  │
│  │ - Perform AI task (write code, documentation, etc.)           │  │
│  │ - Use dmtools CLI for Jira/Confluence/Figma access           │  │
│  │ - Write output to outputs/response.md                         │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        JavaScript Action                             │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ - Read outputs/response.md                                     │  │
│  │ - Create git branch & commit                                   │  │
│  │ - Push changes & create PR                                     │  │
│  │ - Update Jira ticket (status, comments, labels)               │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites

1. **GitHub repository** with DMTools configured
2. **GitHub Actions** enabled
3. **Secrets configured** (see [Configuration](#configuration))
4. **Cursor API key** for AI operations

### Step 1: Set Up GitHub Actions Workflow

Create `.github/workflows/ai-teammate.yml`:

```yaml
name: AI Teammate

on:
  workflow_dispatch:
    inputs:
      config_file:
        description: 'Path to config'
        required: true
      encoded_config:
        description: 'Encoded or JSON Agent Config'
        required: false

permissions:
  contents: write
  pull-requests: write
  actions: read
  
jobs:
  cursor-agent:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout repository
      uses: actions/checkout@v4

    - name: Set up Java 23
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '23'

    - name: Install Cursor CLI
      run: |
        curl https://cursor.com/install -fsS | bash
        echo "$HOME/.local/bin" >> $GITHUB_PATH

    - name: Install DMTools CLI
      run: |
        curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
        echo "$HOME/.dmtools/bin" >> $GITHUB_PATH

    - name: Run AI Teammate
      env:
        CURSOR_API_KEY: ${{ secrets.CURSOR_API_KEY }}
        JIRA_EMAIL: ${{ secrets.JIRA_EMAIL }}
        JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
        JIRA_BASE_PATH: ${{ vars.JIRA_BASE_PATH }}
        GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
        PAT_TOKEN: ${{ secrets.PAT_TOKEN }}
        DMTOOLS_INTEGRATIONS: "jira,confluence,figma,ai,cli,file"
      run: |
        dmtools run "${{ inputs.config_file }}" "${ENCODED_CONFIG:-}"
```

### Step 2: Create Agent Configuration

Create `agents/story_description.json`:

```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Experienced Business Analyst",
      "instructions": [
        "Read ticket details from input folder",
        "Write enhanced story description following template",
        "Use dmtools CLI to read Confluence templates and Figma designs",
        "Check child tickets for questions and solution designs",
        "Write output to outputs/response.md"
      ],
      "formattingRules": "Use Jira markup format",
      "knownInfo": ""
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Enhance story description based on input\""
    ],
    "outputType": "field",
    "fieldName": "Description",
    "operationType": "Replace",
    "ticketContextDepth": 1,
    "skipAIProcessing": true,
    "inputJql": "key = PROJ-123",
    "postJSAction": "agents/assignForReview.js"
  }
}
```

### Step 3: Trigger the Workflow

```bash
# Via GitHub CLI
gh workflow run ai-teammate.yml \
  -f config_file=agents/story_description.json

# Via GitHub UI
# Go to Actions → AI Teammate → Run workflow
# Enter: agents/story_description.json
```

---

## Configuration

### GitHub Secrets

Add these secrets in **Settings → Secrets and variables → Actions → Secrets**:

| Secret | Description | Example |
|--------|-------------|---------|
| `CURSOR_API_KEY` | Cursor AI API key | `cur_...` |
| `JIRA_EMAIL` | Jira account email | `user@company.com` |
| `JIRA_API_TOKEN` | Jira API token | `ATATT3xFfGF0T...` |
| `GEMINI_API_KEY` | Google Gemini key (optional) | `AIza...` |
| `FIGMA_TOKEN` | Figma API token (optional) | `figd_...` |
| `PAT_TOKEN` | GitHub Personal Access Token for PR creation | `ghp_...` |

### GitHub Variables

Add these variables in **Settings → Secrets and variables → Actions → Variables**:

| Variable | Description | Example |
|----------|-------------|---------|
| `JIRA_BASE_PATH` | Jira URL | `https://company.atlassian.net` |
| `JIRA_AUTH_TYPE` | Auth type | `Basic` |
| `CONFLUENCE_BASE_PATH` | Confluence URL | `https://company.atlassian.net/wiki` |
| `CONFLUENCE_DEFAULT_SPACE` | Default space | `TEAM` |
| `FIGMA_BASE_PATH` | Figma API URL | `https://api.figma.com/v1` |

---

## Agent Configuration Structure

Agent configurations follow this structure:

```json
{
  "name": "Teammate",
  "params": {
    // Agent AI parameters
    "agentParams": {
      "aiRole": "Role description",
      "instructions": ["Instruction 1", "Instruction 2"],
      "formattingRules": "Output format rules",
      "knownInfo": "Additional context",
      "fewShots": "Example outputs"
    },
    
    // CLI commands to execute
    "cliCommands": [
      "./scripts/run-cursor-agent.sh \"Task description\""
    ],
    
    // Output configuration
    "outputType": "field|comment|creation|none",
    "fieldName": "Description",
    "operationType": "Replace|Append",
    
    // Ticket selection
    "inputJql": "key = PROJ-123",
    "ticketContextDepth": 0,
    
    // Processing options
    "skipAIProcessing": true|false,
    "attachResponseAsFile": true|false,
    
    // Post-processing
    "postJSAction": "agents/myAction.js",
    
    // User context
    "initiator": "user-account-id"
  }
}
```

### Configuration Options

#### `agentParams`
AI agent configuration:
- **`aiRole`**: Role/persona for the AI (e.g., "Senior Java Developer")
- **`instructions`**: Array of instructions for the AI
- **`formattingRules`**: Output format requirements
- **`knownInfo`**: Additional context or knowledge
- **`fewShots`**: Example outputs for few-shot learning

#### `cliCommands`
Commands to execute:
```json
"cliCommands": [
  "./cicd/scripts/run-cursor-agent.sh \"Implement feature X\""
]
```

#### `outputType`
Where to put results:
- **`field`**: Update a Jira field
- **`comment`**: Add as Jira comment
- **`confluence`**: Create/update Confluence page
- **`none`**: No automatic output

#### `ticketContextDepth`
How much context to include:
- **`0`**: Only the ticket itself
- **`1`**: Ticket + direct children (subtasks)
- **`2`**: Ticket + children + linked tickets

#### `skipAIProcessing`
- **`true`**: Use CLI output directly (no AI processing)
- **`false`**: Process through AI (default)

#### `postJSAction`
JavaScript file to run after completion:
```json
"postJSAction": "agents/developTicketAndCreatePR.js"
```

---

## Use Cases & Examples

### Use Case 1: Story Description Enhancement

**Goal:** Enhance story descriptions with AI

**Configuration:** `agents/story_description.json`
```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Senior Business Analyst",
      "instructions": [
        "Read ticket from input folder",
        "Enhance description following Confluence template",
        "Use dmtools to read child tickets for context",
        "Check Figma designs if referenced",
        "Write enhanced description to outputs/response.md"
      ],
      "formattingRules": "Use Jira markup: h2., *, {code}, etc."
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Enhance story description\""
    ],
    "outputType": "field",
    "fieldName": "Description",
    "operationType": "Replace",
    "ticketContextDepth": 1,
    "skipAIProcessing": true,
    "inputJql": "key = DMC-532",
    "postJSAction": "agents/assignForReview.js"
  }
}
```

**Workflow:**
1. Fetches ticket `DMC-532`
2. Includes child tickets (questions, solution design)
3. Cursor agent enhances description
4. Updates ticket description
5. Assigns to initiator for review

### Use Case 2: Automated Development

**Goal:** Implement ticket and create PR

**Configuration:** `agents/story_development.json`
```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Senior Java Software Engineer",
      "instructions": [
        "Read ticket requirements from input folder",
        "Implement code changes following architecture",
        "Create unit tests with good coverage",
        "Write development summary to outputs/response.md",
        "DO NOT create branches or push - handled by post-JS action"
      ],
      "formattingRules": "Markdown with sections: Approach, Files Modified, Test Coverage, Issues/Notes"
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Implement ticket\""
    ],
    "outputType": "none",
    "ticketContextDepth": 1,
    "skipAIProcessing": true,
    "postJSAction": "agents/developTicketAndCreatePR.js"
  }
}
```

**Workflow:**
1. Fetches ticket with full context
2. Cursor agent implements code + tests
3. Writes summary to `outputs/response.md`
4. JS action creates branch, commits, pushes
5. Creates PR with summary as description
6. Updates ticket status to "In Review"
7. Posts comment with PR link

### Use Case 3: Solution Design Creation

**Goal:** Create solution design tickets

**Configuration:** `agents/createSolutionDesignTicketsAndAssignForReview.js`
```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Solution Architect",
      "instructions": [
        "Analyze story requirements",
        "Create solution design document",
        "Break down into technical tasks",
        "Identify risks and dependencies"
      ]
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Create solution design\""
    ],
    "outputType": "comment",
    "ticketContextDepth": 1,
    "postJSAction": "agents/createSolutionDesignTicketsAndAssignForReview.js"
  }
}
```

---

## See Also

- **[Agent Configuration](agent-configuration.md)** - Complete configuration reference
- **[JavaScript Actions](javascript-actions.md)** - Post-processing with JS
- **[Jobs Documentation](../jobs/README.md)** - Available jobs reference
