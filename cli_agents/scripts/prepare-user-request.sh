#!/bin/bash

# prepare-user-request.sh - Process and validate user request
# Handles both raw text and base64 encoded requests

set -e

USER_REQUEST_RAW="$1"
OUTPUT_DIR="${2:-outputs}"

echo "🔧 Preparing user request..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Handle GZIP compressed, base64 encoded, and raw text requests
if [[ "$USER_REQUEST_RAW" == GZIP_COMPRESSED:* ]]; then
    # Handle GZIP compressed requests (from GitHub webhook large payload processing)
    echo "🗜️ Detected GZIP compressed request, decompressing..."
    
    # Extract the base64 compressed data (remove GZIP_COMPRESSED: prefix)
    COMPRESSED_DATA="${USER_REQUEST_RAW#GZIP_COMPRESSED:}"
    
    # Check if we have gzip available, install if needed in CI environment
    if ! command -v gzip >/dev/null 2>&1; then
        echo "🔧 Installing gzip for decompression..."
        if command -v apt-get >/dev/null 2>&1; then
            sudo apt-get update -qq && sudo apt-get install -y gzip
        elif command -v brew >/dev/null 2>&1; then
            brew install gzip
        fi
    fi
    
    # Decode base64 and decompress using gzip
    if echo "$COMPRESSED_DATA" | base64 -d | gunzip > "$OUTPUT_DIR/user-request.txt" 2>/dev/null; then
        echo "✅ Request decompressed successfully"
        DECOMPRESSED_SIZE=$(wc -c < "$OUTPUT_DIR/user-request.txt")
        COMPRESSED_SIZE=${#COMPRESSED_DATA}
        echo "📊 Compression ratio: $COMPRESSED_SIZE chars → $DECOMPRESSED_SIZE chars"
    else
        echo "❌ ERROR: Failed to decompress GZIP data"
        echo "Falling back to treating as raw text..."
        echo "$USER_REQUEST_RAW" > "$OUTPUT_DIR/user-request.txt"
    fi
    
elif echo "$USER_REQUEST_RAW" | grep -qE '^[A-Za-z0-9+/]*={0,2}$' && [ ${#USER_REQUEST_RAW} -gt 20 ]; then
    # Check if input appears to be base64 encoded (valid base64 chars and reasonable length)
    echo "🔍 Detected possible base64 encoded request, attempting decode..."
    if USER_REQUEST_DECODED=$(echo "$USER_REQUEST_RAW" | base64 -d 2>/dev/null) && [ -n "$USER_REQUEST_DECODED" ]; then
        # Successful decode and non-empty result
        echo "✅ Base64 decoded successfully"
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
