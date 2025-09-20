# DMTools CLI

DMTools CLI provides command-line access to Jira, Confluence, and Figma integrations with 61 built-in tools.

## Quick Install

```bash
curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
```

This will:
- Download the latest DMTools JAR
- Install the `dmtools` command to `~/.dmtools/bin/`
- Add the binary to your PATH
- Set up shell integration

After installation, restart your shell or run:
```bash
source ~/.zshrc  # or ~/.bashrc
```

## GitHub Actions Integration

### Basic Setup
```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '23'

- name: Install DMTools CLI
  run: |
    curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
    echo "$HOME/.dmtools/bin" >> $GITHUB_PATH
```

### Optimized Setup with Caching (Recommended)
```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '23'

# Cache DMTools CLI to avoid re-downloading
- name: Cache DMTools CLI
  id: cache-dmtools
  uses: actions/cache@v4
  with:
    path: ~/.dmtools
    key: dmtools-cli-${{ runner.os }}-${{ hashFiles('.github/workflows/*.yml') }}
    restore-keys: |
      dmtools-cli-${{ runner.os }}-

# Install only if not cached
- name: Install DMTools CLI
  if: steps.cache-dmtools.outputs.cache-hit != 'true'
  run: |
    curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash

# Add to PATH
- name: Add DMTools to PATH
  run: echo "$HOME/.dmtools/bin" >> $GITHUB_PATH
```

### Complete Example with Environment Variables
```yaml
- name: Use DMTools CLI
  env:
    # Jira Configuration (Modern approach: separate email + API token)
    JIRA_EMAIL: ${{ secrets.JIRA_EMAIL }}
    JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
    JIRA_BASE_PATH: ${{ vars.JIRA_BASE_PATH }}
    JIRA_AUTH_TYPE: ${{ vars.JIRA_AUTH_TYPE }}
    JIRA_MAX_SEARCH_RESULTS: ${{ vars.JIRA_MAX_SEARCH_RESULTS }}
    
    # Confluence Configuration (Modern approach: separate email + API token)
    CONFLUENCE_EMAIL: ${{ secrets.CONFLUENCE_EMAIL }}
    CONFLUENCE_API_TOKEN: ${{ secrets.CONFLUENCE_API_TOKEN }}
    CONFLUENCE_BASE_PATH: ${{ vars.CONFLUENCE_BASE_PATH }}
    CONFLUENCE_DEFAULT_SPACE: ${{ vars.CONFLUENCE_DEFAULT_SPACE }}
    CONFLUENCE_AUTH_TYPE: ${{ vars.CONFLUENCE_AUTH_TYPE }}
    
    # Figma Configuration
    FIGMA_TOKEN: ${{ secrets.FIGMA_TOKEN }}
    FIGMA_BASE_PATH: ${{ vars.FIGMA_BASE_PATH }}
    
    # DMTools Settings
    DMTOOLS_INTEGRATIONS: "jira,confluence,figma"
  run: |
    dmtools jira_get_ticket PROJ-123
    dmtools confluence_content_by_title "My Page"
```

**Note**: Java 23 is required. The install script will detect GitHub Actions environment and provide helpful error messages if Java is not set up first.

## GitLab CI/CD Integration

```yaml
stages:
  - test

dmtools-example:
  stage: test
  image: openjdk:23
  
  cache:
    key: "dmtools-cli-$CI_RUNNER_OS"
    paths:
      - ~/.dmtools/
    policy: pull-push
  
  before_script:
    - apt-get update -qq && apt-get install -y curl
    - |
      if [ ! -f "$HOME/.dmtools/dmtools.jar" ]; then
        curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
      fi
    - export PATH="$HOME/.dmtools/bin:$PATH"

  script:
    - dmtools list

  variables:
    JIRA_EMAIL: "$JIRA_EMAIL"
    JIRA_API_TOKEN: "$JIRA_API_TOKEN"
    JIRA_BASE_PATH: "$JIRA_BASE_PATH"
    CONFLUENCE_EMAIL: "$CONFLUENCE_EMAIL"
    CONFLUENCE_API_TOKEN: "$CONFLUENCE_API_TOKEN"
    CONFLUENCE_BASE_PATH: "$CONFLUENCE_BASE_PATH"
    FIGMA_TOKEN: "$FIGMA_TOKEN"
    DMTOOLS_INTEGRATIONS: "jira,confluence,figma"
```

## Prerequisites

- **Java 23 or later** (required)
- curl or wget (for installation)

### Java Installation

The install script will attempt to install Java automatically on macOS and Linux:

**macOS (via Homebrew):**
```bash
brew install openjdk@23
```

**Ubuntu/Debian:**
```bash
sudo apt-get install openjdk-23-jdk
```

**RHEL/CentOS:**
```bash
sudo yum install java-23-openjdk-devel
```

**Fedora:**
```bash
sudo dnf install java-23-openjdk-devel
```

## Configuration

Set up integrations using environment variables:

```bash
# Jira Configuration (Modern approach)
export JIRA_EMAIL="your-email@domain.com"
export JIRA_API_TOKEN="your-jira-api-token"
export JIRA_BASE_PATH="https://your-domain.atlassian.net"
export JIRA_AUTH_TYPE="Basic"  # or "Bearer" for some setups

# Confluence Configuration (Modern approach)
export CONFLUENCE_EMAIL="your-email@domain.com"
export CONFLUENCE_API_TOKEN="your-confluence-api-token"
export CONFLUENCE_BASE_PATH="https://your-domain.atlassian.net/wiki"
export CONFLUENCE_DEFAULT_SPACE="YOUR_SPACE"
export CONFLUENCE_AUTH_TYPE="Basic"  # or "Bearer"

# Figma Configuration
export FIGMA_TOKEN="your-figma-token"
export FIGMA_BASE_PATH="https://api.figma.com"

# Optional: Limit available integrations
export DMTOOLS_INTEGRATIONS="jira,confluence,figma"

# Optional: Performance settings
export JIRA_MAX_SEARCH_RESULTS="1000"
export SLEEP_TIME_REQUEST="300"
```

### Legacy Environment Variables (Still Supported)
If you prefer the legacy approach, you can still use:
```bash
# Legacy Jira (base64 encoded email:token)
export JIRA_LOGIN_PASS_TOKEN="base64-encoded-email:token"

# Legacy Confluence (base64 encoded email:token)  
export CONFLUENCE_LOGIN_PASS_TOKEN="base64-encoded-email:token"
```

## Usage

### List Available Tools
```bash
dmtools list
```

### Multiple Input Methods

**Positional Arguments:**
```bash
dmtools jira_get_ticket DMC-479 summary,description
```

**JSON Data:**
```bash
dmtools jira_get_ticket --data '{"key": "DMC-479", "fields": ["summary"]}'
```

**File Input:**
```bash
dmtools jira_get_ticket --file params.json
```

**Heredoc Input:**
```bash
dmtools jira_get_ticket <<EOF
{
  "key": "DMC-479",
  "fields": ["summary", "description"]
}
EOF
```

**STDIN/Pipe Input:**
```bash
echo '{"key": "DMC-479"}' | dmtools jira_get_ticket
```

**Debug Mode:**
```bash
dmtools jira_get_ticket --verbose DMC-479
```

## Available Tools

### Jira Tools (45 tools)
- **Ticket Management**: `jira_get_ticket`, `jira_create_ticket_basic`, `jira_update_ticket`
- **Search**: `jira_search_by_jql`, `jira_search_by_page`
- **Comments**: `jira_post_comment`, `jira_get_comments`
- **Fields**: `jira_get_fields`, `jira_update_field`, `jira_clear_field`
- **Workflow**: `jira_get_transitions`, `jira_move_to_status`
- **Versions**: `jira_get_fix_versions`, `jira_set_fix_version`, `jira_add_fix_version`
- **Links**: `jira_link_issues`, `jira_get_issue_link_types`

### Confluence Tools (16 tools)
- **Content**: `confluence_content_by_id`, `confluence_content_by_title`
- **Search**: `confluence_search_content_by_text`
- **Pages**: `confluence_create_page`, `confluence_update_page`
- **Navigation**: `confluence_get_children_by_id`, `confluence_get_children_by_name`
- **Users**: `confluence_get_current_user_profile`

### Figma Tools (6 tools)
- **Files**: `figma_get_file_structure`, `figma_download_image_of_file`
- **Icons**: `figma_get_icons`, `figma_download_image_as_file`
- **SVG**: `figma_get_svg_content`
- **Screenshots**: `figma_get_screen_source`

## Examples

### Jira Operations
```bash
# Get ticket details
dmtools jira_get_ticket DMC-479

# Search tickets with JQL
dmtools jira_search_by_jql "project = DMC AND status = Open" "summary,status,assignee"

# Post comment
dmtools jira_post_comment DMC-479 "This is a test comment"

# Create ticket
dmtools jira_create_ticket_basic PROJ Task "Fix bug" "Description of the bug"
```

### Confluence Operations
```bash
# Get page by title
dmtools confluence_content_by_title "My Page Title"

# Search content
dmtools confluence_search_content_by_text "search query" 10

# Get page children
dmtools confluence_get_children_by_name PROJ "Parent Page Name"
```

### Figma Operations
```bash
# Get file structure
dmtools figma_get_file_structure "https://www.figma.com/file/abc123/Design"

# Extract icons/graphics
dmtools figma_get_icons "https://www.figma.com/file/abc123/Design"

# Download as PNG
dmtools figma_download_image_as_file "https://www.figma.com/file/abc123/Design" "123:456" "png"
```

## Output Format

All tools return clean JSON results by default:

```bash
dmtools jira_get_ticket DMC-479
# Returns:
{"key":"DMC-479","fields":{"summary":"Fix login issue","status":{"name":"In Progress"}}}
```

Use `--verbose` for debug output including logs.

## Manual Installation

If you prefer manual installation:

1. Download the latest JAR and script from [releases](https://github.com/IstiN/dmtools/releases/latest)
2. Place them in your preferred location
3. Make the script executable: `chmod +x dmtools.sh`
4. Add to your PATH or create a symlink

## Development

For local development:
```bash
git clone https://github.com/IstiN/dmtools.git
cd dmtools
./gradlew build
./dmtools.sh list  # Uses local build
```

## Troubleshooting

### Command not found
- Restart your shell or run: `source ~/.zshrc` (or `~/.bashrc`)
- Check that `~/.dmtools/bin` is in your PATH

### Java not found
- Install Java 23 or later
- Ensure `java` command is available in PATH

### JAR file not found
- Run the installer again: `curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash`
- Or manually download from GitHub releases

### Authentication issues
- Check your environment variables (JIRA_URL, JIRA_USERNAME, JIRA_PASSWORD, etc.)
- Verify API tokens are correct and have proper permissions