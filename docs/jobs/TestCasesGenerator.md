# TestCasesGenerator

Generate test cases for user stories using AI.

## Overview

TestCasesGenerator is a QA automation job that:
- ✅ Analyzes user story requirements
- ✅ Finds related existing test cases
- ✅ Generates new test cases with priorities
- ✅ Links test cases to stories
- ✅ Supports custom fields and Xray integration

---

## Prerequisites

Before using TestCasesGenerator, you must configure:

### ✅ Required

| Component | Purpose | Setup Guide |
|-----------|---------|-------------|
| **AI Provider** | Generate test cases with AI | [AI Configuration](../configuration/ai-providers.md) |
| **Tracker** | Read stories, create test cases | [Tracker Configuration](../configuration/tracker-configuration.md) |

**At least one of each must be configured.**

### 🔧 Recommended (Optional)

| Component | Purpose | Setup Guide |
|-----------|---------|-------------|
| **Confluence** | Store rules, examples, documentation | [Confluence Configuration](../configuration/confluence-configuration.md) |

---

## Quick Setup Check

Verify your configuration before running TestCasesGenerator:

```bash
# 1. Check AI Provider
dmtools gemini_ai_chat "Test message"
# OR
dmtools anthropic_ai_chat "Test message"

# 2. Check Tracker
dmtools jira_get_ticket YOUR-123
# OR
dmtools ado_get_work_item 12345

# 3. Check Confluence (optional)
dmtools confluence_content_by_title "Test Page"
```

✅ **All commands should return successful responses.**

If any fail, see configuration guides above.

---

## Quick Start

### Basic Usage

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND status = 'Ready for QA'",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "testCasesPriorities": "High, Medium, Low",
    "outputType": "creation"
  }
}
```

### Run

```bash
./dmtools.sh run test-cases-job.json
```

---

## Parameters Reference

### Required Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `inputJql` | String | JQL query to select stories for test case generation | `"project = PROJ AND type = Story"` |
| `existingTestCasesJql` | String | JQL query to fetch existing test cases for comparison | `"project = PROJ AND issueType = 'Test'"` |

### Core Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `testCasesPriorities` | String | - | Comma-separated priorities for generated test cases |
| `testCaseIssueType` | String | `"Test Case"` | Issue type for created test cases |
| `isFindRelated` | Boolean | `true` | Enable finding related existing test cases |
| `isLinkRelated` | Boolean | `true` | Link found related test cases to story |
| `isGenerateNew` | Boolean | `true` | Generate new test cases |
| `outputType` | String | `"creation"` | Output type: `creation`, `comment`, `field`, `none` |

### JavaScript Integration Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `jqlModifierJSAction` | String | JavaScript to dynamically modify `existingTestCasesJql` based on story ticket properties | `"examples/js/jqlModifierByLabels.js"` |
| `preprocessJSAction` | String | JavaScript to preprocess test cases before creation (e.g., handle temp IDs) | `"agents/js/preprocessXrayTestCases.js"` |
| `postJSAction` | String | JavaScript to execute after test case creation | `"agents/js/assignForReview.js"` |

### Confluence Integration Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `confluencePages` | String or String[] | Confluence page URLs to include as context (testing standards, guidelines, documentation) | `["https://company.atlassian.net/wiki/spaces/QA/pages/123/Standards"]` |
| `customFieldsRules` | String | Rules for custom fields - can be Confluence URL or inline text | `"https://company.atlassian.net/wiki/spaces/QA/pages/456/Fields"` |
| `relatedTestCasesRules` | String | Rules for finding related test cases - can be Confluence URL or inline text | `"https://company.atlassian.net/wiki/spaces/QA/pages/789/Relations"` |
| `examples` | String | Example test cases from Confluence to guide generation | `"https://company.atlassian.net/wiki/spaces/QA/pages/111/Examples"` |
| `isOverridePromptExamples` | Boolean | Use Confluence examples instead of built-in examples | `true` |

**See:** [Confluence Configuration](../configuration/confluence-configuration.md) for detailed setup

### Custom Fields Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `testCasesCustomFields` | String[] | Custom field names to include in test cases |
| `testCasesRelatedFields` | String[] | Fields to fetch from existing test cases |
| `testCasesExampleFields` | String[] | Fields to include in example test cases |

### AI Model Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `modelTestCasesCreation` | String | AI model for test case generation |
| `modelTestCasesRelation` | String | AI model for finding related test cases |
| `modelTestCaseRelation` | String | AI model for verifying single relation |
| `modelTestCaseDeduplication` | String | AI model for deduplication |

### Performance Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `enableParallelTestCaseCheck` | Boolean | `false` | Enable parallel verification of related test cases |
| `parallelTestCaseCheckThreads` | Integer | `5` | Thread count for parallel checks |
| `enableParallelPostVerification` | Boolean | `false` | Enable parallel post-verification |
| `parallelPostVerificationThreads` | Integer | `3` | Thread count for post-verification |

---

## JavaScript Integration: jqlModifierJSAction

### When to Use

Use `jqlModifierJSAction` when you need to:
- Filter test cases by platform (web vs mobile)
- Adjust test case scope based on ticket priority
- Exclude test cases by custom fields
- Apply environment-specific filters

### Execution Flow

```
Story ticket loaded → jqlModifierJSAction executes → Modified JQL used to fetch test cases
```

### Input Parameters (JavaScript)

The JavaScript function receives:

```javascript
{
  ticket: {
    key: "PROJ-123",
    labels: ["web", "high-priority"],
    priority: "High",
    customFields: { ... }
  },
  jobParams: {
    existingTestCasesJql: "project = PROJ AND issueType = 'Test'",
    testCaseIssueType: "Test",
    inputJql: "...",
    targetProject: "PROJ"
  },
  existingTestCasesJql: "project = PROJ AND issueType = 'Test'"
}
```

### Return Format

Return modified JQL in one of two formats:

```javascript
// Option 1: JSONObject with field
return { existingTestCasesJql: "modified JQL" };

// Option 2: Direct string
return "modified JQL";
```

### Example: Filter by Platform Labels

**File**: `examples/js/jqlModifierByLabels.js`

```javascript
function action(params) {
    const ticket = params.ticket;
    let jql = params.existingTestCasesJql;

    // Exclude mobile tests for web stories
    if (ticket.labels && ticket.labels.includes("web")) {
        jql += " and labels not in (mobile)";
    }

    // Exclude web tests for mobile stories
    if (ticket.labels && ticket.labels.includes("mobile")) {
        jql += " and labels not in (web)";
    }

    return { existingTestCasesJql: jql };
}
```

**Job Config**:

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "jqlModifierJSAction": "examples/js/jqlModifierByLabels.js"
  }
}
```

**See**: `examples/js/jql-modifier-examples-README.md` for more examples and detailed guide.

### Error Handling

**Fail-Safe Behavior**: If JavaScript fails or returns invalid data, the original `existingTestCasesJql` is used. The job never fails due to JQL modifier errors.

---

## Complete Examples

### Example 1: Basic Test Case Generation

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = ECOM AND type = Story AND status = 'Ready for QA'",
    "existingTestCasesJql": "project = ECOM AND issueType = 'Test' AND status != Archived",
    "testCasesPriorities": "High, Medium, Low",
    "testCaseIssueType": "Test",
    "outputType": "creation",
    "initiator": "qa.lead@company.com"
  }
}
```

### Example 2: With JQL Modifier for Platform Filtering

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key in (ECOM-123, ECOM-456)",
    "existingTestCasesJql": "project = ECOM AND issueType = 'Test'",
    "testCasesPriorities": "High, Medium, Low",
    "jqlModifierJSAction": "examples/js/jqlModifierByLabels.js",
    "outputType": "creation"
  }
}
```

**Config File**: See `examples/js/job-configs/test_cases_with_jql_modifier.json`

### Example 3: With Xray Preconditions (Preprocessing)

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Story",
    "existingTestCasesJql": "project = PROJ AND issueType in ('Test', 'Precondition')",
    "testCasesPriorities": "High, Medium, Low",
    "testCasesCustomFields": ["preconditions", "testSteps"],
    "preprocessJSAction": "agents/js/preprocessXrayTestCases.js",
    "outputType": "creation"
  }
}
```

### Example 4: With Confluence Rules and Documentation

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Story",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "testCasesPriorities": "High, Medium, Low",
    "confluencePages": [
      "https://company.atlassian.net/wiki/spaces/QA/pages/123/Testing+Standards",
      "https://company.atlassian.net/wiki/spaces/API/pages/456/API+Documentation"
    ],
    "customFieldsRules": "https://company.atlassian.net/wiki/spaces/QA/pages/789/Custom+Fields",
    "examples": "https://company.atlassian.net/wiki/spaces/QA/pages/111/Test+Case+Examples",
    "isOverridePromptExamples": true,
    "outputType": "creation"
  }
}
```

**See:** [Confluence Configuration](../configuration/confluence-configuration.md)

### Example 5: Comment-Only Mode (Dry Run)

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "testCasesPriorities": "High, Medium, Low",
    "outputType": "comment"
  }
}
```

---

## Best Practices

### 1. Start with Comment Mode

Test configuration with `outputType: "comment"` before creating tickets:

```json
{
  "outputType": "comment"
}
```

### 2. Use JQL Filters to Reduce AI Context

Narrow down existing test cases with precise JQL:

```json
{
  "existingTestCasesJql": "project = PROJ AND issueType = 'Test' AND labels in (regression, smoke) AND created >= -90d"
}
```

### 3. Leverage JQL Modifiers for Dynamic Filtering

Use `jqlModifierJSAction` to apply context-aware filters based on ticket properties.

### 4. Organize JS Actions

Keep JavaScript files organized:
```
examples/
  js/
    jqlModifierByLabels.js
    jqlModifierSimple.js
    job-configs/
      test_cases_with_jql_modifier.json
agents/
  js/
    preprocessXrayTestCases.js
    assignForReview.js
```

---

## Troubleshooting

### Issue: Too Many Test Cases Generated

**Solution**: Refine `existingTestCasesJql` or use `jqlModifierJSAction` to filter more aggressively.

### Issue: Test Cases Not Linked

**Solution**: Check `isLinkRelated` is `true` and test case relationship is correct.

### Issue: Custom Fields Not Populated

**Solution**: Ensure `testCasesCustomFields` includes the field names and `customFieldsRules` provides context.

### Issue: JQL Modifier Not Working

**Solution**: Check JavaScript syntax, ensure it returns valid JQL. Enable debug logging to see modifier output.

---

## See Also

### Configuration Guides

- **[AI Providers Configuration](../configuration/ai-providers.md)** - Setup Gemini, OpenAI, Claude, etc.
- **[Tracker Configuration](../configuration/tracker-configuration.md)** - Setup Jira, ADO, Rally
- **[Confluence Configuration](../configuration/confluence-configuration.md)** - Store rules and documentation
- **[Complete Configuration Guide](../getting-started/configuration.md)** - All configuration options

### Integration Guides

- **[MCP Tools Reference](../README-MCP.md)** - Available MCP tools for JavaScript actions
- **[JavaScript Actions Guide](../ai-teammate/javascript-actions.md)** - JS integration patterns
- **[JQL Modifier Examples](../../examples/js/jql-modifier-examples-README.md)** - Dynamic JQL modification examples
