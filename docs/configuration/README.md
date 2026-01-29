# Configuration Guides

Detailed configuration guides for DMTools components.

## Overview

This directory contains in-depth configuration guides for specific integrations. For a complete overview of all configuration options, see [Getting Started > Configuration](../getting-started/configuration.md).

---

## Available Guides

### 🤖 [AI Providers](ai-providers.md)

Configure AI models for test case generation and automation.

**Covers:**
- Google Gemini (recommended)
- OpenAI (GPT-4)
- Claude (Anthropic)
- EPAM DIAL (enterprise)
- AWS Bedrock
- Ollama (local)

**Required for:** TestCasesGenerator, AI-powered jobs

---

### 🎫 [Tracker Configuration](tracker-configuration.md)

Configure issue tracking systems for reading stories and creating test cases.

**Covers:**
- Jira Cloud (recommended)
- Jira Server/Data Center
- Jira + Xray (test management)
- Azure DevOps (ADO)
- Rally

**Required for:** TestCasesGenerator, all automation jobs

---

### 📚 [Confluence Configuration](confluence-configuration.md)

Configure Confluence for storing rules, examples, and documentation.

**Covers:**
- Confluence Cloud setup
- Confluence Server/Data Center
- Storing test case generation rules
- Documenting custom fields
- Test case examples and templates
- Using Confluence pages in jobs

**Optional but recommended for:** TestCasesGenerator, documentation jobs

---

## Quick Start

### 1. Minimum Configuration

To use TestCasesGenerator, you need **at minimum**:

✅ **One AI Provider** → [AI Providers Guide](ai-providers.md)
✅ **One Tracker** → [Tracker Configuration Guide](tracker-configuration.md)

### 2. Recommended Configuration

For best results, also configure:

🔧 **Confluence** → [Confluence Configuration Guide](confluence-configuration.md)

---

## Configuration Flow

```
1. AI Provider (Required)
   ├─→ Gemini (easiest, free tier)
   ├─→ OpenAI (high quality)
   └─→ Claude (long context)

2. Tracker (Required)
   ├─→ Jira Cloud (most common)
   ├─→ Jira + Xray (test management)
   ├─→ Rally
   └─→ Azure DevOps (Microsoft stack)

3. Confluence (Recommended)
   └─→ Store rules and examples
```

---

## Configuration Files

DMTools uses environment variables loaded from `.env` files:

### File Precedence

1. `dmtools.env` (current directory) ← **Highest priority**
2. `dmtools-local.env` (current directory)
3. `dmtools.env` (script directory)
4. `dmtools-local.env` (script directory) ← **Lowest priority**

### Example Structure

```bash
# dmtools.env - Shared team configuration

# AI Provider
GEMINI_API_KEY=AIza...
GEMINI_DEFAULT_MODEL=gemini-1.5-pro

# Jira
JIRA_BASE_PATH=https://company.atlassian.net
JIRA_EMAIL=team@company.com
JIRA_API_TOKEN=ATATT...

# Confluence (optional)
CONFLUENCE_BASE_PATH=https://company.atlassian.net/wiki
CONFLUENCE_DEFAULT_SPACE=QA
```

**Security:** Always secure your environment files:
```bash
chmod 600 dmtools.env
echo "*.env" >> .gitignore
```

---

## Testing Your Configuration

After configuration, verify everything works:

```bash
# Test AI Provider
dmtools gemini_ai_chat "Test message"

# Test Tracker
dmtools jira_get_ticket YOUR-123

# Test Confluence (optional)
dmtools confluence_content_by_title "Test Page"
```

✅ **All commands should return successful responses.**

---

## Common Configuration Patterns

### Pattern 1: Jira + Gemini (Simple)

**Use Case:** Small team, getting started quickly

```bash
# AI
GEMINI_API_KEY=AIza...

# Jira
JIRA_BASE_PATH=https://company.atlassian.net
JIRA_EMAIL=user@company.com
JIRA_API_TOKEN=ATATT...
```

**Job Config:**
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key = PROJ-123",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "testCasesPriorities": "High, Medium, Low",
    "outputType": "creation"
  }
}
```

---

### Pattern 2: Jira + Xray + Gemini + Confluence (Advanced)

**Use Case:** Professional QA team with test management

```bash
# AI
GEMINI_API_KEY=AIza...
GEMINI_DEFAULT_MODEL=gemini-1.5-pro

# Jira + Xray
JIRA_BASE_PATH=https://company.atlassian.net
JIRA_EMAIL=user@company.com
JIRA_API_TOKEN=ATATT...

# Xray Optimization
XRAY_PARALLEL_FETCH_ENABLED=true
XRAY_PARALLEL_THREADS=2

# Confluence
CONFLUENCE_BASE_PATH=https://company.atlassian.net/wiki
CONFLUENCE_DEFAULT_SPACE=QA
```

**Job Config:**
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "project = PROJ AND type = Story",
    "existingTestCasesJql": "project = PROJ AND issueType = 'Test'",
    "testCaseIssueType": "Test",
    "testCasesPriorities": "High, Medium, Low",
    "testCasesCustomFields": ["preconditions", "testSteps"],
    "confluencePages": [
      "https://company.atlassian.net/wiki/spaces/QA/pages/123/Rules"
    ],
    "customFieldsRules": "https://company.atlassian.net/wiki/spaces/QA/pages/456/Fields",
    "preprocessJSAction": "agents/js/preprocessXrayTestCases.js",
    "outputType": "creation"
  }
}
```

---

### Pattern 3: ADO + OpenAI (Enterprise)

**Use Case:** Microsoft-centric organization

```bash
# AI
OPEN_AI_API_KEY=sk-...
OPEN_AI_MODEL=gpt-4o

# Azure DevOps
ADO_BASE_PATH=https://dev.azure.com/company
ADO_PROJECT=MyProject
ADO_TOKEN=ado-pat-token
```

**Job Config:**
```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "SELECT [System.Id] FROM WorkItems WHERE [System.State] = 'Active'",
    "existingTestCasesJql": "SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'Test Case'",
    "testCaseIssueType": "Test Case",
    "testCasesPriorities": "1, 2, 3, 4",
    "modelTestCasesCreation": "gpt-4o",
    "outputType": "creation"
  }
}
```

---

## Troubleshooting

### Environment Variables Not Loading

```bash
# Check file exists
ls -la dmtools.env

# Check permissions
chmod 600 dmtools.env

# Check format (no spaces around =)
cat dmtools.env | grep "="
```

### Authentication Failures

**Jira/Confluence:**
```bash
curl -u "your-email@company.com:YOUR_TOKEN" \
  https://your-company.atlassian.net/rest/api/3/myself
```

**Expected:** Your user profile in JSON

**ADO:**
```bash
curl -u ":YOUR_TOKEN" \
  https://dev.azure.com/{org}/_apis/projects?api-version=7.0
```

**Expected:** List of projects in JSON

### API Key Issues

**Gemini:**
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"
```

**Expected:** List of available models

---

## Next Steps

✅ **Configuration complete!** Now you can:

1. **[Run TestCasesGenerator](../jobs/TestCasesGenerator.md)** - Generate test cases
2. **[Explore MCP Tools](../README-MCP.md)** - 67+ available tools
3. **[JavaScript Actions](../ai-teammate/javascript-actions.md)** - Advanced automation

---

## See Also

- **[Complete Configuration](../getting-started/configuration.md)** - All configuration options
- **[Installation Guide](../getting-started/installation.md)** - Install DMTools
- **[First Steps](../getting-started/first-steps.md)** - Getting started tutorial
- **[Jobs Documentation](../jobs/README.md)** - Available automation jobs
