# Instructions for user_story

## Field-Specific Guidelines

### summary
- **Purpose**: A concise title that clearly identifies the feature or functionality being developed
- **Format**: Brief phrase (typically 5-15 words), written in title case; start with a clear noun phrase or action or follow [Feature Area]: [Action/Capability] for [Subject/Entity] format
- **Required Elements**: Feature name, primary action or capability, target component/system (if applicable)
- **Best Practices**: 
  - Use clear, descriptive language that identifies the feature at a glance
  - Keep under 80 characters
  - Use consistent terminology across related stories
  - Make it specific enough to distinguish from similar features
  - Be specific about what's being implemented
  - Use present tense
  - Focus on user-facing functionality
  - Avoid technical jargon unless necessary for clarity
- **Examples**: 
  - "AWS Bedrock API Support"
  - "Refresh Token Support"
  - "Jira Mermaid Index"
  - "Microsoft Teams Integration"
  - "AI Teammate and Expert jobs needs to have preJSAction function"
  - "Chat Enablement in Main App and configuration with Real API"
  - "MCP Server Configuration Management for AI Integration"
  - "Job Configuration Management: CRUD Operations for Saved Jobs"
  - "CSS / JS moving to one source to avoid code duplication"
- **Common Mistakes to Avoid**: 
  - Overly technical summaries that obscure the business value
  - Vague titles that don't indicate the specific functionality
  - Including implementation details in the summary
  - Too generic titles
  - Vague summaries that could apply to multiple stories
  - Missing the business context
  - Using technical jargon that stakeholders won't understand

### description
- **Purpose**: Comprehensive explanation of the feature, including business context, user story, acceptance criteria, and technical details
- **Format**: Structured document with clear sections using Markdown/Wiki formatting
- **Required Elements**:
  - Story Points (if using estimation)
  - Business Context section
  - User Story in "As a... I want... So that..." format
  - Acceptance Criteria with numbered ACs
  - Business Rules section
  - Out of Scope section
  - References to related work (when applicable)
  - Feature overview (1-2 sentences)
  - Detailed feature list (bulleted)
- **Best Practices**:
  - Use heading levels (h1, h2, h3) or equivalent formatting to create clear hierarchy
  - Structure acceptance criteria as numbered items (AC 1, AC 2, etc.) or as a checklist
  - Include checkboxes ([ ]) for trackable implementation items
  - Use bullet points for lists within sections
  - Include technical context when relevant
  - Define business rules that govern the implementation
  - Clearly state what is out of scope to prevent scope creep
  - Format code snippets, file paths, and technical terms with {{double curly braces}}
  - Reference related tickets or documentation when applicable
  - Group related acceptance criteria under clear headings
  - Start with a clear statement of user value
- **Examples**:
  ```
  *Story Points:* 8

  *Business Context:*
  Users need to integrate AWS Bedrock API as an AI provider option in the DMTools platform, similar to existing providers like Ollama, Gemini, and Anthropic. This enables users to leverage AWS Bedrock's managed AI services, including Claude Sonnet, Qwen, and Amazon Nova models, for their automation and AI-powered workflows.

  *User Story:*
  As a DMTools user
  I want to use AWS Bedrock API as an AI provider with the same interface and configuration pattern as OllamaClient
  So that I can leverage AWS Bedrock models for AI-powered automation tasks with consistent configuration and usage patterns

  *Acceptance Criteria:*

  AC 1 - AWS Bedrock Client Implementation
  - System implements {{BedrockAIClient}} class that extends {{AbstractRestClient}} and implements {{AI}} interface
  - System implements {{BasicBedrockAI}} class that wraps {{BedrockAIClient}} and reads configuration from properties
  - System supports AWS Bedrock API endpoint structure
  - System uses Bearer token authentication via {{Authorization: Bearer {AWS_BEARER_TOKEN_BEDROCK}}} header
  - System sets {{Content-Type: application/json}} header for all requests

  [Additional ACs...]

  *Business Rules:*
  - AWS Bedrock integration follows the same architectural pattern as existing AI providers
  - Configuration properties must follow the naming convention: {{AWS_BEDROCK_*}} for all Bedrock-specific settings
  - {{DEFAULT_LLM}} configuration takes precedence over auto-detection when set to "bedrock"
  - Image encoding must use base64 without data URI prefix to match AWS Bedrock API requirements

  *Out of Scope:*
  - AWS SDK for Java integration (implementation uses direct HTTP calls via {{AbstractRestClient}})
  - Support for AWS Bedrock streaming responses
  - Support for AWS Bedrock batch inference
  ```

  ```
  *Story Points:* 8

  *Business Context:*
  Currently, AI Teammate and Expert jobs support {{postJSAction}} functions that execute after AI processing completes. However, there's no equivalent mechanism to execute logic before AI processing begins. This limits the ability to perform pre-execution checks, ticket validation, status changes, or conditional execution control.

  *User Story:*
  As a developer using AI Teammate and Expert jobs
  I want to execute custom JavaScript functions before AI processing begins
  So that I can validate tickets, update their status, apply labels, and conditionally control job execution based on ticket state

  *Acceptance Criteria:*
  AC 1 - Pre-Action Configuration Parameter
  - [ ] Add {{preJSAction}} configuration parameter to Teammate job config ({{dmtools-server/src/main/resources/jobs/teammate.json}})
  - [ ] Add {{preJSAction}} configuration parameter to Expert job config ({{dmtools-server/src/main/resources/jobs/expert.json}})
  - [ ] Parameter should support inline JavaScript, resource paths (e.g., {{agentFunctions/preProcess.js}}), or GitHub URLs

  *Business Rules:*
  - Pre-action functions must complete within the configured timeout period
  - Pre-action execution failures should not block AI processing (fail-safe design)

  *Out of Scope:*
  - Pre-action execution in other job types beyond Teammate and Expert
  - Asynchronous or parallel pre-action execution
  ```

  ```
  Enable users to create, save, update, and manage reusable job configurations that can be executed multiple times with different parameters.

  *Features:*

  * Save job configurations with predefined parameters and integration mappings
  * CRUD operations for job configurations (Create, Read, Update, Delete)
  * User-scoped job configurations with proper authentication
  * Parameter override capability during execution
  * Execution history and statistics tracking
  * Webhook endpoints for external job triggering

  *Acceptance Criteria:*

  * [ ] Users can save job configurations with name, description, and parameters
  * [ ] Users can list their saved job configurations
  * [ ] Users can update existing job configurations
  * [ ] Users can delete job configurations they own
  * [ ] Users can execute saved jobs with parameter overrides
  * [ ] System tracks execution count and last execution time
  * [ ] Webhook endpoint available for external systems to trigger jobs
  ```
- **Common Mistakes to Avoid**:
  - Missing key sections (business context, user story, acceptance criteria)
  - Vague acceptance criteria without clear verification points
  - Mixing implementation details with requirements
  - Omitting business rules or out of scope items
  - Inconsistent formatting that makes the document hard to read
  - Missing business context or user story statement
  - Forgetting to define what's out of scope
  - Vague or untestable acceptance criteria
  - Including implementation details instead of user-facing functionality
  - Forgetting to structure the content with proper formatting
  - Omitting key user workflows

### diagrams
- **Purpose**: Visual representation of system architecture, workflows, data models, or user flows to enhance understanding
- **Format**: 
  - Mermaid syntax for diagrams, embedded within code blocks or referenced as attachments
  - Embedded images or links to diagram tools
  - Markdown references to attached files
  - Structured with clear titles and descriptions
- **Required Elements**:
  - Diagram type declaration (sequenceDiagram, flowchart, classDiagram, etc.)
  - Clear labels for components/actors
  - Logical flow or relationship indicators
  - Title or caption explaining the diagram's purpose
  - Legend if multiple node/arrow types are used
  - Proper attachment or linking to the ticket
  - Reference to diagram in the description when applicable
  - Brief description of what the diagram represents
  - Reference to related acceptance criteria or features
- **Best Practices**:
  - Use appropriate diagram type for the content (sequence for interactions, flowchart for processes)
  - Include diagram within {noformat} tags for Jira rendering or ```mermaid blocks
  - Add explanatory text before or after the diagram
  - Use consistent naming conventions for components
  - Keep diagrams focused on one aspect of the system
  - Include legend or notes for complex diagrams
  - Reference diagrams in the description text where relevant
  - Include diagrams as attachments with descriptive filenames
  - Label key components in diagrams
  - Use standard notation (UML, BPMN, etc.) when appropriate
  - Use color-coding consistently
- **Examples**:
  ```
  h2. Token Flow Diagram

  {noformat}sequenceDiagram
      participant Client
      participant AuthController
      participant JwtUtils
      participant UserService
      participant DB

      Note over Client,DB: Initial Login
      Client->>AuthController: POST /api/auth/local-login
      AuthController->>JwtUtils: generateJwtToken(email, userId)
      JwtUtils-->>AuthController: accessToken
      AuthController->>JwtUtils: generateRefreshToken(email, userId)
      JwtUtils-->>AuthController: refreshToken
      AuthController-->>Client: {accessToken, refreshToken, expiresIn}

      Note over Client,DB: Access Token Expired
      Client->>AuthController: POST /api/auth/refresh<br/>{refreshToken}
      AuthController->>JwtUtils: validateRefreshToken(refreshToken)
      JwtUtils-->>AuthController: valid
      AuthController->>JwtUtils: getClaimsFromRefreshToken(refreshToken)
      JwtUtils-->>AuthController: {email, userId}
      AuthController->>UserService: findByEmail(email)
      UserService->>DB: SELECT user
      DB-->>UserService: user
      UserService-->>AuthController: user
      AuthController->>JwtUtils: generateJwtToken(email, userId)
      JwtUtils-->>AuthController: newAccessToken
      AuthController->>JwtUtils: generateRefreshToken(email, userId)
      JwtUtils-->>AuthController: newRefreshToken (rotated)
      AuthController-->>Client: {accessToken, refreshToken, expiresIn, refreshExpiresIn}
      Note over Client: Client must update stored refresh token{noformat}
  ```

  ```
  *Sequence Diagram for Authentication Flow:*

  ```mermaid
  sequenceDiagram
      participant User
      participant TeamsClient
      participant MicrosoftGraph
      participant TokenCache
      
      User->>TeamsClient: Authenticate()
      TeamsClient->>TokenCache: Check for cached token
      alt Token exists and valid
          TokenCache-->>TeamsClient: Return cached token
      else No valid token
          TeamsClient->>MicrosoftGraph: Request new token
          MicrosoftGraph-->>TeamsClient: Return new token
          TeamsClient->>TokenCache: Store token
      end
      TeamsClient-->>User: Authentication complete
  ```
  ```
  
  ```
  *Workflow Diagram:*
  ![Job Configuration Workflow](link-to-diagram-or-attachment)

  *Architecture Diagram:*
  See attached file: [job_config_architecture.png]

  *UI Mockup:*
  The job configuration form should include the following elements:
  ![UI Mockup](link-to-mockup)
  ```
  
  - "See attached class diagram showing the relationship between Chat components"
  - "Implementation follows the sequence diagram attached to this ticket"
- **Common Mistakes to Avoid**:
  - Overly complex diagrams that try to show too much
  - Missing labels or unclear relationships
  - Incorrect syntax that prevents rendering
  - Diagrams without context or explanation
  - Using diagrams when text would be clearer
  - Not updating diagrams when requirements change
  - Inconsistent terminology between diagrams and text
  - Attaching diagrams without referencing them in the description
  - Using overly complex diagrams without explanation
  - Missing context or connection to the user story
  - Outdated diagrams that don't match current requirements
  - Poor resolution or unreadable diagrams
  - Diagrams that focus on implementation rather than user experience

## Integration Guidelines
- The **summary** should align with the primary objective described in the **description**
- **Diagrams** should visually represent complex aspects of the functionality described in the **description**
- **Acceptance criteria** in the description should be testable and align with any workflow shown in **diagrams**
- **Business rules** in the description should be reflected in constraints shown in **diagrams**
- When referencing technical components in the **description**, ensure they match the naming in **diagrams**
- **Diagrams** should be placed after the relevant section in the **description** that they illustrate
- For complex features, include multiple **diagrams** to illustrate different aspects (architecture, sequence, data model)
- Use consistent terminology across **summary**, **description**, and **diagrams**
- Business context should explain why the feature is needed and how it relates to existing functionality
- References to diagrams should appear in relevant sections of the description
- Out of scope items should clarify boundaries implied by acceptance criteria
- Story points should reflect the complexity evident in the description and diagrams
- Each acceptance criterion should be traceable to a feature or capability
- Technical constraints or dependencies should be consistent across all fields

## Quality Checklist

### Summary Quality
- [ ] Is the summary concise and descriptive?
- [ ] Does it clearly identify the feature being developed?
- [ ] Is it free of implementation details?
- [ ] Would stakeholders understand what this story is about from the summary alone?
- [ ] Is it consistent with related stories in terminology?
- [ ] Would a non-technical stakeholder understand what this story is about?

### Description Quality
- [ ] Does the business context explain why this feature is valuable?
- [ ] Is the user story written in the standard format with clear benefit?
- [ ] Are acceptance criteria specific, measurable, and testable?
- [ ] Is each AC focused on a single aspect of functionality?
- [ ] Are business rules clearly defined?
- [ ] Is the out of scope section included to prevent scope creep?
- [ ] Are technical details appropriate and clear?
- [ ] Is the formatting consistent and readable?
- [ ] Are code paths, file references, and technical parameters clearly identified?
- [ ] Are all acceptance criteria grouped logically?
- [ ] Are story points included?
- [ ] Are references to related work included when applicable?
- [ ] Is the user value clearly stated?
- [ ] Are all features listed with sufficient detail?
- [ ] Are all user workflows covered?
- [ ] Is there a clear definition of "done"?

### Diagram Quality
- [ ] Do diagrams add value beyond what text alone provides?
- [ ] Is the diagram type appropriate for what's being illustrated?
- [ ] Are all components and relationships clearly labeled?
- [ ] Is the diagram syntax correct for proper rendering?
- [ ] Is there explanatory text providing context for the diagram?
- [ ] Does the diagram align with the written requirements?
- [ ] Is the diagram focused on a specific aspect rather than trying to show everything?
- [ ] Are diagrams referenced appropriately in the description text?
- [ ] Are diagrams properly attached or linked?
- [ ] Do diagrams clearly illustrate the concepts they're meant to explain?
- [ ] Do diagrams clearly illustrate the relevant workflows or architecture?
- [ ] Are diagrams at an appropriate level of detail?
- [ ] Do diagrams focus on user experience rather than implementation details?

### Overall Integration/Story Quality
- [ ] Is terminology consistent across all fields?
- [ ] Do diagrams support and clarify the written requirements?
- [ ] Are technical components named consistently in text and diagrams?
- [ ] Is the level of detail appropriate for the audience?
- [ ] Would a new team member understand the requirements from this story?
- [ ] Are dependencies on other systems or components identified?
- [ ] Are technical constraints and limitations documented?
- [ ] Does the story follow consistent formatting throughout?
- [ ] Do all elements (summary, description, diagrams) tell a consistent story?
- [ ] Is there traceability between acceptance criteria and diagrams?
- [ ] Does the description provide enough context for the diagrams to be meaningful?
- [ ] Is the story focused on a single cohesive feature or capability?
- [ ] Is it clear how this story fits into the larger epic or project?
- [ ] Are dependencies or prerequisites identified?
- [ ] Is the scope appropriate for a single iteration?
- [ ] Would a developer have enough information to implement this story?
- [ ] Would a QA engineer have enough information to test this story?