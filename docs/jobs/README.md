# Jobs Documentation

DMTools includes 20 automation jobs powered by **JobRunner** for various software development management tasks.

## What are Jobs?

**Jobs** are automated workflows that combine:
- **AI capabilities** (code generation, analysis, documentation)
- **Integration access** (Jira, Confluence, GitHub, Figma)
- **Business logic** (reports, estimates, test generation)

Each job accepts JSON configuration and produces structured output.

---

## Quick Start

### List Available Jobs

```bash
# List all 20 jobs
dmtools --list-jobs
```

**Output:**
```
Available Jobs:
  1. PreSaleSupport
  2. DocumentationGenerator
  3. RequirementsCollector
  4. JEstimator
  5. TestCasesGenerator
  6. SolutionArchitectureCreator
  7. DiagramsCreator
  8. CodeGenerator
  9. DevProductivityReport
  10. BAProductivityReport
  11. BusinessAnalyticDORGeneration
  12. QAProductivityReport
  13. ScrumMasterDaily
  14. Expert
  15. Teammate
  16. SourceCodeTrackerSyncJob
  17. SourceCodeCommitTrackerSyncJob
  18. UserStoryGenerator
  19. UnitTestsGenerator
  20. CommitsTriage
  21. JSRunner
```

### Run a Job

```bash
# Create configuration file
cat > my-job.json << EOF
{
  "name": "Expert",
  "params": {
    "question": "What is the architecture of this system?",
    "outputFormat": "markdown"
  }
}
EOF

# Execute job
dmtools run my-job.json
```

---

## Job Categories

Jobs are organized by primary function:

### Business Analysis Jobs
Process requirements, generate stories, and create documentation:
- **RequirementsCollector** - Collect and organize requirements from tickets
- **UserStoryGenerator** - Generate well-formatted user stories
- **BusinessAnalyticDORGeneration** - Create Definition of Ready documents
- **PreSaleSupport** - Process RFPs and generate proposals

### Development Jobs
Code generation, testing, and analysis:
- **CodeGenerator** - Generate code from specifications
- **UnitTestsGenerator** - Create unit tests automatically
- **CommitsTriage** - Analyze and categorize commits
- **SourceCodeTrackerSyncJob** - Sync source code with tracker

### QA Jobs
Test case generation and quality assurance:
- **TestCasesGenerator** - Generate test cases from stories
- **QAProductivityReport** - Track QA team productivity

### Reporting Jobs
Productivity and status reports:
- **DevProductivityReport** - Developer productivity metrics
- **BAProductivityReport** - BA team productivity tracking
- **ScrumMasterDaily** - Daily standup reports

### Architecture Jobs
Solution design and documentation:
- **SolutionArchitectureCreator** - Generate solution architecture
- **DiagramsCreator** - Create technical diagrams
- **DocumentationGenerator** - Generate technical documentation

### Automation Jobs
Advanced automation and AI assistance:
- **Expert** - AI expert for answering questions
- **Teammate** - AI teammate for complex workflows
- **JEstimator** - Estimate ticket complexity
- **JSRunner** - Execute JavaScript automation scripts
- **SourceCodeCommitTrackerSyncJob** - Sync commits to tracker

---

## Job Configuration Format

All jobs use a standard JSON configuration format:

```json
{
  "name": "<JobName>",
  "params": {
    // Job-specific parameters
  },
  "metadata": {
    // Optional metadata
    "version": "1.0",
    "timeout": 30
  }
}
```

### Common Parameters

Most jobs support these common parameters:

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `inputJql` | String | JQL query for ticket selection | `"project = PROJ AND status = Open"` |
| `initiator` | String | User ID of person running job | `"user@company.com"` |
| `outputType` | String | Output destination | `"field"`, `"comment"`, `"creation"`, `"none"` |
| `fieldName` | String | Target field for output | `"Description"`, `"Acceptance Criteria"` |
| `ticketContextDepth` | Integer | How many related tickets to include | `0`, `1`, `2` |

---

## Running Jobs

### Method 1: Configuration File

```bash
# Create config.json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Story AND status = 'Ready for QA'",
    "outputType": "comment",
    "initiator": "qa.lead@company.com"
  }
}

# Run job
dmtools run config.json
```

### Method 2: With Parameter Overrides

```bash
# Base configuration
cat > base-config.json << EOF
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "project = PROJ",
    "outputType": "field",
    "fieldName": "Description"
  }
}
EOF

# Override with different JQL
dmtools run base-config.json "$(echo '{"params":{"inputJql":"key = PROJ-123"}}' | base64)"
```

### Method 3: GitHub Actions

```yaml
- name: Generate Test Cases
  env:
    JIRA_EMAIL: ${{ secrets.JIRA_EMAIL }}
    JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
    GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
  run: |
    dmtools run .github/configs/test-cases-job.json
```

---

## Job Execution Modes

Jobs can run in different modes:

### CLI Mode (Default)
- Uses environment variables
- Direct execution from command line
- Best for local development

```bash
export JIRA_BASE_PATH="https://company.atlassian.net"
export GEMINI_API_KEY="AIza..."
dmtools run my-job.json
```

### Server-Managed Mode
- Used when running through DMTools server
- Credentials managed by server
- Best for web UI and OAuth flows

### Agent Mode
- Used in AI Teammate workflows
- Combines CLI + AI + automation
- Best for GitHub Actions automation

---

## Job Output Types

Jobs can output results to different destinations:

### 1. Field Output (`outputType: "field"`)
Updates a Jira ticket field:
```json
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "outputType": "field",
    "fieldName": "Description",
    "operationType": "Replace"
  }
}
```

### 2. Comment Output (`outputType: "comment"`)
Adds a Jira comment:
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "outputType": "comment"
  }
}
```

### 3. Creation Output (`outputType: "creation"`)
Creates a new ticket:
```json
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Epic",
    "outputType": "creation"
  }
}
```

### 4. None Output (`outputType: "none"`)
Returns result without updating anything:
```json
{
  "name": "Expert",
  "params": {
    "question": "Explain the architecture",
    "outputType": "none"
  }
}
```

---

## Advanced Features

### Ticket Context Depth

Control how much related information is included:

```json
{
  "name": "Teammate",
  "params": {
    "inputJql": "key = PROJ-123",
    "ticketContextDepth": 2
  }
}
```

- **0**: Only the ticket itself
- **1**: Ticket + direct children (subtasks)
- **2**: Ticket + children + linked tickets

### Post-Processing with JavaScript

Execute JavaScript after job completion:

```json
{
  "name": "Teammate",
  "params": {
    "inputJql": "key = PROJ-123",
    "postJSAction": "agents/assignForReview.js"
  }
}
```

See **[AI Teammate > JavaScript Actions](../ai-teammate/javascript-actions.md)** for details.

### CLI Command Integration

Run external commands during job execution:

```json
{
  "name": "Teammate",
  "params": {
    "cliCommands": [
      "./scripts/run-cursor-agent.sh \"Implement the feature from input folder\""
    ],
    "skipAIProcessing": true
  }
}
```

### Attach Response as File

Save job output as attachment:

```json
{
  "name": "CodeGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "attachResponseAsFile": true
  }
}
```

---

## Job Examples by Use Case

### Use Case 1: Automated Story Enhancement

Enhance all stories in a sprint with AI-generated acceptance criteria:

```json
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "project = PROJ AND sprint = 42 AND type = Story",
    "outputType": "field",
    "fieldName": "Acceptance Criteria",
    "operationType": "Append",
    "agentParams": {
      "aiRole": "Senior Business Analyst",
      "instructions": [
        "Generate detailed acceptance criteria in Gherkin format",
        "Include edge cases and error scenarios",
        "Follow the team's acceptance criteria template"
      ]
    }
  }
}
```

### Use Case 2: Automated Test Case Generation

Generate test cases for all stories ready for QA:

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND status = 'Ready for QA' AND 'Test Cases' is EMPTY",
    "outputType": "comment",
    "initiator": "qa.lead@company.com"
  }
}
```

### Use Case 3: Weekly Productivity Report

Generate developer productivity report:

```json
{
  "name": "DevProductivityReport",
  "params": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-07",
    "team": "Backend Team",
    "outputType": "comment"
  }
}
```

### Use Case 4: AI Expert Consultation

Ask AI expert about architecture:

```json
{
  "name": "Expert",
  "params": {
    "question": "What are the main components of the authentication system and how do they interact?",
    "context": ["source:src/auth/**/*.java", "confluence:ARCH/Authentication Design"],
    "outputFormat": "markdown",
    "maxTokens": 2000
  }
}
```

---

## Troubleshooting

### Job Execution Fails

```bash
# Run with debug logging
dmtools run my-job.json --debug

# Check logs
tail -f ~/.dmtools/logs/dmtools.log
```

### Missing Parameters Error

**Error:** `Required parameter 'inputJql' is missing`

**Solution:** Ensure all required parameters are in config:
```json
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "key = PROJ-123"  // Required!
  }
}
```

### Integration Not Available

**Error:** `Jira integration not configured`

**Solution:** Set environment variables:
```bash
export JIRA_BASE_PATH="https://company.atlassian.net"
export JIRA_EMAIL="user@company.com"
export JIRA_API_TOKEN="your-token"
```

### AI Provider Errors

**Error:** `AI provider not configured`

**Solution:** Configure AI provider:
```bash
export GEMINI_API_KEY="AIza..."
# or
export OPEN_AI_API_KEY="sk-..."
```

---

## Best Practices

### 1. Start Small
Test jobs with single tickets before running on multiple:
```json
{
  "name": "UserStoryGenerator",
  "params": {
    "inputJql": "key = PROJ-123"  // Single ticket first
  }
}
```

### 2. Use Dry Run
For jobs that modify data, test output first:
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "outputType": "none"  // Check output before committing
  }
}
```

### 3. Organize Configurations
Keep job configs in a dedicated folder:
```
.github/
  jobs/
    test-cases.json
    user-stories.json
    daily-report.json
```

### 4. Use Version Control
Track job configurations in Git:
```bash
git add .github/jobs/*.json
git commit -m "Add test cases generation job config"
```

### 5. Monitor Execution
Set up logging and monitoring:
```bash
# Redirect output to log file
dmtools run my-job.json 2>&1 | tee job-execution.log
```

---

## See Also

- **[AI Teammate Workflows](../ai-teammate/README.md)** - Automate jobs with GitHub Actions
- **[MCP Tools Reference](../cli-usage/mcp-tools.md)** - Command-line interface basics
- **[Configuration Guide](../getting-started/configuration.md)** - Environment setup
- **[API Reference](../api/rest-api.md)** - Programmatic access to jobs
