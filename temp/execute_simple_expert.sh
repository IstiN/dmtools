#!/bin/bash

echo "=== Executing Expert Job with Simplified Parameters (No Confluence) ==="
curl -X POST "http://localhost:8080/api/v1/jobs/configurations/6a6727db-f767-482c-a1ed-46ccd2bb3d71/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d '{
  "parameterOverrides": {
    "request": "Please analyze this ticket and provide technical recommendations",
    "inputJql": "key = DMC-123",
    "initiator": "istin2007@gmail.com",
    "systemRequest": "Act as a senior software engineer and provide technical analysis",
    "systemRequestCommentAlias": "Technical Analysis",
    "projectContext": "This is a test analysis for a technical ticket"
  },
  "integrationOverrides": {
    "tracker": "23435689-6f51-4a9b-b8c4-9848a6ede41f",
    "ai": "9e04bca1-a8f7-44c5-8f00-d6e208e17384"
  },
  "executionMode": "SERVER_MANAGED"
}' \
-v 