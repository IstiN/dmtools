# DMtools Installation Guide

## 🚀 Quick Installation

The fastest way to install DMtools:

```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

This script will:
1. ✅ Check for Java 23+ (install if missing)
2. ✅ Download the latest DMtools release
3. ✅ Install to `~/.dmtools/`
4. ✅ Create the `dmtools` command alias
5. ✅ Set up shell integration (bash/zsh)

## 📦 Installation Methods

### Method 1: Automatic Installation (Recommended)

```bash
# Latest stable version
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash

# Specific version
DMTOOLS_VERSION=1.2.3 curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

### Method 2: Local Development Installation

For developers working on DMtools:

```bash
# Clone the repository
git clone https://github.com/IstiN/dmtools.git
cd dmtools/dmtools-core

# Build and install locally
./buildInstallLocal.sh

# This will:
# 1. Build the fat JAR with ./gradlew :dmtools-core:shadowJar
# 2. Copy to ~/.dmtools/dmtools.jar
# 3. Use your locally built version
```

### Method 3: Manual Installation

```bash
# Download the JAR directly
DMTOOLS_VERSION=1.0.8  # Replace with desired version
wget -O ~/.dmtools/dmtools.jar \
  "https://github.com/IstiN/dmtools/releases/download/v${DMTOOLS_VERSION}/dmtools-v${DMTOOLS_VERSION}-all.jar"

# Create the wrapper script
cat > ~/bin/dmtools << 'EOF'
#!/bin/bash
java -jar ~/.dmtools/dmtools.jar "$@"
EOF

chmod +x ~/bin/dmtools

# Add to PATH in ~/.bashrc or ~/.zshrc
echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

## ☕ Java Installation

DMtools requires Java 23 or higher. The installer handles this automatically, but you can also install manually:

### Automatic Java Installation

The install script will automatically install Java 23 using SDKMAN if Java is missing or outdated:

```bash
# The installer runs these commands internally:
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 23-open
sdk default java 23-open
```

### Manual Java Installation

#### macOS
```bash
# Using Homebrew
brew install openjdk@23
echo 'export PATH="/opt/homebrew/opt/openjdk@23/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Linux
```bash
# Using SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 23-open

# Or using package manager (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-23-jdk
```

#### Windows (WSL)
```bash
# In WSL terminal
sudo apt update
sudo apt install openjdk-23-jdk

# Or use SDKMAN as shown above
```

### Verify Java Version
```bash
java -version
# Should show: openjdk version "23" or higher
```

## ✅ Verification

After installation, verify DMtools is working:

```bash
# Check version
dmtools --version
# Output: DMtools version v1.0.8 (or your installed version)

# List all available MCP tools
dmtools list
# Should show 67+ tools organized by category

# Test a simple command
dmtools help
# Shows usage information

# Test MCP tool (if configured)
dmtools cli_execute_command "echo 'DMtools is working!'"
```

## 🗂️ Installation Structure

After installation, you'll have:

```
~/.dmtools/
├── dmtools.jar           # Main DMtools JAR file
├── dmtools.env          # Environment configuration (created by you)
└── logs/                # Execution logs (created on first run)

~/bin/
└── dmtools              # Executable wrapper script

~/.bashrc or ~/.zshrc
└── # PATH and alias additions
```

## 🔧 Shell Integration

The installer adds these to your shell configuration:

```bash
# Added to ~/.bashrc or ~/.zshrc
export PATH="$HOME/bin:$PATH"
alias dmtools='java -jar ~/.dmtools/dmtools.jar'

# For development with local JAR
alias dmtools-dev='java -jar /path/to/your/dmtools-core/build/libs/dmtools-v*-all.jar'
```

## 🐳 Docker Installation (Optional)

For containerized environments:

```dockerfile
FROM openjdk:23-slim

# Install DMtools
RUN apt-get update && apt-get install -y curl bash \
    && curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash \
    && apt-get clean

# Set up environment
ENV PATH="/root/bin:${PATH}"

WORKDIR /workspace

ENTRYPOINT ["dmtools"]
```

Build and run:
```bash
docker build -t dmtools .
docker run -it dmtools --version
```

## 🔄 Updating DMtools

### Update to Latest Version
```bash
# Re-run the installer
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

### Update to Specific Version
```bash
DMTOOLS_VERSION=1.2.3 curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

### Check Current Version
```bash
dmtools --version
```

## 🗑️ Uninstallation

To completely remove DMtools:

```bash
# Remove DMtools files
rm -rf ~/.dmtools
rm ~/bin/dmtools

# Remove from shell configuration
# Edit ~/.bashrc or ~/.zshrc and remove DMtools-related lines

# Reload shell
source ~/.bashrc  # or ~/.zshrc
```

## 🆘 Common Issues

If you encounter problems, check the [Troubleshooting Guide](troubleshooting.md).

## 📝 Next Steps

After installation:
1. [Configure environment variables](../configuration/README.md)
2. [Set up your first integration](../configuration/integrations/jira.md)
3. [Configure an AI provider](../configuration/ai-providers/gemini.md)
4. [Run your first command](../examples/workflows/)

---

*Need help? Report issues at [github.com/IstiN/dmtools/issues](https://github.com/IstiN/dmtools/issues)*