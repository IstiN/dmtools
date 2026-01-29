# DMTools Installation Guide

## One-Line Installation

### macOS / Linux / Git Bash (Windows)
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install | bash
```

### Windows Command Prompt (cmd.exe)
**Step 1:** Download install.bat
```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o install.bat
```

**Step 2:** Run the installer
```cmd
install.bat
```

### Windows PowerShell
```powershell
# PowerShell 5.1+
Invoke-RestMethod -Uri 'https://github.com/IstiN/dmtools/releases/latest/download/install.ps1' | Invoke-Expression

# Short version (PowerShell 7+)
irm https://github.com/IstiN/dmtools/releases/latest/download/install.ps1 | iex
```

---

## Troubleshooting

### Fish Shell (macOS/Linux)
If you use fish shell and get "Unknown command: dmtools" after installation:

1. The installer created `~/.config/fish/conf.d/dmtools.fish`
2. Restart fish or run:
   ```fish
   source ~/.config/fish/conf.d/dmtools.fish
   ```

### Windows: "irm is not recognized"
You're likely running the command in **cmd.exe** instead of **PowerShell**.

**Solution 1:** Use install.bat (recommended)
```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o install.bat && install.bat
```

**Solution 2:** Open PowerShell and run:
```powershell
Invoke-RestMethod -Uri 'https://github.com/IstiN/dmtools/releases/latest/download/install.ps1' | Invoke-Expression
```

**Solution 3:** Use Git Bash (if installed)
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
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/v1.7.118/install | bash
```

### Windows PowerShell
```powershell
Invoke-RestMethod -Uri 'https://github.com/IstiN/dmtools/releases/download/v1.7.118/install.ps1' | Invoke-Expression
```

---

## Force Local Java Installation (Testing)

Install Java 23 locally even if system Java exists:

```bash
# macOS/Linux/Git Bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.sh | DMTOOLS_FORCE_LOCAL_JAVA=true bash
```

```powershell
# Windows PowerShell
$env:DMTOOLS_FORCE_LOCAL_JAVA="true"
Invoke-RestMethod -Uri 'https://github.com/IstiN/dmtools/releases/latest/download/install.ps1' | Invoke-Expression
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

