# Maintaining DMtools AI Skill Documentation

## 🔄 Keeping Documentation Accurate

This skill documentation is designed to be **automatically generated** from the actual DMtools source code to ensure 100% accuracy.

## 📝 Update Workflow

### 1. After Code Changes

When MCP tools are added or modified in DMtools:

```bash
# Build DMtools to generate MCPToolRegistry
./gradlew :dmtools-core:compileJava

# Install locally
./buildInstallLocal.sh

# Regenerate MCP tools documentation (creates tables)
./scripts/generate-mcp-tables.sh
```

### 2. Configuration Documentation

When updating configuration guides (e.g., `configuration/integrations/jira.md`):

**✅ DO**:
- Check actual source code for configuration options
- Reference generated TOOLS-REFERENCE.md instead of manually listing tools
- Verify environment variable names in PropertyReader.java
- Test configuration examples

**❌ DON'T**:
- Manually create tool tables (they will be inaccurate)
- Guess at configuration options
- Copy-paste from old documentation

### 3. Finding Accurate Information

#### Configuration Variables

Check these files for actual configuration:
```bash
# Jira configuration
vim dmtools-core/src/main/java/com/github/istin/dmtools/common/utils/PropertyReader.java

# Environment variables
grep -r "JIRA_" dmtools-core/src/main/java/com/github/istin/dmtools

# Authentication types
grep -r "authType" dmtools-core/src/main/java/com/github/istin/dmtools/atlassian
```

#### MCP Tools

Always generate from source:
```bash
# Option 1: Use dmtools list (most accurate)
dmtools list > references/mcp-tools/raw-output.json

# Option 2: Read MCPToolRegistry
cat dmtools-core/build/generated/sources/annotationProcessor/java/main/com/github/istin/dmtools/mcp/generated/MCPToolRegistry.java
```

#### Code Examples

Extract from actual agent files:
```bash
# JavaScript agents
find agents/js -name "*.js"

# Teammate configurations
find agents -name "*.json"
```

## 🤖 Automated Generation Scripts

### Generate MCP Tools Reference

```bash
./scripts/generate-skill-mcp-reference.sh
```

This script:
- Runs `dmtools list` to get actual tool definitions
- Generates `references/mcp-tools/TOOLS-REFERENCE.md`
- Includes complete JSON schema for each tool
- Reflects the current build accurately

### Future: Full Documentation Generator

Create `scripts/generate-all-docs.sh`:
```bash
#!/bin/bash
# Generate all documentation from source

# 1. MCP Tools
./scripts/generate-skill-mcp-reference.sh

# 2. Configuration Options
# Parse PropertyReader.java for env vars
# Generate configuration reference

# 3. Example Code
# Extract from agents/ directory
# Generate example snippets

# 4. Architecture Diagrams
# Generate from code structure
```

## 📋 Pre-Release Checklist

Before creating a new skill release:

- [ ] Build DMtools: `./gradlew :dmtools-core:compileJava`
- [ ] Install locally: `./buildInstallLocal.sh`
- [ ] Regenerate MCP reference: `./scripts/generate-skill-mcp-reference.sh`
- [ ] Update SKILL.md tool counts if changed
- [ ] Verify all example commands work
- [ ] Test configuration examples
- [ ] Check links in all markdown files
- [ ] Update CHANGELOG.md

## 🔍 Verification Commands

### Test Configuration Examples

```bash
# Test Jira config
export JIRA_BASE_PATH=https://test.atlassian.net
export JIRA_LOGIN_PASS_TOKEN=$(echo -n "test@test.com:token" | base64)
dmtools jira_get_ticket TEST-1

# Test AI provider
export GEMINI_API_KEY=test
dmtools gemini_ai_chat "test"
```

### Verify Tool Counts

```bash
# Count tools by integration
dmtools list | jq '.tools | group_by(.name | split("_")[0]) | map({integration: .[0].name | split("_")[0], count: length})'

# Total tools
dmtools list | jq '.tools | length'
```

### Check Links

```bash
# Find all markdown links
find . -name "*.md" -exec grep -H "\[.*\](.*)" {} \;

# Check for broken links (requires markdown-link-check)
find . -name "*.md" -exec markdown-link-check {} \;
```

## 🎯 Quality Standards

### Documentation Must Be:

1. **Accurate** - Generated from source code when possible
2. **Complete** - Cover all major features and integrations
3. **Tested** - All examples must work
4. **Current** - Updated with each release
5. **Clear** - Easy to understand for new users

### Code Examples Must:

1. **Work** - Tested and verified
2. **Be Real** - From actual DMtools codebase
3. **Be Complete** - Include all necessary imports/setup
4. **Show Errors** - Include error handling patterns

## 📚 Documentation Structure

```
dmtools-ai-docs/
├── SKILL.md                    # Main skill entry (update tool counts)
├── references/
│   ├── installation/           # Setup guides (rarely changes)
│   ├── configuration/          # Config guides (check source code)
│   │   ├── integrations/       # Platform-specific (verify env vars)
│   │   └── ai-providers/       # AI setup (test examples)
│   ├── agents/                 # Agent dev (extract from codebase)
│   ├── test-generation/        # Test automation (real examples)
│   ├── mcp-tools/              # AUTO-GENERATED - run script
│   │   └── TOOLS-REFERENCE.md  # Generated from dmtools list
│   └── examples/               # Code examples (from agents/)
└── install.sh                  # Skill installer (test before release)
```

## 🚀 Release Process

1. **Update Code**: Make changes to DMtools
2. **Build**: `./gradlew :dmtools-core:compileJava`
3. **Install**: `./buildInstallLocal.sh`
4. **Generate Docs**: `./scripts/generate-skill-mcp-reference.sh`
5. **Update Metadata**: Version in SKILL.md
6. **Test**: Verify examples and commands
7. **Commit**: `git commit -m "Update skill docs for v1.x.x"`
8. **Release**: `./release-skill.sh 1.x.x`

## 🔗 Related Files

- `scripts/generate-skill-mcp-reference.sh` - MCP tools generator
- `SKILL.md` - Main skill definition
- `README.md` - Installation instructions
- `release-skill.sh` - Release automation

---

*Keep documentation synchronized with code by automating generation wherever possible.*