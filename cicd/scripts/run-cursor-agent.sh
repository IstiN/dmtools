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

# List MCP servers
echo ""
echo "=== MCP Server Status ==="
echo "Listing available MCP servers..."
# Ensure Cursor reads config from both project and XDG locations
export CURSOR_CONFIG_DIR="$HOME/.config/cursor"
mkdir -p "$CURSOR_CONFIG_DIR"
$AGENT_BIN mcp list || true

# Check if MCP servers are available
echo ""
echo "=== Execution Parameters ==="
echo "User request: $USER_REQUEST"
echo "Selected model: $MODEL"
echo "API Key configured: $([ -n "$CURSOR_API_KEY" ] && echo 'YES' || echo 'NO')"

# Execute the cursor agent with the user request
echo ""
echo "=== Starting Cursor Agent ==="
echo "Executing: $AGENT_BIN --print \"$USER_REQUEST\" --model $MODEL --force --output-format=text"
echo ""

# Run cursor-agent non-interactively with watchdog to avoid hangs
LOG_FILE="$(mktemp)"
DONE_PATTERN="${AGENT_DONE_PATTERN:-Wrote }"
DONE_FILE="${AGENT_DONE_FILE:-}"
MAX_SECONDS="${AGENT_MAX_SECONDS:-300}"

"$AGENT_BIN" --print "$USER_REQUEST" --model "$MODEL" --force --output-format=text > "$LOG_FILE" 2>&1 &
AGENT_PID=$!

start_time=$(date +%s)
exit_code=0

while kill -0 "$AGENT_PID" 2>/dev/null; do
  now=$(date +%s)
  elapsed=$(( now - start_time ))
  if [ "$elapsed" -ge "$MAX_SECONDS" ]; then
    echo "⚠️ Agent exceeded ${MAX_SECONDS}s, terminating..."
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    sleep 2
    kill -KILL "$AGENT_PID" 2>/dev/null || true
    exit_code=124
    break
  fi

  if grep -q "$DONE_PATTERN" "$LOG_FILE" 2>/dev/null; then
    echo "Detected completion pattern '$DONE_PATTERN'; stopping agent..."
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    wait "$AGENT_PID" 2>/dev/null || true
    exit_code=0
    break
  fi

  if [ -n "$DONE_FILE" ] && [ -f "$DONE_FILE" ]; then
    echo "Detected target file '$DONE_FILE'; stopping agent..."
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    wait "$AGENT_PID" 2>/dev/null || true
    exit_code=0
    break
  fi

  sleep 1
done

if [ "$exit_code" -eq 0 ] && kill -0 "$AGENT_PID" 2>/dev/null; then
  wait "$AGENT_PID"
  exit_code=$?
fi

echo ""
echo "--- Agent Output ---"
cat "$LOG_FILE" || true
echo "---------------------"
rm -f "$LOG_FILE" || true

echo ""
echo "=== Execution Completed ==="
if [ $exit_code -eq 0 ]; then
    echo "✅ Cursor Agent execution completed successfully"
elif [ $exit_code -eq 124 ]; then
    echo "⚠️  Cursor Agent execution timed out (5 minutes)"
else
    echo "❌ Cursor Agent execution failed with exit code: $exit_code"
fi

exit $exit_code
