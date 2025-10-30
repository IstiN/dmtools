# Agent Scripts

This directory contains JavaScript agent scripts for automating Jira workflow tasks.

## Directory Structure

```
agents/js/
├── config.js                                      # Centralized configuration constants
├── common/                                        # Shared utility functions
│   ├── aiResponseParser.js                       # AI response parsing utilities
│   └── jiraHelpers.js                            # Jira operation helpers
├── assignForReview.js                            # Assign ticket to initiator and move to "In Review"
├── createQuestionsAndAssignForReview.js          # Create question subtasks and assign for review
├── createSolutionDesignTicketsAndAssignForReview.js  # Create SD subtasks (Core/API/UI)
├── developTicketAndCreatePR.js                   # Git operations and PR creation workflow
├── enhanceSDAPIDescriptionAndAssess.js           # Enhance SD API ticket descriptions
└── enhanceSDCoreDescriptionAndAssess.js          # Enhance SD CORE ticket descriptions
```

## Work-In-Progress (WIP) Label System

All agent jobs now support a WIP label system to prevent automated processing of tickets being manually worked on:

### How It Works

1. **Pre-Action Check**: `checkWipLabel.js` runs before any processing
2. **Dynamic Label Generation**: WIP label is generated from `contextId` (e.g., `api_development` → `api_development_wip`)
3. **Label Detection**: Checks if ticket has the generated WIP label
4. **Skip Processing**: If WIP label found, posts comment and returns `false` to skip
5. **Label Removal**: WIP label is automatically removed when processing completes successfully

### WIP Labels by Job

Each job has a unique WIP label dynamically generated from `metadata.contextId`:

| Job Configuration | Context ID | Generated WIP Label |
|-------------------|------------|---------------------|
| `api_development.json` | `api_development` | `api_development_wip` |
| `story_development.json` | `story_development` | `story_development_wip` |
| `sd_api_description.json` | `sd_api_description` | `sd_api_description_wip` |
| `sd_core_description.json` | `sd_core_description` | `sd_core_description_wip` |
| `core_description.json` | `core_description` | `core_description_wip` |
| `story_solution_design.json` | `story_solution_design` | `story_solution_design_wip` |
| `story_questions.json` | `story_questions` | `story_questions_wip` |
| `story_description.json` | `story_description` | `story_description_wip` |

### Usage Example

To prevent automated processing of a ticket:
1. Add the appropriate WIP label to the ticket (e.g., `api_development_wip`)
2. Automated job will detect the label and skip processing
3. Comment will be posted explaining why processing was skipped
4. Remove the label when ready for automated processing

## Configuration (`config.js`)

Centralized configuration for all agent scripts. Contains:

### Issue Types
- `ISSUE_TYPES.SUBTASK`: 'Subtask'
- `ISSUE_TYPES.TASK`: 'Task'
- `ISSUE_TYPES.STORY`: 'Story'
- `ISSUE_TYPES.BUG`: 'Bug'

### Statuses
- `STATUSES.IN_REVIEW`: 'In Review'
- `STATUSES.IN_PROGRESS`: 'In Progress'
- `STATUSES.TODO`: 'To Do'
- `STATUSES.DONE`: 'Done'

### Priorities
- `PRIORITIES.LOW`, `MEDIUM`, `HIGH`, `HIGHEST`, `LOWEST`

### Labels
- `LABELS.AI_GENERATED`: 'ai_generated'
- `LABELS.AI_QUESTIONS_ASKED`: 'ai_questions_asked'
- `LABELS.AI_SOLUTION_DESIGN_CREATED`: 'ai_solution_design_created'
- `LABELS.AI_DEVELOPED`: 'ai_developed'
- `LABELS.SD_CORE`: 'sd_core'
- `LABELS.SD_API`: 'sd_api'
- `LABELS.SD_UI`: 'sd_ui'
- `LABELS.NEEDS_API_IMPLEMENTATION`: 'needs_api_implementation'
- `LABELS.NEEDS_CORE_IMPLEMENTATION`: 'needs_core_implementation'

### Git Configuration
- `GIT_CONFIG.AUTHOR_NAME`: 'AI Teammate'
- `GIT_CONFIG.AUTHOR_EMAIL`: 'agent.ai.native@gmail.com'
- `GIT_CONFIG.DEFAULT_BASE_BRANCH`: 'main'
- `GIT_CONFIG.DEFAULT_ISSUE_TYPE_PREFIX`: 'feature'

### Solution Design Modules
- `SOLUTION_DESIGN_MODULES`: Array of module configurations (core, api, ui)

### Diagram Defaults & Formatting
- `DIAGRAM_DEFAULTS`: Default Mermaid diagrams for API and Core
- `DIAGRAM_FORMAT`: Wrapper formats for Jira code blocks

## Common Utilities

### `jiraHelpers.js`
- `assignForReview(ticketKey, initiatorId)`: Assign ticket and move to "In Review" status
- `extractTicketKey(result)`: Extract ticket key from Jira API response
- `setTicketPriority(ticketKey, priority)`: Set ticket priority

### `aiResponseParser.js`
- `parseQuestionsResponse(response)`: Parse AI-generated questions array
- `buildSummary(summary, index)`: Build ticket summary with length constraints
- `buildDescription(question)`: Build ticket description

## Agent Scripts

### `checkWipLabel.js` (Pre-Action)
Pre-action script that checks for Work-In-Progress (WIP) labels on tickets before processing.

**Functionality:**
- Dynamically generates WIP label from `metadata.contextId` (e.g., `api_development` → `api_development_wip`)
- Checks if ticket has the generated WIP label
- Returns `false` to stop processing if WIP label is found
- Posts a comment to the ticket explaining why processing was skipped
- Returns `true` to continue processing if no WIP label exists

**Usage:** Configured as `preJSAction` in all job configs to prevent automated processing of tickets that are being manually worked on.

### `assignForReview.js`
Simple action to assign a ticket to the initiator and move it to "In Review" status. Also removes the WIP label if configured.

### `createQuestionsAndAssignForReview.js`
Creates subtasks based on AI-generated questions, then assigns the parent ticket for review.
- Uses `ISSUE_TYPES.SUBTASK` for creating question tickets
- Adds `LABELS.AI_QUESTIONS_ASKED` label
- Moves to `STATUSES.IN_REVIEW`

### `createSolutionDesignTicketsAndAssignForReview.js`
Creates solution design subtasks (SD CORE, SD API, SD UI) based on AI module analysis.
- Creates subtasks with `PRIORITIES.MEDIUM`
- Adds module-specific labels (`LABELS.SD_CORE`, etc.)
- Posts summary comment with analysis results

### `developTicketAndCreatePR.js`
Handles complete git workflow: branch creation, commit, push, and PR creation.
- Configures git author as `GIT_CONFIG.AUTHOR_NAME`
- Creates unique branch names (e.g., `feature/DMC-123`)
- Creates PR using GitHub CLI with outputs/response.md as body
- Moves ticket to `STATUSES.IN_REVIEW`
- Adds `LABELS.AI_DEVELOPED` label

### `enhanceSDAPIDescriptionAndAssess.js`
Enhances SD API ticket descriptions with technical details and diagrams.
- Updates description and Diagrams field with Mermaid sequence diagrams
- Adds `LABELS.NEEDS_API_IMPLEMENTATION` if needed
- Moves to `STATUSES.IN_REVIEW`

### `enhanceSDCoreDescriptionAndAssess.js`
Enhances SD CORE ticket descriptions with technical details and diagrams.
- Updates description and Diagrams field with Mermaid graphs
- Adds `LABELS.NEEDS_CORE_IMPLEMENTATION` if needed
- Moves to `STATUSES.IN_REVIEW`

## Usage

Each agent script exports an `action(params)` function that expects:

```javascript
params = {
    ticket: {
        key: 'DMC-123',
        fields: {
            summary: 'Ticket summary',
            description: 'Ticket description',
            labels: ['label1', 'label2', ...] // Ticket labels
        }
    },
    response: '...', // AI-generated response (format varies by agent)
    initiator: 'accountId123', // Jira account ID of initiator
    metadata: {
        contextId: 'api_development' // Context ID (WIP label generated as contextId + '_wip')
    }
}
```

Returns:
```javascript
{
    success: true/false,
    message: '...',
    error: '...' // if success is false
}
```

## Migration Notes

All scripts have been refactored to use centralized configuration from `config.js`. This makes it easy to:
- Update hardcoded values like statuses, priorities, and labels in one place
- Maintain consistency across all agent scripts
- Easily modify git configuration or diagram defaults

When adding new constants, add them to `config.js` and export them in the module.exports object.

