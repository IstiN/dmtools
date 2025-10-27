# DMTools Documentation

Welcome to the DMTools documentation! This guide will help you get started with DMTools, understand its features, and integrate it into your workflow.

---

## 🎯 Quick Navigation

### 🚀 Getting Started
Start here if you're new to DMTools:
- **[Installation Guide](getting-started/installation.md)** - Install DMTools CLI in 5 minutes
- **[Configuration Guide](getting-started/configuration.md)** - Set up integrations and API keys
- **[First Steps](getting-started/first-steps.md)** - Your first commands and workflows

### 💻 CLI Usage
Using DMTools from the command line:
- **[MCP Tools Reference](cli-usage/mcp-tools.md)** - 67 built-in tools for Jira, Confluence, Figma

### ⚙️ Jobs (JobRunner)
Automated workflows and jobs:
- **[Jobs Overview](jobs/README.md)** - Understanding JobRunner and all 20 available jobs

### 🤖 AI Teammate Workflows
Advanced automation with GitHub Actions:
- **[AI Teammate Overview](ai-teammate/README.md)** - Understanding the AI Teammate concept
- **[Agent Configuration](ai-teammate/agent-configuration.md)** - Creating custom agents
- **[JavaScript Actions](ai-teammate/javascript-actions.md)** - Post-processing with JS

### 🔌 Integrations
Connect DMTools to your tools:
- **[Gemini Integration](integrations/gemini.md)** - Google Gemini AI integration
- See **[Configuration Guide](getting-started/configuration.md)** for Jira, Confluence, Figma, GitHub, and other integrations

### 🛠️ Configuration
Detailed configuration reference:
- **[Configuration Guide](getting-started/configuration.md)** - Complete environment setup with all integrations

### 📡 API Reference
Programmatic access to DMTools:
- **[REST API](api/rest-api.md)** - Server REST API documentation
- **[Agents API](api/agents-api.md)** - Automation agents API
- **[Chat API](api/chat-api.md)** - Interactive chat interface
- **[Agents Examples](api/agents-examples.md)** - Detailed agent examples
- **[API Reference](api/api-reference.md)** - Complete API reference

### 👨‍💻 Development
Contributing and building from source:
- **[Architecture](development/architecture.md)** - System architecture and design
- **[Building from Source](development/building.md)** - Development environment setup
- **[Project Structure](development/project-structure.md)** - Codebase organization
- **[Commit Rules](development/commit-rules.md)** - Commit message conventions
- **[Testing Rules](development/testing-rules.md)** - Testing guidelines

### 🚀 Deployment
Production deployment guides:
- **[Production Deployment](deployment/production.md)** - Deploy to Google App Engine
- **[OAuth Setup](deployment/oauth-setup.md)** - Configure authentication
- **[OAuth Proxy](deployment/oauth-proxy.md)** - Proxy server setup

---

## 📚 Popular Pages

### For New Users
1. **[Installation Guide](getting-started/installation.md)** - Get DMTools running quickly
2. **[Configuration Guide](getting-started/configuration.md)** - Set up your integrations
3. **[First Steps](getting-started/first-steps.md)** - Learn basic commands
4. **[MCP Tools Reference](cli-usage/mcp-tools.md)** - Explore available tools

### For Automation
1. **[AI Teammate Overview](ai-teammate/README.md)** - Understand the automation workflow
2. **[Agent Configuration](ai-teammate/agent-configuration.md)** - Configure your agents
3. **[JavaScript Actions](ai-teammate/javascript-actions.md)** - Write custom actions
4. **[Jobs Overview](jobs/README.md)** - Run automated jobs

### For Developers
1. **[Architecture](development/architecture.md)** - Understand the system design
2. **[Building from Source](development/building.md)** - Set up dev environment
3. **[Project Structure](development/project-structure.md)** - Navigate the codebase
4. **[API Reference](api/rest-api.md)** - Integrate with DMTools

---

## 🎓 Learning Path

### Beginner Path
1. Install DMTools → [Installation](getting-started/installation.md)
2. Configure integrations → [Configuration](getting-started/configuration.md)
3. Try first commands → [First Steps](getting-started/first-steps.md)
4. Explore CLI tools → [MCP Tools](cli-usage/mcp-tools.md)

### Automation Path
1. Understand AI Teammate → [Overview](ai-teammate/README.md)
2. Learn job system → [Jobs](jobs/README.md)
3. Configure agents → [Agent Config](ai-teammate/agent-configuration.md)
4. Write JavaScript actions → [JS Actions](ai-teammate/javascript-actions.md)

### Developer Path
1. Review architecture → [Architecture](development/architecture.md)
2. Build from source → [Building](development/building.md)
3. Understand structure → [Structure](development/project-structure.md)
4. Use the API → [API Docs](api/rest-api.md)

---

## 📖 Documentation Structure

```
docs/
├── README.md                           # You are here
├── INDEX.md                            # Quick file reference
│
├── getting-started/                    # New user guides
│   ├── installation.md
│   ├── configuration.md
│   └── first-steps.md
│
├── cli-usage/                          # CLI reference
│   └── mcp-tools.md
│
├── jobs/                               # JobRunner system
│   └── README.md
│
├── ai-teammate/                        # Automation workflows
│   ├── README.md
│   ├── agent-configuration.md
│   └── javascript-actions.md
│
├── api/                                # API documentation
│   ├── rest-api.md
│   ├── agents-api.md
│   ├── chat-api.md
│   ├── agents-examples.md
│   └── api-reference.md
│
├── integrations/                       # Integration guides
│   └── gemini.md
│
├── development/                        # Developer docs
│   ├── architecture.md
│   ├── building.md
│   ├── project-structure.md
│   ├── project-overview.md
│   ├── dependencies.md
│   ├── commit-rules.md
│   ├── testing-rules.md
│   ├── cursor-setup.md
│   ├── aider-gemini-setup.md
│   └── uri-factory-improvements.md
│
└── deployment/                         # Deployment guides
    ├── production.md
    ├── oauth-setup.md
    └── oauth-proxy.md
```

---

## 💡 Quick Tips

### 🔑 Essential Commands
```bash
# List all available tools
dmtools mcp list

# Get a Jira ticket
dmtools mcp jira_get_ticket '{"key": "PROJ-123"}'

# Search Jira tickets
dmtools mcp jira_search_by_jql '{"searchQueryJQL": "project = PROJ", "fields": ["summary", "status"]}'

# Run a job
dmtools run my-config.json
```

### 🎯 Common Use Cases
- **Automate ticket updates** → [AI Teammate](ai-teammate/README.md)
- **Generate documentation** → [Jobs: Documentation](jobs/README.md)
- **Track productivity** → [Jobs: Reports](jobs/README.md)
- **Integrate with CI/CD** → [Installation](getting-started/installation.md)

### 🔗 External Links
- [GitHub Repository](https://github.com/istin/dmtools)
- [Issue Tracker](https://github.com/istin/dmtools/issues)

---

## 📋 All Documentation Files

See [INDEX.md](INDEX.md) for a complete alphabetical list of all documentation files.

---

**Version:** 1.0  
**Last Updated:** 2025-01-05  
**Status:** ✅ Complete and up-to-date
