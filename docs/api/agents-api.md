# Agent & Orchestrator API Documentation

## Overview

The DMTools server provides comprehensive Agent and Orchestrator APIs for executing AI-powered tasks and complex workflows. These APIs enable direct execution of individual agents and orchestration of multi-step processes.

## Quick Access

**For complete API documentation with request/response schemas, parameter descriptions, and interactive testing:**

👉 **Visit Swagger UI: `http://localhost:8080/swagger-ui.html`**

## API Categories

### 🤖 Agent Management APIs
- Execute individual AI agents with specific parameters
- Get detailed agent information and parameter schemas
- Access 15+ specialized agents for various tasks

### 🔄 Orchestrator APIs
- Execute complex multi-step workflows
- Manage orchestration processes with comprehensive configuration
- Chain multiple agents for advanced automation

### 💬 Enhanced Chat APIs
- Chat interface with agent tool integration
- MCP (Model Context Protocol) tools support
- Real-time access to JIRA, GitHub, Confluence, and other integrations

## Available Agents

### Testing & Quality Assurance
- `TestCaseGeneratorAgent` - Generate comprehensive test cases
- `AutomationTestingGeneratorAgent` - Create automated testing strategies
- `RelatedTestCaseAgent` - Find related test scenarios

### Presentation & Content
- `PresentationContentGeneratorAgent` - Generate presentation content
- `PresentationSlideFormatterAgent` - Format presentation slides
- `ContentMergeAgent` - Merge content from multiple sources

### Business Analysis
- `BusinessAreaAssessmentAgent` - Assess business areas and processes
- `TaskProgressAgent` - Track and analyze task progress
- `SearchResultsAssessmentAgent` - Evaluate search results

### Development Support
- `SourceImpactAssessmentAgent` - Analyze code impact
- `TaskExecutionAgent` - Guide task execution
- `SnippetExtensionAgent` - Extend code snippets

## Available Orchestrators

### Workflow Management
- `ContextOrchestrator` - Manage contextual workflows
- `PresentationMakerOrchestrator` - End-to-end presentation creation
- `ConfluenceSearchOrchestrator` - Advanced Confluence search workflows
- `TrackerSearchOrchestrator` - Cross-platform ticket management

## Base URLs

```
Agents API: http://localhost:8080/api/v1/agents
Chat API: http://localhost:8080/api/v1/chat
```

## Key Features

### 🎯 Intelligent Agent Selection
- Automatic agent recommendation based on task description
- Manual agent filtering for specific workflows
- Parameter validation and schema documentation

### 🔧 MCP Tools Integration
- 50+ integration tools for JIRA, GitHub, Confluence, Bitbucket
- Real-time data access to project management systems
- Seamless integration with development workflows

### 📊 Enhanced Information API
- Detailed agent parameter descriptions
- Return type specifications
- Usage examples and best practices

## Getting Started

1. **Start the server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Explore available agents**:
   ```bash
   curl "http://localhost:8080/api/v1/agents/available?detailed=true"
   ```

3. **Access interactive documentation**:
   Visit `http://localhost:8080/swagger-ui.html` for complete API documentation

## Authentication

Authentication is handled through OAuth2 integration. See the main README.md for configuration details.

## Error Handling

All endpoints return standard HTTP status codes with descriptive error messages. Detailed error schemas are available in the Swagger documentation.

## Support

For detailed API usage, parameter schemas, and interactive testing:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **GitHub Issues**: [DMTools Issues](https://github.com/IstiN/dmtools/issues)
- **Documentation**: See main [README.md](../README.md) for configuration and setup 