# DMTools Documentation

> **Comprehensive development management toolkit with CLI, automation agents, and integrations**

## 📚 Quick Navigation

### 🚀 Getting Started
Start here if you're new to DMTools:
- **[Installation Guide](getting-started/installation.md)** - Install DMTools CLI in 5 minutes
- **[Configuration Guide](getting-started/configuration.md)** - Set up integrations and API keys
- **[First Steps](getting-started/first-steps.md)** - Your first commands and workflows

### 💻 CLI Usage
Using DMTools from the command line:
- **[CLI Overview](cli-usage/overview.md)** - Command structure and basic usage
- **[MCP Tools Reference](cli-usage/mcp-tools.md)** - 67 built-in tools for Jira, Confluence, Figma
- **[CLI Examples](cli-usage/examples.md)** - Common use cases and patterns

### ⚙️ Jobs (JobRunner)
Automated workflows and jobs:
- **[Jobs Overview](jobs/README.md)** - Understanding JobRunner and available jobs
- **[Business Analysis Jobs](jobs/business-analysis/)** - Requirements, stories, DOR generation
- **[Development Jobs](jobs/development/)** - Code generation, unit tests, commits triage
- **[QA Jobs](jobs/qa/)** - Test cases generation, QA productivity reports
- **[Reporting Jobs](jobs/reports/)** - Productivity reports, daily standups
- **[Architecture Jobs](jobs/architecture/)** - Solution architecture, diagrams
- **[Automation Jobs](jobs/automation/)** - Source code sync, expert assistance

### 🤖 AI Teammate Workflows
Advanced automation with GitHub Actions:
- **[AI Teammate Overview](ai-teammate/README.md)** - Understanding the AI Teammate concept
- **[GitHub Actions Integration](ai-teammate/github-actions.md)** - Setting up workflows
- **[Agent Configuration](ai-teammate/agent-configuration.md)** - Creating custom agents
- **[JavaScript Actions](ai-teammate/javascript-actions.md)** - Post-processing with JS
- **[Workflow Examples](ai-teammate/examples.md)** - Real-world automation patterns

### 🔌 Integrations
Connect DMTools to your tools:
- **[Jira Integration](integrations/jira.md)** - Connect to Jira for issue tracking
- **[Confluence Integration](integrations/confluence.md)** - Work with Confluence pages
- **[GitHub Integration](integrations/github.md)** - Source code and PR management
- **[GitLab Integration](integrations/gitlab.md)** - GitLab integration setup
- **[Bitbucket Integration](integrations/bitbucket.md)** - Bitbucket repository access
- **[Figma Integration](integrations/figma.md)** - Design files and assets
- **[AI Providers](integrations/ai-providers.md)** - OpenAI, Gemini, and other AI services

### 🛠️ Configuration
Detailed configuration reference:
- **[Environment Variables](configuration/environment-variables.md)** - Complete reference
- **[Properties Files](configuration/properties-file.md)** - config.properties format
- **[CI/CD Integration](configuration/ci-cd.md)** - GitHub Actions, GitLab CI
- **[Security Best Practices](configuration/security.md)** - Secure credential management

### 📡 API Reference
Programmatic access to DMTools:
- **[REST API](api/rest-api.md)** - Server REST API documentation
- **[Agents API](api/agents-api.md)** - Automation agents API
- **[Chat API](api/chat-api.md)** - Conversational AI interface
- **[Agents Examples](api/agents-examples.md)** - Real-world agent examples
- **[API Reference](api/api-reference.md)** - Complete API reference

### 🏗️ Development
Contributing to DMTools:
- **[Building from Source](development/building.md)** - Build and development setup
- **[Architecture](development/architecture.md)** - System design and components
- **[Project Overview](development/project-overview.md)** - High-level project overview
- **[Project Structure](development/project-structure.md)** - Codebase organization
- **[Dependencies](development/dependencies.md)** - Project dependencies
- **[Commit Rules](development/commit-rules.md)** - Commit message conventions
- **[Testing Rules](development/testing-rules.md)** - Testing guidelines and best practices
- **[Cursor Setup](development/cursor-setup.md)** - Cursor IDE integration with MCP
- **[Aider + Gemini Setup](development/aider-gemini-setup.md)** - AI coding assistant setup
- **[URI Factory](development/uri-factory-improvements.md)** - URI to object factory improvements

### 🚢 Deployment
Production deployment guides:
- **[OAuth Setup](deployment/oauth-setup.md)** - OAuth2 authentication configuration
- **[OAuth Proxy](deployment/oauth-proxy.md)** - OAuth proxy documentation
- **[Production Deployment](deployment/production.md)** - Deploy to App Engine

---

## 🎯 Common Use Cases

### For Business Analysts
1. [Generate user stories from requirements](jobs/business-analysis/user-story-generator.md)
2. [Collect and organize requirements](jobs/business-analysis/requirements-collector.md)
3. [Create Definition of Ready documents](jobs/business-analysis/dor-generation.md)

### For Developers
1. [Generate code from tickets](jobs/development/code-generator.md)
2. [Create unit tests automatically](jobs/development/unit-tests-generator.md)
3. [Triage commits and analyze changes](jobs/development/commits-triage.md)
4. [Automate PR creation with AI](ai-teammate/examples.md#automated-development)

### For QA Engineers
1. [Generate test cases from stories](jobs/qa/test-cases-generator.md)
2. [Track QA productivity](jobs/qa/qa-productivity-report.md)

### For Managers
1. [Dev productivity reports](jobs/reports/dev-productivity.md)
2. [Daily standup reports](jobs/reports/scrum-master-daily.md)
3. [BA productivity tracking](jobs/reports/ba-productivity.md)

### For Solution Architects
1. [Generate solution architecture](jobs/architecture/solution-architecture-creator.md)
2. [Create technical diagrams](jobs/architecture/diagrams-creator.md)

---

## 🆘 Getting Help

- **Troubleshooting**: Check [CLI Usage > Examples](cli-usage/examples.md) for common issues
- **Configuration Issues**: See [Configuration > Environment Variables](configuration/environment-variables.md)
- **API Questions**: Review [API Reference](api/)
- **GitHub Issues**: [Report bugs or request features](https://github.com/IstiN/dmtools/issues)

---

## 📖 About This Documentation

This documentation follows a **user journey structure** to help you find what you need quickly:
- **New users** → Start with [Getting Started](getting-started/)
- **CLI users** → Jump to [CLI Usage](cli-usage/) or [MCP Tools](cli-usage/mcp-tools.md)
- **Automation** → Explore [AI Teammate Workflows](ai-teammate/)
- **Developers** → Check [Development](development/) and [API Reference](api/)
- **DevOps** → See [Deployment](deployment/) guides

---

**Version:** 1.7.16+ | **License:** See [LICENSE](../LICENSE) | **Repository:** [GitHub](https://github.com/IstiN/dmtools)
