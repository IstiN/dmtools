# Configuration Guide

Configure DMTools to work with your Jira, Confluence, Figma, GitHub, and AI providers.

## Quick Start

DMTools uses **environment variables** for configuration. The easiest way is to use `.env` files.

### Step 1: Create Configuration File

```bash
# Create a dmtools.env file in your project directory
touch dmtools.env
chmod 600 dmtools.env  # Secure the file
```

### Step 2: Add Your Credentials

Edit `dmtools.env` with your favorite editor:

```bash
# Jira Configuration
JIRA_BASE_PATH=https://your-company.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=ATATT3xFfGF0T...

# Confluence Configuration
CONFLUENCE_BASE_PATH=https://your-company.atlassian.net/wiki
CONFLUENCE_EMAIL=your-email@company.com
CONFLUENCE_API_TOKEN=ATATT3xFfGF0T...
CONFLUENCE_DEFAULT_SPACE=YOURSPACE

# AI Provider (Gemini recommended)
GEMINI_API_KEY=AIza...

# Optional: GitHub for source code operations
GITHUB_TOKEN=ghp_...
```

### Step 3: Test Configuration

```bash
# Test Jira connection
dmtools jira_get_ticket YOUR-123

# Test Confluence connection
dmtools confluence_content_by_title "Test Page"
```

---

## Environment File Precedence

DMTools loads environment variables from multiple files in this order (first found wins):

1. **`.env`** (current directory) - highest priority
2. **`dmtools.env`** (current directory)
3. **`dmtools-local.env`** (current directory) - for personal overrides
4. **`.env`** (script directory)
5. **`dmtools.env`** (script directory)
6. **`dmtools-local.env`** (script directory) - lowest priority

**Tip:** Use `dmtools.env` for shared team configuration and `dmtools-local.env` for your personal overrides.

---

## Essential Configuration Variables

### Jira (Required for Jira operations)

| Variable | Description | Example |
|----------|-------------|---------|
| `JIRA_BASE_PATH` | Your Jira instance URL | `https://company.atlassian.net` |
| `JIRA_EMAIL` | Your Jira email | `user@company.com` |
| `JIRA_API_TOKEN` | Jira API token | `ATATT3xFfGF0T...` |
| `JIRA_AUTH_TYPE` | Authentication type | `Basic` (default) or `Bearer` |

**How to get Jira API Token:**
1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Give it a label (e.g., "DMTools")
4. Copy the token

### Confluence (Required for Confluence operations)

| Variable | Description | Example |
|----------|-------------|---------|
| `CONFLUENCE_BASE_PATH` | Confluence URL | `https://company.atlassian.net/wiki` |
| `CONFLUENCE_EMAIL` | Your Confluence email | `user@company.com` |
| `CONFLUENCE_API_TOKEN` | Confluence API token | `ATATT3xFfGF0T...` |
| `CONFLUENCE_DEFAULT_SPACE` | Default space key | `TEAM` |
| `CONFLUENCE_AUTH_TYPE` | Authentication type | `Basic` (default) |

**How to get Confluence API Token:**
- Same as Jira (Atlassian uses unified tokens)
- You can use the same token for both Jira and Confluence

### AI Providers (Recommended for automation jobs)

**Google Gemini (Recommended):**
```bash
GEMINI_API_KEY=AIza...
GEMINI_DEFAULT_MODEL=gemini-1.5-pro
GEMINI_BASE_PATH=https://generativelanguage.googleapis.com/v1beta
```

**How to get Gemini API Key:**
1. Go to https://aistudio.google.com/app/apikey
2. Click "Create API key"
3. Copy the key (starts with `AIza`)

**OpenAI:**
```bash
OPEN_AI_API_KEY=sk-...
OPEN_AI_MODEL=gpt-4o
OPEN_AI_BATH_PATH=https://api.openai.com/v1
```

**How to get OpenAI API Key:**
1. Go to https://platform.openai.com/api-keys
2. Click "Create new secret key"
3. Copy the key (starts with `sk-`)

---

## Integration Configuration

### GitHub

```bash
SOURCE_GITHUB_TOKEN=ghp_...
SOURCE_GITHUB_WORKSPACE=your-org
SOURCE_GITHUB_REPOSITORY=your-repo
SOURCE_GITHUB_BRANCH=main
SOURCE_GITHUB_BASE_PATH=https://api.github.com
```

**How to get GitHub Token:**
1. Go to https://github.com/settings/tokens
2. Generate new token (classic)
3. Select scopes: `repo`, `read:org`, `read:user`
4. Copy the token (starts with `ghp_`)

### GitLab

```bash
GITLAB_TOKEN=glpat-...
GITLAB_WORKSPACE=your-group
GITLAB_REPOSITORY=your-repo
GITLAB_BRANCH=main
GITLAB_BASE_PATH=https://gitlab.com/api/v4
```

### Bitbucket

```bash
BITBUCKET_TOKEN=Bearer your-token
BITBUCKET_API_VERSION=V2
BITBUCKET_WORKSPACE=your-workspace
BITBUCKET_REPOSITORY=your-repo
BITBUCKET_BRANCH=main
BITBUCKET_BASE_PATH=https://api.bitbucket.org
```

### Figma

```bash
FIGMA_TOKEN=figd_...
FIGMA_BASE_PATH=https://api.figma.com/v1
```

**How to get Figma Token:**
1. Go to https://www.figma.com/settings (Personal Access Tokens section)
2. Click "Create new token"
3. Copy the token (starts with `figd_`)

---

## Advanced Configuration

### Performance Settings

```bash
# Delay between API requests (milliseconds)
SLEEP_TIME_REQUEST=300

# Maximum search results from Jira
JIRA_MAX_SEARCH_RESULTS=1000

# Clear Jira cache on startup
JIRA_CLEAR_CACHE=true

# Enable detailed Jira logging
JIRA_LOGGING_ENABLED=true
```

### AI Configuration

```bash
# AI retry settings
AI_RETRY_AMOUNT=3
AI_RETRY_DELAY_STEP=20000

# Model selection for specific tasks
CODE_AI_MODEL=gpt-4o
TEST_AI_MODEL=gpt-4o

# Prompt chunk configuration
PROMPT_CHUNK_TOKEN_LIMIT=4000
PROMPT_CHUNK_MAX_SINGLE_FILE_SIZE_MB=4
PROMPT_CHUNK_MAX_TOTAL_FILES_SIZE_MB=4
PROMPT_CHUNK_MAX_FILES=10
```

### Integration Selection

Limit which integrations are loaded (improves startup time):

```bash
DMTOOLS_INTEGRATIONS="jira,confluence,figma,ai"
```

Available integrations: `jira`, `confluence`, `figma`, `github`, `gitlab`, `bitbucket`, `ai`, `cli`, `file`

---

## Configuration for CI/CD

### GitHub Actions

Set secrets and variables in your repository:

**Secrets** (Settings > Secrets and variables > Actions):
```
JIRA_EMAIL
JIRA_API_TOKEN
CONFLUENCE_EMAIL
CONFLUENCE_API_TOKEN
GEMINI_API_KEY
FIGMA_TOKEN
GITHUB_TOKEN (usually auto-provided)
```

**Variables** (Settings > Secrets and variables > Actions):
```
JIRA_BASE_PATH
JIRA_AUTH_TYPE
CONFLUENCE_BASE_PATH
CONFLUENCE_DEFAULT_SPACE
```

**In workflow file:**
```yaml
- name: Run DMTools
  env:
    JIRA_EMAIL: ${{ secrets.JIRA_EMAIL }}
    JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
    JIRA_BASE_PATH: ${{ vars.JIRA_BASE_PATH }}
    JIRA_AUTH_TYPE: ${{ vars.JIRA_AUTH_TYPE }}
  run: dmtools jira_get_ticket PROJ-123
```

### GitLab CI/CD

Add variables in Settings > CI/CD > Variables:
```
JIRA_EMAIL
JIRA_API_TOKEN
JIRA_BASE_PATH
GEMINI_API_KEY
```

**In `.gitlab-ci.yml`:**
```yaml
script:
  - export JIRA_EMAIL="$JIRA_EMAIL"
  - export JIRA_API_TOKEN="$JIRA_API_TOKEN"
  - dmtools jira_get_ticket PROJ-123
```

---

## Security Best Practices

### 🔒 File Permissions

```bash
# Secure your configuration files
chmod 600 dmtools.env
chmod 600 dmtools-local.env
chmod 600 .env
```

### 🚫 Git Ignore

Ensure environment files are ignored by Git:

```bash
# Check if .gitignore includes environment files
cat .gitignore | grep -E "\.env|dmtools.env"

# Add to .gitignore if missing
cat >> .gitignore << EOF
.env
dmtools.env
dmtools-local.env
*.env.local
EOF
```

### 🔑 Token Rotation

- **Rotate tokens regularly** (every 90 days recommended)
- **Use different tokens** for development vs production
- **Revoke old tokens** after rotation

### 📦 Secret Management

For production:
- Use **GitHub Secrets** / **GitLab Variables**
- Use **AWS Secrets Manager** / **Azure Key Vault**
- Use **HashiCorp Vault** for enterprise environments

---

## Troubleshooting

### Environment Variables Not Loading

```bash
# Check file permissions
ls -la dmtools.env

# Check file format (no spaces around =)
cat dmtools.env | grep "="

# Check for hidden characters
cat -A dmtools.env
```

### Authentication Failures

**Jira/Confluence:**
```bash
# Test token validity
curl -u "your-email@company.com:YOUR_TOKEN" \
  https://your-company.atlassian.net/rest/api/3/myself

# Should return your user info if token is valid
```

**Gemini:**
```bash
# Test API key
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"

# Should return list of available models
```

### Wrong Base Path

```bash
# Jira Cloud (correct)
JIRA_BASE_PATH=https://your-company.atlassian.net

# Jira Server/Data Center (correct)
JIRA_BASE_PATH=https://jira.your-company.com

# Incorrect (don't include /rest/api)
JIRA_BASE_PATH=https://your-company.atlassian.net/rest/api/3  # ❌
```

---

## Example Complete Configuration

Here's a complete example configuration file:

```bash
# dmtools.env - Complete Example Configuration

# ============================================
# Jira Configuration
# ============================================
JIRA_BASE_PATH=https://mycompany.atlassian.net
JIRA_EMAIL=john.doe@mycompany.com
JIRA_API_TOKEN=ATATT3xFfGF0T1234567890abcdefghijklmnop
JIRA_AUTH_TYPE=Basic
JIRA_MAX_SEARCH_RESULTS=1000
JIRA_LOGGING_ENABLED=false
JIRA_CLEAR_CACHE=false

# ============================================
# Confluence Configuration
# ============================================
CONFLUENCE_BASE_PATH=https://mycompany.atlassian.net/wiki
CONFLUENCE_EMAIL=john.doe@mycompany.com
CONFLUENCE_API_TOKEN=ATATT3xFfGF0T1234567890abcdefghijklmnop
CONFLUENCE_DEFAULT_SPACE=ENG
CONFLUENCE_AUTH_TYPE=Basic

# ============================================
# AI Provider (Gemini)
# ============================================
GEMINI_API_KEY=AIzaSyD1234567890abcdefghijklmnopqrstuvwxyz
GEMINI_DEFAULT_MODEL=gemini-1.5-pro
GEMINI_BASE_PATH=https://generativelanguage.googleapis.com/v1beta

# ============================================
# GitHub Integration
# ============================================
SOURCE_GITHUB_TOKEN=ghp_1234567890abcdefghijklmnopqrstuvwxyz
SOURCE_GITHUB_WORKSPACE=mycompany
SOURCE_GITHUB_REPOSITORY=myproject
SOURCE_GITHUB_BRANCH=main
SOURCE_GITHUB_BASE_PATH=https://api.github.com

# ============================================
# Figma Integration
# ============================================
FIGMA_TOKEN=figd_1234567890abcdefghijklmnopqrstuvwxyz
FIGMA_BASE_PATH=https://api.figma.com/v1

# ============================================
# Performance & Behavior
# ============================================
SLEEP_TIME_REQUEST=300
DMTOOLS_INTEGRATIONS=jira,confluence,figma,ai,github
```

---

## Next Steps

✅ Configuration complete! Now you're ready to use DMTools:

👉 **[First Steps](first-steps.md)** - Run your first commands

👉 **[MCP Tools Reference](../cli-usage/mcp-tools.md)** - Learn the command structure

👉 **[MCP Tools](../cli-usage/mcp-tools.md)** - Explore all 67 available tools

For more detailed information, see:
- **[First Steps](first-steps.md)** - Start using DMTools CLI
- **[MCP Tools](../cli-usage/mcp-tools.md)** - Available CLI tools
