/**
 * Configuration constants for agent scripts
 * Central location for all hardcoded values used across agent workflows
 */

// Jira Issue Types
const ISSUE_TYPES = {
    SUBTASK: 'Subtask',
    TASK: 'Task',
    STORY: 'Story',
    BUG: 'Bug'
};

// Jira Statuses
const STATUSES = {
    IN_REVIEW: 'In Review',
    IN_PROGRESS: 'In Progress',
    TODO: 'To Do',
    DONE: 'Done'
};

// Jira Priorities
const PRIORITIES = {
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
    HIGHEST: 'Highest',
    LOWEST: 'Lowest'
};

// Labels
const LABELS = {
    AI_GENERATED: 'ai_generated',
    AI_QUESTIONS_ASKED: 'ai_questions_asked',
    AI_SOLUTION_DESIGN_CREATED: 'ai_solution_design_created',
    AI_DEVELOPED: 'ai_developed',
    SD_CORE: 'sd_core',
    SD_API: 'sd_api',
    SD_UI: 'sd_ui',
    NEEDS_API_IMPLEMENTATION: 'needs_api_implementation',
    NEEDS_CORE_IMPLEMENTATION: 'needs_core_implementation'
};

// Git Configuration
const GIT_CONFIG = {
    AUTHOR_NAME: 'AI Teammate',
    AUTHOR_EMAIL: 'agent.ai.native@gmail.com',
    DEFAULT_BASE_BRANCH: 'main',
    DEFAULT_ISSUE_TYPE_PREFIX: 'feature'
};

// Solution Design Module Prefixes
const MODULE_PREFIXES = {
    CORE: '[SD CORE]',
    API: '[SD API]',
    UI: '[SD UI]'
};

// Module Configuration for Solution Design
const SOLUTION_DESIGN_MODULES = [
    { flag: 'core', prefix: MODULE_PREFIXES.CORE, label: LABELS.SD_CORE },
    { flag: 'api', prefix: MODULE_PREFIXES.API, label: LABELS.SD_API },
    { flag: 'ui', prefix: MODULE_PREFIXES.UI, label: LABELS.SD_UI }
];

// Diagram Defaults
const DIAGRAM_DEFAULTS = {
    API_SEQUENCE: 'sequenceDiagram\n    participant Client\n    participant API\n    Client->>API: Request\n    API-->>Client: Response',
    CORE_GRAPH: 'graph TD\n    A[SD CORE Enhancement] --> B[Technical Implementation]'
};

// Diagram Formatting
const DIAGRAM_FORMAT = {
    MERMAID_WRAPPER_START: '{code:mermaid}\n',
    MERMAID_WRAPPER_END: '\n{code}'
};

// Field Names
const JIRA_FIELDS = {
    DIAGRAMS: 'Diagrams'
};

// Summary Length Constraints
const SUMMARY_MAX_LENGTH = 120;

// Export all configuration
module.exports = {
    ISSUE_TYPES,
    STATUSES,
    PRIORITIES,
    LABELS,
    GIT_CONFIG,
    MODULE_PREFIXES,
    SOLUTION_DESIGN_MODULES,
    DIAGRAM_DEFAULTS,
    DIAGRAM_FORMAT,
    JIRA_FIELDS,
    SUMMARY_MAX_LENGTH
};

