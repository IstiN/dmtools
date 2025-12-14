# JavaScript MCP Examples for DMTools

This directory contains comprehensive examples of all available MCP (Model Context Protocol) tools that can be used in DMTools JavaScript post-actions.

## Overview

DMTools provides 61+ MCP tools accessible from JavaScript contexts, enabling powerful automation and integration capabilities. These examples demonstrate how to use each tool effectively.

## Available Examples

### 🎫 [jira-mcp-examples.js](./jira-mcp-examples.js)
**40+ Jira MCP Tools**

Comprehensive examples covering all Jira operations:
- **Ticket Operations**: Get, create, update, search tickets
- **Comments**: Post, retrieve, conditional posting
- **Fields**: Update fields, labels, priorities, fix versions
- **Transitions**: Get available transitions, move status
- **Linking**: Link issues, get link types
- **Search**: JQL search, pagination, subtasks
- **Project Info**: Components, versions, statuses, fields
- **User Operations**: Profiles, assignments
- **Advanced**: Custom requests, field operations

**Note**: Excludes `jira_delete_ticket` for safety

### 🏢 [confluence-mcp-examples.js](./confluence-mcp-examples.js)
**16 Confluence MCP Tools**

Complete Confluence integration examples:
- **Content Retrieval**: By ID, title, URL, search
- **Content Management**: Create, update, find-or-create
- **Navigation**: Get children, attachments
- **User Operations**: Current user, user profiles
- **Search**: Text search, content discovery
- **History**: Update with history comments

### 🤖 [ai-mcp-examples.js](./ai-mcp-examples.js)
**2 AI MCP Tools**

AI integration examples for both providers:
- **Gemini AI**: `gemini_ai_chat` - Google Gemini integration
- **Dial AI**: `dial_ai_chat` - Dial AI integration

**Use Cases**:
- Ticket analysis and insights
- Code review assistance
- Test case generation
- Documentation creation
- Risk assessment
- Architecture recommendations
- Workflow automation

### 🎨 [figma-mcp-examples.js](./figma-mcp-examples.js)
**6 Figma MCP Tools**

Design system integration examples:
- **File Analysis**: Get structure, extract elements
- **Asset Export**: PNG, SVG, JPG downloads
- **Content Extraction**: SVG content, screen capture
- **Design Tokens**: Color, typography, spacing extraction
- **Workflow Integration**: Asset preparation for development

## Usage Instructions

### 1. Basic Usage
Copy relevant examples to your job's `postJSAction` parameter:

```javascript
function action(params) {
    // Your custom logic here
    const result = jira_update_field({
        key: params.ticket.key,
        field: "labels",
        value: ["automated", "processed"]
    });
    
    return {
        success: true,
        message: "Label added successfully",
        result: result
    };
}
```

### 2. Parameter Access
All examples use the standard parameter structure:

```javascript
function action(params) {
    // Available parameters:
    console.log("Ticket:", params.ticket.key);
    console.log("Job params:", params.jobParams);
    console.log("Response:", params.response);
    console.log("Initiator:", params.initiator);
    
    // Ticket object properties:
    // - key: Ticket key (e.g., "PROJ-123")
    // - title: Ticket title/summary
    // - description: Ticket description
    // - status: Current status
    // - labels: Array of labels
    // - fields: Raw Jira fields JSON
    // - And more...
}
```

### 3. Error Handling
Always include proper error handling:

```javascript
function action(params) {
    try {
        const result = jira_get_ticket({
            ticketKey: params.ticket.key
        });
        
        return {
            success: true,
            result: result
        };
    } catch (error) {
        console.error("Operation failed:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
```

## Integration Points

### Expert Job Integration
```javascript
// In Expert job postJSAction parameter
js(expertParams.getPostJSAction())
    .mcp(trackerClient, confluence, sourceCode)
    .withJobContext(expertParams, ticket, response)
    .with("initiator", initiator)
    .with("systemRequest", systemRequestCommentAlias)
    .with("request", request)
    .execute();
```

### Teammate Job Integration
```javascript
// In Teammate job postJSAction parameter
js(expertParams.getPostJSAction())
    .mcp(trackerClient, confluence, sourceCode)
    .withJobContext(expertParams, ticket, response)
    .with("initiator", initiator)
    .execute();
```

## Configuration Requirements

### Jira Configuration
- Jira base URL and authentication
- Proper project permissions
- Field access permissions

### Confluence Configuration
- Confluence base URL and authentication
- Space access permissions
- Content creation permissions

### AI Configuration
- **Gemini AI**: API key and model configuration
- **Dial AI**: Service endpoint and authentication
- **AWS Bedrock**: Region, model ID, bearer token, and optional parameters

#### AWS Bedrock Model Configuration

Bedrock supports multiple model families with different capabilities:

**1. Amazon Nova Models** (supports images via `/invoke` endpoint)
- **Inference Profile ARN Format**: `arn:aws:bedrock:region:account:inference-profile/eu.amazon.nova-lite-v1:0`
- **Direct Model ID**: `eu.amazon.nova-lite-v1:0`
- **Supports**: Text and images (PNG, JPEG, GIF, WebP)
- **Configuration**:
  ```env
  BEDROCK_REGION=eu-north-1
  BEDROCK_MODEL_ID=arn:aws:bedrock:eu-north-1:713881790201:inference-profile/eu.amazon.nova-lite-v1:0
  BEDROCK_BEARER_TOKEN=your-token
  # Or use direct model ID:
  # BEDROCK_MODEL_ID=eu.amazon.nova-lite-v1:0
  ```

**2. Qwen Models** (text only, no images)
- **Models**: `qwen.qwen3-coder-480b-a35b-v1:0`, `qwen.qwen-max-2401:0`
- **Supports**: Text only
- **Configuration**:
  ```env
  BEDROCK_MODEL_ID=qwen.qwen3-coder-480b-a35b-v1:0
  BEDROCK_BEARER_TOKEN=your-token
  ```

**3. Claude Models** (supports images)
- **Models**: `anthropic.claude-3-5-sonnet-20241022-v2:0`, `anthropic.claude-3-opus-20240229-v1:0`
- **Supports**: Text and images
- **Configuration**:
  ```env
  BEDROCK_MODEL_ID=anthropic.claude-3-5-sonnet-20241022-v2:0
  BEDROCK_BEARER_TOKEN=your-token
  ```

**4. Mistral Models** (some support images)
- **Models**: `mistral.mistral-large-2407-v1:0`, `mistral.pixtral-large-2502-v1:0` (supports images)
- **Supports**: Text (and images for pixtral models)
- **Configuration**:
  ```env
  BEDROCK_MODEL_ID=mistral.pixtral-large-2502-v1:0
  BEDROCK_BEARER_TOKEN=your-token
  ```

**Optional Parameters**:
- `BEDROCK_MAX_TOKENS`: Maximum tokens in response (default: 4096)
- `BEDROCK_TEMPERATURE`: Temperature for response randomness (default: 1.0)
- `BEDROCK_BASE_PATH`: Custom base path (auto-constructed from region if not provided)
- `AWS_BEARER_TOKEN_BEDROCK`: Alternative token name (checked first, then falls back to `BEDROCK_BEARER_TOKEN`)

**Note**: For inference profile ARN, the system automatically extracts the model ID for the `/invoke` endpoint path while using the full ARN for configuration.

### Figma Configuration
- Figma API token
- File access permissions
- Public or accessible file URLs

## Best Practices

### 1. Resource Management
- Use try-catch blocks for all MCP calls
- Handle null/undefined responses gracefully
- Limit batch operations to avoid timeouts

### 2. Performance Optimization
- Cache results when possible
- Use pagination for large datasets
- Implement timeouts for long operations

### 3. Security Considerations
- Validate input parameters
- Sanitize user-provided content
- Use appropriate permissions for operations

### 4. Debugging
- Use `console.log()` for debugging
- Check browser/server logs for errors
- Validate MCP tool responses

## Common Patterns

### 1. Conditional Operations
```javascript
// Only update if condition is met
if (params.ticket.status === "In Progress") {
    const result = jira_update_field({
        key: params.ticket.key,
        field: "labels",
        value: ["in-progress-processed"]
    });
}
```

### 2. Batch Operations
```javascript
// Process multiple operations
const operations = [
    () => jira_post_comment({key: params.ticket.key, comment: "Processing started"}),
    () => jira_update_field({key: params.ticket.key, field: "labels", value: ["processed"]}),
    () => jira_post_comment({key: params.ticket.key, comment: "Processing completed"})
];

const results = operations.map(op => {
    try {
        return op();
    } catch (error) {
        return {error: error.toString()};
    }
});
```

### 3. Cross-Platform Integration
```javascript
// Combine Jira and Confluence operations
const ticketInfo = jira_get_ticket({key: params.ticket.key});
const confluencePage = confluence_create_page({
    title: `Documentation for ${params.ticket.key}`,
    body: `<h1>${ticketInfo.summary}</h1><p>${ticketInfo.description}</p>`,
    parentId: "123456",
    space: "DOCS"
});
```

## Troubleshooting

### Common Issues
1. **"Required parameter missing"**: Check parameter names and types
2. **"Authentication failed"**: Verify API credentials
3. **"Permission denied"**: Check user permissions
4. **"Network timeout"**: Implement retry logic or reduce batch sizes

### Debug Steps
1. Check console logs for detailed error messages
2. Verify MCP tool parameter formats
3. Test with minimal examples first
4. Validate authentication and permissions

## Support

For additional help:
1. Check the main DMTools documentation
2. Review the MCP tool definitions in the generated code
3. Test with the provided examples
4. Check server logs for detailed error information

## Contributing

When adding new examples:
1. Follow the existing pattern and structure
2. Include comprehensive error handling
3. Add clear comments and documentation
4. Test with real data before committing
5. Update this README with new capabilities

