# OPENAI MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all openai tools
dmtools list | jq '.tools[] | select(.name | startswith("openai_"))'

# Example usage
dmtools openai_ai_chat [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for openai tools
const result = openai_ai_chat(...);
const result = openai_ai_chat_with_files(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `openai_ai_chat` | Send a text message to OpenAI and get response | `message` (string, **required**) |
| `openai_ai_chat_with_files` | Send a text message to OpenAI with file attachments. Supports images for vision models (gpt-4-vision-preview, gpt-4-turbo, etc.). | `message` (string, **required**)<br>`filePaths` (array, **required**) |

## Detailed Parameter Information

### `openai_ai_chat`

Send a text message to OpenAI and get response

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to AI

**Example:**
```bash
dmtools openai_ai_chat "value"
```

```javascript
// In JavaScript agent
const result = openai_ai_chat("message");
```

---

### `openai_ai_chat_with_files`

Send a text message to OpenAI with file attachments. Supports images for vision models (gpt-4-vision-preview, gpt-4-turbo, etc.).

**Parameters:**

- **`message`** (string) 🔴 Required
  - Text message to send to OpenAI
  - Example: `What is in this image? Please analyze the content.`

- **`filePaths`** (array) 🔴 Required
  - Array of file paths to attach to the message (images only for vision models)
  - Example: `['/path/to/image.png', '/path/to/photo.jpg']`

**Example:**
```bash
dmtools openai_ai_chat_with_files "value" "value"
```

```javascript
// In JavaScript agent
const result = openai_ai_chat_with_files("message", "filePaths");
```

---

