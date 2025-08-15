#!/bin/bash

# extract-response.sh - Extract structured response from CLI tool output
# Supports both AIDER_RESPONSE and RESPONSE tags

set -e

OUTPUT_FILE="$1"
RESPONSE_TYPE="${2:-RESPONSE}"  # RESPONSE or AIDER_RESPONSE
EXIT_CODE="${3:-0}"

echo "🔍 Extracting response from output file: $OUTPUT_FILE"
echo "🏷️ Looking for response type: $RESPONSE_TYPE"

# Function to extract response content
extract_response() {
    local tag_name="$1"
    local output_file="$2"
    
    if grep -q "<$tag_name>" "$output_file"; then
        echo "✅ Found $tag_name tags in output"
        
        # Use awk to extract only the LAST response block to avoid duplicates
        awk -v tag="$tag_name" '
            BEGIN { 
                start_tag = "<" tag ">"
                end_tag = "</" tag ">"
            }
            $0 ~ start_tag { 
                in_response = 1; 
                response = ""; 
                next 
            }
            $0 ~ end_tag { 
                in_response = 0; 
                final_response = response; 
                next 
            }
            in_response { 
                response = response $0 "\n" 
            }
            END { 
                if (final_response != "") print final_response; 
                else print response 
            }
        ' "$output_file"
        return 0
    else
        return 1
    fi
}

# Try to extract response with the specified tag first
EXTRACTED_RESPONSE=""
if EXTRACTED_RESPONSE=$(extract_response "$RESPONSE_TYPE" "$OUTPUT_FILE"); then
    RESPONSE_SIZE=$(echo "$EXTRACTED_RESPONSE" | wc -c)
    echo "✅ Response extracted successfully (size: $RESPONSE_SIZE bytes)"
    
    # Output the response
    echo "$EXTRACTED_RESPONSE"
    
    # Show preview for verification in logs
    echo "🔍 First 5 lines of response:" >&2
    echo "$EXTRACTED_RESPONSE" | head -5 >&2
    echo "🔍 Last 3 lines of response:" >&2
    echo "$EXTRACTED_RESPONSE" | tail -3 >&2
    
    exit 0
fi

# Try alternative response tag if the first one failed
ALT_RESPONSE_TYPE="AIDER_RESPONSE"
if [ "$RESPONSE_TYPE" != "$ALT_RESPONSE_TYPE" ]; then
    echo "🔄 Trying alternative response tag: $ALT_RESPONSE_TYPE" >&2
    if EXTRACTED_RESPONSE=$(extract_response "$ALT_RESPONSE_TYPE" "$OUTPUT_FILE"); then
        RESPONSE_SIZE=$(echo "$EXTRACTED_RESPONSE" | wc -c)
        echo "✅ Response extracted with alternative tag (size: $RESPONSE_SIZE bytes)" >&2
        echo "$EXTRACTED_RESPONSE"
        exit 0
    fi
fi

echo "❌ No response tags found in output" >&2

# Check if CLI tool crashed and provide fallback response
if [ "$EXIT_CODE" -ne 0 ]; then
    echo "⚠️ CLI tool crashed (exit code: $EXIT_CODE), creating fallback response" >&2
    
    cat << EOF
# CLI Tool Execution Error

**Status**: CLI execution failed with exit code $EXIT_CODE

**Issue**: This appears to be a crash in the CLI tool, possibly related to command usage or model limitations.

**Recommendation**:
- Check the execution logs for specific error details
- Review the user request for problematic commands
- Consider simplifying the request or breaking it into smaller tasks

**Next Steps**:
- Check if any files were modified despite the crash
- Manual implementation may be required if CLI cannot handle this request
EOF
    exit 0
fi

# Show what we have for debugging
echo "📁 Output file search results:" >&2
grep -n "RESPONSE\|ERROR\|Exception\|Traceback" "$OUTPUT_FILE" || echo "No relevant tags found" >&2

echo "ERROR: No valid response found in output" >&2
exit 1
