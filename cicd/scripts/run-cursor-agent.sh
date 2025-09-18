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

# List MCP servers (non-blocking)
echo ""
echo "=== MCP Server Status ==="
echo "Listing available MCP servers..."
# Ensure Cursor reads config from both project and XDG locations
export CURSOR_CONFIG_DIR="$HOME/.config/cursor"
mkdir -p "$CURSOR_CONFIG_DIR"
if command -v timeout >/dev/null 2>&1; then
    timeout 3 $AGENT_BIN mcp list || true
else
    $AGENT_BIN mcp list || true
fi

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

# Optionally disable watchdog and run directly
if [ "${AGENT_DISABLE_WATCHDOG:-0}" = "1" ]; then
  echo "Watchdog disabled (AGENT_DISABLE_WATCHDOG=1). Running agent directly..."
  $AGENT_BIN --print "$USER_REQUEST" --model "$MODEL" --force --output-format=text
  exit_code=$?
  echo ""
  echo "=== Execution Completed ==="
  if [ $exit_code -eq 0 ]; then
      echo "✅ Cursor Agent execution completed successfully"
  else
      echo "❌ Cursor Agent execution failed with exit code: $exit_code"
  fi
  exit $exit_code
fi

# Run cursor-agent non-interactively with watchdog to avoid hangs
LOG_FILE="$(mktemp)"
# Support multiple success markers separated by '|'
DONE_PATTERNS="${AGENT_DONE_PATTERNS:-Wrote |Created |Updated |Finished|Completed}"
DONE_FILE="${AGENT_DONE_FILE:-}"
MAX_SECONDS="${AGENT_MAX_SECONDS:-300}"
# Idle timeout if no new output for N seconds
IDLE_SECONDS="${AGENT_IDLE_SECONDS:-25}"

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

  if egrep -q "$DONE_PATTERNS" "$LOG_FILE" 2>/dev/null; then
    echo "Detected completion patterns '$DONE_PATTERNS'; stopping agent..."
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

  # Idle detection: stop if no output for IDLE_SECONDS
  current_bytes=$(wc -c < "$LOG_FILE" 2>/dev/null || echo 0)
  if [ -z "$last_bytes" ]; then last_bytes=$current_bytes; fi
  if [ "$current_bytes" -gt "$last_bytes" ]; then
    last_bytes=$current_bytes
    last_output_time=$now
  else
    idle_elapsed=$(( now - ${last_output_time:-$now} ))
    if [ "$idle_elapsed" -ge "$IDLE_SECONDS" ]; then
      echo "No output for ${IDLE_SECONDS}s (idle). Stopping agent..."
      kill -TERM "$AGENT_PID" 2>/dev/null || true
      wait "$AGENT_PID" 2>/dev/null || true
      exit_code=0
      break
    fi
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
