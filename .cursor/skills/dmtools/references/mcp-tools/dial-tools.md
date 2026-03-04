# DIAL MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all dial tools
dmtools list | jq '.tools[] | select(.name | startswith("dial_"))'

# Example usage
dmtools dial_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for dial tools
const result = dial_ai_chat(...);
const result = dial_ai_chat_with_files(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `dial_ai_chat` | Send a text message to Dial AI and get response | `message` (string, **required**) |
| `dial_ai_chat_with_files` | Send a text message to Dial AI with file attachments. Supports images and documents for analysis and questions. | `message` (string, **required**)<br>`filePaths` (string, **required**) |

## Detailed Parameter Information

### `dial_ai_chat`

Send a text message to Dial AI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools dial_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = dial_ai_chat("message");
```

---

### `dial_ai_chat_with_files`

Send a text message to Dial AI with file attachments. Supports images and documents for analysis and questions.

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to Dial AI
  - Example: `What is in this image? Please analyze the document content.`

- **`filePaths`** (string) 🔴 Required
  - Comma-separated list of file paths to attach (supports images and documents)
  - Example: `/path/to/image.png,/path/to/document.pdf`

**Example:**
```bash
dmtools dial_ai_chat_with_files "value" "value"
```

```javascript
// In JavaScript agent
const result = dial_ai_chat_with_files("message", "filePaths");
```

---

