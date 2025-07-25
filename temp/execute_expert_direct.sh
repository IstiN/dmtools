#!/bin/bash

echo "=== Executing Expert Job Directly (No Saved Config) ==="
curl -X POST "http://localhost:8080/api/v1/jobs/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d '{
  "jobName": "Expert",
  "params": {
    "request": "Please analyze this ticket and provide technical recommendations",
    "inputJql": "key = DMC-123",
    "initiator": "istin2007@gmail.com",
    "systemRequest": "Act as a senior software engineer and provide technical analysis",
    "systemRequestCommentAlias": "Technical Analysis",
    "projectContext": "This is a test analysis for a technical ticket"
  },
  "requiredIntegrations": ["tracker", "ai"]
}' \
-v 