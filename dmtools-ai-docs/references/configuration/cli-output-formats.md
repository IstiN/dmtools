# CLI Output Formats

DMtools supports three output formats when executing MCP tools from the command line.
The format controls how tool results are serialised and printed to stdout.

---

## 📊 Format Comparison

Measured on a real Jira ticket response:

| Format | Tokens | Characters | Best for                            |
|--------|-------:|-----------:|-------------------------------------|
| `json` | 11,041 | 36,555 | Machine parsing, piping to `jq`     |
| `toon` | 10,078 | 32,456 | Passing results to LLMs (TOON spec) |
| `mini` | 3,307 | 11,597 | Maximum token savings with LLMs ⚠️ some data omitted |

---

## 🎛️ Available Formats

### `json` (default)
Pretty-printed JSON. Identical to previous behaviour. Safe for piping to `jq` or any JSON parser.

```
{
  "key": "PROJ-123",
  "fields": {
    "summary": "Fix login bug",
    "status": {
      "name": "In Progress"
    }
  }
}
```

### `toon`
[TOON format](https://github.com/toon-format/toon-java) — compact, YAML-like serialisation.
Reduces tokens by ~9% vs JSON while staying human-readable and fully spec-compliant.

```
key: PROJ-123
fields:
  summary: Fix login bug
  status:
    name: In Progress
```

### `mini`
DMTools-specific LLM-optimised format powered by [`LLMOptimizedJson`](../../../../../../dmtools-core/src/main/java/com/github/istin/dmtools/common/utils/LLMOptimizedJson.java).

`mini` is DMTools' own implementation that strips JSON down to only the data that is **relevant for LLM reasoning** — summaries, statuses, descriptions, priorities, key identifiers — while discarding noise like avatar URLs, icon links, self-references, expand markers, and other metadata that adds tokens without adding meaning.

> ⚠️ **Data loss is intentional and expected.** Fields such as `avatarUrls`, `iconUrl`, `self`, `expand`, `renderedFields`, and other purely presentational or navigational links are removed. Do not use `mini` when you need the complete raw API response.

If the result object implements `ToText`, its `toText()` output is used directly instead of the JSON conversion path.

Uses `LLMOptimizedJson` `Next key1,key2,…` tabular headers. Reduces tokens by ~70% vs JSON.

```
Next key,fields
PROJ-123
Next summary,status
Fix login bug
Next name
In Progress
```

---

## 🚀 Usage

### Per-invocation flags

```bash
# Use TOON format for a single call
dmtools jira_get_ticket PROJ-123 --toon

# Use mini format
dmtools jira_get_ticket PROJ-123 --mini

# Generic flag (json | toon | mini)
dmtools jira_get_ticket PROJ-123 --output toon
dmtools jira_get_ticket PROJ-123 --output=mini

# Explicit JSON (same as default)
dmtools jira_get_ticket PROJ-123 --output json
```

### Default via `dmtools.env`

Set a permanent default so every tool call uses your preferred format:

```bash
# dmtools.env
CLI_OUTPUT=mini
```

Override the default on individual calls with any of the flags above:

```bash
# Global default is mini, but this call uses json
dmtools jira_get_ticket PROJ-123 --output json
```

### Default via environment variable

```bash
export CLI_OUTPUT=toon
dmtools jira_search_by_jql "project = PROJ AND status = 'In Progress'"
```

---

## 🔢 Precedence

Format resolution follows this order (highest wins):

1. **CLI flag** — `--toon`, `--mini`, `--output <format>`
2. **Environment variable** — `CLI_OUTPUT` in shell
3. **`dmtools.env` / `config.properties`** — `CLI_OUTPUT=<format>`
4. **Default** — `json`

---

## 🤖 LLM Workflow Example

When piping tool output into an LLM prompt, use `--mini` to minimise context tokens:

```bash
# Fetch ticket in mini format and pass directly to an AI tool
TICKET=$(dmtools jira_get_ticket PROJ-123 --mini)

dmtools gemini_ai_chat --data "{\"message\": \"Summarise this ticket:\\n$TICKET\"}"
```

Using `--toon` is a good middle ground when the LLM needs to reference specific field names:

```bash
TICKET=$(dmtools jira_get_ticket PROJ-123 --toon)
dmtools anthropic_ai_chat --data "{\"message\": \"Write acceptance criteria for:\\n$TICKET\"}"
```
