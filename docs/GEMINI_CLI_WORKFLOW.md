# Gemini CLI Two-Phase Implementation Workflow

## Overview

This workflow provides an alternative to the Aider-based implementation using [Google's Gemini CLI GitHub Action](https://github.com/google-github-actions/run-gemini-cli). It follows the same two-phase approach but leverages Google's native Gemini integration.

## Key Differences from Aider Workflow

| Aspect | Aider Workflow | Gemini CLI Workflow |
|--------|----------------|---------------------|
| **Tool** | Third-party Aider CLI | Google's official Gemini CLI |
| **Installation** | pip install aider-install | Built into the action |
| **File Access** | Direct file system access | Context provided via prompts |
| **Git Integration** | Native git commands in Aider | Manual git operations in workflow |
| **Model Support** | Gemini via API | Gemini API + Vertex AI support |
| **Caching** | Extensive caching system | Action-level caching |
| **File Discovery** | JSON + automatic file inclusion | JSON parsing + manual handling |

## Setup Requirements

### 1. Gemini API Key

1. **Get API Key**: Visit [Google AI Studio](https://aistudio.google.com/) to obtain your Gemini API key
2. **Add to Repository Secrets**:
   - Go to repository **Settings > Secrets and variables > Actions**
   - Click **New repository secret**
   - Name: `GEMINI_API_KEY`
   - Value: Your API key from Google AI Studio

### 2. Optional: Vertex AI Setup

For enhanced security and enterprise features, you can use Vertex AI instead of the direct API:

1. **Set up Google Cloud Project**
2. **Configure Workload Identity Federation** (recommended over service account keys)
3. **Set repository variables**:
   - `GCP_PROJECT_ID`: Your Google Cloud project ID
   - `GCP_WIF_PROVIDER`: Workload Identity Provider resource name
   - `GCP_SERVICE_ACCOUNT`: Service account email

For detailed Vertex AI setup, see the [Authentication documentation](https://github.com/google-github-actions/run-gemini-cli#authentication).

## Workflow Features

### ✅ **Two-Phase Implementation**
- **Phase 1**: File discovery and analysis using Gemini CLI
- **Phase 2**: Implementation using discovered file context

### ✅ **Model Selection**
- Support for latest Gemini models:
  - `gemini-2.0-flash-exp` (default)
  - `gemini-1.5-pro-latest`
  - `gemini-1.5-flash-latest`
  - `gemini-1.0-pro-latest`

### ✅ **Enhanced Code Generation**
- **Gemini Code Assist**: Optional enhanced code generation capabilities
- **Base64 Input Support**: Handles both plain text and base64 encoded requests
- **Dynamic Prompt Loading**: Reads prompts from `aider/discovery-prompt.md` and `aider/implementation-prompt.md` files
- **Context Combination**: Automatically combines prompts with user request and discovery results

### ✅ **DMTools Integration**
- Enforces DMC-XXX ticket requirement
- Follows DMTools branching and commit standards
- Automatic PR creation with structured descriptions

### ✅ **Comprehensive Logging**
- Discovery analysis results
- Implementation summaries
- Artifact uploads for debugging

## Usage

### Basic Usage

1. **Navigate** to repository Actions tab
2. **Select** "Gemini CLI Two-Phase Implementation" workflow
3. **Click** "Run workflow"
4. **Fill in**:
   - **User Request**: Your implementation request (must include DMC-XXX, supports base64 encoding)
   - **Model**: Choose Gemini model (optional, default: gemini-2.0-flash-exp)
   - **Use Vertex AI**: Check if using Vertex AI (optional)
   - **Use Gemini Code Assist**: Enable enhanced code generation (optional)
   - **PR Title**: Custom title (optional)
   - **PR Base Branch**: Target branch for PR (optional, default: main)

### Example Requests

```
DMC-413 - Fix 500 Internal Server Error when deleting AI Job Configuration that has associated job executions by implementing cascading deletion of related job executions and logs.
```

```
DMC-456 - Add user authentication endpoint with OAuth2 integration including JWT token generation, refresh token support, and user profile management.
```

## Dynamic Prompt Loading

Unlike hardcoded prompts, this workflow dynamically reads and combines prompt files:

### Discovery Phase Process:
1. **Read** `aider/discovery-prompt.md` file content
2. **Read** user request from `gemini-outputs/user-request.txt`
3. **Combine** into `gemini-outputs/discovery-prompt-combined.md`:
   ```markdown
   # Discovery Phase - User Request Context
   
   **User Request:**
   ```
   [User's actual request here]
   ```
   
   ---
   
   [Full content of aider/discovery-prompt.md]
   ```

### Implementation Phase Process:
1. **Read** `aider/implementation-prompt.md` file content  
2. **Read** user request from `gemini-outputs/user-request.txt`
3. **Read** discovery results from `gemini-outputs/affected-files.json`
4. **Combine** into `gemini-outputs/implementation-prompt-combined.md`:
   ```markdown
   # Implementation Phase - Context
   
   **User Request:**
   ```
   [User's actual request here]
   ```
   
   **Discovery Results:**
   ```json
   [Complete discovery JSON here]
   ```
   
   ---
   
   [Full content of aider/implementation-prompt.md]
   ```

### Benefits:
- ✅ **No Hardcoding**: Prompts read from actual files, not embedded in workflow
- ✅ **Easy Updates**: Change prompts by editing `.md` files, no workflow changes needed
- ✅ **Context Rich**: Combines prompts with user request and discovery results
- ✅ **Consistency**: Uses same prompt files as Aider workflow
- ✅ **Debugging**: Combined prompts saved as artifacts for inspection

## Workflow Structure

### Phase 1: Discovery
```yaml
- name: Phase 1 - File Discovery and Analysis
  run: |
    # Read the combined prompt content
    DISCOVERY_PROMPT_CONTENT=$(cat gemini-outputs/discovery-prompt-combined.md)
    
    # Export environment variables for Gemini CLI
    export GEMINI_API_KEY="${{ secrets.GEMINI_API_KEY }}"
    export GOOGLE_GENAI_USE_VERTEXAI="${{ github.event.inputs.use_vertex_ai }}"
    export GOOGLE_GENAI_USE_GCA="${{ github.event.inputs.use_gemini_code_assist }}"
    
    # Create settings file
    cat > .gemini/settings.json << 'EOF'
    {
      "model": "${{ github.event.inputs.model }}",
      "temperature": 0.1,
      "maxOutputTokens": 8192
    }
    EOF
    
    # Run Gemini CLI with prompt content
    GEMINI_RESPONSE=$(gemini --yolo --prompt "$DISCOVERY_PROMPT_CONTENT")
```

### Phase 2: Implementation  
```yaml
- name: Phase 2 - Implementation using discovered files
  run: |
    # Read the combined prompt content
    IMPLEMENTATION_PROMPT_CONTENT=$(cat gemini-outputs/implementation-prompt-combined.md)
    
    # Run Gemini CLI with prompt content
    GEMINI_RESPONSE=$(gemini --yolo --prompt "$IMPLEMENTATION_PROMPT_CONTENT")
```

## Output Structure

### Discovery Phase Output
The workflow generates `gemini-outputs/affected-files.json`:
```json
{
  "ticket_number": "DMC-XXX",
  "request_summary": "Brief description",
  "affected_files": {
    "to_modify": ["file1.java", "file2.java"],
    "to_create": ["newfile.java"],
    "to_reference": ["contextfile.java"],
    "tests_needed": ["test1Test.java"]
  },
  "dependencies": ["relationship descriptions"],
  "implementation_notes": ["important considerations"],
  "estimated_complexity": "MEDIUM",
  "potential_risks": ["risk assessments"]
}
```

### Implementation Phase Output
- **Code Changes**: Actual file modifications and creations
- **Git Branch**: Automatically created with unique naming
- **Pull Request**: Auto-created with comprehensive description
- **Artifacts**: All logs and outputs uploaded for review

## Advantages of Gemini CLI Approach

### ✅ **Official Google Integration**
- Native support for all Gemini models
- Direct integration with Vertex AI
- Official maintenance and updates

### ✅ **Enterprise Security**
- Workload Identity Federation support
- No third-party tool dependencies
- Google Cloud security compliance

### ✅ **Simplified Setup**
- No complex installation or caching
- Built-in model management
- Streamlined configuration

### ✅ **Consistent Experience**
- Same interface as other Google AI tools
- Unified authentication across Google services
- Integrated with Google Cloud ecosystem

## Considerations

### 🔍 **Context Limitations**
Unlike Aider which can directly access files, Gemini CLI receives context through prompts. The workflow handles this by:
- Parsing discovery JSON manually
- Including relevant file content in prompts
- Managing git operations externally

### 🔍 **File Handling**
- Discovery phase generates file lists
- Implementation phase receives context via prompts
- Manual git operations for branch/commit management

### 🔍 **Debugging**
- All interactions logged to artifacts
- Comprehensive error handling
- Fallback mechanisms for failed discovery

## Comparison Matrix

| Feature | Aider Workflow | Gemini CLI Workflow | Winner |
|---------|----------------|---------------------|---------|
| **Setup Complexity** | High (installation, caching) | Low (just API key) | 🟢 Gemini CLI |
| **File Access** | Direct filesystem | Prompt-based | 🟢 Aider |
| **Git Integration** | Native | Manual | 🟢 Aider |
| **Official Support** | Third-party | Google official | 🟢 Gemini CLI |
| **Model Updates** | Manual | Automatic | 🟢 Gemini CLI |
| **Enterprise Security** | Limited | Full Vertex AI | 🟢 Gemini CLI |
| **Debugging** | File-based logs | Artifact uploads | 🟡 Tie |
| **Context Management** | Automatic | Manual parsing | 🟢 Aider |

## When to Use Which

### Use **Aider Workflow** when:
- You need direct file system access
- Complex multi-file refactoring is required
- You prefer native git integration
- Local development patterns are important

### Use **Gemini CLI Workflow** when:
- You want official Google integration
- Enterprise security (Vertex AI) is required
- Simplified setup is preferred
- Consistent Google ecosystem experience is valued

Both workflows implement the same two-phase discovery pattern and produce similar results with different technical approaches.
