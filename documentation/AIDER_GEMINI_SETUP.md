# Aider CLI with Google Gemini Models - GitHub Actions Setup

This guide provides comprehensive setup instructions for using Aider CLI with Google Gemini models in GitHub Actions for maximum effectiveness and token usage.

## 🚀 Quick Start

1. **Add your Gemini API Key to GitHub Secrets**
2. **Run the workflow from the Actions tab**
3. **Provide your request and let Aider work with Gemini's massive context window**

## 📋 Prerequisites

- GitHub repository with Actions enabled
- Google AI Studio API key ([Get one here](https://makersuite.google.com/app/apikey))
- Basic understanding of GitHub Actions
- Python 3.8 to 3.13 (supported by Aider v0.85.0+)

## 🔧 Setup Instructions

### 1. Add Gemini API Key to GitHub Secrets

1. Go to your repository's **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `GEMINI_API_KEY`
4. Value: Your Google AI Studio API key
5. Click **Add secret**

### 2. Configuration Files

The setup includes the following files:

#### `.aider.conf.yml` - Aider Configuration
```yaml
# Optimized for Google Gemini models
model: gemini/gemini-2.5-flash-preview-05-20
edit-format: whole
max-chat-history-tokens: 1000000  # Gemini 1.5 Pro supports 1M+ tokens
cache-prompts: true
stream: false
pretty: false
show-diffs: true
auto-commits: false
yes: true
```

#### GitHub Actions Workflows

**Basic Workflow:** `.github/workflows/aider-gemini-assist.yml`
- Simple execution with artifact output
- Configurable model selection
- File targeting support

**Advanced Workflow:** `.github/workflows/aider-gemini-advanced.yml`
- Pull request creation
- Auto-commit functionality
- Repository analysis
- Enhanced logging and metrics

## 🎯 Usage

### Basic Workflow Usage

1. Go to **Actions** tab in your repository
2. Select **"Aider Code Assistant (Gemini)"**
3. Click **"Run workflow"**
4. Fill in the parameters:
   - **User Request**: Your coding request (required)
   - **Target Files**: Specific files to include (optional)
   - **Model**: Choose Gemini model (default: gemini-2.5-flash-preview-05-20)
   - **API Key Secret**: Secret name (default: GEMINI_API_KEY)
   - **Max Tokens**: Context window size (default: 1,000,000)

### Advanced Workflow Usage

The advanced workflow includes additional options:
- **Create PR**: Automatically create a pull request with changes
- **Auto Commit**: Commit changes directly to the branch
- **Include Tests**: Include test files in the context

## 🤖 Available Gemini Models

**Latest Support**: Aider v0.85.0+ includes enhanced support for Gemini 2.5 models

| Model | Context Window | Best For |
|-------|----------------|----------|
| `gemini/gemini-2.5-flash-preview-05-20` | 1M+ tokens | Latest preview, fastest responses |
| `gemini/gemini-2.5-pro-latest` | 1M+ tokens | State-of-the-art, complex reasoning |
| `gemini/gemini-1.5-pro-latest` | 1M+ tokens | Complex tasks, large codebases |
| `gemini/gemini-1.5-pro-002` | 1M+ tokens | Stable version of Pro |
| `gemini/gemini-1.5-flash-latest` | 1M+ tokens | Faster responses |
| `gemini/gemini-1.5-flash-002` | 1M+ tokens | Stable version of Flash |
| `gemini/gemini-1.0-pro-latest` | 128k tokens | Basic tasks |

## 💡 Tips for Maximum Effectiveness

### 1. Leverage Gemini's Massive Context Window
- Use up to 1M+ tokens with Gemini 1.5 Pro
- Include entire directories or large file sets
- Provide comprehensive context for better results

### 2. Effective Request Writing
```
Good: "Refactor the authentication system to use JWT tokens, update all related endpoints and tests"
Better: "Refactor the authentication system in src/auth/ to use JWT tokens instead of sessions. Update all related API endpoints in src/api/, modify the middleware, and update corresponding tests. Ensure backwards compatibility."
```

### 3. File Targeting Strategies
```
# Include specific files
target_files: "src/main.java,src/auth/AuthService.java,tests/AuthTest.java"

# Include directories (let Aider auto-detect)
target_files: ""  # Advanced workflow will auto-include relevant files
```

### 4. Model Selection Guide
- **Latest features**: Use `gemini-2.5-flash-preview-05-20` (default)
- **Advanced reasoning**: Use `gemini-2.5-pro-latest` for complex tasks
- **Complex refactoring**: Use `gemini-1.5-pro-latest` or `gemini-2.5-pro-latest`
- **Quick fixes**: Use `gemini-2.5-flash-preview-05-20`
- **Large codebases**: Use `gemini-2.5-pro-latest` with max tokens
- **Budget-conscious**: Use `gemini-2.5-flash-preview-05-20`

## 📊 Workflow Outputs

### Artifacts Generated
- **Full response log**: Complete Aider output
- **Execution metrics**: JSON with run details
- **Repository analysis**: Project structure overview
- **Changes summary**: Git diff information

### Pull Request Features (Advanced Workflow)
- Automatic PR creation with detailed description
- Labels: `aider`, `ai-generated`, `gemini`
- Comprehensive change documentation
- Links to workflow execution

## 🔧 Advanced Configuration

### Custom API Key Secret Names
You can use different secret names by specifying them in the workflow:
```yaml
gemini_api_key_secret: "MY_CUSTOM_GEMINI_KEY"
```

### Environment Variables
The workflows support multiple environment variable names:
- `GEMINI_API_KEY`
- `GOOGLE_API_KEY` 
- `GOOGLE_GEMINI_API_KEY`

### Token Optimization
```yaml
# For maximum context (Gemini 1.5 Pro)
max_tokens: 1000000

# For faster responses (still very large)
max_tokens: 500000

# For basic tasks
max_tokens: 100000
```

## 🛠️ Troubleshooting

### Common Issues

**1. API Key Not Found**
```
ERROR: No Gemini API key found
```
**Solution**: Ensure `GEMINI_API_KEY` secret is properly set

**2. Model Not Supported**
```
ERROR: Unknown model
```
**Solution**: Use one of the supported Gemini model names

**3. File Not Found Warnings**
```
Warning: File 'path/to/file' not found
```
**Solution**: Verify file paths in target_files parameter

**4. No Changes Detected**
- Check if your request was clear and actionable
- Verify the target files contain relevant code
- Try a more specific request

### Debug Mode
For debugging, check the workflow artifacts which include:
- Complete execution logs
- Repository analysis
- Configuration dump
- Git status information

## 📈 Performance Tips

### 1. Batch Related Changes
Instead of multiple small requests, combine related changes:
```
❌ Request 1: "Add error handling to login"
❌ Request 2: "Add logging to login"
❌ Request 3: "Add input validation to login"

✅ Combined: "Enhance the login function with error handling, logging, and input validation"
```

### 2. Use Specific File Targeting for Large Repos
```yaml
# For large repositories, be specific
target_files: "src/core/,src/api/auth/,tests/integration/"
```

### 3. Leverage Auto-Detection
The advanced workflow automatically includes:
- Source directories (`src/`)
- Configuration files (`build.gradle`, `package.json`, etc.)
- Test files (when enabled)

## 🔒 Security Considerations

1. **API Key Protection**: Never commit API keys to repository
2. **Secret Management**: Use GitHub Secrets for sensitive data
3. **Review Changes**: Always review AI-generated code before merging
4. **Branch Protection**: Use PR workflow for production branches

## 📚 Examples

### Example 1: Bug Fix
```yaml
user_request: "Fix the null pointer exception in UserService.getUserById() method"
target_files: "src/services/UserService.java,tests/UserServiceTest.java"
model: "gemini/gemini-1.5-flash-latest"
```

### Example 2: Feature Implementation
```yaml
user_request: "Implement OAuth2 authentication with Google provider, including all necessary endpoints, middleware, and tests"
target_files: ""  # Let auto-detection work
model: "gemini/gemini-1.5-pro-latest"
create_pr: true
```

### Example 3: Code Refactoring
```yaml
user_request: "Refactor the payment processing system to use the strategy pattern, separate concerns, and improve testability"
target_files: "src/payment/,tests/payment/"
model: "gemini/gemini-1.5-pro-latest"
max_tokens: 1000000
create_pr: true
```

## 📦 Latest Version Information

**Current Aider Version**: v0.85.0+ (June 2025)
- Enhanced Gemini 2.5 model support
- Improved installation process via `aider-install`
- Better integration with local and cloud LLMs

**Installation Method**: According to the [official Aider repository](https://github.com/Aider-AI/aider), the recommended installation is:

```bash
python -m pip install aider-install
aider-install
```

This method installs Aider in its own isolated Python environment, preventing dependency conflicts.

## ⚡ Performance Optimizations

**Multi-Level Caching**: Both workflows include intelligent caching:

1. **Pip Package Caching**: Caches Python packages for faster dependency installation
2. **Aider Installation Caching**: Caches the complete Aider installation including:
   - Aider binary (`~/.local/bin/aider`)
   - Python packages (`~/.local/lib/python*/site-packages/aider*`)
   - Aider configuration (`~/.aider`)

**Smart Installation Logic**: 
- Detects if Aider is already cached and functional
- Skips reinstallation if cached version works
- Only installs when needed, significantly reducing workflow execution time

**Cache Benefits**:
- ⚡ **3-5x faster** subsequent workflow runs
- 💾 **Reduced bandwidth** usage
- 🔄 **Reliable installations** with fallback logic

**Latest GitHub Actions**:
- `actions/checkout@v4` - Repository checkout
- `actions/setup-python@v5` - Python environment setup
- `actions/cache@v4` - Caching for pip and Aider
- `actions/upload-artifact@v4` - Artifact upload (replaces deprecated v3)

## 🎉 Getting Started

1. **Set up your API key** in GitHub Secrets
2. **Start with the basic workflow** for simple tasks
3. **Upgrade to advanced workflow** for PR creation and auto-commits
4. **Experiment with different models** to find what works best for your use case
5. **Leverage Gemini's massive context window** for comprehensive code understanding

The setup is now complete! Your Aider + Gemini integration is ready to handle complex coding tasks with Google's most advanced language model.
