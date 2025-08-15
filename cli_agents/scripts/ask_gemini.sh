#!/bin/bash

# ask_gemini.sh - Direct Gemini API call without CLI
# Based on geminiChatViaJs.js logic

set -e

# Parameters
PROMPT_TEXT="$1"
MODEL="${2:-gemini-2.5-flash-preview-05-20}"
OUTPUT_FILE="${3:-response.json}"

# Validate API key
if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ ERROR: GEMINI_API_KEY environment variable is not set"
    exit 1
fi

echo "🚀 Calling Gemini API directly..."
echo "📋 Model: $MODEL"
echo "📏 Prompt length: ${#PROMPT_TEXT} characters"
echo "📄 Output file: $OUTPUT_FILE"

# Prepare API URL
API_URL="https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${GEMINI_API_KEY}"

# Escape the prompt text for JSON
ESCAPED_PROMPT=$(echo "$PROMPT_TEXT" | jq -Rs .)

# Prepare request payload
REQUEST_PAYLOAD=$(cat << EOF
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": $ESCAPED_PROMPT
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "topK": 40,
    "topP": 0.95,
    "maxOutputTokens": 8192
  }
}
EOF
)

echo "🔍 Request URL: ${API_URL:0:100}..."
echo "📏 Request payload size: ${#REQUEST_PAYLOAD} bytes"

# Make API call
echo "🚀 Executing API request..."
if RESPONSE=$(curl -s -X POST "$API_URL" \
    -H "Content-Type: application/json" \
    -d "$REQUEST_PAYLOAD"); then
    
    echo "✅ API request successful"
    echo "📏 Response length: ${#RESPONSE} characters"
    
    # Check for API errors
    if echo "$RESPONSE" | jq -e '.error' >/dev/null 2>&1; then
        echo "❌ API Error:"
        echo "$RESPONSE" | jq '.error'
        exit 1
    fi
    
    # Extract text content
    if EXTRACTED_TEXT=$(echo "$RESPONSE" | jq -r '.candidates[0].content.parts[0].text' 2>/dev/null); then
        if [ "$EXTRACTED_TEXT" != "null" ] && [ -n "$EXTRACTED_TEXT" ]; then
            echo "✅ Successfully extracted text content"
            echo "📏 Extracted text length: ${#EXTRACTED_TEXT} characters"
            
            # Save response to file
            echo "$EXTRACTED_TEXT" > "$OUTPUT_FILE"
            echo "📄 Response saved to: $OUTPUT_FILE"
            
            # Also save full JSON response for debugging
            echo "$RESPONSE" > "${OUTPUT_FILE}.full.json"
            echo "📄 Full response saved to: ${OUTPUT_FILE}.full.json"
            
            exit 0
        else
            echo "❌ Could not extract text content from response"
            echo "📋 Response structure:"
            echo "$RESPONSE" | jq '.' || echo "$RESPONSE"
            exit 1
        fi
    else
        echo "❌ Failed to parse response"
        echo "📋 Raw response:"
        echo "$RESPONSE"
        exit 1
    fi
else
    echo "❌ API request failed"
    exit 1
fi
