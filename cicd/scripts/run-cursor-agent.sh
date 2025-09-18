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

# Set up environment for MCP
MCP_IDENTIFIER="${MCP_IDENTIFIER:-dmtools}"
export CURSOR_CONFIG_DIR="$HOME/.config/cursor"
mkdir -p "$CURSOR_CONFIG_DIR"

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

# For debug: run cursor-agent in interactive mode for 5 seconds
echo ""
echo "=== Starting Cursor Agent (Interactive Debug Mode) ==="
echo "Running 'cursor-agent' for 5 seconds, then stopping..."
echo ""

# Start cursor-agent in interactive mode
$AGENT_BIN &
AGENT_PID=$!

# Wait 5 seconds then kill it
sleep 5
echo ""
echo "Stopping cursor-agent after 5 seconds..."
kill -TERM "$AGENT_PID" 2>/dev/null || true
sleep 1
kill -KILL "$AGENT_PID" 2>/dev/null || true
wait "$AGENT_PID" 2>/dev/null || true

exit_code=0

echo ""
echo "=== Execution Completed ==="
if [ $exit_code -eq 0 ]; then
  echo "✅ Cursor Agent execution finished"
else
  echo "❌ Cursor Agent reported a non-zero exit"
fi

echo ""
echo "=== ~/.cursor contents (DEBUG) ==="
if [ -d "$HOME/.cursor" ]; then
  echo "Directory listing:"
  ls -la "$HOME/.cursor" || true
  echo ""
  echo "--- Files and directories from ~/.cursor ---"
  for item in "$HOME/.cursor"/*; do
    if [ -f "$item" ]; then
      echo ""
      echo "##### FILE: $(basename "$item") #####"
      # Limit very large files to last 200 lines
      if [ $(wc -c < "$item" 2>/dev/null || echo 0) -gt 200000 ]; then
        echo "(file too large, showing last 200 lines)"
        tail -200 "$item" || true
      else
        cat "$item" || true
      fi
    elif [ -d "$item" ]; then
      echo ""
      echo "##### DIRECTORY: $(basename "$item") #####"
      ls -la "$item" || true
      # Show contents of files in subdirectories (up to 2 levels)
      for subitem in "$item"/*; do
        if [ -f "$subitem" ]; then
          echo ""
          echo "--- FILE: $(basename "$item")/$(basename "$subitem") ---"
          if [ $(wc -c < "$subitem" 2>/dev/null || echo 0) -gt 10000 ]; then
            echo "(file too large, showing first 50 lines)"
            head -50 "$subitem" || true
          else
            cat "$subitem" || true
          fi
        fi
      done
    fi
  done
else
  echo "~/.cursor directory not found"
fi

echo ""
echo "=== ~/.config/cursor contents (DEBUG) ==="
if [ -d "$HOME/.config/cursor" ]; then
  echo "Directory listing:"
  ls -la "$HOME/.config/cursor" || true
  echo ""
  echo "--- Files from ~/.config/cursor ---"
  for item in "$HOME/.config/cursor"/*; do
    if [ -f "$item" ]; then
      echo ""
      echo "##### FILE: $(basename "$item") #####"
      cat "$item" || true
    fi
  done
else
  echo "~/.config/cursor directory not found"
fi
