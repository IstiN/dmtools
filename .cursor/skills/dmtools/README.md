# DMtools Agent Skill

Universal AI assistant skill for DMtools - works with Cursor, Claude, Codex, and any [Agent Skills](https://agentskills.io) compatible system.

## 🚀 Quick Install

### Interactive Installation

```bash
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | bash
```

The installer will:
1. Detect your AI assistant (Cursor, Claude, Codex)
2. Download the latest DMtools skill
3. Let you choose where to install (or auto-select in non-interactive mode)

### Non-Interactive Installation

For CI/CD, automation, or scripting:

```bash
# Auto-install to first detected location (when piped)
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | bash

# Install to all detected locations
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | INSTALL_LOCATION=all bash

# With downloaded script
INSTALL_LOCATION=all bash install.sh  # All locations
INSTALL_LOCATION=1 bash install.sh    # First location
bash install.sh --all                 # All locations (flag)
bash install.sh --help                # Show help
```

## 📦 Manual Installation

1. Download the latest release from [Releases](https://github.com/IstiN/dmtools/releases)
2. Extract to one of these directories:

| Location | Scope | Platform |
|----------|-------|----------|
| `.cursor/skills/` | Project-level | Cursor |
| `.claude/skills/` | Project-level | Claude |
| `.codex/skills/` | Project-level | Codex |
| `~/.cursor/skills/` | User-level (global) | Cursor |
| `~/.claude/skills/` | User-level (global) | Claude |
| `~/.codex/skills/` | User-level (global) | Codex |

## 🎯 What's Included

This skill provides comprehensive DMtools knowledge:

- **Installation & Configuration** - Setup guides for all platforms
- **67+ MCP Tools** - Complete reference with examples
- **JavaScript Agents** - Development with GraalJS and MCP tools
- **Test Generation** - Xray, Cucumber, and automated testing
- **Integrations** - Jira, Azure DevOps, Figma, Confluence, Teams
- **AI Providers** - Gemini, OpenAI, Claude, DIAL, Ollama

## 💬 Usage

Once installed, the skill is available in your AI assistant:

### Cursor
- Type `/dmtools` to invoke explicitly
- Or mention DMtools and the agent will use the skill automatically
- View in: Settings → Rules → Agent Decides

### Claude
- Type `/dmtools` in chat
- Or ask about DMtools naturally

### Any AI Assistant
- The skill loads when relevant to your question
- Ask about installation, configuration, or development

## 📚 Example Questions

- "How do I install DMtools?"
- "Help me configure Jira integration for DMtools"
- "Show me how to create a JavaScript agent with MCP tools"
- "Generate test cases from user story PROJ-123"
- "Set up Gemini AI provider for DMtools"
- "Create a teammate configuration for test generation"

## 🏗️ Skill Structure

```
dmtools/
├── SKILL.md              # Main skill definition (Agent Skills standard)
└── references/           # Detailed documentation
    ├── installation/     # Setup guides
    ├── configuration/    # Integration configs
    ├── agents/          # Agent development
    ├── test-generation/ # Test automation
    └── examples/        # Code examples
```

## 🔄 Updating

To update to the latest version:

```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

## 🤝 Compatibility

This skill follows the [Agent Skills](https://agentskills.io) open standard and works with:
- ✅ Cursor IDE
- ✅ Claude Desktop
- ✅ Codex
- ✅ Any Agent Skills compatible system

## 📝 License

Apache License 2.0

## 🔗 Resources

- **DMtools Repository**: [github.com/IstiN/dmtools](https://github.com/IstiN/dmtools)
- **Agent Skills Standard**: [agentskills.io](https://agentskills.io)
- **Issues & Support**: [github.com/IstiN/dmtools/issues](https://github.com/IstiN/dmtools/issues)

---

*This skill enables AI assistants to provide comprehensive DMtools guidance and code generation.*