# Confluence Configuration

Configure Confluence for reading documentation, pages, and knowledge base content.

## Overview

**Confluence is optional but recommended** for automation jobs. It's used to:
- Read documentation pages for context
- Extract knowledge base articles
- Access team guidelines and standards
- Store and retrieve structured information

**Used by jobs:**
- **TestCasesGenerator** - Read test case rules, examples, custom field definitions
- **RequirementsCollector** - Extract requirements from documentation
- **DocumentationGenerator** - Read existing documentation
- **UserStoryGenerator** - Access product documentation

---

## Setup Steps

### 1. Get API Token

Confluence uses the same API token as Jira (for Atlassian Cloud):

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click **"Create API token"**
3. Give it a label (e.g., "DMTools")
4. Copy the token (starts with `ATATT`)

**Note:** You can use the same token for both Jira and Confluence if they're on the same Atlassian instance.

### 2. Configure in dmtools.env

```bash
# Confluence Configuration
CONFLUENCE_BASE_PATH=https://your-company.atlassian.net/wiki
CONFLUENCE_EMAIL=your-email@company.com
CONFLUENCE_API_TOKEN=ATATT3xFfGF0T1234567890abcdefghijklmnop
CONFLUENCE_DEFAULT_SPACE=YOURSPACE
CONFLUENCE_AUTH_TYPE=Basic
```

### 3. Test Connection

```bash
# Test Confluence connection
dmtools confluence_content_by_title "Test Page"

# Search for content
dmtools confluence_search_content_by_text "test cases"

# Get current user
dmtools confluence_get_current_user_profile
```

**Expected output:** Page content or search results in JSON format

---

## Configuration Options

### Basic Configuration (Required)

```bash
# Required
CONFLUENCE_BASE_PATH=https://your-company.atlassian.net/wiki
CONFLUENCE_EMAIL=your-email@company.com
CONFLUENCE_API_TOKEN=ATATT...

# Recommended
CONFLUENCE_DEFAULT_SPACE=QA           # Default space for searches
CONFLUENCE_AUTH_TYPE=Basic            # Authentication type
```

### Advanced Configuration (Optional)

```bash
# Performance
CONFLUENCE_MAX_RESULTS=100            # Max results per search
CONFLUENCE_CACHE_ENABLED=true         # Enable page caching
CONFLUENCE_REQUEST_DELAY=200          # Delay between requests (ms)
```

---

## Confluence Server / Data Center

For on-premise Confluence installations:

```bash
# Confluence Server/DC Configuration
CONFLUENCE_BASE_PATH=https://confluence.your-company.com
CONFLUENCE_EMAIL=your-username
CONFLUENCE_API_TOKEN=your-password-or-token
CONFLUENCE_DEFAULT_SPACE=YOURSPACE
CONFLUENCE_AUTH_TYPE=Basic
```

---

## Available Confluence MCP Tools

Use these tools in JavaScript actions or directly via CLI:

### Content Operations

| Tool | Purpose | Example |
|------|---------|---------|
| `confluence_content_by_title` | Get page by title and space | `dmtools confluence_content_by_title "Test Rules" "QA"` |
| `confluence_content_by_id` | Get page by ID | `dmtools confluence_content_by_id "123456789"` |
| `confluence_search_content_by_text` | Search pages | `dmtools confluence_search_content_by_text "test cases"` |
| `confluence_find_content` | Advanced search | `dmtools confluence_find_content "type=page AND space=QA"` |
| `confluence_contents_by_urls` | Get multiple pages by URLs | `dmtools confluence_contents_by_urls "url1,url2"` |

### Page Management

| Tool | Purpose | Example |
|------|---------|---------|
| `confluence_create_page` | Create new page | `dmtools confluence_create_page "QA" "New Page" "Content"` |
| `confluence_update_page` | Update existing page | `dmtools confluence_update_page "123456789" "Updated content"` |
| `confluence_update_page_with_history` | Update with version history | - |
| `confluence_get_children_by_id` | Get child pages | `dmtools confluence_get_children_by_id "123456789"` |
| `confluence_get_children_by_name` | Get children by parent name | - |

### Attachment Operations

| Tool | Purpose | Example |
|------|---------|---------|
| `confluence_get_content_attachments` | Get page attachments | - |
| `confluence_download_attachment` | Download attachment | - |

### User Operations

| Tool | Purpose | Example |
|------|---------|---------|
| `confluence_get_current_user_profile` | Get your profile | `dmtools confluence_get_current_user_profile` |
| `confluence_get_user_profile_by_id` | Get user by ID | - |

### Advanced Operations

| Tool | Purpose | Example |
|------|---------|---------|
| `confluence_find_or_create` | Find page or create if not exists | - |
| `confluence_find_content_by_title_and_space` | Find specific page | - |

**Complete list:** See [Confluence MCP Tools](../README-MCP.md#confluence-tools)

---

## Using Confluence with Jobs

### TestCasesGenerator

Reference Confluence pages for test case rules and examples:

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "confluencePages": [
      "https://company.atlassian.net/wiki/spaces/QA/pages/123/Testing+Standards"
    ],
    "customFieldsRules": "https://company.atlassian.net/wiki/spaces/QA/pages/456/Custom+Fields"
  }
}
```

**See:** [TestCasesGenerator - Confluence Integration](../jobs/TestCasesGenerator.md#confluence-integration-parameters) for details on rules structure and examples.

### RequirementsCollector

```json
{
  "name": "RequirementsCollector",
  "params": {
    "documentationPages": [
      "https://company.atlassian.net/wiki/spaces/PRODUCT/pages/789/Requirements"
    ]
  }
}
```

### UserStoryGenerator

```json
{
  "name": "UserStoryGenerator",
  "params": {
    "productDocumentation": "https://company.atlassian.net/wiki/spaces/PRODUCT/pages/111/Product+Guide"
  }
}
```

---

## Troubleshooting

### Page Not Found (404)

**Problem:** Page doesn't exist or wrong URL

**Solution:**
```bash
# Check page exists
curl -u "your-email@company.com:YOUR_TOKEN" \
  "https://your-company.atlassian.net/wiki/rest/api/content/123456789"

# Verify page ID in URL:
# https://company.atlassian.net/wiki/spaces/SPACE/pages/123456789/Page+Title
#                                                              ^^^^^^^^^ page ID
```

### Permission Denied (403)

**Problem:** No access to page or space

**Solution:**
- Check user has **view** permission for the page
- Check user has **view** permission for the space
- Contact space admin to grant permissions

### Authentication Failed (401)

**Problem:** Invalid token or credentials

**Solution:**
```bash
# Test token validity
curl -u "your-email@company.com:YOUR_TOKEN" \
  https://your-company.atlassian.net/wiki/rest/api/user/current

# Should return your user info if valid
```

### Slow Page Load

**Problem:** Confluence pages load slowly

**Solution:**
```bash
# Enable caching
CONFLUENCE_CACHE_ENABLED=true

# Reduce max results
CONFLUENCE_MAX_RESULTS=50

# Increase request delay if hitting rate limits
CONFLUENCE_REQUEST_DELAY=500
```

### Wrong Base Path

**Problem:** Connection fails

**Solution:**
```bash
# Confluence Cloud (correct)
CONFLUENCE_BASE_PATH=https://your-company.atlassian.net/wiki

# NOT these:
# https://your-company.atlassian.net/wiki/rest/api  # ❌ Too specific
# https://your-company.atlassian.net                # ❌ Missing /wiki
```

---

## Security Best Practices

### 🔒 Protect API Token

```bash
# Secure environment file
chmod 600 dmtools.env

# Never commit tokens
echo "dmtools.env" >> .gitignore
```

### 👥 Use Service Account (Recommended)

- Create dedicated Confluence user for automation
- Grant minimum required permissions (read-only if possible)
- Easier to track and audit usage
- Doesn't affect personal user sessions

### 📊 Audit Page Access

- Review Confluence audit logs regularly
- Monitor which pages are accessed by automation
- Restrict sensitive pages if needed

### 🔄 Rotate Tokens Regularly

- Rotate API tokens every 90 days
- Use different tokens for dev/prod
- Revoke old tokens immediately after rotation

---

## Performance Optimization

### Enable Caching

```bash
# Cache Confluence pages to reduce API calls
CONFLUENCE_CACHE_ENABLED=true
```

**Benefits:**
- Faster subsequent access to same pages
- Reduces API rate limit usage
- Lower latency for repeated operations

### Rate Limiting

```bash
# Delay between requests (milliseconds)
CONFLUENCE_REQUEST_DELAY=200

# Max results per search (lower = faster, more requests)
CONFLUENCE_MAX_RESULTS=100
```

**Tuning:**
- Increase `CONFLUENCE_REQUEST_DELAY` if hitting 429 (rate limit) errors
- Decrease `CONFLUENCE_MAX_RESULTS` if searches timeout
- Monitor Confluence audit logs for usage patterns

---

## Next Steps

✅ **Confluence configured!** You're ready to use jobs with documentation:

👉 **[TestCasesGenerator Guide](../jobs/TestCasesGenerator.md)** - Use Confluence for rules and examples

👉 **[AI Provider Configuration](ai-providers.md)** - Configure AI models

👉 **[Tracker Configuration](tracker-configuration.md)** - Configure Jira/ADO

---

## See Also

- [Complete Configuration Guide](../getting-started/configuration.md)
- [Confluence MCP Tools](../README-MCP.md#confluence-tools)
- [TestCasesGenerator - Confluence Integration](../jobs/TestCasesGenerator.md#confluence-integration-parameters)
- [JavaScript Actions Guide](../ai-teammate/javascript-actions.md)
