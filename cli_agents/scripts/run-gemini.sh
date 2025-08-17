#!/bin/bash

# run-gemini.sh - Execute Gemini CLI with specified parameters
# Handles prompt preparation and execution

set -e

# Parameters
PHASE="$1"              # discovery or implementation
USER_REQUEST_FILE="$2"
RULES_FOLDER="$3"       # optional folder with .mdc rules files
MODEL="${4:-gemini-2.5-flash-preview-05-20}"
USE_VERTEX_AI="${5:-false}"
USE_GCA="${6:-false}"
OUTPUT_DIR="${7:-outputs}"

echo "🚀 Starting Gemini CLI execution..."
echo "📋 Phase: $PHASE"
echo "📋 Rules folder: ${RULES_FOLDER:-None}"
echo "📋 Model: $MODEL"
echo "📋 Use Vertex AI: $USE_VERTEX_AI"
echo "📋 Use Gemini Code Assist: $USE_GCA"

# Clean up previous files
echo "🧹 Cleaning up previous files..."
rm -f "$OUTPUT_DIR/response.md"
rm -f "$OUTPUT_DIR"/response-log_*.txt
rm -f "$OUTPUT_DIR"/*-prompt-combined.md
echo "✅ Cleanup completed"

# Validate phase
case "$PHASE" in
    "discovery"|"implementation")
        ;;
    *)
        echo "ERROR: Invalid phase '$PHASE'. Must be 'discovery' or 'implementation'"
        exit 1
        ;;
esac

# Set environment variables for Gemini CLI
export GOOGLE_GENAI_USE_VERTEXAI="$USE_VERTEX_AI"
export GOOGLE_GENAI_USE_GCA="$USE_GCA"

# Prepare prompt file based on phase
PROMPT_FILE="cli_agents/prompts/${PHASE}-prompt.md"
COMBINED_PROMPT_FILE="$OUTPUT_DIR/${PHASE}-prompt-combined.md"

if [ ! -f "$PROMPT_FILE" ]; then
    echo "ERROR: Prompt file not found: $PROMPT_FILE"
    exit 1
fi

if [ ! -f "$USER_REQUEST_FILE" ]; then
    echo "ERROR: User request file not found: $USER_REQUEST_FILE"
    exit 1
fi

# Read the prompt content
PROMPT_CONTENT=$(cat "$PROMPT_FILE")

# Read user request content  
USER_REQUEST=$(cat "$USER_REQUEST_FILE")

# Aggregate rules if rules folder is provided
RULES_CONTENT=""
if [ ! -z "$RULES_FOLDER" ] && [ -d "$RULES_FOLDER" ]; then
    echo "📋 Aggregating rules from: $RULES_FOLDER"
    RULES_SCRIPT="cli_agents/scripts/aggregate-rules.sh"
    
    if [ -f "$RULES_SCRIPT" ]; then
        RULES_CONTENT=$(bash "$RULES_SCRIPT" "$RULES_FOLDER" 2>/dev/null | grep -v "📋\|📄\|⚠️" || echo "")
        if [ ! -z "$RULES_CONTENT" ]; then
            echo "✅ Rules aggregated successfully ($(echo "$RULES_CONTENT" | wc -l) lines)"
        else
            echo "⚠️ No rules content found in $RULES_FOLDER"
        fi
    else
        echo "⚠️ Rules aggregation script not found: $RULES_SCRIPT"
    fi
else
    echo "📋 No rules folder specified or folder doesn't exist"
fi

# Prepare combined prompt based on phase
echo "📋 Preparing $PHASE prompt..."

if [ "$PHASE" = "discovery" ]; then
    # Discovery phase prompt - use the template with user request
    CURRENT_DIR=$(pwd)
    cat > "$COMBINED_PROMPT_FILE" << EOF
# User Request Analysis

**Working Directory:** $CURRENT_DIR

**User Request:**
\`\`\`
$USER_REQUEST
\`\`\`

$(if [ ! -z "$RULES_CONTENT" ]; then
echo "---"
echo ""
echo "$RULES_CONTENT"
echo ""
fi)

---

$PROMPT_CONTENT
EOF

elif [ "$PHASE" = "implementation" ]; then
    # Implementation phase prompt
    cat > "$COMBINED_PROMPT_FILE" << EOF
# Implementation Phase

**User Request:**
\`\`\`
$USER_REQUEST
\`\`\`

$(if [ ! -z "$RULES_CONTENT" ]; then
echo "---"
echo ""
echo "$RULES_CONTENT"
echo ""
fi)

---

$PROMPT_CONTENT
EOF

fi

echo "✅ Combined $PHASE prompt prepared in $COMBINED_PROMPT_FILE"

# Read the combined prompt content
COMBINED_PROMPT_CONTENT=$(cat "$COMBINED_PROMPT_FILE")

# Create output file for response
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESPONSE_FILE="$OUTPUT_DIR/response.md"
RESPONSE_LOG_FILE="$OUTPUT_DIR/response-log_${TIMESTAMP}.txt"

# Run Gemini CLI and capture output
echo "📋 Running Gemini CLI to process user request..."

# Debug information
echo "🔍 Debug: Checking Gemini CLI availability..."
which gemini || echo "❌ WARNING: gemini command not found in PATH"
echo "🔍 Debug: Gemini CLI version:"
gemini --version 2>&1 || echo "❌ WARNING: Could not get Gemini CLI version"

echo "🔍 Debug: Environment check:"
echo "- GEMINI_API_KEY: ${GEMINI_API_KEY:+SET (${#GEMINI_API_KEY} chars)} ${GEMINI_API_KEY:-NOT_SET}"
echo "- Model: $MODEL"
echo "- Rules folder: ${RULES_FOLDER:-None}"
echo "- Rules content: ${RULES_CONTENT:+$(echo "$RULES_CONTENT" | wc -l) lines} ${RULES_CONTENT:-None}"
echo "- Combined prompt file size: $(wc -c < "$COMBINED_PROMPT_FILE") bytes"

echo "🚀 Executing: gemini ${GEMINI_DEBUG_FLAG} --yolo --prompt <prompt_content>"
echo "📏 Prompt length: ${#COMBINED_PROMPT_CONTENT} characters"

# Set Node.js options to handle event listener limits
export NODE_OPTIONS="--max-old-space-size=4096 --max-http-header-size=8192"

# Configure debug modes based on environment variable from workflow
if [ "${GEMINI_DEBUG_ENABLED:-true}" = "true" ]; then
  echo "🔍 Debug mode ENABLED for Gemini CLI execution"
  export DEBUG=1
  export DEBUG_MODE=1
  GEMINI_DEBUG_FLAG="--debug"
else
  echo "🔇 Debug mode DISABLED for Gemini CLI execution"
  GEMINI_DEBUG_FLAG=""
fi

# No process cleanup needed - single execution per workflow

if GEMINI_RESPONSE=$(gemini $GEMINI_DEBUG_FLAG --yolo --prompt "$COMBINED_PROMPT_CONTENT" 2>&1); then
    GEMINI_EXIT_CODE=0
    echo "✅ Gemini CLI execution successful"
    echo "📏 Response length: ${#GEMINI_RESPONSE} characters"
    
    # Check if Gemini created the response.md file itself
    if [ -f "$RESPONSE_FILE" ]; then
        echo "📄 Gemini created response file: $RESPONSE_FILE"
        echo "📏 Response file size: $(wc -c < "$RESPONSE_FILE") bytes"
    else
        # Fallback: create response file from Gemini output (cleaned)
        echo "📄 Creating response file from Gemini output"
        # Remove console logs and keep only the clean response
        echo "$GEMINI_RESPONSE" | sed '/^(node:[0-9]*)/d' | sed '/Both GOOGLE_API_KEY and GEMINI_API_KEY are set/d' > "$RESPONSE_FILE"
        echo "📏 Created response file size: $(wc -c < "$RESPONSE_FILE") bytes"
    fi
    
    echo "📄 Response available at: $RESPONSE_FILE"
else
    GEMINI_EXIT_CODE=$?
    echo "❌ ERROR: Gemini CLI failed with exit code $GEMINI_EXIT_CODE"
    echo "📏 Error response length: ${#GEMINI_RESPONSE} characters"
    echo "🔍 Error details:"
    echo "$GEMINI_RESPONSE" | head -20  # Show first 20 lines of error
    
    # Write error response to markdown file
    cat > "$RESPONSE_FILE" << EOF
# Error Response

**Generated:** $(date)
**Phase:** $PHASE
**Model:** $MODEL
**Exit Code:** $GEMINI_EXIT_CODE

---

## Error Output:
\`\`\`
$GEMINI_RESPONSE
\`\`\`
EOF
    
    echo "📄 Error response written to: $RESPONSE_FILE"
fi

# Create detailed log file for debugging
{
    echo "=== GEMINI CLI EXECUTION LOG ==="
    echo "Phase: $PHASE"
    echo "Model: $MODEL"
    echo "Use Vertex AI: $USE_VERTEX_AI"
    echo "Use Gemini Code Assist: $USE_GCA"
    echo "Timestamp: $TIMESTAMP"
    echo "=== USER REQUEST CONTENT ==="
    cat "$USER_REQUEST_FILE"
    echo "=== END USER REQUEST ==="
    echo "=== GEMINI CLI RESPONSE ==="
    echo "$GEMINI_RESPONSE"
    echo "=== GEMINI EXIT CODE: $GEMINI_EXIT_CODE ==="
    echo "=== EXECUTION END ==="
} > "$RESPONSE_LOG_FILE"

echo "📋 Response log saved to: $RESPONSE_LOG_FILE"

# Output the response file path and exit code for use by calling script
echo "$RESPONSE_FILE"
exit $GEMINI_EXIT_CODE
