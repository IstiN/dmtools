#!/bin/bash

echo "=== Executing Expert Job Configuration ==="
curl -X POST "http://localhost:8080/api/v1/jobs/configurations/6a6727db-f767-482c-a1ed-46ccd2bb3d71/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d "{}" \
-v 