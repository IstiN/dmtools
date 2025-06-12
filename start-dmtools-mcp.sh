#!/bin/bash

# DMTools MCP Server Startup Script
# This script starts the DMTools server with MCP support for Cursor and other LLM tools

echo "🚀 Starting DMTools MCP Server..."
echo "📍 Server will be available at: http://localhost:8080"
echo "🔧 MCP Endpoint: http://localhost:8080/mcp/"
echo "📚 Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""

# Check if config files exist
if [ ! -f "src/main/resources/config.properties" ]; then
    echo "⚠️  Warning: config.properties not found. Please configure your integrations first."
    echo "   Copy config.properties.example to config.properties and edit it."
    echo ""
fi

# Build and run the application
echo "🔨 Building application..."
./gradlew build -x test

if [ $? -eq 0 ]; then
    echo "✅ Build successful! Starting server..."
    echo ""
    echo "🔗 To use with Cursor, add this to your mcp.json:"
    echo '{'
    echo '  "mcpServers": {'
    echo '    "dmtools": {'
    echo '      "url": "http://localhost:8080/mcp/"'
    echo '    }'
    echo '  }'
    echo '}'
    echo ""
    echo "🛑 Press Ctrl+C to stop the server"
    echo ""
    
    # Start the server
    ./gradlew bootRun
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi 