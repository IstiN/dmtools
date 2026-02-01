#!/bin/bash

# Update DMtools AI Skill Documentation
# This script regenerates all auto-generated documentation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🔄 Updating DMtools AI Skill Documentation"
echo ""

# Check if dmtools is installed
if ! command -v dmtools &> /dev/null && [ ! -f ~/.dmtools/dmtools.jar ]; then
    echo "❌ dmtools not found. Building and installing..."
    cd "$PROJECT_ROOT"
    ./gradlew :dmtools-core:shadowJar
    ./buildInstallLocal.sh
    echo "✅ DMtools installed"
    echo ""
fi

# Generate MCP tools documentation
echo "📝 Generating MCP tools documentation..."
cd "$PROJECT_ROOT"
./scripts/generate-mcp-tables.sh

echo ""
echo "📊 Documentation Statistics:"
echo ""

# Count tools by integration
TOOLS_JSON="$PROJECT_ROOT/dmtools-ai-docs/references/mcp-tools/tools-raw.json"
if [ -f "$TOOLS_JSON" ]; then
    TOTAL_TOOLS=$(jq '.tools | length' "$TOOLS_JSON")
    echo "   Total MCP Tools: $TOTAL_TOOLS"
    echo ""
    echo "   By Integration:"

    # Extract integration counts
    jq -r '.tools | group_by(.name | split("_")[0]) | map({integration: (.[0].name | split("_")[0]), count: length}) | .[] | "      - \(.integration): \(.count) tools"' "$TOOLS_JSON"
fi

echo ""
echo "✅ Documentation update complete!"
echo ""
echo "📁 Generated files:"
echo "   - references/mcp-tools/README.md (Main index)"
echo "   - references/mcp-tools/jira-tools.md"
echo "   - references/mcp-tools/teams-tools.md"
echo "   - references/mcp-tools/figma-tools.md"
echo "   - references/mcp-tools/file-tools.md"
echo "   - references/mcp-tools/cli-tools.md"
echo ""
echo "💡 Next steps:"
echo "   1. Review generated documentation"
echo "   2. Update SKILL.md if tool counts changed"
echo "   3. Commit changes: git add dmtools-ai-docs/"
echo "   4. Create release: ./release-skill.sh X.Y.Z"