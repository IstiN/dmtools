# DMTools Installation Guide

## One-Line Installation

### macOS / Linux / Git Bash (Windows)
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install | bash
```

### Windows (All Windows terminals)
**Copy and paste this single command** (works in cmd.exe, PowerShell, or Windows Terminal):

```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

**What this does:**
- ✅ Works in Command Prompt (cmd.exe)
- ✅ Works in PowerShell (any version)
- ✅ Works in Windows Terminal
- ✅ Downloads and runs installer automatically
- ✅ Requires only curl (built into Windows 10 1803+)
- ⚠️ **Windows only** - do not use on macOS/Linux

---

## Troubleshooting

### Fish Shell (macOS/Linux)
If you use fish shell and get "Unknown command: dmtools" after installation:

1. The installer created `~/.config/fish/conf.d/dmtools.fish`
2. Restart fish or run:
   ```fish
   source ~/.config/fish/conf.d/dmtools.fish
   ```

### Windows: Installation Issues

**Solution 1:** Use the universal command (recommended)
```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

**Solution 2:** Use Git Bash (if installed)
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install | bash
```

### Windows: "curl is not recognized"
- Windows 10 1803+ has curl built-in
- If missing, install Git for Windows: https://git-scm.com/download/win
- Or download install.bat manually from GitHub releases

---

## Specific Version Installation

### macOS / Linux / Git Bash

**Method 1 - Version-pinned via raw.githubusercontent (recommended, faster, no breaking changes):**
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/v1.7.126/install.sh | bash -s v1.7.126
```

**Method 2 - From main branch (always latest script):**
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash -s v1.7.126
```

**Method 3 - Pass environment variable to bash:**
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | DMTOOLS_VERSION=v1.7.126 bash
```

**Method 4 - Using export:**
```bash
export DMTOOLS_VERSION=v1.7.126
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

**⚠️ This does NOT work** (environment variable doesn't cross pipe):
```bash
DMTOOLS_VERSION=v1.7.126 curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | bash
```

### Windows (cmd.exe)

**Version-pinned via raw.githubusercontent (recommended, faster, no breaking changes):**
```cmd
set DMTOOLS_VERSION=v1.7.126 && curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/v1.7.126/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

**From main branch (always latest script):**
```cmd
set DMTOOLS_VERSION=v1.7.126 && curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

### Windows (PowerShell)

```powershell
$env:DMTOOLS_VERSION = "v1.7.126"
Invoke-RestMethod -Uri "https://raw.githubusercontent.com/IstiN/dmtools/v1.7.126/install.ps1" | Invoke-Expression
```

**Important**: If you don't specify a version, the installer will download the **latest** release.

---

## Force Local Java Installation (Testing)

Install Java 23 locally even if system Java exists:

```bash
# macOS/Linux/Git Bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | DMTOOLS_FORCE_LOCAL_JAVA=true bash
```

```cmd
# Windows - Not currently supported for local Java installation
# Please install Java 23 manually from https://adoptium.net/
```

---

## Verification

After installation, verify dmtools is working:

```bash
dmtools --version
dmtools list
```

If command not found:
- **bash/zsh:** `source ~/.bashrc` or `source ~/.zshrc`
- **fish:** `source ~/.config/fish/conf.d/dmtools.fish`
- **Windows:** Restart your terminal

---

## Manual Installation

1. Download the latest JAR: https://github.com/IstiN/dmtools/releases/latest
2. Download dmtools.sh (Unix) or dmtools.cmd (Windows)
3. Place in `~/.dmtools/` (Unix) or `%USERPROFILE%\.dmtools\` (Windows)
4. Add to PATH manually

