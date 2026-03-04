# KB MCP Tools

**Total Tools**: 5

## Quick Reference

```bash
# List all kb tools
dmtools list | jq '.tools[] | select(.name | startswith("kb_"))'

# Example usage
dmtools kb_get [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for kb tools
const result = kb_get(...);
const result = kb_build(...);
const result = kb_process(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `kb_aggregate` | Generate AI descriptions for existing KB structure WITHOUT processing new data. Use this after kb_process to add AI-generated descriptions for people and topics. | `smart_mode` (string, optional)<br>`output_path` (string, optional)<br>`source_name` (string, optional) |
| `kb_build` | Build or update knowledge base from input file. Processes chat messages, documentation, or any text data to create indexed, searchable knowledge base. Returns JSON with statistics. | `input_file` (string, **required**)<br>`output_path` (string, optional)<br>`date_time` (string, **required**)<br>`source_name` (string, **required**)<br>`clean_source` (string, optional) |
| `kb_get` | Get last sync date for a knowledge base source. Returns ISO 8601 date string or 'Source not found' message. | `output_path` (string, optional)<br>`source_name` (string, **required**) |
| `kb_process` | Process input file and build KB structure WITHOUT generating AI descriptions (fast mode). Use this for bulk data processing. Run kb_aggregate later to generate descriptions. | `input_file` (string, **required**)<br>`output_path` (string, optional)<br>`date_time` (string, **required**)<br>`source_name` (string, **required**)<br>`clean_source` (string, optional) |
| `kb_process_inbox` | Scan inbox/raw/ folders and process all unprocessed files automatically. Files are processed in place. Returns JSON with processed and skipped file details. | `generate_descriptions` (string, optional)<br>`output_path` (string, optional)<br>`smart_aggregation` (string, optional) |

## Detailed Parameter Information

### `kb_aggregate`

Generate AI descriptions for existing KB structure WITHOUT processing new data. Use this after kb_process to add AI-generated descriptions for people and topics.

**Parameters:**

- **`smart_mode`** (string) ⚪ Optional
  - Only regenerate descriptions if Q/A/N files have changed. Default: true
  - Example: `true`

- **`output_path`** (string) ⚪ Optional
  - Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory
  - Example: `/path/to/knowledge-base`

- **`source_name`** (string) ⚪ Optional
  - Optional: Filter to only regenerate for specific source. If null/empty, regenerates for ALL sources (recommended for description regeneration).
  - Example: `teams_chat`

**Example:**
```bash
dmtools kb_aggregate "value" "value"
```

```javascript
// In JavaScript agent
const result = kb_aggregate("smart_mode", "output_path");
```

---

### `kb_build`

Build or update knowledge base from input file. Processes chat messages, documentation, or any text data to create indexed, searchable knowledge base. Returns JSON with statistics.

**Parameters:**

- **`input_file`** (string) 🔴 Required
  - Path to input file (JSON or text format). Can be JSON array of messages or plain text.
  - Example: `/path/to/messages.json`

- **`output_path`** (string) ⚪ Optional
  - Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory
  - Example: `/path/to/knowledge-base`

- **`date_time`** (string) 🔴 Required
  - Sync date/time in ISO 8601 format (e.g., '2024-10-10T12:00:00Z')
  - Example: `2024-10-10T12:00:00Z`

- **`source_name`** (string) 🔴 Required
  - Name of the data source (e.g., 'teams_chat', 'slack_general')
  - Example: `teams_chat`

- **`clean_source`** (string) ⚪ Optional
  - Optional. If true, removes all existing Q/A/N from this source before processing. Use for content refresh (e.g., Confluence pages).
  - Example: `true`

**Example:**
```bash
dmtools kb_build "value" "value"
```

```javascript
// In JavaScript agent
const result = kb_build("input_file", "output_path");
```

---

### `kb_get`

Get last sync date for a knowledge base source. Returns ISO 8601 date string or 'Source not found' message.

**Parameters:**

- **`output_path`** (string) ⚪ Optional
  - Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory
  - Example: `/path/to/knowledge-base`

- **`source_name`** (string) 🔴 Required
  - Name of the data source (e.g., 'teams_chat', 'slack_general')
  - Example: `teams_chat`

**Example:**
```bash
dmtools kb_get "value" "value"
```

```javascript
// In JavaScript agent
const result = kb_get("output_path", "source_name");
```

---

### `kb_process`

Process input file and build KB structure WITHOUT generating AI descriptions (fast mode). Use this for bulk data processing. Run kb_aggregate later to generate descriptions.

**Parameters:**

- **`input_file`** (string) 🔴 Required
  - Path to input file (JSON or text format). Can be JSON array of messages or plain text.
  - Example: `/path/to/messages.json`

- **`output_path`** (string) ⚪ Optional
  - Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory
  - Example: `/path/to/knowledge-base`

- **`date_time`** (string) 🔴 Required
  - Sync date/time in ISO 8601 format (e.g., '2024-10-10T12:00:00Z')
  - Example: `2024-10-10T12:00:00Z`

- **`source_name`** (string) 🔴 Required
  - Name of the data source (e.g., 'teams_chat', 'slack_general')
  - Example: `teams_chat`

- **`clean_source`** (string) ⚪ Optional
  - Optional. If true, removes all existing Q/A/N from this source before processing. Use for content refresh (e.g., Confluence pages).
  - Example: `true`

**Example:**
```bash
dmtools kb_process "value" "value"
```

```javascript
// In JavaScript agent
const result = kb_process("input_file", "output_path");
```

---

### `kb_process_inbox`

Scan inbox/raw/ folders and process all unprocessed files automatically. Files are processed in place. Returns JSON with processed and skipped file details.

**Parameters:**

- **`generate_descriptions`** (string) ⚪ Optional
  - Generate AI descriptions after processing. Default: true
  - Example: `true`

- **`output_path`** (string) ⚪ Optional
  - Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory
  - Example: `/path/to/knowledge-base`

- **`smart_aggregation`** (string) ⚪ Optional
  - Only regenerate descriptions if Q/A/N changed. Default: true
  - Example: `true`

**Example:**
```bash
dmtools kb_process_inbox "value" "value"
```

```javascript
// In JavaScript agent
const result = kb_process_inbox("generate_descriptions", "output_path");
```

---

