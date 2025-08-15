#!/bin/bash

# run-aider.sh - Execute Aider with specified parameters and capture output
# Handles file discovery, context loading, and execution

set -e

# Parameters
PROMPT_FILE="$1"
USER_REQUEST_FILE="$2"
TARGET_FILES="$3"
MODEL="${4:-gemini/gemini-2.5-flash-preview-05-20}"
MAX_TOKENS="${5:-1000000}"
MODE="${6:-analysis}"  # analysis or coding
OUTPUT_DIR="${7:-outputs}"

echo "🚀 Starting Aider execution..."
echo "📋 Mode: $MODE"
echo "📋 Model: $MODEL"
echo "📋 Max Tokens: $MAX_TOKENS"
echo "📋 Prompt File: $PROMPT_FILE"
echo "📋 Target Files: ${TARGET_FILES:-'Auto-discovered + specified'}"

# Set timestamp for unique output file
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="$OUTPUT_DIR/response_${TIMESTAMP}.txt"

# Prepare the command with optimization flags
AIDER_CMD="aider --model $MODEL"
AIDER_CMD="$AIDER_CMD --max-chat-history-tokens $MAX_TOKENS"
AIDER_CMD="$AIDER_CMD --no-check-update --yes-always --no-stream"

# For coding mode, allow commits and modifications
if [ "$MODE" = "coding" ]; then
    # Allow Aider to make commits and work with git
    AIDER_CMD="$AIDER_CMD --dirty-commits"
    echo "🔧 Coding mode: allowing file modifications and git operations"
else
    # Analysis mode - more restrictive
    AIDER_CMD="$AIDER_CMD --no-auto-commits"
    echo "📊 Analysis mode: read-only analysis"
fi

# Add user request file to context - /ask command in prompt will handle auto-discovery
AIDER_CMD="$AIDER_CMD $USER_REQUEST_FILE"

# For coding mode, check if discovery JSON exists and add discovered files
if [ "$MODE" = "coding" ]; then
    echo "🔍 Checking for discovery JSON file..."
    echo "📁 Current directory: $(pwd)"
    echo "📁 Files in $OUTPUT_DIR:"
    ls -la "$OUTPUT_DIR/" || echo "No $OUTPUT_DIR directory found"
    
    if [ -f "$OUTPUT_DIR/affected-files.json" ]; then
        echo "✅ Found discovery JSON file, processing discovered files..."
        
        # Add the JSON file itself to context first
        AIDER_CMD="$AIDER_CMD $OUTPUT_DIR/affected-files.json"
        echo "📋 Added discovery JSON to context"
    
        # Install jq for JSON parsing if not available
        if ! command -v jq >/dev/null 2>&1; then
            echo "📦 Installing jq for JSON parsing..."
            if command -v apt-get >/dev/null 2>&1; then
                sudo apt-get update >/dev/null 2>&1
                sudo apt-get install -y jq >/dev/null 2>&1
            elif command -v brew >/dev/null 2>&1; then
                brew install jq >/dev/null 2>&1
            else
                echo "⚠️ Cannot install jq, skipping file discovery parsing"
            fi
        fi
        
        if command -v jq >/dev/null 2>&1; then
            # Extract file lists from JSON and add to Aider command
            echo "📋 Extracting discovered files from JSON..."
            
            # Function to add files if they exist
            add_files_from_json() {
                local json_path="$1"
                local file_type="$2"
                echo "🔍 Processing $file_type files..."
                
                local files=$(jq -r ".affected_files.$json_path[]?" "$OUTPUT_DIR/affected-files.json" 2>/dev/null | grep -v "^null$" || true)
                if [ -n "$files" ]; then
                    while IFS= read -r file; do
                        if [ -n "$file" ] && [ "$file" != "null" ]; then
                            if [ -f "$file" ]; then
                                AIDER_CMD="$AIDER_CMD $file"
                                echo "✅ Added $file_type: $file"
                            else
                                echo "⚠️ $file_type file not found: $file" | tee -a "$OUTPUT_FILE"
                            fi
                        fi
                    done <<< "$files"
                else
                    echo "ℹ️ No $file_type files specified"
                fi
            }
            
            # Add files to modify (highest priority)
            add_files_from_json "to_modify" "to_modify"
            
            # Add files to create (if they already exist as templates/examples)
            add_files_from_json "to_create" "to_create"
            
            # Add reference files for context
            add_files_from_json "to_reference" "reference"
            
            # Add test files
            add_files_from_json "tests_needed" "test"
        fi
        
        echo "✅ Discovery file processing complete"
    else
        echo "ℹ️ No discovery JSON found, proceeding with manual file specification"
    fi
fi

# Add additional target files if specified
if [ -n "$TARGET_FILES" ]; then
    IFS=',' read -ra FILES <<< "$TARGET_FILES"
    for file in "${FILES[@]}"; do
        file=$(echo "$file" | xargs)  # Trim whitespace
        if [ -f "$file" ]; then
            AIDER_CMD="$AIDER_CMD $file"
        else
            echo "Warning: File '$file' not found" | tee -a "$OUTPUT_FILE"
        fi
    done
fi

# Execute and capture output
echo "=== AIDER EXECUTION START ===" | tee -a "$OUTPUT_FILE"
echo "Mode: $MODE" | tee -a "$OUTPUT_FILE"
echo "Model: $MODEL" | tee -a "$OUTPUT_FILE"
echo "Max Tokens: $MAX_TOKENS" | tee -a "$OUTPUT_FILE"
echo "Target Files: ${TARGET_FILES:-'Auto-discovered + specified'}" | tee -a "$OUTPUT_FILE"
echo "Prompt File: $PROMPT_FILE" | tee -a "$OUTPUT_FILE"
echo "Timestamp: $TIMESTAMP" | tee -a "$OUTPUT_FILE"

# Log discovered files if in coding mode and JSON exists
if [ "$MODE" = "coding" ] && [ -f "$OUTPUT_DIR/affected-files.json" ]; then
    echo "=== DISCOVERED FILES FROM JSON ===" | tee -a "$OUTPUT_FILE"
    cat "$OUTPUT_DIR/affected-files.json" | tee -a "$OUTPUT_FILE"
    echo "=== END DISCOVERED FILES ===" | tee -a "$OUTPUT_FILE"
fi

echo "=== USER REQUEST CONTENT ===" | tee -a "$OUTPUT_FILE"
cat "$USER_REQUEST_FILE" | tee -a "$OUTPUT_FILE"
echo "=== END USER REQUEST ===" | tee -a "$OUTPUT_FILE"
echo "=== AIDER RESPONSE ===" | tee -a "$OUTPUT_FILE"

# Use the specified prompt file
FINAL_CMD="$AIDER_CMD --message-file $PROMPT_FILE"
echo "=== AIDER COMMAND TO BE EXECUTED ===" | tee -a "$OUTPUT_FILE"
echo "$FINAL_CMD" | tee -a "$OUTPUT_FILE"
echo "=== END AIDER COMMAND ===" | tee -a "$OUTPUT_FILE"

# Run aider with detailed prompt file and context files
# Capture exit code and handle errors gracefully
set +e  # Don't exit on command failure
eval "$FINAL_CMD" 2>&1 | tee -a "$OUTPUT_FILE"
AIDER_EXIT_CODE=$?
set -e  # Re-enable exit on error

echo "=== AIDER EXIT CODE: $AIDER_EXIT_CODE ===" | tee -a "$OUTPUT_FILE"

if [ $AIDER_EXIT_CODE -ne 0 ]; then
    echo "⚠️ Aider exited with error code: $AIDER_EXIT_CODE" | tee -a "$OUTPUT_FILE"
    echo "This may indicate a crash or command error, but checking for outputs..." | tee -a "$OUTPUT_FILE"
else
    echo "✅ Aider completed successfully" | tee -a "$OUTPUT_FILE"
fi

echo "=== AIDER EXECUTION END ===" | tee -a "$OUTPUT_FILE"

# Output the file path and exit code for use by calling script
echo "$OUTPUT_FILE"
exit $AIDER_EXIT_CODE
