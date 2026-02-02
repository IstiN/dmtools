# OLLAMA MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all ollama tools
dmtools list | jq '.tools[] | select(.name | startswith("ollama_"))'

# Example usage
dmtools ollama_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for ollama tools
const result = ollama_ai_chat(...);
const result = ollama_ai_chat_with_files(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `ollama_ai_chat` | Send a text message to Ollama AI and get response | `message` (string, **required**) |
| `ollama_ai_chat_with_files` | Send a text message to Ollama AI with file attachments. Supports images and other file types for analysis and questions. | `message` (string, **required**)<br>`filePaths` (array, **required**) |

## Detailed Parameter Information

### `ollama_ai_chat`

Send a text message to Ollama AI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools ollama_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = ollama_ai_chat("message");
```

---

### `ollama_ai_chat_with_files`

Send a text message to Ollama AI with file attachments. Supports images and other file types for analysis and questions.

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to Ollama AI
  - Example: `What is in this image? Please analyze the document content.`

- **`filePaths`** (array) 🔴 Required
  - Array of file paths to attach to the message
  - Example: `['/path/to/image.png', '/path/to/document.pdf']`

**Example:**
```bash
dmtools ollama_ai_chat_with_files "value" "value"
```

```javascript
// In JavaScript agent
const result = ollama_ai_chat_with_files("message", "filePaths");
```

---

