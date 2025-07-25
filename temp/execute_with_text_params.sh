#!/bin/bash

echo "=== Executing Expert Job with Text Parameters (No Confluence URLs) ==="
curl -X POST "http://localhost:8080/api/v1/jobs/configurations/6a6727db-f767-482c-a1ed-46ccd2bb3d71/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d '{
  "parameterOverrides": {
    "request": "Make acceptance criteria for this ticket",
    "inputJql": "key in (DMC-61)",
    "initiator": "istin2007@gmail.com",
    "systemRequest": "Act as a business analyst and create detailed acceptance criteria for this ticket. Focus on clear, testable conditions that define when the feature is complete.",
    "systemRequestCommentAlias": "Acceptance Criteria",
    "projectContext": "This is a software development project focused on improving user experience and system functionality."
  },
  "integrationOverrides": {
    "TrackerClient": "23435689-6f51-4a9b-b8c4-9848a6ede41f",
    "AI": "9e04bca1-a8f7-44c5-8f00-d6e208e17384"
  },
  "executionMode": "SERVER_MANAGED"
}' \
-v 