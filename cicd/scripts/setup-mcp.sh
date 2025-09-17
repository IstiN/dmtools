#!/bin/bash
set -e

# Setup MCP Configuration for Cursor Agent
# This script creates MCP configuration files in both global and local locations

echo "=== MCP Configuration Setup ==="

# Check if MCP URL is provided
if [ -z "$MCP_DMTOOLS_URL" ] && [ -z "$INPUT_MCP_URL" ]; then
    echo "Error: No MCP URL provided. Please set MCP_DMTOOLS_URL environment variable or INPUT_MCP_URL"
    exit 1
fi

# Use input URL if provided, otherwise use environment variable
if [ -n "$INPUT_MCP_URL" ]; then
    MCP_URL="$INPUT_MCP_URL"
    echo "✓ Using MCP URL from input parameter"
else
    MCP_URL="$MCP_DMTOOLS_URL"
    echo "✓ Using MCP URL from environment variable"
fi

echo "Current working directory: $(pwd)"
echo "Home directory: $HOME"
echo "User: $(whoami)"

# Create directories
echo "Creating directories..."
mkdir -p ~/.cursor
mkdir -p .cursor

# Create MCP configuration JSON
create_mcp_config() {
    local file_path="$1"
    cat > "$file_path" << EOF
{
  "mcpServers": {
    "dmtools": {
      "url": "$MCP_URL"
    }
  }
}
EOF
}

# Create configuration files
echo "Creating MCP configuration files..."
create_mcp_config ~/.cursor/mcp.json
create_mcp_config .cursor/mcp.json

echo "✓ MCP configuration files created"

# Verify files exist and are readable
echo ""
echo "=== Verification ==="
echo "Files in current directory:"
ls -la

echo ""
echo "Files in ~/.cursor/:"
ls -la ~/.cursor/ || echo "~/.cursor/ directory not found"

echo ""
echo "Checking MCP configuration files:"
echo "~/.cursor/mcp.json exists: $(test -f ~/.cursor/mcp.json && echo 'YES' || echo 'NO')"
echo ".cursor/mcp.json exists: $(test -f .cursor/mcp.json && echo 'YES' || echo 'NO')"

# Show file contents and permissions
if [ -f ~/.cursor/mcp.json ]; then
    echo ""
    echo "Contents of ~/.cursor/mcp.json:"
    cat ~/.cursor/mcp.json
    echo ""
    echo "File permissions:"
    ls -la ~/.cursor/mcp.json
fi

if [ -f .cursor/mcp.json ]; then
    echo ""
    echo "Contents of .cursor/mcp.json:"
    cat .cursor/mcp.json
    echo ""
    echo "File permissions:"
    ls -la .cursor/mcp.json
fi

# Validate JSON
echo ""
echo "=== JSON Validation ==="
if [ -f ~/.cursor/mcp.json ]; then
    if python3 -m json.tool ~/.cursor/mcp.json > /dev/null 2>&1; then
        echo "✓ ~/.cursor/mcp.json - Valid JSON"
    else
        echo "✗ ~/.cursor/mcp.json - Invalid JSON"
        exit 1
    fi
fi

if [ -f .cursor/mcp.json ]; then
    if python3 -m json.tool .cursor/mcp.json > /dev/null 2>&1; then
        echo "✓ .cursor/mcp.json - Valid JSON"
    else
        echo "✗ .cursor/mcp.json - Invalid JSON"
        exit 1
    fi
fi

echo ""
echo "✅ MCP configuration setup completed successfully!"
