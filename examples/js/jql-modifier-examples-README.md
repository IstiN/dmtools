# JQL Modifier Examples for TestCasesGenerator

This directory contains example JavaScript files that demonstrate how to use the `jqlModifierJSAction` parameter in TestCasesGenerator to dynamically filter existing test cases based on story ticket properties.

## Overview

The JQL modifier feature allows you to:
- Filter test cases by platform (web, mobile, API)
- Adjust test case scope based on ticket priority or labels
- Exclude irrelevant test cases before AI processing
- Reduce AI context size for better performance

## Examples

### 1. jqlModifierByLabels.js

**Purpose**: Filter test cases based on ticket labels (platform-based filtering)

**Use Case**:
- If story has "web" label → exclude test cases with "mobile" label
- If story has "mobile" label → exclude test cases with "web" label
- If story has "api" label → include only test cases with "api" label

**Example**:
```javascript
function action(params) {
    const ticket = params.ticket;
    let jql = params.existingTestCasesJql;

    if (ticket.labels && ticket.labels.includes("web")) {
        jql += " and labels not in (mobile)";
    }

    if (ticket.labels && ticket.labels.includes("mobile")) {
        jql += " and labels not in (web)";
    }

    return { existingTestCasesJql: jql };
}
```

**Usage**:
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "jqlModifierJSAction": "examples/js/jqlModifierByLabels.js",
    "outputType": "creation"
  }
}
```

### 2. jqlModifierSimple.js

**Purpose**: Simple test case to verify the JQL modifier mechanism works

**Use Case**:
- Appends a label filter to verify the feature is functioning
- Useful for testing and learning

**Example**:
```javascript
function action(params) {
    const jql = params.existingTestCasesJql || "";
    const modifiedJql = jql + " and labels = ai_generated";

    console.log("Original JQL:", jql);
    console.log("Modified JQL:", modifiedJql);

    return { existingTestCasesJql: modifiedJql };
}
```

**Usage**:
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "jqlModifierJSAction": "examples/js/jqlModifierSimple.js"
  }
}
```

## Job Configuration Examples

See `job-configs/test_cases_with_jql_modifier.json` for a complete working example.

## Input Parameters

Your JavaScript function receives these parameters:

```javascript
{
  ticket: {
    key: "PROJ-123",
    labels: ["web", "high-priority"],
    priority: "High",
    status: "In Progress",
    title: "User login feature",
    description: "..."
  },
  jobParams: {
    existingTestCasesJql: "project = PROJ AND issueType = 'Test'",
    testCaseIssueType: "Test",
    inputJql: "key = PROJ-123",
    targetProject: "PROJ"
  },
  existingTestCasesJql: "project = PROJ AND issueType = 'Test'"
}
```

## Return Format

Your function can return the modified JQL in two ways:

### Option 1: Object with field (recommended)
```javascript
return { existingTestCasesJql: "modified JQL here" };
```

### Option 2: Direct string
```javascript
return "modified JQL here";
```

## Error Handling

**Fail-Safe Behavior**: If your JavaScript throws an error or returns invalid data, TestCasesGenerator will automatically fall back to the original `existingTestCasesJql`. The job will never fail due to JQL modifier errors.

## Best Practices

1. **Always log modifications**: Use `console.log()` to track JQL changes for debugging
2. **Test incrementally**: Start with simple modifications before adding complex logic
3. **Use comment mode first**: Test with `"outputType": "comment"` before creating tickets
4. **Validate input**: Check if `ticket.labels` exists before accessing it
5. **Keep it simple**: JQL modifiers should focus on filtering, not complex logic

## Custom Examples

### Filter by Priority
```javascript
function action(params) {
    const ticket = params.ticket;
    let jql = params.existingTestCasesJql;

    if (ticket.priority === "High" || ticket.priority === "Highest") {
        // Only include high-priority test cases
        jql += " and priority in (High, Highest)";
    }

    return { existingTestCasesJql: jql };
}
```

### Filter by Custom Field
```javascript
function action(params) {
    const ticket = params.ticket;
    let jql = params.existingTestCasesJql;

    // Assuming ticket has custom field "Environment"
    const environment = ticket.customFields?.environment;
    if (environment) {
        jql += ` and environment = "${environment}"`;
    }

    return { existingTestCasesJql: jql };
}
```

### Filter by Recent Test Cases Only
```javascript
function action(params) {
    let jql = params.existingTestCasesJql;

    // Only include test cases created in last 90 days
    jql += " and created >= -90d";

    return { existingTestCasesJql: jql };
}
```

## See Also

- [TestCasesGenerator Documentation](../../docs/jobs/TestCasesGenerator.md)
- [MCP Tools Reference](../../docs/README-MCP.md)
- [JavaScript Actions Guide](../agents/js/README.md)
