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

# Execute the cursor agent with stream-json and stop on result event
echo ""
echo "=== Starting Cursor Agent ==="
OUTPUT_FORMAT="${OUTPUT_FORMAT:-stream-json}"
echo "Executing: $AGENT_BIN -p \"$USER_REQUEST\" --model $MODEL --force --output-format=$OUTPUT_FORMAT"
echo ""

LOG_FILE="$(mktemp)"
# Start agent writing to a log file and stream it to console in real time
$AGENT_BIN -p "$USER_REQUEST" --model "$MODEL" --force --output-format="$OUTPUT_FORMAT" > "$LOG_FILE" 2>&1 &
AGENT_PID=$!
tail -n +1 -f "$LOG_FILE" &
TAIL_PID=$!

exit_code=0

while kill -0 "$AGENT_PID" 2>/dev/null; do
  if grep -q '"type"\s*:\s*"result"' "$LOG_FILE"; then
    RESULT_LINE=$(grep '"type"\s*:\s*"result"' "$LOG_FILE" | tail -1)
    echo "Detected result event:"
    echo "$RESULT_LINE"
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    wait "$AGENT_PID" 2>/dev/null || true
    kill -TERM "$TAIL_PID" 2>/dev/null || true
    wait "$TAIL_PID" 2>/dev/null || true
    exit_code=0
    break
  fi
  sleep 1
done

if kill -0 "$AGENT_PID" 2>/dev/null; then :; else wait "$AGENT_PID" 2>/dev/null || true; fi

# Ensure tail is stopped
kill -TERM "$TAIL_PID" 2>/dev/null || true
wait "$TAIL_PID" 2>/dev/null || true

echo ""
echo "--- Agent Output captured in: $LOG_FILE ---"
rm -f "$LOG_FILE" || true

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
      # Show contents of files in subdirectories (up to 3 levels deep)
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
        elif [ -d "$subitem" ]; then
          echo ""
          echo "--- SUBDIRECTORY: $(basename "$item")/$(basename "$subitem") ---"
          ls -la "$subitem" || true
          # Go one more level deep
          for subsubitem in "$subitem"/*; do
            if [ -f "$subsubitem" ]; then
              echo ""
              echo "--- FILE: $(basename "$item")/$(basename "$subitem")/$(basename "$subsubitem") ---"
              if [ $(wc -c < "$subsubitem" 2>/dev/null || echo 0) -gt 10000 ]; then
                echo "(file too large, showing first 50 lines)"
                head -50 "$subsubitem" || true
              else
                cat "$subsubitem" || true
              fi
            fi
          done
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
