# BEDROCK MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all bedrock tools
dmtools list | jq '.tools[] | select(.name | startswith("bedrock_"))'

# Example usage
dmtools bedrock_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for bedrock tools
const result = bedrock_ai_chat(...);
const result = bedrock_list_models(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `bedrock_ai_chat` | Send a text message to AWS Bedrock AI and get response | `message` (string, **required**) |
| `bedrock_list_models` | Get a list of available AWS Bedrock foundation models | None |

## Detailed Parameter Information

### `bedrock_ai_chat`

Send a text message to AWS Bedrock AI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools bedrock_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = bedrock_ai_chat("message");
```

---

### `bedrock_list_models`

Get a list of available AWS Bedrock foundation models

**Parameters:** None

**Example:**
```bash
dmtools bedrock_list_models
```

```javascript
// In JavaScript agent
const result = bedrock_list_models();
```

---

