#!/bin/bash
set -e

# Run Cursor Agent with MCP support
# This script handles cursor-agent detection, MCP server listing, and execution

echo "=== Cursor Agent Execution ==="

# Check required parameters
if [ -z "$USER_REQUEST" ]; then
    echo "Error: USER_REQUEST environment variable is required"
    exit 1
fi

if [ -z "$MODEL" ]; then
    echo "Warning: MODEL not specified, using default"
    MODEL="sonnet-4"
fi

# Ensure cursor-agent is available on PATH
if ! command -v cursor-agent >/dev/null 2>&1; then
    echo "Error: cursor-agent not found in PATH"
    echo "PATH: $PATH"
    exit 1
fi

AGENT_BIN="cursor-agent"
AGENT_PATH=$(command -v "$AGENT_BIN")
echo "Using Cursor Agent at: $AGENT_PATH"

# Show version
echo "Cursor Agent version: $($AGENT_BIN --version)"

# Print working directory and MCP config locations
echo ""
echo "CWD: $(pwd)"
echo "HOME: $HOME"
echo "Project .cursor exists: $(test -d .cursor && echo YES || echo NO)"
echo "- .cursor/mcp.json exists: $(test -f .cursor/mcp.json && echo YES || echo NO)"
echo "- ~/.cursor/mcp.json exists: $(test -f "$HOME/.cursor/mcp.json" && echo YES || echo NO)"
echo "- ~/.config/cursor/mcp.json exists: $(test -f "$HOME/.config/cursor/mcp.json" && echo YES || echo NO)"

# Authenticate MCP server (best effort)
MCP_IDENTIFIER="${MCP_IDENTIFIER:-dmtools}"
echo "" 
echo "Authenticating MCP server: $MCP_IDENTIFIER"
export CURSOR_CONFIG_DIR="$HOME/.config/cursor"
mkdir -p "$CURSOR_CONFIG_DIR"
$AGENT_BIN mcp login "$MCP_IDENTIFIER" || true

# Verify MCP tools for the configured server
echo ""
echo "=== MCP Server Status ==="
echo "Listing available tools for: $MCP_IDENTIFIER"
# Ensure Cursor reads config from both project and XDG locations
$AGENT_BIN mcp list-tools "$MCP_IDENTIFIER" || true

# Check if MCP servers are available
echo ""
echo "=== Execution Parameters ==="
echo "User request: $USER_REQUEST"
echo "Selected model: $MODEL"
echo "API Key configured: $([ -n "$CURSOR_API_KEY" ] && echo 'YES' || echo 'NO')"

# Execute the cursor agent with the user request (no watchdog/timeout)
echo ""
echo "=== Starting Cursor Agent ==="
echo "Executing: $AGENT_BIN -p \"$USER_REQUEST\" --model $MODEL --force --output-format=text"
echo ""

#$AGENT_BIN -p "$USER_REQUEST" --model "$MODEL" --force --output-format=text
cursor-agent -p "write a short hello world to output/response.md file for test purposes" --model "sonnet-4" 

echo ""
echo "=== Execution Completed ==="
echo "✅ Cursor Agent execution finished"
