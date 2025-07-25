#!/bin/bash

echo "=== Checking Confluence Integration Configuration ==="
curl -X GET "http://localhost:8080/api/integrations/61e7b2b4-eb74-4754-b025-deb73daeb7d7" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" | jq . 