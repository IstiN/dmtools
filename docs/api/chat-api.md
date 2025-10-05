# Chat API Documentation - MCP Tools Integration

## Overview

The DMTools Chat API provides AI-powered conversational interfaces with integrated access to development tools, project management systems, and specialized AI agents through MCP (Model Context Protocol) tools.

## Quick Access

**For complete API documentation with request/response schemas, parameter descriptions, and interactive testing:**

👉 **Visit Swagger UI: `http://localhost:8080/swagger-ui.html`**

## Key Features

### 🛠️ MCP Tools Integration
- **50+ Integration Tools**: Direct access to JIRA, GitHub, Confluence, Bitbucket, GitLab
- **AI Agents**: Test case generation, presentation creation, business assessment
- **Orchestrators**: Complex workflow automation and multi-step processes
- **Real-time Data**: Live access to your project management and development tools

### 🎯 Smart Tool Selection
- **Auto-Detection**: AI automatically selects appropriate tools based on user requests
- **Manual Filtering**: Specify which agents or orchestrators to use
- **Fallback Support**: Graceful degradation to basic chat if tools fail

### 📎 File Attachment Support
- Support for multiple file types (PDF, Word, Excel, images)
- Intelligent file analysis and content extraction
- Integration with AI responses for comprehensive insights

## Base URL

```
http://localhost:8080/api/v1/chat
```

## API Endpoints

### Chat Completions
- `POST /completions` - Text-based chat with optional MCP tools
- `POST /completions-with-files` - Chat with file attachment support
- `POST /simple` - Quick single-message chat
- `GET /health` - Service health check

## Available MCP Tools

### 🎫 Project Management
- JIRA ticket management (search, create, update, comment)
- Issue tracking and status management
- Project reporting and analytics

### 🐙 Source Control
- GitHub repository management (PRs, commits, files)
- Bitbucket integration
- GitLab support
- Code review and analysis

### 📄 Documentation
- Confluence page management (search, create, update)
- Documentation generation and updates
- Knowledge base integration

### 🤖 AI Agents & Orchestrators
- Test case generation and automation
- Presentation creation and formatting
- Business analysis and assessment
- Content summarization and analysis

## Getting Started

1. **Start the server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Test basic chat**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/chat/simple \
     -d "message=Hello, how can you help me?"
   ```

3. **Access interactive documentation**:
   Visit `http://localhost:8080/swagger-ui.html` for complete API documentation

## Frontend Integration

### JavaScript Chat Component

DMTools provides a ready-to-use chat component with MCP tools support:

```javascript
const chat = DMChatComponent.createEmbedded('chat-container', {
    title: 'DMTools AI Assistant',
    enableMcpTools: true,
    welcomeMessage: 'Hi! I can access your JIRA, GitHub, and Confluence data.'
});
```

### Example User Interactions

**Project Management:**
- "Show me all high-priority tickets assigned to my team"
- "Create a weekly status report from recent JIRA activity"
- "What pull requests need my review?"

**Documentation & Analysis:**
- "Create a Confluence page documenting our API endpoints"
- "Analyze this code file and suggest improvements"
- "Generate test cases for the user authentication feature"

## Authentication

Authentication is handled through OAuth2 integration. See the main README.md for configuration details.

## Error Handling

All endpoints return standard HTTP status codes with descriptive error messages. Detailed error schemas are available in the Swagger documentation.

## Support

For detailed API usage, parameter schemas, and interactive testing:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **GitHub Issues**: [DMTools Issues](https://github.com/IstiN/dmtools/issues)
- **Documentation**: See main [README.md](README.md) for configuration and setup 