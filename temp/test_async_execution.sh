#!/bin/bash

echo "=== Testing Async Job Execution API ==="

# First, let's get a fresh authentication token
echo "Getting authentication token..."
AUTH_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "istin2007@gmail.com", "password": "your_password"}')

TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Failed to get authentication token"
  echo "Response: $AUTH_RESPONSE"
  exit 1
fi

echo "✅ Got authentication token"

# Test async job execution
echo ""
echo "Testing async job execution..."
EXECUTION_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/jobs/execute" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "Expert",
    "params": {
      "request": "Test async execution",
      "inputJql": "key = TEST-123",
      "initiator": "test@example.com"
    },
    "requiredIntegrations": ["tracker", "ai"]
  }')

echo "Execution Response:"
echo "$EXECUTION_RESPONSE" | jq '.' 2>/dev/null || echo "$EXECUTION_RESPONSE"

# Extract execution ID if present
EXECUTION_ID=$(echo "$EXECUTION_RESPONSE" | jq -r '.executionId // empty' 2>/dev/null)

if [ ! -z "$EXECUTION_ID" ]; then
  echo ""
  echo "✅ Got execution ID: $EXECUTION_ID"
  echo ""
  echo "Testing execution status endpoint..."
  
  # Wait a moment
  sleep 2
  
  # Check execution status
  STATUS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/jobs/executions/$EXECUTION_ID/status" \
    -H "Authorization: Bearer $TOKEN")
  
  echo "Status Response:"
  echo "$STATUS_RESPONSE" | jq '.' 2>/dev/null || echo "$STATUS_RESPONSE"
else
  echo "❌ No execution ID found in response"
fi

echo ""
echo "=== Test Complete ===" 