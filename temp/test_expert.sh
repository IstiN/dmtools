#!/bin/bash

curl -X POST "http://localhost:8080/api/v1/jobs/configurations/da2c671f-aadc-4877-836b-5440c797205d/execute" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzEyMzQ1MywiZXhwIjoxNzUzMjA5ODUzfQ.RePCh799UT3e5DHRqHO6NM-YEVKubAJ4Pf5b5xyFihw" \
-H "Content-Type: application/json" \
-d "{}" \
-v 