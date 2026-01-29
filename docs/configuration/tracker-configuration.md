# Tracker Configuration

Configure issue tracking systems for TestCasesGenerator and other automation jobs.

## Overview

**At least one tracker must be configured** for TestCasesGenerator to work. The job uses the tracker to:
- Read user stories and requirements
- Find existing test cases
- Create new test cases
- Link test cases to stories
- Post comments and updates

---

## Supported Trackers

| Tracker | Features | Setup Difficulty |
|---------|----------|------------------|
| **Jira Cloud** | Full support, most tested | Easy ⭐⭐⭐ |
| **Jira Server/DC** | Full support | Easy ⭐⭐⭐ |
| **Jira + Xray** | Test case management, preconditions | Easy ⭐⭐⭐ |
| **Azure DevOps** | Work items, test cases | Medium ⭐⭐ |
| **Rally** | User stories, test cases | Medium ⭐⭐ |

---

## Jira Cloud (Recommended)

**Why Jira Cloud:**
- ✅ Most widely used
- ✅ Fully tested with TestCasesGenerator
- ✅ Easy API token setup
- ✅ Works with Xray for test management

### Setup Steps

#### 1. Get API Token

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click **"Create API token"**
3. Give it a label (e.g., "DMTools")
4. Copy the token (starts with `ATATT`)

#### 2. Configure in dmtools.env

```bash
# Jira Cloud Configuration
JIRA_BASE_PATH=https://your-company.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=ATATT3xFfGF0T1234567890abcdefghijklmnop
JIRA_AUTH_TYPE=Basic
```

#### 3. Test Connection

```bash
# Test Jira connection
dmtools jira_get_ticket YOUR-123

# Test search
dmtools jira_search_by_jql "project = YOUR"
```

**Expected output:** Ticket details in JSON format

### Configuration Options

```bash
# Performance Settings
JIRA_MAX_SEARCH_RESULTS=1000
JIRA_LOGGING_ENABLED=false
JIRA_CLEAR_CACHE=false
SLEEP_TIME_REQUEST=300  # Delay between requests (ms)

# Authentication
JIRA_AUTH_TYPE=Basic  # or Bearer
```

---

## Jira Server / Data Center

**Differences from Cloud:**
- Different base URL structure
- May use username/password instead of token
- Some API endpoints differ

### Setup Steps

#### 1. Get Credentials

**Option A: API Token (if available)**
- Same as Jira Cloud

**Option B: Username/Password**
- Use your Jira login credentials
- Encode as base64: `echo -n "username:password" | base64`

#### 2. Configure in dmtools.env

```bash
# Jira Server/DC Configuration
JIRA_BASE_PATH=https://jira.your-company.com
JIRA_EMAIL=your-username
JIRA_API_TOKEN=your-password-or-token
JIRA_AUTH_TYPE=Basic
```

#### 3. Test Connection

```bash
dmtools jira_get_ticket YOUR-123
```

---

## Jira + Xray (Test Management)

**Why Xray:**
- ✅ Native test case management in Jira
- ✅ Test steps, preconditions, test execution
- ✅ Fully supported by TestCasesGenerator
- ✅ Custom fields for test cases

### Setup Steps

#### 1. Install Xray Plugin

1. Go to Jira Administration > Find new apps
2. Search for **"Xray Test Management"**
3. Install and configure

#### 2. Configure Same as Jira

```bash
# Same configuration as Jira Cloud/Server
JIRA_BASE_PATH=https://your-company.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=ATATT3xFfGF0T...
```

#### 3. Enable Xray Optimization (Optional)

For faster test case fetching:

```bash
# Xray Parallel Fetch (optional, for large test suites)
XRAY_PARALLEL_FETCH_ENABLED=true
XRAY_PARALLEL_BATCH_SIZE=100
XRAY_PARALLEL_THREADS=2
XRAY_PARALLEL_DELAY_MS=500
```

**Performance:**
- Sequential: ~185s for 10,000 tests
- Parallel: ~48-73s for 10,000 tests (60-74% faster)

#### 4. Test Xray Connection

```bash
# Get test case details
dmtools jira_xray_get_test_details TEST-123

# Get test steps
dmtools jira_xray_get_test_steps TEST-123

# Search for test cases
dmtools jira_xray_search_tickets "project = YOUR AND issueType = Test"
```

### Xray-Specific Parameters

When using Xray with TestCasesGenerator:

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "testCaseIssueType": "Test",
    "testCasesCustomFields": ["preconditions", "testSteps"],
    "preprocessJSAction": "agents/js/preprocessXrayTestCases.js"
  }
}
```

**Available Xray MCP Tools:**
- `jira_xray_create_precondition` - Create test preconditions
- `jira_xray_get_test_details` - Get full test case details
- `jira_xray_get_test_steps` - Get test steps
- `jira_xray_get_preconditions` - Get precondition list
- `jira_xray_add_test_step` - Add single test step
- `jira_xray_add_test_steps` - Add multiple test steps
- `jira_xray_add_precondition_to_test` - Link precondition to test
- `jira_xray_add_preconditions_to_test` - Link multiple preconditions

---

## Azure DevOps (ADO)

**Why ADO:**
- ✅ Microsoft ecosystem integration
- ✅ Work items + test cases
- ✅ Built-in test management
- ⚠️ Different API structure than Jira

### Setup Steps

#### 1. Get Personal Access Token (PAT)

1. Go to https://dev.azure.com/{organization}/_usersSettings/tokens
2. Click **"New Token"**
3. Set scopes:
   - ✅ Work Items (Read, Write)
   - ✅ Test Management (Read, Write)
4. Copy the token

#### 2. Configure in dmtools.env

```bash
# Azure DevOps Configuration
ADO_BASE_PATH=https://dev.azure.com/your-organization
ADO_PROJECT=YourProject
ADO_TOKEN=your-personal-access-token
ADO_API_VERSION=7.0
```

#### 3. Test Connection

```bash
# Get work item
dmtools ado_get_work_item 12345

# Search by WIQL
dmtools ado_search_by_wiql "SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'User Story'"
```

### ADO-Specific Parameters

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "SELECT [System.Id] FROM WorkItems WHERE [System.State] = 'Active'",
    "existingTestCasesJql": "SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'Test Case'",
    "testCaseIssueType": "Test Case"
  }
}
```

**Available ADO MCP Tools:**
- `ado_get_work_item` - Get work item details
- `ado_search_by_wiql` - Search using WIQL
- `ado_create_work_item` - Create work item
- `ado_update_description` - Update work item description
- `ado_post_comment` - Add comment
- `ado_assign_work_item` - Assign to user
- `ado_move_to_state` - Change state
- `ado_link_work_items` - Create link between work items

---

## Rally

**Why Rally:**
- ✅ Agile project management
- ✅ User stories + test cases
- ⚠️ Less commonly used than Jira/ADO

### Setup Steps

#### 1. Get API Key

1. Log into Rally
2. Go to Settings > API Keys
3. Generate new API key
4. Copy the key

#### 2. Configure in dmtools.env

```bash
# Rally Configuration
RALLY_BASE_PATH=https://rally1.rallydev.com/slm/webservice/v2.0
RALLY_API_KEY=your-rally-api-key
RALLY_WORKSPACE=your-workspace
RALLY_PROJECT=your-project
```

#### 3. Test Connection

```bash
# Test Rally connection (custom implementation may be needed)
```

**Note:** Rally support in dmtools is more limited than Jira/ADO. Contact the team if you need Rally support.

---

## Tracker-Specific Considerations

### Issue Type Names

Different trackers use different names for test cases:

| Tracker | User Story Type | Test Case Type |
|---------|----------------|----------------|
| **Jira** | `Story`, `Task`, `Epic` | `Test Case`, `Test` |
| **Jira + Xray** | `Story` | `Test` (built-in) |
| **ADO** | `User Story` | `Test Case` |
| **Rally** | `User Story` | `Test Case` |

Configure in TestCasesGenerator:

```json
{
  "params": {
    "testCaseIssueType": "Test"  // Jira/Xray
    // OR
    "testCaseIssueType": "Test Case"  // ADO/Rally
  }
}
```

### Query Languages

| Tracker | Query Language | Example |
|---------|---------------|---------|
| **Jira** | JQL | `project = PROJ AND issueType = Story` |
| **ADO** | WIQL | `SELECT [System.Id] FROM WorkItems WHERE [System.WorkItemType] = 'User Story'` |
| **Rally** | Rally Query | `(Project = "MyProject")` |

### Custom Fields

All trackers support custom fields, but with different approaches:

**Jira:**
```json
{
  "testCasesCustomFields": ["customfield_10001", "customfield_10002"]
}
```

**ADO:**
```json
{
  "testCasesCustomFields": ["Custom.FieldName", "Custom.AnotherField"]
}
```

---

## Multi-Tracker Setup (Advanced)

You can configure multiple trackers simultaneously:

```bash
# Jira for user stories
JIRA_BASE_PATH=https://company.atlassian.net
JIRA_EMAIL=user@company.com
JIRA_API_TOKEN=ATATT...

# ADO for test cases
ADO_BASE_PATH=https://dev.azure.com/company
ADO_PROJECT=TestProject
ADO_TOKEN=ado-pat-token
```

**Note:** TestCasesGenerator uses one tracker per job. Specify tracker in job config if needed.

---

## Troubleshooting

### 401 Unauthorized

**Jira:**
```bash
# Check token validity
curl -u "your-email@company.com:YOUR_TOKEN" \
  https://your-company.atlassian.net/rest/api/3/myself

# Should return your user info
```

**ADO:**
```bash
# Check PAT validity
curl -u ":YOUR_TOKEN" \
  https://dev.azure.com/{organization}/_apis/projects?api-version=7.0
```

### 404 Not Found

**Problem:** Wrong base path or project

**Solution:**
```bash
# Jira Cloud (correct)
JIRA_BASE_PATH=https://your-company.atlassian.net
# Not: https://your-company.atlassian.net/rest/api/3  # ❌

# ADO (correct)
ADO_BASE_PATH=https://dev.azure.com/your-organization
# Not: https://dev.azure.com/your-organization/your-project  # ❌
```

### Rate Limiting

**Problem:** 429 Too Many Requests

**Solution:**
```bash
# Increase delay between requests
SLEEP_TIME_REQUEST=500  # milliseconds

# Or reduce search results
JIRA_MAX_SEARCH_RESULTS=500
```

### Slow Performance

**Problem:** Test case fetching is slow

**Solution for Jira + Xray:**
```bash
# Enable parallel fetch
XRAY_PARALLEL_FETCH_ENABLED=true
XRAY_PARALLEL_THREADS=3
XRAY_PARALLEL_DELAY_MS=250
```

---

## Security Best Practices

### 🔒 Protect Credentials

```bash
# Secure environment file
chmod 600 dmtools.env

# Never commit credentials
echo "dmtools.env" >> .gitignore
```

### 🔑 Token Permissions

**Jira - Minimum Required:**
- ✅ Browse Projects
- ✅ Create Issues
- ✅ Edit Issues
- ✅ Add Comments
- ✅ Link Issues

**ADO - Minimum Required:**
- ✅ Work Items (Read, Write)
- ✅ Test Management (Read, Write)

### 🔄 Rotate Tokens Regularly

- Rotate every 90 days
- Use different tokens for dev/prod
- Revoke old tokens immediately

---

## Next Steps

✅ **Tracker configured!** Now configure Confluence for rules:

👉 **[Confluence Configuration](confluence-configuration.md)** - For rules and documentation

👉 **[AI Provider Configuration](ai-providers.md)** - Configure AI models

👉 **[TestCasesGenerator Guide](../jobs/TestCasesGenerator.md)** - Start generating test cases

---

## See Also

- [Complete Configuration Guide](../getting-started/configuration.md)
- [MCP Tools Reference](../README-MCP.md)
- [Jira MCP Tools](../mcp/README.md#jira-tools)
- [ADO MCP Tools](../mcp/ado_mcp.md)
