#!/bin/bash

# prepare-user-request.sh - Process and validate user request
# Handles both raw text and base64 encoded requests

set -e

USER_REQUEST_RAW="$1"
OUTPUT_DIR="${2:-outputs}"

echo "🔧 Preparing user request..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Handle both raw text and base64 encoded user requests
if echo "$USER_REQUEST_RAW" | base64 -d >/dev/null 2>&1 && [ ${#USER_REQUEST_RAW} -gt 100 ]; then
    # Input appears to be base64 encoded (long string that decodes successfully)
    echo "🔍 Detected base64 encoded input, decoding..."
    echo "$USER_REQUEST_RAW" | base64 -d > "$OUTPUT_DIR/user-request.txt"
    echo "💾 User request decoded and saved to $OUTPUT_DIR/user-request.txt"
elif echo "$USER_REQUEST_RAW" | grep -qE '^[A-Za-z0-9+/]*={0,2}$' && [ ${#USER_REQUEST_RAW} -gt 50 ]; then
    # Alternative base64 detection for different input formats
    echo "🔍 Detected possible base64 encoded request, attempting decode..."
    if USER_REQUEST_DECODED=$(echo "$USER_REQUEST_RAW" | base64 -d 2>/dev/null); then
        echo "$USER_REQUEST_DECODED" > "$OUTPUT_DIR/user-request.txt"
        echo "💾 User request decoded and saved to $OUTPUT_DIR/user-request.txt"
    else
        echo "🔍 Base64 decode failed, treating as plain text"
        echo "$USER_REQUEST_RAW" > "$OUTPUT_DIR/user-request.txt"
        echo "💾 User request saved as plain text to $OUTPUT_DIR/user-request.txt"
    fi
else
    # Input appears to be raw text
    echo "🔍 Detected raw text input, saving directly..."
    echo "$USER_REQUEST_RAW" > "$OUTPUT_DIR/user-request.txt"
    echo "💾 User request saved to $OUTPUT_DIR/user-request.txt"
fi

# Print the actual user request content for debugging  
echo "🔍 USER REQUEST CONTENT:"
cat "$OUTPUT_DIR/user-request.txt"
echo "🔍 END USER REQUEST"

# Validate that we have a ticket number (DMC-XXX format)
if ! grep -q "DMC-[0-9]\+" "$OUTPUT_DIR/user-request.txt"; then
    echo "⚠️ WARNING: No DMC ticket number found in user request"
    echo "Please ensure the request includes a DMC-XXX ticket number"
fi

echo "✅ User request preparation complete"
