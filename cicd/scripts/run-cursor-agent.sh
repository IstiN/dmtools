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

# Function to find cursor-agent executable
find_cursor_agent() {
    echo "Detecting cursor-agent executable..." >&2
    
    # Try different possible locations
    if [ -f "$HOME/.cursor/bin/cursor-agent" ]; then
        echo "✓ Found cursor-agent at $HOME/.cursor/bin/cursor-agent" >&2
        echo "$HOME/.cursor/bin/cursor-agent"
    elif [ -f "$HOME/.local/bin/cursor-agent" ]; then
        echo "✓ Found cursor-agent at $HOME/.local/bin/cursor-agent" >&2
        echo "$HOME/.local/bin/cursor-agent"
    elif command -v cursor-agent &> /dev/null; then
        local path=$(command -v cursor-agent)
        echo "✓ Found cursor-agent at $path" >&2
        echo "$path"
    else
        echo "✗ cursor-agent not found in standard locations" >&2
        echo "Searching for cursor-agent..." >&2
        find "$HOME" -name "cursor-agent" -type f 2>/dev/null | head -5 >&2
        echo "Available executables in $HOME/.cursor:" >&2
        find "$HOME/.cursor" -type f -executable 2>/dev/null >&2 || echo "No executables found" >&2
        echo "Available executables in $HOME/.local:" >&2
        find "$HOME/.local" -type f -executable -name "*cursor*" 2>/dev/null >&2 || echo "No cursor executables found" >&2
        return 1
    fi
}

# Find cursor-agent
CURSOR_AGENT_PATH=$(find_cursor_agent)
if [ $? -ne 0 ]; then
    echo "Error: cursor-agent executable not found"
    exit 1
fi

echo "Using Cursor Agent at: $CURSOR_AGENT_PATH"

# Show version
echo "Cursor Agent version: $($CURSOR_AGENT_PATH --version)"

# List MCP servers
echo ""
echo "=== MCP Server Status ==="
echo "Listing available MCP servers..."
$CURSOR_AGENT_PATH mcp list

# Check if MCP servers are available
echo ""
echo "=== Execution Parameters ==="
echo "User request: $USER_REQUEST"
echo "Selected model: $MODEL"
echo "API Key configured: $([ -n "$CURSOR_API_KEY" ] && echo 'YES' || echo 'NO')"

# Execute the cursor agent with the user request
echo ""
echo "=== Starting Cursor Agent ==="
echo "Executing: $CURSOR_AGENT_PATH --print \"$USER_REQUEST\" --model $MODEL --force --output-format=text"
echo ""

# Run with timeout to prevent hanging (if timeout command is available)
# Use --print for non-interactive mode, --force for write permissions, and --output-format for structured output
if command -v timeout &> /dev/null; then
    timeout 300 "$CURSOR_AGENT_PATH" --print "$USER_REQUEST" --model "$MODEL" --force --output-format=text
    exit_code=$?
else
    "$CURSOR_AGENT_PATH" --print "$USER_REQUEST" --model "$MODEL" --force --output-format=text
    exit_code=$?
fi

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
