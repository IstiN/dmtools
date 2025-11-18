# Installation Guide

Get DMTools CLI installed and ready to use in minutes.

## Quick Install (Recommended)

### Linux/macOS/WSL

```bash
curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash
```

### Windows

On Windows, choose the method based on your environment:

#### Option 1: PowerShell (Native Windows)

**Step 1: Download and fix line endings**
```powershell
# Download the script
Invoke-WebRequest -Uri "https://github.com/IstiN/dmtools/releases/latest/download/install.sh" -OutFile "install.sh"

# Convert Windows line endings to Unix (PowerShell-native)
(Get-Content install.sh -Raw) -replace "`r`n", "`n" | Set-Content install.sh -NoNewline
```

**Step 2: Run in Git Bash or WSL**
```bash
# Open Git Bash or WSL and run:
bash install.sh
rm install.sh
```

#### Option 2: Git Bash

**Method A: Strip carriage returns inline**
```bash
curl.exe -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | tr -d '\r' | bash
```

**Method B: Download, convert, then run**
```bash
# Download the script
curl.exe -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh -o install.sh

# Convert line endings (choose one method):
# If dos2unix is installed:
dos2unix install.sh

# OR if sed is available (Git Bash):
sed -i 's/\r$//' install.sh

# OR using PowerShell from Git Bash:
powershell.exe -Command "(Get-Content install.sh -Raw) -replace \"`r`n\", \"`n\" | Set-Content install.sh -NoNewline"

# Run the script
bash install.sh

# Clean up
rm install.sh
```

#### Option 3: WSL (Windows Subsystem for Linux)

If you're using WSL, use the standard Linux command:
```bash
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | bash
```

If you encounter line ending issues in WSL:
```bash
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh -o install.sh
sed -i 's/\r$//' install.sh
bash install.sh
rm install.sh
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

#### Windows: Java Detection

On Windows, the installer automatically checks both PATH and JAVA_HOME. The installer will:

1. **Check PATH first** - Looks for `java` command in your PATH
2. **Check JAVA_HOME if not in PATH** - Automatically detects JAVA_HOME from environment variables
3. **Convert Windows paths** - Converts Windows paths (`C:\...`) to Git Bash format (`/c/...`)
4. **Find java.exe** - Locates `java.exe` in `$JAVA_HOME/bin/`
5. **Add to PATH** - Temporarily adds Java to PATH for the installation

**Setting up Java:**

**Option 1: Add Java to PATH (Recommended)**
- Add Java's `bin` directory to Windows PATH environment variable
- Example: `C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot\bin`
- Restart your terminal after changing PATH

**Option 2: Set JAVA_HOME**
- Set JAVA_HOME environment variable in Windows to Java installation directory
- Example: `C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot`
- The installer will automatically detect and use it
- **Optional:** In Git Bash, you can also export JAVA_HOME:
  ```bash
  export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-23.0.2.7-hotspot"
  # Or use Windows path format (script will automatically convert it)
  export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-23.0.2.7-hotspot"
  ```

The installer automatically handles path conversion and Java detection, so you don't need to manually convert paths.

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

### Linux/macOS/WSL/Git Bash

**Step 1: Download**

Download the latest files from [GitHub Releases](https://github.com/IstiN/dmtools/releases/latest):
- `dmtools.jar` - Main application
- `dmtools.sh` - Shell wrapper script

**Step 2: Create Directory**

```bash
mkdir -p ~/.dmtools
mv dmtools.jar ~/.dmtools/
```

**Step 3: Install CLI Wrapper**

```bash
mkdir -p ~/.dmtools/bin
cat > ~/.dmtools/bin/dmtools << 'EOF'
#!/bin/bash
exec java -cp "$HOME/.dmtools/dmtools.jar" com.github.istin.dmtools.job.JobRunner "$@"
EOF
chmod +x ~/.dmtools/bin/dmtools
```

**Step 4: Add to PATH**

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

### Windows PowerShell

**Step 1: Get Latest Version**

```powershell
# Get latest version tag
$response = Invoke-RestMethod -Uri "https://api.github.com/repos/IstiN/dmtools/releases/latest"
$version = $response.tag_name
Write-Host "Latest version: $version"
```

**Step 2: Create Directory and Download Files**

```powershell
# Create installation directory
$installDir = "$env:USERPROFILE\.dmtools"
$binDir = "$installDir\bin"
New-Item -ItemType Directory -Force -Path $installDir | Out-Null
New-Item -ItemType Directory -Force -Path $binDir | Out-Null

# Download JAR file
$jarUrl = "https://github.com/IstiN/dmtools/releases/download/$version/dmtools-$version-all.jar"
$jarPath = "$installDir\dmtools.jar"
Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath
Write-Host "Downloaded JAR to: $jarPath"

# Download shell script wrapper
$scriptUrl = "https://github.com/IstiN/dmtools/releases/download/$version/dmtools.sh"
$scriptPath = "$binDir\dmtools"
Invoke-WebRequest -Uri $scriptUrl -OutFile $scriptPath

# Convert line endings to Unix format
$content = Get-Content $scriptPath -Raw
$content = $content -replace "`r`n", "`n"
Set-Content -Path $scriptPath -Value $content -NoNewline
Write-Host "Downloaded and fixed script: $scriptPath"
```

**Step 3: Add to PATH**

```powershell
# Add to user PATH (persistent)
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable("Path", "$userPath;$binDir", "User")
    Write-Host "Added $binDir to PATH"
    Write-Host "Please restart your terminal for PATH changes to take effect"
} else {
    Write-Host "$binDir is already in PATH"
}

# Add to current session PATH
$env:Path += ";$binDir"
```

**Step 4: Verify Installation**

```powershell
# Test the installation (requires Git Bash or WSL)
# Open Git Bash or WSL and run:
# bash ~/.dmtools/bin/dmtools list
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

### Windows PowerShell: "tr" or "sed" not recognized

**Error:** `tr : The term 'tr' is not recognized` or `sed : The term 'sed' is not recognized`

**Cause:** You're running Unix commands (`tr`, `sed`) in PowerShell, which doesn't have these commands.

**Solution:** Use PowerShell-native commands instead:

```powershell
# Download and fix line endings in PowerShell
Invoke-WebRequest -Uri "https://github.com/IstiN/dmtools/releases/latest/download/install.sh" -OutFile "install.sh"
(Get-Content install.sh -Raw) -replace "`r`n", "`n" | Set-Content install.sh -NoNewline

# Then run in Git Bash or WSL:
# bash install.sh
```

**Alternative:** Use Git Bash instead of PowerShell for the installation commands. Open Git Bash and use the commands from the "Git Bash" section above.

### Windows: Line Ending Errors (`$'\r': command not found`)

If you see errors like `$'\r': command not found`, `invalid option`, or `syntax error near unexpected token`, the script has Windows line endings (`\r\n` instead of `\n`).

**PowerShell Solution:**
```powershell
# Download and fix line endings
Invoke-WebRequest -Uri "https://github.com/IstiN/dmtools/releases/latest/download/install.sh" -OutFile "install.sh"
(Get-Content install.sh -Raw) -replace "`r`n", "`n" | Set-Content install.sh -NoNewline

# Then run in Git Bash or WSL:
# bash install.sh
```

**Git Bash Solution:**
```bash
curl.exe -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh | tr -d '\r' | bash
```

**Git Bash Alternative (if tr doesn't work):**
```bash
curl.exe -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh -o install.sh
sed -i 's/\r$//' install.sh
bash install.sh
rm install.sh
```

**WSL Solution:**
```bash
curl -fsSL https://github.com/IstiN/dmtools/releases/latest/download/install.sh -o install.sh
sed -i 's/\r$//' install.sh
bash install.sh
rm install.sh
```

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

### "java: command not found" or "Java 23 is required but not installed"

**Windows (Git Bash):** If Java is installed but not detected:

The installer automatically checks JAVA_HOME, but if it's still not found:

1. **Verify JAVA_HOME is set in Windows:**
   - Open Windows System Properties → Environment Variables
   - Ensure JAVA_HOME is set to: `C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot`
   - Restart Git Bash to pick up the environment variable

2. **Or export JAVA_HOME in Git Bash before running installer:**
   ```bash
   # The installer will automatically convert Windows paths
   export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-23.0.2.7-hotspot"
   # Or use Git Bash format
   export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-23.0.2.7-hotspot"
   bash install.sh
   ```

3. **Or add Java to PATH (Windows):**
   - Open Windows System Properties → Environment Variables
   - Add `C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot\bin` to PATH
   - Restart Git Bash

4. **Verify Java is accessible:**
   ```bash
   # If JAVA_HOME is set, test it:
   "$JAVA_HOME/bin/java.exe" -version
   # Should show: openjdk version "23" or higher
   ```

**Linux/macOS:** Install Java 23 or later. See [Prerequisites](#java-23-or-later-required) above.

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

👉 **[MCP Tools Reference](../cli-usage/mcp-tools.md)** - Learn the command structure
