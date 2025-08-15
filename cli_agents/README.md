# CLI Agents - Refactored Structure

This directory contains the refactored CLI agents infrastructure, replacing the previous aider-specific structure with a more generic approach that supports multiple CLI tools.

## Structure

```
cli_agents/
├── prompts/                 # AI prompts (tool-agnostic)
│   ├── assist-prompt.md     # Analysis/assistance prompt
│   ├── coding-prompt.md     # General coding workflow prompt
│   ├── discovery-prompt.md  # Phase 1: File discovery
│   └── implementation-prompt.md # Phase 2: Implementation
├── scripts/                 # Reusable shell scripts
│   ├── setup-cli-env.sh     # Setup environment for different CLI tools
│   ├── prepare-user-request.sh # Process user requests (base64/text)
│   ├── extract-response.sh  # Extract structured responses
│   ├── run-aider.sh         # Execute Aider with parameters
│   ├── run-gemini.sh        # Execute Gemini CLI with parameters
│   └── git-workflow.sh      # Handle Git operations (DMTools standards)
└── README.md               # This file
```

## Key Changes

1. **Tool-Agnostic Prompts**: Removed aider-specific references and folder paths
2. **Unified Scripts**: Extracted complex shell logic from workflows into reusable scripts
3. **Standardized Output**: Uses `outputs/` directory instead of `aider-outputs/`
4. **Response Tags**: Supports both `<AIDER_RESPONSE>` and `<RESPONSE>` tags
5. **DMTools Integration**: Git workflow script follows DMTools branching and commit standards

## Supported CLI Tools

- **Aider**: For iterative code editing with AI assistance
- **Gemini CLI**: For Google Gemini API interactions
- **Extensible**: Easy to add new CLI tools

## Workflow Integration

The GitHub Actions workflows now use this structure:
- `.github/workflows/aider-gemini-assist.yml` - Analysis/assistance workflow
- `.github/workflows/gemini-cli-implementation.yml` - Two-phase implementation workflow

Both workflows use the shared action:
- `.github/actions/aider-setup/action.yml` - Unified CLI agent setup

## Usage

### Direct Script Usage

```bash
# Setup environment
./cli_agents/scripts/setup-cli-env.sh "aider" "gemini/gemini-2.5-flash-preview-05-20" "$API_KEY"

# Prepare user request
./cli_agents/scripts/prepare-user-request.sh "$USER_REQUEST" "outputs"

# Run Aider
./cli_agents/scripts/run-aider.sh "cli_agents/prompts/assist-prompt.md" "outputs/user-request.txt" "" "gemini/gemini-2.5-flash-preview-05-20" "1000000" "analysis" "outputs"

# Run Gemini CLI
./cli_agents/scripts/run-gemini.sh "discovery" "outputs/user-request.txt" "gemini-2.5-flash-preview-05-20" "false" "false" "outputs"

# Extract response
./cli_agents/scripts/extract-response.sh "outputs/response_file.txt" "RESPONSE" "0"

# Git workflow
./cli_agents/scripts/git-workflow.sh "full-workflow" "DMC-123" "Implementation message" "PR Title"
```

### GitHub Actions Usage

The workflows automatically use these scripts through the shared action, providing a clean separation of concerns and easier maintenance.

## Migration from Old Structure

- `aider/assist-prompt.md` → `cli_agents/prompts/assist-prompt.md`
- `aider/discovery-prompt.md` → `cli_agents/prompts/discovery-prompt.md`
- `aider/implementation-prompt.md` → `cli_agents/prompts/implementation-prompt.md`
- `aider/coding-prompt.md` → `cli_agents/prompts/coding-prompt.md`
- `aider-outputs/` → `outputs/`
- Complex workflow steps → Modular scripts in `cli_agents/scripts/`

All references to aider-specific terms have been removed from prompts, making them suitable for any CLI tool that supports structured responses.
