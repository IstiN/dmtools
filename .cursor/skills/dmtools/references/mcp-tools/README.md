# DMtools MCP Tools Reference

Complete reference for all MCP tools available in DMtools.

**Total Integrations**: 5
**Total Tools**: 96

*Auto-generated from `dmtools list` on: 2026-02-01 21:38:49*

## Quick Start

```bash
# List all available tools
dmtools list

# List tools for specific integration
dmtools list | jq '.tools[] | select(.name | startswith("jira_"))'

# Execute a tool
dmtools <tool_name> [arguments]
```

## Integrations

| Integration | Tools | Documentation |
|-------------|-------|---------------|
| **CLI** | 1 | [cli-tools.md](cli-tools.md) |
| **FIGMA** | 12 | [figma-tools.md](figma-tools.md) |
| **FILE** | 4 | [file-tools.md](file-tools.md) |
| **JIRA** | 52 | [jira-tools.md](jira-tools.md) |
| **TEAMS** | 27 | [teams-tools.md](teams-tools.md) |

## Usage in JavaScript Agents

All MCP tools are directly accessible as JavaScript functions:

```javascript
// Direct MCP tool access
const ticket = jira_get_ticket('PROJ-123');
const workItem = ado_get_work_item(12345);
const response = gemini_ai_chat('Analyze this');
file_write('output.txt', 'content');
```

## Integration Categories

### Issue Tracking

- [JIRA](jira-tools.md) - 52 tools

### Communication

- [TEAMS](teams-tools.md) - 27 tools

### Design

- [FIGMA](figma-tools.md) - 12 tools

### Authentication

- [TEAMS](teams-tools.md) - 27 tools

### File Operations

- [FILE](file-tools.md) - 4 tools

### CLI Operations

- [CLI](cli-tools.md) - 1 tools

