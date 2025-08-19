#!/bin/bash

# aggregate-rules.sh - Aggregate .mdc files from a folder into combined text
# Usage: ./aggregate-rules.sh <rules_folder>

set -e

RULES_FOLDER="$1"

if [ -z "$RULES_FOLDER" ]; then
    echo "ERROR: Rules folder not specified"
    echo "Usage: $0 <rules_folder>"
    exit 1
fi

if [ ! -d "$RULES_FOLDER" ]; then
    echo "ERROR: Rules folder not found: $RULES_FOLDER"
    exit 1
fi

echo "📋 Aggregating .mdc files from: $RULES_FOLDER"

# Find all .mdc files in the folder
MDC_FILES=$(find "$RULES_FOLDER" -name "*.mdc" -type f | sort)

if [ -z "$MDC_FILES" ]; then
    echo "⚠️ No .mdc files found in $RULES_FOLDER"
    echo ""
    exit 0
fi

echo "📄 Found $(echo "$MDC_FILES" | wc -l) .mdc files:"
echo "$MDC_FILES" | sed 's/^/  - /'
echo ""

# Aggregate all .mdc files
echo "# Project Rules and Guidelines"
echo ""
echo "The following rules and guidelines should be followed when working on this project:"
echo ""

for file in $MDC_FILES; do
    if [ -f "$file" ]; then
        # Extract filename without path and extension
        filename=$(basename "$file" .mdc)
        
        echo "## Rule: $filename"
        echo ""
        echo "\`\`\`"
        echo "Source: $file"
        echo "\`\`\`"
        echo ""
        
        # Extract description from the front matter if available
        description=$(grep -E "^description:" "$file" | sed 's/description: *//')
        if [ ! -z "$description" ]; then
            echo "**Description**: $description"
            echo ""
        fi
        
        # Extract key sections (first 20 lines after frontmatter, excluding empty lines)
        echo "**Key Guidelines**:"
        echo ""
        grep -v "^---$\|^description:\|^globs:\|^alwaysApply:" "$file" | head -20 | grep -v "^$" | sed 's/^/- /'
        echo ""
        echo "*For complete details, see: $file*"
        echo ""
        echo "---"
        echo ""
    fi
done

echo "📋 Rules aggregation completed successfully"
