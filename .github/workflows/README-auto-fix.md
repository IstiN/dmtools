# Auto-fix Test Failures System

## Overview

The auto-fix test failures system automatically detects when unit tests fail in pull requests and uses the agentic workflows framework to analyze and fix the issues. The system is built using a reusable workflow pattern for maximum flexibility and maintainability.

## How It Works

### 1. Trigger Detection
- **Workflow**: `auto-fix-test-failures.yml`
- **Trigger**: `workflow_run` on completion of "PR Unit Tests" workflow
- **Condition**: Only triggers when the workflow conclusion is 'failure'
- **Scope**: Runs on all branches (`**`)

### 2. Failure Analysis
The system performs the following checks:
- ✅ Verifies the failure is from a test-related workflow (contains "Test", "CI", "Unit", or "Integration")
- 🔍 Extracts the branch name where the failure occurred
- 📋 Gathers PR information if the branch has an associated pull request
- 📊 Collects failure context including workflow URLs and failed job details

### 3. Context Preparation
Creates a comprehensive failure context for Gemini including:
- **Branch name** and **PR number** (if applicable)
- **Failed workflow URL** for detailed log analysis  
- **Failed job names** and their conclusions
- **Specific instructions** for Gemini to:
  - Analyze test failure logs
  - Identify root cause issues
  - Fix underlying code problems
  - Ensure all tests pass
  - Maintain existing functionality

### 4. Auto-fix Execution
- 🔄 **Reusable Workflow**: Uses `IstiN/dmtools-agentic-workflows/.github/workflows/reusable-auto-fix-test-failures.yml`
- 📤 **Context Passing**: Sends comprehensive failure details with workflow URL and branch info  
- 🎯 **Specialized Prompts**: Uses dedicated auto-fix prompt template optimized for test failure resolution
- ⚙️ **Model Selection**: Uses `gemini-2.5-flash-preview-05-20` with debug logging enabled
- 📋 **Custom Rules**: Applies DMTools-specific test fixing rules via `test-fixing-rules.md`

### 5. PR Communication
If a PR exists for the failing branch:
- 💬 Adds a comment explaining that auto-fix has been triggered
- 🔗 Provides links to the failed workflow and actions tab
- ✨ Uses clear emoji indicators for easy visual scanning

## Workflow Configuration

### Target Workflows
Currently monitors:
```yaml
workflows: ["PR Unit Tests"]
```

To add more workflows, update the array:
```yaml
workflows: ["PR Unit Tests", "Integration Tests", "E2E Tests"]
```

### Reusable Workflow Configuration
```yaml
uses: IstiN/dmtools-agentic-workflows/.github/workflows/reusable-auto-fix-test-failures.yml@main
with:
  failure_context: ${{ needs.check-test-failures.outputs.failure-context }}
  branch_name: ${{ needs.check-test-failures.outputs.branch-name }}
  workflow_run_url: ${{ needs.check-test-failures.outputs.workflow-run-url }}
  pr_number: ${{ needs.check-test-failures.outputs.pr-number }}
  
  # AI Configuration
  model: 'gemini-2.5-flash-preview-05-20'
  pr_base_branch: ${{ needs.check-test-failures.outputs.branch-name }}
  enable_debug_logging: true
  
  # Project-specific customizations
  custom_rules_file: 'dmtools/test-fixing-rules.md'
  workflows_repo: 'IstiN/dmtools-agentic-workflows'
  workflows_ref: 'main'
```

### Permissions Required
```yaml
permissions:
  contents: write      # For code modifications
  pull-requests: write # For PR comments
  issues: write        # For issue management  
  actions: read        # For reading workflow details
```

### Required Secrets
- `PAT_TOKEN`: Personal Access Token for GitHub API access
- `GEMINI_API_KEY`: API key for Gemini AI (passed to reusable workflow)

## Example Flow

1. 📝 **Developer creates PR** → Triggers "PR Unit Tests"
2. ❌ **Tests fail** → `auto-fix-test-failures.yml` detects failure  
3. 🔍 **System analyzes** → Extracts failure context and PR details
4. 🔐 **Context encoding** → Base64 encodes failure details for safe transmission
5. 🤖 **Gemini triggered** → `gemini-cli-implementation.yml` runs targeting failing branch
6. 🛠️ **AI fixes issues** → Gemini analyzes logs, fixes code in new branch
7. 📤 **Fix PR created** → New PR targeting the original failing branch
8. 🔄 **Merge fix PR** → Fixes are applied to the original failing branch
9. ✅ **Tests pass** → Original PR is now ready for review

## Monitoring

### Logs to Watch
- **Auto-fix workflow logs**: Check if failure detection and triggering works
- **Gemini implementation logs**: Monitor the actual fixing process
- **PR comments**: See auto-fix status updates

### Common Issues
- **False triggers**: Adjust workflow name regex if needed
- **Missing context**: Verify GitHub API access and token permissions
- **Failed fixes**: Check Gemini model selection and context quality

## Benefits

### Immediate Benefits:
- 🚀 **Faster feedback loop**: Automatic issue resolution without developer intervention
- 🎯 **Precise fixing**: AI has full context of what failed and why
- 📈 **Improved productivity**: Developers focus on features while AI handles test fixes
- 🔄 **Consistent process**: Standardized approach to test failure resolution

### Reusable Workflow Architecture Benefits:
- 🔧 **Modularity**: Separate detection logic from auto-fix implementation
- 🎨 **Customization**: Project-specific rules and prompts without workflow duplication  
- 🔄 **Reusability**: Same auto-fix workflow can be used across multiple projects
- 📦 **Maintainability**: Centralized auto-fix logic in dmtools-agentic-workflows
- 🚀 **Scalability**: Easy to add more projects using the same pattern
- 🎯 **Specialization**: Dedicated auto-fix prompt optimized for test failure resolution
- 🛠️ **Extensibility**: Easy to extend with additional context files and custom rules

## Technical Implementation Details

### Reusable Workflow Architecture
The system is built using a modular reusable workflow approach:

#### Components:
1. **Detection Layer** (`dmtools/.github/workflows/auto-fix-test-failures.yml`):
   - Monitors for test failures
   - Extracts failure context and metadata
   - Triggers the reusable auto-fix workflow

2. **Reusable Workflow** (`dmtools-agentic-workflows/.github/workflows/reusable-auto-fix-test-failures.yml`):
   - Specialized auto-fix implementation
   - Uses dedicated auto-fix prompt template
   - Handles Git workflow and PR creation

3. **Prompt Template** (`dmtools-agentic-workflows/prompts/auto-fix-test-failures-prompt.md`):
   - Optimized instructions for test failure analysis
   - Specific guidance for different failure types
   - Best practices for code fixes

### PR Targeting Strategy
The auto-fix uses a two-PR approach:
1. **Original PR**: Contains the failing tests (e.g., `feature/DMC-415-auth-config`)
2. **Fix PR**: Created by Gemini targeting the failing branch (e.g., `auto-fix/DMC-415-test-fixes`)

**Flow:**
```
Original Branch (failing tests)
    ↑
    └── Fix PR (contains fixes)
         └── Fix Branch (Gemini creates this)
```

This approach ensures:
- ✅ Fixes are isolated and reviewable
- ✅ Original PR history is preserved  
- ✅ Safe rollback if fixes cause issues
- ✅ Clear audit trail of automated changes

### Custom Rules Integration
Project-specific customization through:
- **Custom Rules File**: `dmtools/test-fixing-rules.md` with DMTools-specific context
- **Additional Context**: Files can be included for extra context
- **Prompt Customization**: Custom auto-fix prompts can override defaults

### Workflow Parameters
```yaml
# Core context
failure_context: "Detailed failure information..."
branch_name: "feature/branch-name"  
workflow_run_url: "https://github.com/.../actions/runs/..."
pr_number: "123"

# AI configuration
model: 'gemini-2.5-flash-preview-05-20'
pr_base_branch: ${{ needs.check-test-failures.outputs.branch-name }}
enable_debug_logging: true

# Customization
custom_rules_file: 'dmtools/test-fixing-rules.md'
workflows_repo: 'IstiN/dmtools-agentic-workflows'
workflows_ref: 'main'
```

## Customization

### Modify Failure Context
Edit the `FAILURE_CONTEXT` section in `get-failure-context` step to:
- Add more specific instructions
- Include additional log analysis
- Customize AI behavior for different failure types

### Change AI Model
Update the `model` parameter in the trigger step:
```bash
-f model="gemini-1.5-pro-latest"  # For more complex fixes
```

### Add Workflow Filters
Modify the workflow name regex:
```bash
if [[ "$WORKFLOW_NAME" =~ (Test|CI|Unit|Integration|E2E) ]]; then
```

This system creates a fully automated test failure resolution pipeline, reducing manual intervention and improving development velocity.
