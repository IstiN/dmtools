#!/bin/bash

echo "=== Creating Expert Job Configuration ==="
curl -X POST "http://localhost:8080/api/v1/job-configurations" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d '{
  "name": "Sample Expert Analysis",
  "description": "Test Expert job configuration for analyzing tickets",
  "jobType": "Expert",
  "jobParameters": {
    "request": "Please analyze this ticket and provide recommendations",
    "inputJql": "key = DMC-123",
    "initiator": "istin2007@gmail.com",
    "systemRequest": "Act as a senior software engineer and provide technical analysis",
    "systemRequestCommentAlias": "Technical Analysis"
  },
  "integrationMappings": {
    "tracker": "jira",
    "ai": "openai",
    "wiki": "confluence"
  },
  "enabled": true
}' | jq . 