#!/bin/bash

# Generate MCP Tools Reference for AI Skill from dmtools list output
# This extracts the actual list of tools directly from dmtools

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_FILE="$PROJECT_ROOT/dmtools-ai-docs/references/mcp-tools/TOOLS-REFERENCE.md"

echo "Generating MCP tools reference from dmtools list..."

# Check if dmtools is available
if ! command -v dmtools &> /dev/null && [ ! -f ~/.dmtools/dmtools.jar ]; then
    echo "Error: dmtools not found. Run ./buildInstallLocal.sh first"
    exit 1
fi

# Create output directory
mkdir -p "$(dirname "$OUTPUT_FILE")"

# Get the tools list
TOOLS_OUTPUT=$(dmtools list 2>/dev/null || java -jar ~/.dmtools/dmtools.jar list 2>/dev/null || ./dmtools.sh list 2>/dev/null)

# Generate markdown file
cat > "$OUTPUT_FILE" << 'EOF'
# DMtools MCP Tools Reference

This is the complete list of available MCP tools, generated directly from the running DMtools instance.

## How to Use

### From Command Line
```bash
# List all tools
dmtools list

# Execute a tool
dmtools <tool_name> [arguments]

# Examples
dmtools jira_get_ticket PROJ-123
dmtools gemini_ai_chat "Hello"
```

### From JavaScript Agents
All MCP tools are directly accessible as JavaScript functions:

```javascript
// Direct function calls
const ticket = jira_get_ticket("PROJ-123");
const response = gemini_ai_chat("Analyze this");
file_write("output.txt", "content");
```

## Available Tools

EOF

# Append the actual tools list
echo "$TOOLS_OUTPUT" >> "$OUTPUT_FILE"

# Add footer
cat >> "$OUTPUT_FILE" << 'EOF'

## Notes

- Tools are organized by integration (jira, ado, teams, figma, etc.)
- All tools support direct invocation from JavaScript agents
- Parameters and descriptions are shown in the list above
- For detailed parameter information, use: `dmtools <tool_name> --help`

---

*This reference is generated from the actual MCPToolRegistry and reflects the current build.*
EOF

echo "✅ Generated: $OUTPUT_FILE"
wc -l "$OUTPUT_FILE"