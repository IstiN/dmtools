# DMTools Environment Configuration

This document explains how to configure DMTools using environment files for secure and convenient management of API keys, tokens, and other configuration values.

## Quick Start

1. **Copy the example file:**
   ```bash
   cp dmtools.env.example dmtools.env
   ```

2. **Edit your configuration:**
   ```bash
   # Edit with your preferred editor
   nano dmtools.env
   # or
   vim dmtools.env
   ```

3. **Fill in your actual values** (remove the example placeholders)

4. **Run DMTools** - it will automatically load your configuration:
   ```bash
   ./dmtools.sh jira_get_ticket YOUR-123
   ```

## Environment File Precedence

DMTools loads environment variables from multiple files in this order (first found wins):

1. **`.env`** (current directory) - highest priority
2. **`dmtools.env`** (current directory) 
3. **`dmtools-local.env`** (current directory)
4. **`.env`** (script directory)
5. **`dmtools.env`** (script directory)
6. **`dmtools-local.env`** (script directory) - lowest priority

## File Format

Environment files support:

- **Key=Value pairs:** `JIRA_API_TOKEN=your-token-here`
- **Comments:** Lines starting with `#` are ignored
- **Quoted values:** `JIRA_EMAIL="user@company.com"`
- **Empty lines:** Ignored for readability

### Example:
```bash
# Jira Configuration
JIRA_BASE_PATH=https://mycompany.atlassian.net
JIRA_EMAIL=my.email@company.com
JIRA_API_TOKEN=ATATT3xFfGF0T...

# AI Configuration
GEMINI_API_KEY=AIza...
```

## Security Best Practices

### 🔒 File Permissions
Ensure your environment files are not readable by others:
```bash
chmod 600 dmtools.env
```

### 🚫 Git Ignore
Environment files are automatically ignored by Git (see `.gitignore`), but always double-check:
```bash
git status  # Should not show dmtools.env or .env files
```

### 📁 Organization
- Use **`dmtools.env`** for shared team configuration (if needed)
- Use **`dmtools-local.env`** for personal/local overrides
- Use **`.env`** for project-specific configuration

## Common Configuration Variables

### Essential for Most Users
| Variable | Description | Example |
|----------|-------------|---------|
| `JIRA_BASE_PATH` | Your Jira instance URL | `https://company.atlassian.net` |
| `JIRA_EMAIL` | Your Jira email | `user@company.com` |
| `JIRA_API_TOKEN` | Jira API token | `ATATT3xFfGF0T...` |
| `CONFLUENCE_BASE_PATH` | Confluence URL | `https://company.atlassian.net/wiki` |
| `GEMINI_API_KEY` | Google Gemini API key | `AIza...` |

### Source Control Integration
| Variable | Description |
|----------|-------------|
| `GITHUB_TOKEN` | GitHub Personal Access Token |
| `GITLAB_TOKEN` | GitLab Personal Access Token |
| `BITBUCKET_TOKEN` | Bitbucket App Password |

### AI Providers
| Variable | Description |
|----------|-------------|
| `GEMINI_API_KEY` | Google Gemini API key |
| `OPEN_AI_API_KEY` | OpenAI API key |
| `GEMINI_DEFAULT_MODEL` | Default Gemini model |

## Getting API Tokens

### Jira/Confluence (Atlassian)
1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Give it a label (e.g., "DMTools")
4. Copy the token immediately (you won't see it again)

### GitHub
1. Go to https://github.com/settings/tokens
2. Click "Generate new token" → "Generate new token (classic)"
3. Select scopes: `repo`, `read:org`, `read:user`
4. Copy the token (starts with `ghp_`)

### Google Gemini
1. Go to https://aistudio.google.com/app/apikey
2. Click "Create API key"
3. Copy the key (starts with `AIza`)

### Figma
1. Go to https://www.figma.com/settings (Personal Access Tokens section)
2. Click "Create new token"
3. Copy the token (starts with `figd_`)

## Troubleshooting

### Environment Not Loading
- Check file permissions: `ls -la dmtools.env`
- Verify file format: no spaces around `=`
- Check for hidden characters: `cat -A dmtools.env`

### Variables Not Taking Effect
- Ensure no typos in variable names
- Check precedence - a higher priority file might override your values
- Use `./dmtools.sh --verbose` to see debug information

### Permission Errors
```bash
# Fix file permissions
chmod 600 dmtools.env

# Check current permissions
ls -la dmtools.env
```

## Advanced Usage

### Multiple Environment Files
You can use different files for different purposes:

```bash
# Team shared configuration
dmtools.env

# Your personal overrides  
dmtools-local.env

# Project-specific settings
.env
```

### Environment Variables Override
Environment files don't override already-set environment variables. If you need to override a system environment variable, unset it first:

```bash
unset JIRA_BASE_PATH
./dmtools.sh jira_get_ticket YOUR-123
```

### Integration with CI/CD
For CI/CD pipelines, you can set environment variables directly instead of using files:

```yaml
# GitHub Actions example
env:
  JIRA_BASE_PATH: ${{ secrets.JIRA_BASE_PATH }}
  JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
```

## Support

If you encounter issues with environment configuration:

1. Check this documentation
2. Verify your environment file syntax
3. Test with `./dmtools.sh --verbose` for debug information
4. Check the DMTools logs for specific error messages

For more help, refer to the main [README.md](README.md) or open an issue on the project repository.
