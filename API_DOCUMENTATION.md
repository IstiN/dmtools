# DMTools Server API Documentation

## Overview

DMTools Server provides REST APIs for presentation generation, job execution, and development management tools. The server includes comprehensive OpenAPI/Swagger documentation accessible via web interface.

## Quick Start

1. **Start the server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Access API documentation**:
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html`
   - **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

## API Categories

### 🔧 Job Management API
Execute jobs with various parameters for automation tasks.

### 📊 Presentation Management API  
Create and manage presentations with dynamic content generation.

### 🤖 Agent & Orchestrator APIs
Access AI-powered agents and orchestration workflows.

### 💬 Chat API
AI-powered chat with MCP tools integration for real-time data access.

### 🔐 Authentication API
OAuth2 and security management endpoints.

## Base URLs

- **Local Development**: `http://localhost:8080`
- **Production**: Configure in `application.properties`

## Using the API

For detailed API documentation including:
- Request/response schemas
- Parameter descriptions
- Example requests
- Error codes and responses
- Authentication requirements

**Visit the Swagger UI at: `http://localhost:8080/swagger-ui.html`**

## Error Handling

All endpoints return appropriate HTTP status codes:
- **200 OK**: Successful operation
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **500 Internal Server Error**: Server-side errors

## Configuration

Server configuration is managed through `application.properties`. See the main README.md for detailed configuration options.

## Support

For issues or questions:
- Check the [GitHub Issues](https://github.com/IstiN/dmtools/issues)
- Review the [main documentation](README.md)
- Use the Swagger UI for interactive API exploration 