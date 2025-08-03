#!/bin/bash

# Test script to verify DMC-371 bug fix
# This script tests PUT/GET operations for job configurations with integrationMappings

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Bearer test-token"

echo "🔧 Testing DMC-371 Fix: Job Configuration integrationMappings persistence"
echo "=================================================================="

# Create a job configuration with integrationMappings
echo -e "\n1. Creating job configuration with integrationMappings..."
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/job-configurations" \
  -H "Content-Type: application/json" \
  -H "${AUTH_HEADER}" \
  -d '{
    "name": "Test Integration Mappings Fix",
    "description": "Testing DMC-371 fix",
    "jobType": "Expert",
    "jobParameters": {
      "question": "Test question"
    },
    "integrationMappings": {
      "TrackerClient": "9decb6c0-1212-4a38-bde6-21b2eabb40a8",
      "AI": "570130c7-5ef7-494a-97ef-434fa7811fe2",
      "Documentation": "64adde21-a2c9-4e58-90c2-393bf4a86221"
    },
    "enabled": true
  }')

echo "Create response: $CREATE_RESPONSE"

# Extract the job configuration ID
JOB_CONFIG_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JOB_CONFIG_ID" ]; then
    echo "❌ Failed to create job configuration. Response: $CREATE_RESPONSE"
    exit 1
fi

echo "✅ Created job configuration with ID: $JOB_CONFIG_ID"

# Retrieve the job configuration
echo -e "\n2. Retrieving job configuration..."
GET_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/job-configurations/${JOB_CONFIG_ID}" \
  -H "${AUTH_HEADER}")

echo "Get response: $GET_RESPONSE"

# Check if integrationMappings are properly returned
echo -e "\n3. Verifying integrationMappings..."
INTEGRATION_MAPPINGS=$(echo "$GET_RESPONSE" | grep -o '"integrationMappings":{[^}]*}')

if [ -z "$INTEGRATION_MAPPINGS" ]; then
    echo "❌ integrationMappings not found in response!"
    exit 1
fi

if [[ "$INTEGRATION_MAPPINGS" == *"TrackerClient"* ]] && [[ "$INTEGRATION_MAPPINGS" == *"AI"* ]] && [[ "$INTEGRATION_MAPPINGS" == *"Documentation"* ]]; then
    echo "✅ integrationMappings correctly persisted and retrieved!"
    echo "Found: $INTEGRATION_MAPPINGS"
else
    echo "❌ integrationMappings content is incorrect!"
    echo "Found: $INTEGRATION_MAPPINGS"
    exit 1
fi

# Test update operation
echo -e "\n4. Testing UPDATE operation..."
UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/v1/job-configurations/${JOB_CONFIG_ID}" \
  -H "Content-Type: application/json" \
  -H "${AUTH_HEADER}" \
  -d '{
    "name": "Updated Test Config", 
    "integrationMappings": {
      "TrackerClient": "updated-tracker-id",
      "AI": "updated-ai-id"
    }
  }')

echo "Update response: $UPDATE_RESPONSE"

# Retrieve again to verify update
echo -e "\n5. Verifying UPDATE worked..."
GET_AFTER_UPDATE=$(curl -s -X GET "${BASE_URL}/api/v1/job-configurations/${JOB_CONFIG_ID}" \
  -H "${AUTH_HEADER}")

echo "Get after update: $GET_AFTER_UPDATE"

UPDATED_MAPPINGS=$(echo "$GET_AFTER_UPDATE" | grep -o '"integrationMappings":{[^}]*}')

if [[ "$UPDATED_MAPPINGS" == *"updated-tracker-id"* ]] && [[ "$UPDATED_MAPPINGS" == *"updated-ai-id"* ]]; then
    echo "✅ UPDATE operation successfully persisted integrationMappings!"
    echo "Found: $UPDATED_MAPPINGS"
else
    echo "❌ UPDATE operation failed to persist integrationMappings!"
    echo "Found: $UPDATED_MAPPINGS"
    exit 1
fi

# Cleanup
echo -e "\n6. Cleaning up..."
curl -s -X DELETE "${BASE_URL}/api/v1/job-configurations/${JOB_CONFIG_ID}" \
  -H "${AUTH_HEADER}" > /dev/null

echo "✅ Cleanup completed"

echo -e "\n🎉 DMC-371 Bug Fix Verification: PASSED"
echo "integrationMappings are now properly persisted and retrieved!"