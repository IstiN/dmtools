# Installation Guide

Get DMTools CLI installed and ready to use in minutes.

## Quick Install (Recommended)

```bash
curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
```

This will:
- ✅ Download the latest DMTools JAR (~50MB)
- ✅ Install the `dmtools` command to `~/.dmtools/bin/`
- ✅ Add the binary to your PATH
- ✅ Set up shell integration (bash/zsh)

After installation, **restart your shell** or run:
```bash
source ~/.zshrc  # or ~/.bashrc for bash users
```

### Verify Installation

```bash
dmtools --version
# Output: DMTools 1.7.16
# A comprehensive development management toolkit

dmtools list
# Output: Lists all 67 available MCP tools
```

---

## Prerequisites

### Java 23 or Later (Required)

DMTools requires Java 23+. The installer will attempt to install Java automatically on macOS and Linux.

#### Check Java Version
```bash
java -version
# Should show: openjdk version "23" or higher
```

#### Install Java Manually

**macOS (Homebrew):**
```bash
brew install openjdk@23
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
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

---

## Manual Installation

If you prefer manual installation or the quick install doesn't work:

### Step 1: Download

Download the latest files from [GitHub Releases](https://github.com/IstiN/dmtools/releases/latest):
- `dmtools.jar` - Main application
- `dmtools.sh` - Shell wrapper script

### Step 2: Create Directory

```bash
mkdir -p ~/.dmtools
mv dmtools.jar ~/.dmtools/
```

### Step 3: Install CLI Wrapper

```bash
mkdir -p ~/.dmtools/bin
cat > ~/.dmtools/bin/dmtools << 'EOF'
#!/bin/bash
exec java -cp "$HOME/.dmtools/dmtools.jar" com.github.istin.dmtools.job.JobRunner "$@"
EOF
chmod +x ~/.dmtools/bin/dmtools
```

### Step 4: Add to PATH

**For bash:**
```bash
echo 'export PATH="$HOME/.dmtools/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

**For zsh:**
```bash
echo 'export PATH="$HOME/.dmtools/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

---

## CI/CD Installation

### GitHub Actions

```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '23'

- name: Cache DMTools CLI
  id: cache-dmtools
  uses: actions/cache@v4
  with:
    path: ~/.dmtools
    key: dmtools-cli-${{ runner.os }}-${{ hashFiles('.github/workflows/*.yml') }}
    restore-keys: |
      dmtools-cli-${{ runner.os }}-

- name: Install DMTools CLI
  if: steps.cache-dmtools.outputs.cache-hit != 'true'
  run: |
    curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash

- name: Add DMTools to PATH
  run: echo "$HOME/.dmtools/bin" >> $GITHUB_PATH
```

### GitLab CI/CD

```yaml
stages:
  - test

dmtools-job:
  stage: test
  image: openjdk:23
  
  cache:
    key: "dmtools-cli-$CI_RUNNER_OS"
    paths:
      - ~/.dmtools/
  
  before_script:
    - apt-get update -qq && apt-get install -y curl
    - |
      if [ ! -f "$HOME/.dmtools/dmtools.jar" ]; then
        curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
      fi
    - export PATH="$HOME/.dmtools/bin:$PATH"
  
  script:
    - dmtools list
```

---

## Development Installation

For local development and contribution:

```bash
# Clone repository
git clone https://github.com/IstiN/dmtools.git
cd dmtools

# Build from source
./gradlew clean build

# Use local build
./dmtools.sh list
```

See [Development > Building from Source](../development/building.md) for more details.

---

## Troubleshooting

### "dmtools: command not found"

**Solution 1:** Restart your shell
```bash
# Restart terminal or run:
source ~/.zshrc  # or ~/.bashrc
```

**Solution 2:** Check PATH
```bash
echo $PATH | grep dmtools
# Should show: ...:/Users/you/.dmtools/bin:...
```

**Solution 3:** Manually add to PATH
```bash
export PATH="$HOME/.dmtools/bin:$PATH"
```

### "java: command not found"

Install Java 23 or later. See [Prerequisites](#java-23-or-later-required) above.

### "JAR file not found"

Reinstall DMTools:
```bash
curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
```

Or manually download the JAR:
```bash
curl -L https://github.com/IstiN/dmtools/releases/latest/download/dmtools.jar \
  -o ~/.dmtools/dmtools.jar
```

### Permission Denied

Make the script executable:
```bash
chmod +x ~/.dmtools/bin/dmtools
```

### Installation Script Fails on GitHub Actions

Ensure Java is set up **before** installing DMTools:
```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '23'

- name: Install DMTools CLI
  run: curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
```

---

## Next Steps

✅ Installation complete! Now configure your integrations:

👉 **[Configuration Guide](configuration.md)** - Set up Jira, Confluence, Figma, and AI providers

👉 **[First Steps](first-steps.md)** - Run your first commands

👉 **[CLI Usage](../cli-usage/overview.md)** - Learn the command structure
