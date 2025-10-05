# DMTools Agents API - Quick Examples

## Overview

This document provides quick examples for using the DMTools Agents API. For complete documentation, schemas, and interactive testing, use the Swagger UI.

## Quick Access

**For complete API documentation with request/response schemas and interactive testing:**

👉 **Visit Swagger UI: `http://localhost:8080/swagger-ui.html`**

## Getting Started

1. **Start the server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Check available agents**:
   ```bash
   curl "http://localhost:8080/api/v1/agents/available"
   ```

3. **Get detailed agent information**:
   ```bash
   curl "http://localhost:8080/api/v1/agents/available?detailed=true"
   ```

## Quick Examples

### Test Case Generation
```bash
# Get agent information
curl "http://localhost:8080/api/v1/agents/agents/TestCaseGeneratorAgent/info"

# Execute agent (use Swagger UI for detailed request schema)
curl -X POST "http://localhost:8080/api/v1/agents/execute" \
  -H "Content-Type: application/json" \
  -d '{"agentName": "TestCaseGeneratorAgent", "parameters": {...}}'
```

### Presentation Creation
```bash
# Execute presentation orchestrator
curl -X POST "http://localhost:8080/api/v1/agents/orchestrators/execute" \
  -H "Content-Type: application/json" \
  -d '{"agentName": "PresentationMakerOrchestrator", "parameters": {...}}'
```

### Business Analysis
```bash
# Business area assessment
curl -X POST "http://localhost:8080/api/v1/agents/execute" \
  -H "Content-Type: application/json" \
  -d '{"agentName": "BusinessAreaAssessmentAgent", "parameters": {...}}'
```

## Agent Categories

### 🧪 Testing Agents
- `TestCaseGeneratorAgent` - Generate comprehensive test cases
- `AutomationTestingGeneratorAgent` - Create automation test scripts
- `RelatedTestCaseAgent` - Find related test scenarios

### 📊 Presentation Agents
- `PresentationContentGeneratorAgent` - Generate presentation content
- `PresentationSlideFormatterAgent` - Format presentation slides

### 🔍 Analysis Agents
- `BusinessAreaAssessmentAgent` - Assess business areas
- `SourceImpactAssessmentAgent` - Analyze code impact
- `SearchResultsAssessmentAgent` - Evaluate search results

### 🔧 Utility Agents
- `ContentMergeAgent` - Merge content from multiple sources
- `SummaryContextAgent` - Create contextual summaries
- `KeywordGeneratorAgent` - Generate relevant keywords

## Orchestrators

### 📈 Workflow Orchestrators
- `PresentationMakerOrchestrator` - End-to-end presentation creation
- `ConfluenceSearchOrchestrator` - Advanced Confluence workflows
- `TrackerSearchOrchestrator` - Cross-platform issue tracking

## Interactive Testing

For detailed parameter schemas, validation rules, and interactive API testing:

1. **Visit Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **Navigate to Agents section**: Find all agent endpoints
3. **Try it out**: Use the interactive forms to test API calls
4. **View schemas**: See detailed request/response structures

## Support

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` - Complete API documentation
- **GitHub Issues**: [DMTools Issues](https://github.com/IstiN/dmtools/issues)
- **Main Documentation**: [README.md](../README.md) - Setup and configuration 