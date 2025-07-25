#!/bin/bash

echo "=== Testing Expert Job with attachResponseAsFile Parameter ==="

# Wait for server to be ready
echo "Waiting for server to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "Server is ready!"
        break
    fi
    sleep 2
done

# Test 1: Expert job with attachResponseAsFile=true (default behavior)
echo "=== Test 1: Expert Job with file attachments enabled ==="
curl -X POST "http://localhost:8080/api/v1/jobs/execute" \
-H "Content-Type: application/json" \
-d '{
  "jobName": "Expert",
  "params": {
    "request": "Please analyze this ticket and provide technical recommendations",
    "inputJql": "key = DMC-123",
    "initiator": "test@example.com",
    "systemRequest": "Act as a senior software engineer",
    "systemRequestCommentAlias": "Technical Analysis",
    "attachResponseAsFile": true
  },
  "requiredIntegrations": ["tracker", "ai"]
}' || echo "Test 1 failed - this might be expected if DMC-123 doesn't exist"

echo -e "\n\n"

# Test 2: Expert job with attachResponseAsFile=false (no file attachments)
echo "=== Test 2: Expert Job with file attachments disabled ==="
curl -X POST "http://localhost:8080/api/v1/jobs/execute" \
-H "Content-Type: application/json" \
-d '{
  "jobName": "Expert",
  "params": {
    "request": "Please analyze this ticket and provide technical recommendations",
    "inputJql": "key = DMC-123",
    "initiator": "test@example.com",
    "systemRequest": "Act as a senior software engineer",
    "systemRequestCommentAlias": "Technical Analysis",
    "attachResponseAsFile": false
  },
  "requiredIntegrations": ["tracker", "ai"]
}' || echo "Test 2 failed - this might be expected if DMC-123 doesn't exist"

echo -e "\n\n"

# Test 3: Check job configuration endpoint includes new parameter
echo "=== Test 3: Verify Expert job configuration includes attachResponseAsFile parameter ==="
curl -s "http://localhost:8080/api/v1/jobs/types/Expert" | grep -A 10 -B 2 "attachResponseAsFile" || echo "Parameter not found in configuration"

echo -e "\n\n=== Testing completed ===" 