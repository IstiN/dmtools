# Instructions for user_story

## Field-Specific Guidelines

### summary
- **Purpose**: A concise title that clearly identifies the feature or functionality being developed
- **Format**: Brief phrase (5-10 words) that captures the essence of the story
- **Required Elements**: Feature name or action, subject/user role when relevant
- **Best Practices**: 
  - Use clear, non-technical language
  - Focus on the "what" not the "how"
  - Keep under 80 characters
  - Use consistent naming conventions
- **Examples**: 
  - "Refresh Token Support"
  - "AWS Bedrock API Integration"
- **Common Mistakes to Avoid**: 
  - Technical implementation details
  - Vague or overly broad titles
  - Acronyms without context
  - Including priority or status in the title

### description
- **Purpose**: Comprehensive explanation of the feature, including business context, user story, acceptance criteria, and implementation details
- **Format**: Structured document with clear sections using headings, bullet points, and formatting
- **Required Elements**: 
  - Story points (optional but common)
  - Business context
  - User story (As a... I want... So that...)
  - Acceptance criteria (numbered AC items)
  - Business rules
  - Out of scope items
- **Best Practices**: 
  - Start with business context to explain "why" this feature matters
  - Write user story from end-user perspective
  - Number acceptance criteria (AC 1, AC 2, etc.)
  - Use checkboxes or status indicators for tracking progress
  - Include technical details after user-focused content
  - Use formatting (headings, lists, code blocks) for readability
  - Reference related tickets or documentation when applicable
  - Define clear boundaries with "Out of Scope" section
- **Examples**:

```
*Story Points:* 5

*Business Context:*
Currently, users must re-authenticate when their access token expires, creating a poor user experience. Implementing refresh tokens will allow seamless session continuation for active users while maintaining security.

*User Story:*
As a user of the application
I want my session to remain active as long as I'm using the system
So that I don't need to repeatedly log in during my work

*Acceptance Criteria:*

AC 1 - Refresh Token Generation
- System generates refresh tokens during login with 30-day expiration
- Refresh tokens are securely stored with proper encryption
- Each user receives a unique refresh token

AC 2 - Token Refresh Endpoint
- System provides /api/auth/refresh endpoint accepting refresh tokens
- Endpoint validates token authenticity and expiration
- Endpoint returns new access token and refresh token pair
- Old refresh tokens are invalidated after use (token rotation)

*Business Rules:*
- Refresh tokens must be rotated on each use for security
- Inactive users (no refresh for 30 days) must re-authenticate
- Implementation must use stateless JWT tokens consistent with current architecture

*Out of Scope:*
- Token revocation endpoint
- Refresh token blacklisting
- Device-specific tokens
```

- **Common Mistakes to Avoid**: 
  - Missing business context or user story
  - Vague acceptance criteria without clear verification points
  - Technical implementation details without user-focused content
  - Undefined scope boundaries
  - Unstructured wall of text
  - Missing validation criteria

## Integration Guidelines
- The summary should reflect the main objective detailed in the description
- User story in the description should align with the summary's focus
- Acceptance criteria should cover all aspects mentioned in the business context
- Business rules should clarify constraints and requirements that span multiple acceptance criteria
- Out of scope items should prevent scope creep by clearly defining boundaries

## Quality Checklist

### Summary Quality
- Is it concise (under 80 characters)?
- Does it clearly identify what is being built?
- Is it free of technical implementation details?
- Would stakeholders understand what this ticket is about?

### Description Quality
- Does it include clear business context explaining why this feature matters?
- Is the user story written from end-user perspective (As a... I want... So that...)?
- Are acceptance criteria specific, measurable, and testable?
- Does each AC have clear verification points?
- Are business rules clearly defined?
- Are out-of-scope items explicitly listed?
- Is the formatting clean and readable with proper sections?
- Are technical details provided where needed but not overwhelming?
- Would a new team member understand what needs to be built?
- Are dependencies or related tickets referenced?

### Overall Ticket Quality
- Is there alignment between summary and description?
- Is the scope clearly defined with boundaries?
- Are all stakeholder needs addressed?
- Is the implementation approach clear without being overly prescriptive?
- Does the ticket provide enough context for estimation?

---

# Instructions for user_story

## Field-Specific Guidelines

### summary
- **Purpose**: Concise title that clearly identifies the feature or functionality being developed
- **Format**: Brief phrase (typically 5-10 words) that captures the essence of the user story
- **Required Elements**: Feature name, primary action or capability being added
- **Best Practices**: 
  - Use clear, non-technical language
  - Focus on the "what" not the "how"
  - Keep under 80 characters
  - Use consistent naming conventions across related stories
- **Examples**: 
  - "KB Inbox Scanner and Processor for Teams/Manual Sources"
  - "Microsoft Teams Integration"
- **Common Mistakes to Avoid**: 
  - Technical implementation details in summary
  - Overly vague titles ("Improvements", "Updates")
  - Acronyms without context
  - Too lengthy or too brief summaries

### description
- **Purpose**: Comprehensive explanation of the user story including business context, user needs, acceptance criteria, and implementation details
- **Format**: Structured document with clear sections (Business Context, User Story, Acceptance Criteria, Business Rules, Out of Scope)
- **Required Elements**:
  - Story points estimation
  - Business context/problem statement
  - User story in "As a... I want to... So that..." format
  - Numbered acceptance criteria with clear, testable requirements
  - Business rules and constraints
  - Out of scope items
- **Best Practices**:
  - Use formatting to improve readability (headers, bullet points, code blocks)
  - Break down acceptance criteria into logical groups
  - Include technical context when relevant
  - Specify clear boundaries with "Out of Scope" section
  - Use consistent terminology throughout
  - Include references to related tickets or documentation
- **Examples**:
  ```
  *Story Points:* 8

  *Business Context:*
  Teams messages and other knowledge sources need automated processing into the KB system. Currently, KB processing requires manual intervention to fetch Teams messages and process files individually.

  *User Story:*
  As a *KB administrator*
  I want to *automatically fetch Teams messages and process inbox files into the KB*
  So that *knowledge is continuously captured and the KB stays current without manual effort*

  *Acceptance Criteria:*

  AC 1 - File Write Tool Extension
  - [ ] FileTools class has new {{file_write}} MCP tool method
  - [ ] Tool accepts {{path}} and {{content}} string parameters
  - [ ] Tool creates parent directories automatically if they don't exist

  *Business Rules:*
  - Chat name sanitization: replace all non-alphanumeric characters with underscore, convert to lowercase
  - Files are never deleted from inbox after processing (KB tracks via analyzed folder)

  *Out of Scope:*
  - File deletion or cleanup from inbox folders (future enhancement)
  - Multiple Teams chats processing in single run (requires separate story)
  ```
- **Common Mistakes to Avoid**:
  - Missing acceptance criteria
  - Vague or untestable requirements
  - Mixing implementation details with user requirements
  - Omitting business context or problem statement
  - No clear definition of what's out of scope
  - Inconsistent terminology

## Integration Guidelines
- The summary should reflect the primary objective described in the description
- User story statement should align with the business context
- Acceptance criteria should cover all aspects mentioned in the user story
- Business rules should provide constraints and guidelines for implementing the acceptance criteria
- Out of scope items should clarify boundaries to prevent scope creep
- Story points should reflect the complexity evident in the acceptance criteria

## Quality Checklist

### Summary Quality
- [ ] Is the summary concise and descriptive?
- [ ] Does it clearly identify the feature being developed?
- [ ] Is it free of implementation details?
- [ ] Would stakeholders understand what this story is about from the summary alone?

### Description Quality
- [ ] Is the business context clearly explained?
- [ ] Is the user story written in the standard format (As a... I want to... So that...)?
- [ ] Are all acceptance criteria specific, measurable, and testable?
- [ ] Are acceptance criteria grouped logically?
- [ ] Are business rules clearly defined?
- [ ] Is the scope boundary clear with out-of-scope items listed?
- [ ] Are story points assigned appropriately for the complexity?
- [ ] Are technical terms and acronyms explained or commonly understood?
- [ ] Are there references to related documentation or tickets where appropriate?

### Overall Coherence
- [ ] Do all parts of the user story align with each other?
- [ ] Is there traceability from business context to user story to acceptance criteria?
- [ ] Are there any contradictions between different sections?
- [ ] Is the level of detail appropriate for the development team to implement?
- [ ] Would a new team member understand what needs to be built?

---

# Instructions for user_story

## Field-Specific Guidelines

### summary
- **Purpose**: A concise statement that clearly identifies what the user story is about
- **Format**: Brief phrase (typically 5-15 words) that captures the essence of the feature or change
- **Required Elements**: Feature name, primary functionality, or key benefit
- **Best Practices**: 
  - Use clear, non-technical language
  - Focus on the primary value or functionality
  - Keep it short but descriptive enough to understand the story's purpose
- **Examples**: 
  - "Chat Enablement in Main App with Real API Integration"
  - "User Authentication with OAuth and Local Login Support"
- **Common Mistakes to Avoid**: 
  - Technical jargon that business stakeholders won't understand
  - Overly vague titles ("UI Improvements")
  - Including implementation details in the summary

### description
- **Purpose**: Comprehensive explanation of the user story including business context, user needs, acceptance criteria, and implementation boundaries
- **Format**: Structured sections with clear headings and formatted lists
- **Required Elements**: 
  - Story Points estimation
  - Business Context section
  - User Story statement (As a... I want... So that...)
  - Acceptance Criteria (numbered AC items with checkboxes)
  - Business Rules
  - Out of Scope items
- **Best Practices**: 
  - Start with story points for effort estimation
  - Provide clear business context explaining why this feature matters
  - Format user story in standard "As a [role], I want [capability], so that [benefit]" format
  - Structure acceptance criteria as testable statements with checkboxes
  - Group related acceptance criteria under clear AC headings
  - List business rules that govern the implementation
  - Explicitly state what is out of scope to prevent scope creep
  - Include references to related tickets or documentation when relevant
  - Use formatting (bold, lists, headings) to improve readability
- **Examples**:

```
*Story Points:* 8

*Business Context:*
Users need direct access to AI chat functionality within the main application to improve productivity and streamline AI-assisted workflows. Currently, chat interface exists only in the styleguide, preventing users from accessing AI assistance while working with their actual data.

*User Story:*
As a authenticated user
I want to access AI chat functionality through a dedicated menu item in the main application
So that I can get AI assistance while working with my actual projects without leaving the application

*Acceptance Criteria:*
AC 1 - Navigation and Authentication
- [ ] New "Chat" menu item appears in the left navigation menu after authentication
- [ ] Chat menu item uses consistent styling with other navigation items
- [ ] Clicking Chat menu item navigates to dedicated chat screen/route
- [ ] Chat functionality respects existing authentication states

AC 2 - Chat Interface Integration
- [ ] Chat screen displays the existing chat interface from styleguide
- [ ] Chat interface maintains full functionality (message input, history, response display)
- [ ] Loading states are properly displayed during AI response generation
- [ ] Error states are handled gracefully with user-friendly messages

*Business Rules:*
- Chat functionality only available to authenticated users
- User can only see and select AI integrations they have access to
- Chat history is stored in memory only and resets on route navigation

*Out of Scope:*
- Chat history persistence across sessions
- Advanced chat features like file uploads or attachments
- Administrative chat management features
```

- **Common Mistakes to Avoid**: 
  - Missing business context or value proposition
  - Vague acceptance criteria that aren't testable
  - Including implementation details instead of focusing on behavior
  - Omitting out-of-scope items, leading to scope creep
  - Writing overly technical descriptions that business stakeholders can't understand
  - Forgetting to include story points for estimation

## Integration Guidelines
- The summary should reflect the primary functionality described in the user story section of the description
- Acceptance criteria should cover all aspects mentioned in the user story statement
- Business rules should clarify constraints mentioned in the acceptance criteria
- Out of scope items should relate to potential extensions of the functionality described
- Story points should reflect the complexity evident in the acceptance criteria
- References should link to related epics, parent tickets, or documentation mentioned in the description

## Quality Checklist

### Summary Quality Check
- Is the summary concise but descriptive?
- Does it clearly communicate what the story is about without technical jargon?
- Would stakeholders understand the purpose from just reading the summary?

### Description Quality Check
- Does the business context clearly explain why this story matters?
- Is the user story statement properly formatted (As a... I want... So that...)?
- Are all acceptance criteria specific, measurable, and testable?
- Do acceptance criteria have clear checkboxes for tracking completion?
- Are business rules clearly defined to guide implementation?
- Is the out of scope section used to prevent scope creep?
- Are story points included for estimation purposes?
- Is the description well-formatted with proper headings and lists?
- Would both technical and non-technical stakeholders understand the requirements?
- Are there any ambiguous terms or requirements that need clarification?
- Do the acceptance criteria cover all aspects of the user story?

---

# Instructions for user_story

## Field-Specific Guidelines

### summary
- Purpose: Concise title that clearly identifies the feature or functionality being developed
- Format: Start with feature area or component, followed by a brief description of the functionality
- Required Elements: Feature area/component, action or capability being added, target user (implied or explicit)
- Best Practices: Keep under 80 characters, use consistent terminology, focus on business value
- Examples: "User Profile Management: Edit Personal Information", "Payment Gateway: Support for Multiple Currencies"
- Common Mistakes to Avoid: Technical implementation details, vague descriptions, overly long titles

### description
- Purpose: Detailed explanation of the feature, its purpose, and acceptance criteria
- Format: Structured content with sections for context, features, and acceptance criteria
- Required Elements: 
  * Business context/user need
  * Feature details/capabilities
  * Acceptance criteria as a checklist
  * Optional: technical considerations, dependencies
- Best Practices: 
  * Use formatting (bold, lists) to improve readability
  * Structure acceptance criteria as testable statements
  * Include user perspective and business value
  * Specify clear boundaries of the feature scope
- Examples:
```
Enable users to manage their notification preferences across multiple channels.

*Features:*
* Channel-specific notification settings (email, SMS, in-app)
* Frequency controls for scheduled notifications
* Category-based filtering options
* Time-of-day delivery preferences

*Acceptance Criteria:*
* [ ] Users can toggle notifications on/off for each channel
* [ ] Users can select frequency (immediate, daily digest, weekly)
* [ ] Settings persist across sessions and devices
* [ ] Changes take effect without requiring application restart
* [ ] Admin panel shows aggregated user preference statistics
```
- Common Mistakes to Avoid: 
  * Missing acceptance criteria
  * Technical implementation details instead of user-focused requirements
  * Ambiguous or untestable criteria
  * Lack of clear scope boundaries

## Integration Guidelines
- The summary should reflect the main objective detailed in the description
- Acceptance criteria in the description should align with the feature scope indicated in the summary
- Each acceptance criterion should be independently testable
- Description should expand on the summary without contradicting it
- Epic relationship should be clear (parent-child relationship)

## Quality Checklist

### Summary Quality
- Does it clearly identify what functionality is being added?
- Is it concise yet descriptive?
- Does it avoid implementation details?
- Is it consistent with other story summaries in the project?

### Description Quality
- Does it explain the business value or user need?
- Are all features clearly listed?
- Are acceptance criteria specific, measurable, and testable?
- Is the scope clearly defined (what's in and what's out)?
- Are dependencies or prerequisites identified?
- Is formatting used effectively to improve readability?
- Does it avoid prescribing specific technical implementations unless necessary?
- Are all stakeholder requirements captured?

### Overall Story Quality
- Is the story sized appropriately (not too large to complete in one iteration)?
- Are the acceptance criteria comprehensive enough for testing?
- Is the story independent enough to be developed without blocking dependencies?
- Does the story deliver tangible value to users or stakeholders?