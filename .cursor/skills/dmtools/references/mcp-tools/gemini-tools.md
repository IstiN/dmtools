# GEMINI MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all gemini tools
dmtools list | jq '.tools[] | select(.name | startswith("gemini_"))'

# Example usage
dmtools gemini_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for gemini tools
const result = gemini_ai_chat(...);
const result = gemini_ai_chat_with_files(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `gemini_ai_chat` | Send a text message to Gemini AI and get response | `message` (string, **required**) |
| `gemini_ai_chat_with_files` | Send a text message to Gemini AI with file attachments. Supports images, documents, and other file types for analysis and questions. | `message` (string, **required**)<br>`filePaths` (array, **required**) |

## Detailed Parameter Information

### `gemini_ai_chat`

Send a text message to Gemini AI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools gemini_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = gemini_ai_chat("message");
```

---

### `gemini_ai_chat_with_files`

Send a text message to Gemini AI with file attachments. Supports images, documents, and other file types for analysis and questions.

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to Gemini AI
  - Example: `What is in this image? Please analyze the document content.`

- **`filePaths`** (array) 🔴 Required
  - Array of file paths to attach to the message
  - Example: `['/path/to/image.png', '/path/to/document.pdf']`

**Example:**
```bash
dmtools gemini_ai_chat_with_files "value" "value"
```

```javascript
// In JavaScript agent
const result = gemini_ai_chat_with_files("message", "filePaths");
```

---

