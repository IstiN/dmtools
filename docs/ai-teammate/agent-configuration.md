# Agent Configuration Reference

Complete reference for configuring AI Teammate agents.

## Configuration File Structure

Agent configurations are JSON files located in the `agents/` folder:

```
agents/
├── story_description.json        # Story enhancement
├── story_development.json        # Automated development
├── story_questions.json          # Question generation
├── story_solution_design.json    # Solution design
├── assignForReview.js            # JavaScript action
├── developTicketAndCreatePR.js   # JavaScript action
└── common/
    ├── jiraHelpers.js
    └── aiResponseParser.js
```

## Basic Structure

```json
{
  "name": "Teammate",
  "params": {
    "agentParams": { /* AI configuration */ },
    "cliCommands": [ /* Commands to run */ ],
    "outputType": "field|comment|creation|none",
    "inputJql": "JQL query",
    "postJSAction": "agents/script.js"
  },
  "metadata": {
    "version": "1.0",
    "description": "What this agent does"
  }
}
```

---

## Configuration Parameters

### Job Name

```json
{
  "name": "Teammate"
}
```

**Available jobs:**
- `Teammate` - AI teammate for complex workflows
- `Expert` - AI expert for questions
- Any other job from the 20 available jobs

See **[Jobs Documentation](../jobs/README.md)** for all options.

---

### Agent Parameters (`agentParams`)

AI agent configuration:

```json
{
  "agentParams": {
    "aiRole": "Senior Java Developer",
    "instructions": [
      "Instruction 1",
      "Instruction 2",
      "Use dmtools CLI command: dmtools jira_get_ticket KEY"
    ],
    "formattingRules": "Output format rules",
    "knownInfo": "Additional context",
    "fewShots": "Example outputs"
  }
}
```

#### `aiRole` (String)
The role/persona for the AI agent.

**Examples:**
- `"Experienced Business Analyst"`
- `"Senior Java Software Engineer"`
- `"Solution Architect"`
- `"QA Lead"`

**Best practices:**
- Be specific about seniority level
- Include technology stack if relevant
- Match the role to the task

#### `instructions` (Array of Strings)
Detailed instructions for the AI.

**Examples:**
```json
"instructions": [
  "Read ticket details from input folder",
  "Enhance description following the template",
  "Use dmtools CLI to read child tickets:",
  "  dmtools jira_search_by_jql \"parent = TICKET-XXX\"",
  "Check Figma designs if referenced:",
  "  dmtools figma_download_image_of_file \"FIGMA_URL\"",
  "Write output to outputs/response.md",
  "DO NOT create branches or push code"
]
```

**Best practices:**
- Be explicit about what to do and what NOT to do
- Include CLI command examples
- Reference file paths (`input/`, `outputs/`)
- Explain where output should go
- Mention important constraints

#### `formattingRules` (String)
Output format requirements.

**Examples:**
```json
"formattingRules": "Use Jira markup: h2. for headings, *bold*, {code}code{code}"
```

```json
"formattingRules": "Markdown with sections: ## Approach, ## Files Modified, ## Test Coverage"
```

**Common formats:**
- Jira markup: `h2.`, `*bold*`, `{code}`, `{quote}`
- Markdown: `##`, `**bold**`, `` `code` ``
- HTML: `<h2>`, `<strong>`, `<pre>`
- Plain text

#### `knownInfo` (String)
Additional context or knowledge base.

**Examples:**
```json
"knownInfo": "Project uses Spring Boot 3.x with Java 17. Follow existing patterns in src/main/java."
```

**Use cases:**
- Project-specific conventions
- Architecture decisions
- Technology stack details
- Team guidelines

#### `fewShots` (String)
Example outputs for few-shot learning.

**Examples:**
```json
"fewShots": "## Approach\nImplemented feature X using pattern Y.\n\n## Files Modified\n- FileA.java - Added method foo()\n- FileB.java - Updated logic"
```

---

### CLI Commands (`cliCommands`)

Commands to execute:

```json
{
  "cliCommands": [
    "./cicd/scripts/run-cursor-agent.sh \"Task description\"",
    "./scripts/custom-script.sh param1 param2"
  ]
}
```

**Common patterns:**

**Run Cursor Agent:**
```json
"cliCommands": [
  "./cicd/scripts/run-cursor-agent.sh \"Read input folder and enhance story description\""
]
```

**Multiple Commands:**
```json
"cliCommands": [
  "echo 'Starting workflow'",
  "./scripts/prepare-context.sh",
  "./cicd/scripts/run-cursor-agent.sh \"Main task\"",
  "./scripts/post-process.sh"
]
```

---

### Output Configuration

#### `outputType`
Where to put the results:

```json
{
  "outputType": "field"  // or "comment", "creation", "none"
}
```

**Options:**

1. **`field`** - Update a Jira field
   ```json
   {
     "outputType": "field",
     "fieldName": "Description",
     "operationType": "Replace"  // or "Append"
   }
   ```

2. **`comment`** - Add a Jira comment
   ```json
   {
     "outputType": "comment"
   }
   ```

3. **`creation`** - Create new ticket
   ```json
   {
     "outputType": "creation"
   }
   ```

4. **`none`** - No automatic output (handled by postJSAction)
   ```json
   {
     "outputType": "none"
   }
   ```

---

### Ticket Selection

#### `inputJql` (String)
JQL query to select tickets:

```json
{
  "inputJql": "key = PROJ-123"
}
```

**Examples:**

**Single ticket:**
```json
"inputJql": "key = DMC-532"
```

**Multiple tickets:**
```json
"inputJql": "project = PROJ AND status = 'Ready for Development'"
```

**Sprint tickets:**
```json
"inputJql": "project = PROJ AND sprint = 42 AND type = Story"
```

**Dynamic (from trigger):**
```json
"inputJql": "key = ${TICKET_KEY}"
```

#### `ticketContextDepth` (Integer)
How much related information to include:

```json
{
  "ticketContextDepth": 1
}
```

**Levels:**
- **`0`**: Only the ticket itself
- **`1`**: Ticket + direct children (subtasks, questions, solution design)
- **`2`**: Ticket + children + linked tickets (blocks, relates to, etc.)

**Impact on context:**
- Higher depth = more context = better decisions
- Higher depth = more tokens = higher cost
- Typical: Use `0` for simple tasks, `1` for development

---

### Processing Options

#### `skipAIProcessing` (Boolean)
Skip AI processing and use CLI output directly:

```json
{
  "skipAIProcessing": true
}
```

**When to use:**
- Cursor agent already provides complete output
- No additional AI processing needed
- Output should be used as-is

**Example workflow:**
1. Cursor agent writes to `outputs/response.md`
2. With `skipAIProcessing: true`, that file content is used directly
3. Post-JS action can then process it further

#### `attachResponseAsFile` (Boolean)
Attach the response as a file to the ticket:

```json
{
  "attachResponseAsFile": true
}
```

**Use cases:**
- Large outputs (code, logs, reports)
- Preserve formatting
- Multiple outputs per ticket

---

### Post-Processing

#### `postJSAction` (String)
JavaScript file to run after completion:

```json
{
  "postJSAction": "agents/assignForReview.js"
}
```

**Common actions:**
- `agents/assignForReview.js` - Assign ticket for review
- `agents/developTicketAndCreatePR.js` - Create PR
- `agents/createSolutionDesignTicketsAndAssignForReview.js` - Create subtasks

See **[JavaScript Actions](javascript-actions.md)** for details.

---

### User Context

#### `initiator` (String)
User ID who triggered the workflow:

```json
{
  "initiator": "712020:2a248756-40e8-49d6-8ddc-6852e518451f"
}
```

**Uses:**
- Assign tickets back to the user
- Track who initiated the workflow
- Send notifications

**How to get user ID:**
```bash
dmtools jira_get_current_user_profile | jq -r '.accountId'
```

---

## Complete Examples

### Example 1: Story Enhancement

**File:** `agents/story_description.json`

```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Experienced Business Analyst",
      "instructions": [
        "Read ticket details from input folder (automatically prepared)",
        "Enhance story description following Confluence template",
        "Check child tickets for questions and solution design context:",
        "  dmtools jira_search_by_jql \"parent = TICKET-KEY\"",
        "If Figma designs are referenced, download and analyze them:",
        "  dmtools figma_download_image_of_file \"FIGMA_URL\"",
        "  dmtools gemini_ai_chat_with_files \"Analyze design\" \"image.png\"",
        "Keep all existing links and attachments in description",
        "Write enhanced description to outputs/response.md",
        "IMPORTANT: Follow Jira markdown template format"
      ],
      "formattingRules": "Use Jira markup: h2. for headings, *text* for bold, {code}code{code} for code blocks",
      "knownInfo": "Team uses specific story template from Confluence page"
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Enhance story description based on input\""
    ],
    "outputType": "field",
    "fieldName": "Description",
    "operationType": "Replace",
    "ticketContextDepth": 1,
    "attachResponseAsFile": false,
    "skipAIProcessing": true,
    "inputJql": "key = DMC-532",
    "initiator": "712020:2a248756-40e8-49d6-8ddc-6852e518451f",
    "postJSAction": "agents/assignForReview.js"
  },
  "metadata": {
    "version": "1.0",
    "description": "Enhance story descriptions with AI following team template"
  }
}
```

### Example 2: Automated Development

**File:** `agents/story_development.json`

```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Senior Java Software Engineer",
      "instructions": [
        "Read ticket details from input folder (includes full context)",
        "Analyze requirements, acceptance criteria, and business rules",
        "Understand existing codebase patterns before implementing",
        "Implement code changes:",
        "  - Source code following existing architecture patterns",
        "  - Unit tests with good coverage following test patterns",
        "  - Documentation ONLY if explicitly mentioned in requirements",
        "DO NOT create git branches, commit, or push (handled by post-action)",
        "Write comprehensive development summary to outputs/response.md:",
        "  - ## Approach: Design decisions made",
        "  - ## Files Modified: List with explanations",
        "  - ## Test Coverage: Tests created and what they cover",
        "  - ## Issues/Notes: Problems or incomplete work",
        "  - ## Warnings: Important notes for reviewers"
      ],
      "formattingRules": "Markdown with sections: ## Approach, ## Files Modified, ## Test Coverage, ## Issues/Notes",
      "fewShots": "## Approach\nImplemented using repository pattern.\n\n## Files Modified\n- UserService.java - Added validation\n- UserServiceTest.java - Unit tests with 95% coverage\n\n## Test Coverage\nHappy path, edge cases, error handling\n\n## Issues/Notes\nAll acceptance criteria met"
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Implement ticket requirements including code and tests. Write summary to outputs/response.md. DO NOT create branches.\""
    ],
    "outputType": "none",
    "ticketContextDepth": 1,
    "attachResponseAsFile": false,
    "skipAIProcessing": true,
    "postJSAction": "agents/developTicketAndCreatePR.js"
  },
  "metadata": {
    "version": "1.0",
    "description": "Automated ticket implementation with PR creation"
  }
}
```

### Example 3: Solution Design Generation

**File:** `agents/story_solution_design.json`

```json
{
  "name": "Teammate",
  "params": {
    "agentParams": {
      "aiRole": "Solution Architect",
      "instructions": [
        "Read story requirements from input folder",
        "Analyze technical requirements and constraints",
        "Create solution design document with:",
        "  - Architecture approach",
        "  - Component breakdown",
        "  - Technical decisions and rationale",
        "  - Risks and mitigation strategies",
        "  - Implementation tasks breakdown",
        "Output JSON format with tasks array for subtask creation",
        "Write to outputs/response.md"
      ],
      "formattingRules": "JSON with structure: {\"approach\": \"...\", \"tasks\": [{\"title\": \"...\", \"description\": \"...\"}]}"
    },
    "cliCommands": [
      "./cicd/scripts/run-cursor-agent.sh \"Create solution design\""
    ],
    "outputType": "comment",
    "ticketContextDepth": 1,
    "skipAIProcessing": true,
    "postJSAction": "agents/createSolutionDesignTicketsAndAssignForReview.js"
  },
  "metadata": {
    "version": "1.0",
    "description": "Generate solution design and create subtasks"
  }
}
```

---

## Best Practices

### 1. Be Explicit in Instructions

❌ **Bad:**
```json
"instructions": ["Enhance the ticket"]
```

✅ **Good:**
```json
"instructions": [
  "Read ticket from input folder",
  "Enhance description following template",
  "Use dmtools to check child tickets",
  "Write output to outputs/response.md",
  "Keep existing attachments and links"
]
```

### 2. Use Appropriate Context Depth

```json
// Simple task - no context needed
"ticketContextDepth": 0

// Development task - need subtasks/questions
"ticketContextDepth": 1

// Complex analysis - need full graph
"ticketContextDepth": 2
```

### 3. Choose Right Output Type

```json
// Small output - use field or comment
"outputType": "field"

// Large output - use file attachment or none + postJSAction
"outputType": "none",
"attachResponseAsFile": true
```

### 4. Organize Configurations

```
agents/
├── story/
│   ├── description.json
│   ├── questions.json
│   └── solution_design.json
├── development/
│   ├── implement.json
│   └── unittest.json
└── common/
    ├── jiraHelpers.js
    └── aiResponseParser.js
```

### 5. Version Your Configurations

```json
{
  "metadata": {
    "version": "2.1",
    "description": "Story enhancement with Figma support",
    "lastModified": "2024-01-15",
    "author": "team@company.com"
  }
}
```

---

## See Also

- **[AI Teammate Overview](README.md)** - Understanding the workflow
- **[JavaScript Actions](javascript-actions.md)** - Post-processing scripts
- **[GitHub Actions Integration](github-actions.md)** - CI/CD setup
- **[Jobs Documentation](../jobs/README.md)** - Available jobs reference
