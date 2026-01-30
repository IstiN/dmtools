# DOR Template Generation Improvements

## Summary of Issues with GPT-5 Output
- Missing platform-specific formatting rules (Jira markdown, Confluence wiki markup)
- Too generic, lacking actionable formatting specifications
- No clear DOR checklist structure
- Missing character limits and platform constraints

## Implemented Improvements

### 1. Enhanced Prompt Template
**File**: `dmtools-core/src/main/resources/ftl/prompts/agents/instruction_generator.xml`

Key enhancements:
- Explicit requirements for formatting rules
- DOR template structure requirements
- Platform-specific formatting specifications
- Comprehensive checklist generation

### 2. Platform Parameter Support
**Files Modified**:
- `InstructionsGeneratorParams.java` - Added `platform` parameter
- `InstructionGeneratorAgent.java` - Added platform to agent params
- `InstructionsGenerator.java` - Pass platform to agent

Supported platforms:
- `jira` - Jira markdown formatting
- `ado` - Azure DevOps wiki syntax
- `confluence` - Confluence wiki markup
- `github` - GitHub Flavored Markdown
- `gitlab` - GitLab Flavored Markdown

### 3. Enhanced DOR Structure

The improved template now generates:

#### Platform Formatting Guidelines
- Specific markdown/markup syntax for the platform
- Code block formatting ({code}, ```, {noformat})
- List formatting (*, -, [ ], 1.)
- Heading hierarchies (h1, h2, #, ##)
- Special syntax ({{fields}}, @mentions, [~username])

#### Comprehensive Field Guidelines
For each field:
- **Purpose**: Clear business value statement
- **Format**: Structure, limits, platform syntax
- **Required Elements**: Numbered must-haves
- **Best Practices**: Platform-specific tips
- **Formatting Examples**: Actual syntax examples
- **Common Mistakes**: Anti-patterns to avoid

#### DOR Checklist
- [ ] Field-specific validation items
- [ ] Cross-field consistency checks
- [ ] Platform compliance verification
- [ ] Quality assurance points

## Usage Example

```json
{
  "name": "InstructionsGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Story",
    "fields": ["summary", "description", "User Story"],
    "instructionType": "user_story",
    "platform": "jira",
    "outputPath": "output/dor-template.md",
    "additionalContext": "Focus on DOR for all fields. Include ALL Jira formatting rules. Generic examples only."
  }
}
```

## Key Improvements Over GPT-5 Output

### Before (GPT-5)
```markdown
### User Story
- Purpose: Standard user-centered statement...
- Format: Three-line canonical template...
- Examples: "As a Marketing Manager..."
```

### After (Enhanced)
```markdown
### User Story
- **Purpose**: Standard user-centered statement...
- **Format**:
  - Structure: Three-line template or single sentence
  - Jira Syntax: Use {noformat} or {code} blocks for formatting
  - Character limit: 500 characters recommended
  - Template:
    ```
    As a [persona]
    I want to [capability]
    So that [benefit]
    ```
- **Required Elements**:
  1. Persona (who) - use @mentions for actual users
  2. Capability (what) - reference {{epic}} if applicable
  3. Benefit (why) - link to business KPIs
- **Formatting Examples**:
  ```jira
  {noformat}
  As a Customer Service Agent
  I want to refresh order status inline
  So that response time improves by 30%
  {noformat}
  ```
- **Common Mistakes**:
  - Using h3 instead of h2 for User Story heading
  - Missing {noformat} tags around story text
  - Exceeding 500 character limit
```

## Additional Recommendations

### 1. Add Field Validation Rules
Include regex patterns or validation rules for each field:
```javascript
"validationRules": {
  "summary": "^[A-Z].{10,80}$",
  "userStory": "As a .+ I want .+ [Ss]o that .+"
}
```

### 2. Include Workflow Integration
Add guidance on how fields affect workflow transitions:
- Which fields block "Ready" status
- Required fields for specific transitions
- Automation trigger requirements

### 3. Add Team-Specific Templates
Support team-specific DOR variations:
```json
"teamTemplate": "backend|frontend|qa|devops"
```

### 4. Generate API/CLI Examples
Include examples of creating tickets via API/CLI with proper formatting:
```bash
jira create --type Story \
  --summary "Payment Gateway: Add Stripe Support" \
  --description $'h2. Business Context\n...'
```

### 5. Add Metrics and Quality Scores
Generate quality scoring rubric:
- Field completeness score
- Formatting compliance score
- Cross-reference integrity score
- Overall DOR readiness percentage

## Testing the Enhanced System

Run with comprehensive DOR focus:
```bash
./dmtools.sh run agents/dor_template_generator.json
```

Expected output structure:
1. Platform-specific formatting guide
2. Field-by-field DOR requirements
3. Actionable checklists
4. Generic, abstracted examples
5. Integration guidelines
6. Quality verification criteria

## Conclusion

The enhanced system addresses GPT-5's limitations by:
- Explicitly requesting formatting specifications
- Adding platform awareness
- Structuring output as actionable DOR checklists
- Including platform-specific syntax examples
- Providing comprehensive validation criteria

This creates truly actionable DOR templates that teams can immediately use for ticket creation and validation.