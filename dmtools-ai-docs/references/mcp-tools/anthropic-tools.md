# ANTHROPIC MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all anthropic tools
dmtools list | jq '.tools[] | select(.name | startswith("anthropic_"))'

# Example usage
dmtools anthropic_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for anthropic tools
const result = anthropic_ai_chat(...);
const result = anthropic_ai_chat_with_files(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `anthropic_ai_chat` | Send a text message to Anthropic Claude AI and get response | `message` (string, **required**) |
| `anthropic_ai_chat_with_files` | Send a text message to Anthropic Claude AI with file attachments. Supports images, documents, and other file types for analysis and questions. | `message` (string, **required**)<br>`filePaths` (array, **required**) |

## Detailed Parameter Information

### `anthropic_ai_chat`

Send a text message to Anthropic Claude AI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools anthropic_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = anthropic_ai_chat("message");
```

---

### `anthropic_ai_chat_with_files`

Send a text message to Anthropic Claude AI with file attachments. Supports images, documents, and other file types for analysis and questions.

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to Anthropic Claude AI
  - Example: `What is in this image? Please analyze the document content.`

- **`filePaths`** (array) 🔴 Required
  - Array of file paths to attach to the message
  - Example: `['/path/to/image.png', '/path/to/document.pdf']`

**Example:**
```bash
dmtools anthropic_ai_chat_with_files "value" "value"
```

```javascript
// In JavaScript agent
const result = anthropic_ai_chat_with_files("message", "filePaths");
```

---

