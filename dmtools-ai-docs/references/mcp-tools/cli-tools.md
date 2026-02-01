# CLI MCP Tools

**Total Tools**: 1

## Quick Reference

```bash
# List all cli tools
dmtools list | jq '.tools[] | select(.name | startswith("cli_"))'

# Example usage
dmtools cli_execute_command [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for cli tools
const result = cli_execute_command(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `cli_execute_command` | Execute CLI commands (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az) from JavaScript post-actions. Returns command output as string. Commands execute synchronously with environment variables inherited from workflow context. Only whitelisted commands allowed for security. | `workingDirectory` (string, optional)<br>`command` (string, **required**) |

## Detailed Parameter Information

### `cli_execute_command`

Execute CLI commands (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az) from JavaScript post-actions. Returns command output as string. Commands execute synchronously with environment variables inherited from workflow context. Only whitelisted commands allowed for security.

**Parameters:**

- **`workingDirectory`** (string) ⚪ Optional
  - Working directory for command execution. Defaults to repository root if not specified. Use absolute path or path relative to current directory.
  - Example: `/path/to/repo`

- **`command`** (string) 🔴 Required
  - CLI command to execute. Must start with whitelisted command (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az).
  - Example: `git commit -m 'Automated update'`

**Example:**
```bash
dmtools cli_execute_command "value" "value"
```

```javascript
// In JavaScript agent
const result = cli_execute_command("workingDirectory", "command");
```

---

