#!/bin/bash
set -e

# Setup Cursor CLI Configuration for Non-Interactive Execution
# This script creates CLI configuration files with full permissions

echo "=== Cursor CLI Configuration Setup ==="

echo "Current working directory: $(pwd)"
echo "Home directory: $HOME"

# Create directories
echo "Creating directories..."
mkdir -p ~/.cursor
mkdir -p .cursor

# Create global CLI configuration with full permissions
echo "Creating global CLI configuration..."
cat > ~/.cursor/cli-config.json << 'EOF'
{
  "version": 1,
  "editor": { 
    "vimMode": false 
  },
  "permissions": {
    "allow": [
      "Shell(*)",
      "Read(**)",
      "Write(**)",
      "Read(*)",
      "Write(*)"
    ],
    "deny": []
  }
}
EOF

# Create project-specific CLI configuration with full permissions
echo "Creating project-specific CLI configuration..."
cat > .cursor/cli.json << 'EOF'
{
  "permissions": {
    "allow": [
      "Shell(*)",
      "Read(**)",
      "Write(**)",
      "Read(*)",
      "Write(*)"
    ],
    "deny": []
  }
}
EOF

echo "✓ CLI configuration files created"

# Verify files exist and are readable
echo ""
echo "=== Verification ==="
echo "Files in ~/.cursor/:"
ls -la ~/.cursor/ || echo "~/.cursor/ directory not found"

echo ""
echo "Files in .cursor/:"
ls -la .cursor/ || echo ".cursor/ directory not found"

echo ""
echo "Checking CLI configuration files:"
echo "~/.cursor/cli-config.json exists: $(test -f ~/.cursor/cli-config.json && echo 'YES' || echo 'NO')"
echo ".cursor/cli.json exists: $(test -f .cursor/cli.json && echo 'YES' || echo 'NO')"

# Show file contents
if [ -f ~/.cursor/cli-config.json ]; then
    echo ""
    echo "Contents of ~/.cursor/cli-config.json:"
    cat ~/.cursor/cli-config.json
    echo ""
    echo "File permissions:"
    ls -la ~/.cursor/cli-config.json
fi

if [ -f .cursor/cli.json ]; then
    echo ""
    echo "Contents of .cursor/cli.json:"
    cat .cursor/cli.json
    echo ""
    echo "File permissions:"
    ls -la .cursor/cli.json
fi

# Validate JSON
echo ""
echo "=== JSON Validation ==="
if [ -f ~/.cursor/cli-config.json ]; then
    if python3 -m json.tool ~/.cursor/cli-config.json > /dev/null 2>&1; then
        echo "✓ ~/.cursor/cli-config.json - Valid JSON"
    else
        echo "✗ ~/.cursor/cli-config.json - Invalid JSON"
        exit 1
    fi
fi

if [ -f .cursor/cli.json ]; then
    if python3 -m json.tool .cursor/cli.json > /dev/null 2>&1; then
        echo "✓ .cursor/cli.json - Valid JSON"
    else
        echo "✗ .cursor/cli.json - Invalid JSON"
        exit 1
    fi
fi

echo ""
echo "✅ Cursor CLI configuration setup completed successfully!"
echo "The agent now has full permissions and will run without user prompts."
