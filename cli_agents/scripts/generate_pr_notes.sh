#!/bin/bash

# generate_pr_notes.sh - Generate PR notes using direct Gemini API call

set -e

# Parameters
USER_REQUEST_FILE="$1"
OUTPUT_DIR="${2:-outputs}"

echo "🔧 Generating PR notes using direct Gemini API..."

# Validate inputs
if [ ! -f "$USER_REQUEST_FILE" ]; then
    echo "❌ ERROR: User request file not found: $USER_REQUEST_FILE"
    exit 1
fi

if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ ERROR: GEMINI_API_KEY environment variable is not set"
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Prepare combined prompt
CURRENT_DIR=$(pwd)
PROMPT_FILE="$OUTPUT_DIR/pr-generation-prompt.md"
USER_REQUEST=$(cat "$USER_REQUEST_FILE")
PR_RULES=$(cat "cli_agents/prompts/pull_request_and_commit.md")

cat > "$PROMPT_FILE" << EOF
# PR Details Generation Request

**Working Directory:** $CURRENT_DIR

**User Request:**
$USER_REQUEST

---

$PR_RULES
EOF

echo "📋 Prompt prepared: $PROMPT_FILE"
PROMPT_CONTENT=$(cat "$PROMPT_FILE")

# Call Gemini API directly
TEMP_RESPONSE="$OUTPUT_DIR/pr_temp_response.txt"

if ./cli_agents/scripts/ask_gemini.sh "$PROMPT_CONTENT" "gemini-2.5-flash-preview-05-20" "$TEMP_RESPONSE"; then
    echo "✅ Gemini API call successful"
    
    # Check if the response contains JSON
    if grep -q '{' "$TEMP_RESPONSE" && grep -q '}' "$TEMP_RESPONSE"; then
        echo "✅ Found JSON in response"
        
        # Extract JSON from response (handling markdown code blocks)
        if JSON_CONTENT=$(sed -n '/```json/,/```/p' "$TEMP_RESPONSE" | sed '1d;$d' | tr -d '\n' | sed 's/[[:space:]]*$//' 2>/dev/null); then
            if [ -z "$JSON_CONTENT" ]; then
                # Fallback: try direct extraction
                JSON_CONTENT=$(grep -oP '\{[^{}]*\}' "$TEMP_RESPONSE" | head -1)
            fi
            
            echo "📋 Extracted JSON:"
            echo "$JSON_CONTENT"
            
            # Validate JSON
            if echo "$JSON_CONTENT" | jq empty 2>/dev/null; then
                echo "✅ JSON validation passed"
                echo "$JSON_CONTENT" | jq '.' > "$OUTPUT_DIR/pr_notes.json"
                echo "📄 PR notes saved to: $OUTPUT_DIR/pr_notes.json"
            else
                echo "❌ Invalid JSON format, trying to fix..."
                # Try to extract and fix JSON
                echo "$JSON_CONTENT" > "$OUTPUT_DIR/pr_notes.json"
                echo "📄 Raw JSON saved (may need manual validation)"
            fi
        else
            echo "❌ Could not extract JSON from response"
            echo "📋 Response content:"
            cat "$TEMP_RESPONSE"
            exit 1
        fi
    else
        echo "❌ No JSON found in response"
        echo "📋 Response content:"
        cat "$TEMP_RESPONSE"
        exit 1
    fi
    
    # Cleanup
    rm -f "$TEMP_RESPONSE"
    
    echo "✅ PR notes generation completed successfully"
else
    echo "❌ Gemini API call failed"
    exit 1
fi
