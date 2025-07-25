#!/bin/bash

echo "=== Executing New Simple Expert Job Configuration ==="
curl -X POST "http://localhost:8080/api/v1/jobs/configurations/e3c33d19-34ab-4397-b208-9090706d6236/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d '{}' \
-v 