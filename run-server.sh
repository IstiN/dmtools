#!/bin/bash
echo "🚀 Starting DMTools Server..."
echo "📁 Working directory: $(pwd)"
echo "☕ Java version: $(java -version 2>&1 | head -n 1)"
echo "🌐 Server will be available at: http://localhost:80"
echo ""

# Set environment variables
export SPRING_PROFILES_ACTIVE=local
export ENV=local
export PORT=80

# Run the server
./gradlew :dmtools-server:bootRun --console=plain -x test 