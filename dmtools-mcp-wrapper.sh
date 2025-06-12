#!/bin/bash

# DMTools MCP Command Wrapper
# This script acts as a command-based MCP server for Cursor

# Function to call the HTTP MCP server
call_mcp_server() {
    local method="$1"
    local params="$2"
    local id="mcp-$(date +%s)"
    
    local payload="{\"method\":\"$method\",\"id\":\"$id\",\"params\":$params}"
    
    curl -s -X POST http://localhost:8080/mcp/ \
        -H "Content-Type: application/json" \
        -d "$payload"
}

# Read JSON-RPC request from stdin
while IFS= read -r line; do
    echo "$line" >&2  # Log to stderr for debugging
    
    # Parse the JSON request (simple parsing for demo)
    if echo "$line" | grep -q '"method":"initialize"'; then
        call_mcp_server "initialize" "{}"
    elif echo "$line" | grep -q '"method":"tools/list"'; then
        call_mcp_server "tools/list" "{}"
    elif echo "$line" | grep -q '"method":"tools/call"'; then
        # Extract tool name and arguments (simplified)
        call_mcp_server "tools/call" "{\"name\":\"jira_get_instance\",\"arguments\":{}}"
    else
        # Default response
        echo '{"id":"unknown","error":{"code":-32601,"message":"Method not found"}}'
    fi
done 