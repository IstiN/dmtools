# AI Providers Configuration

Configure AI providers for automation jobs (TestCasesGenerator, CodeGenerator, UserStoryGenerator, etc.).

## Overview

**At least one AI provider must be configured** for AI-powered jobs to work. Jobs use AI to:
- **TestCasesGenerator**: Generate test cases from user stories, find related tests, deduplicate
- **CodeGenerator**: Generate code from requirements
- **UnitTestsGenerator**: Generate unit tests for code
- **UserStoryGenerator**: Create user stories from requirements
- **RequirementsCollector**: Analyze and structure requirements
- **DocumentationGenerator**: Generate technical documentation

---

## Supported AI Providers

| Provider | Access Method | Best For | Cost | Setup Difficulty |
|----------|---------------|----------|------|------------------|
| **Gemini** | Direct | Fast responses, most features | Low (free tier) | Easy ⭐⭐⭐ |
| **DIAL** | Proxy (OpenAI GPT-5) | Enterprise, internal billing | Varies | Medium ⭐⭐ |
| **Bedrock** | AWS (Anthropic Claude) | Long context, AWS integrated | Medium | Hard ⭐ |
| **Ollama** | Local | Private, free, offline | Free | Medium ⭐⭐ |

---

## Google Gemini (Recommended)

**Why Gemini:**
- ✅ Free tier available (15 requests/minute)
- ✅ Fast response times
- ✅ Excellent quality for all automation jobs
- ✅ Easy to set up
- ✅ Long context window (2M tokens)

### Setup Steps

#### 1. Get API Key

1. Go to https://aistudio.google.com/app/apikey
2. Click **"Create API key"**
3. Select your Google Cloud project (or create new one)
4. Copy the API key (starts with `AIza`)

#### 2. Configure in dmtools.env

```bash
# Google Gemini Configuration
GEMINI_API_KEY=AIzaSyD1234567890abcdefghijklmnopqrstuvwxyz
GEMINI_DEFAULT_MODEL=gemini-3-pro-preview
GEMINI_BASE_PATH=https://generativelanguage.googleapis.com/v1beta
```

#### 3. Test Connection

```bash
dmtools gemini_ai_chat "Hello, please respond with OK"
```

**Expected output:** Response from Gemini AI

### Recommended Models

| Model | Use Case | Context Window |
|-------|----------|----------------|
| `gemini-3-pro-preview` | **Recommended** - Latest, best quality | 2M tokens |
| `gemini-3-flash` | Faster, cheaper responses | 1M tokens |

---

## EPAM DIAL (Enterprise - OpenAI GPT-5)

**Why DIAL:**
- ✅ Enterprise AI proxy
- ✅ Access to latest OpenAI models (GPT-5)
- ✅ Internal company billing
- ✅ Multiple models available
- ⚠️ Requires EPAM infrastructure access

### Setup Steps

#### 1. Get Access

Contact your company's AI platform team for:
- DIAL endpoint URL
- API key
- Available models list

#### 2. Configure in dmtools.env

```bash
# EPAM DIAL Configuration (for OpenAI GPT-5)
DEFAULT_LLM=dial
DIAL_BATH_PATH=https://ai-proxy.lab.epam.com/
DIAL_API_KEY=your-dial-api-key
DIAL_MODEL=gpt-5.2-chat
```

#### 3. Test Connection

```bash
dmtools dial_ai_chat "Test message"
```

### Available GPT-5 Models via DIAL

| Model | Use Case | Context Window |
|-------|----------|----------------|
| `gpt-5.2-chat` | **Recommended** - Latest GPT-5, best quality | 256K tokens |
| `gpt-5-mini` | Fast, cost-effective | 128K tokens |
| `gpt-5-nano` | Very fast, cheapest | 64K tokens |
| `gpt-4o` | Previous generation (via DIAL) | 128K tokens |

### Other Models via DIAL

DIAL also provides access to:
- `anthropic.claude-4-5-sonnet` - Claude via Bedrock
- `gemini-3-pro-preview` - Gemini via DIAL

---

## AWS Bedrock (Anthropic Claude)

**Why Bedrock:**
- ✅ Access to Anthropic Claude models
- ✅ AWS integrated
- ✅ Enterprise compliance
- ✅ Excellent for analysis and long context
- ⚠️ Complex setup (AWS credentials required)

### Setup Steps

#### 1. Configure AWS Credentials

```bash
# AWS Configuration (in ~/.aws/credentials or environment)
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
```

#### 2. Enable Bedrock Models

In AWS Console:
1. Go to Amazon Bedrock
2. Click **"Model access"**
3. Enable desired Anthropic models

#### 3. Configure in dmtools.env

```bash
# AWS Bedrock Configuration (for Anthropic Claude)
BEDROCK_REGION=us-east-1
BEDROCK_MODEL=anthropic.claude-4-5-sonnet-v2
```

#### 4. Test Connection

```bash
dmtools bedrock_ai_chat "Test message"
```

### Recommended Claude Models via Bedrock

| Model | Use Case | Context Window |
|-------|----------|----------------|
| `anthropic.claude-4-5-sonnet-v2` | **Recommended** - Best balance | 200K tokens |
| `anthropic.claude-4-5-opus` | Highest quality, expensive | 200K tokens |
| `anthropic.claude-4-5-haiku` | Fastest, cheapest | 200K tokens |
| `anthropic.claude-3-5-sonnet-v2` | Previous generation | 200K tokens |

---

## Ollama (Local)

**Why Ollama:**
- ✅ Free, runs locally
- ✅ Private, no data sent to cloud
- ✅ Multiple open-source models
- ✅ Offline support
- ⚠️ Requires local GPU (recommended)
- ⚠️ Lower quality than cloud models

### Setup Steps

#### 1. Install Ollama

```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.com/install.sh | sh

# Windows
# Download from https://ollama.com/download
```

#### 2. Pull a Model

```bash
# Recommended for test case generation
ollama pull llama3.1:70b

# Or smaller model (faster, lower quality)
ollama pull llama3.1:8b

# Latest models
ollama pull llama3.2:90b
```

#### 3. Start Ollama Service

```bash
ollama serve
```

#### 4. Configure in dmtools.env

```bash
# Ollama Configuration
OLLAMA_BASE_PATH=http://localhost:11434
OLLAMA_MODEL=llama3.1:70b
```

#### 5. Test Connection

```bash
dmtools ollama_ai_chat "Test message"
```

---

## Model Selection for Different Jobs

You can specify different models for different jobs:

### TestCasesGenerator

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "modelTestCasesCreation": "gemini-3-pro-preview",
    "modelTestCasesRelation": "gemini-3-pro-flash",
    "modelTestCaseRelation": "gemini-3-pro-flash",
    "modelTestCaseDeduplication": "gemini-3-pro-flash"
  }
}
```

**Recommended:**
- **Creation**: `gemini-3-pro-preview`, `gpt-5.2-chat`, `claude-4-5-sonnet-v2` (high quality)
- **Relation/Dedup**: `gemini-3-pro-flash`, `gpt-5-mini` (fast, cheaper)

### CodeGenerator (Deprecated)

```json
{
  "name": "CodeGenerator",
  "params": {
    "model": "gpt-5.2-chat"
  }
}
```

**Recommended:**
- `gpt-5.2-chat` (best for code)
- `gemini-3-pro-preview` (good alternative)
- `claude-4-5-sonnet-v2` (excellent for complex code)

### UserStoryGenerator

```json
{
  "name": "UserStoryGenerator",
  "params": {
    "model": "gemini-3-pro-preview"
  }
}
```

**Recommended:**
- `gemini-3-pro-preview` (fast, good quality)
- `claude-4-5-sonnet-v2` (excellent for requirements analysis)

### UnitTestsGenerator

```json
{
  "name": "UnitTestsGenerator",
  "params": {
    "model": "gpt-5.2-chat"
  }
}
```

**Recommended:**
- `gpt-5.2-chat` (best for test code)
- `gemini-3-pro-preview` (good alternative)

---

## Cost Optimization Strategy

**High-Quality Models** (for main generation):
- TestCasesGenerator: `modelTestCasesCreation`
- CodeGenerator: main code generation
- UserStoryGenerator: story creation

**Fast/Cheap Models** (for auxiliary tasks):
- TestCasesGenerator: relation finding, deduplication
- Post-processing, validation

**Example Configuration:**
```json
{
  "modelTestCasesCreation": "gemini-3-pro-preview",    // High quality
  "modelTestCasesRelation": "gemini-3-flash",      // Fast, cheap
  "modelTestCaseDeduplication": "gemini-3-flash"   // Fast, cheap
}
```

---

## Rate Limits & Retry Configuration

Configure retry behavior for API failures:

```bash
# AI Retry Configuration
AI_RETRY_AMOUNT=3
AI_RETRY_DELAY_STEP=20000  # milliseconds (20 seconds)
```

### Provider Rate Limits

| Provider | Free Tier Limit | Paid Tier |
|----------|----------------|-----------|
| **Gemini** | 15 req/min | 1000 req/min |
| **DIAL** | Varies | Varies by company |
| **Bedrock** | N/A | Depends on AWS tier |

---

## Troubleshooting

### 401 Unauthorized

**Problem:** Invalid API key

**Solution:**
```bash
# Check API key format
echo $GEMINI_API_KEY  # Should start with AIza
echo $DIAL_API_KEY    # Check with your DIAL admin

# Regenerate key if needed
```

### 429 Too Many Requests

**Problem:** Rate limit exceeded

**Solution:**
```bash
# Increase retry delay
AI_RETRY_DELAY_STEP=30000  # 30 seconds

# Or use faster/cheaper model
GEMINI_DEFAULT_MODEL=gemini-3-pro-flash
```

### Connection Timeout

**Problem:** Network issues or wrong base path

**Solution:**
```bash
# Check base path
GEMINI_BASE_PATH=https://generativelanguage.googleapis.com/v1beta  # Correct

# Test connection
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"
```

### Model Not Available

**Problem:** Model doesn't exist or not enabled

**Solution:**
```bash
# For Bedrock - check AWS Console for enabled models
# For DIAL - check with your DIAL admin

# Use correct model name
GEMINI_DEFAULT_MODEL=gemini-3-pro-preview  # Correct
```

---

## Security Best Practices

### 🔒 Protect API Keys

```bash
# Secure environment file
chmod 600 dmtools.env

# Never commit API keys
echo "dmtools.env" >> .gitignore
```

### 🔄 Rotate Keys Regularly

- Rotate API keys every 90 days
- Use different keys for dev/prod
- Revoke old keys immediately after rotation

### 📊 Monitor Usage

- Enable billing alerts in provider console
- Track API usage regularly
- Set up cost budgets

---

## Next Steps

✅ **AI Provider configured!** Now configure your tracker:

👉 **[Tracker Configuration](tracker-configuration.md)** - Jira, ADO, Rally

👉 **[Confluence Configuration](confluence-configuration.md)** - For rules and documentation

👉 **[TestCasesGenerator Guide](../jobs/TestCasesGenerator.md)** - Start generating test cases

---

## See Also

- [Complete Configuration Guide](../getting-started/configuration.md)
- [MCP Tools Reference](../README-MCP.md)
- [Jobs Documentation](../jobs/README.md)
- [TestCasesGenerator Documentation](../jobs/TestCasesGenerator.md)
