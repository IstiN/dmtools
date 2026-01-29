# Release Notes - DMTools v1.7.119

## 🎉 Major Installation Improvements

This release focuses on making DMTools installation seamless across all platforms with automatic Java 23 installation and universal Windows support.

---

## ✨ New Features

### Universal Windows Installation
- **One command that works everywhere**: Copy and paste a single command that works in cmd.exe, PowerShell, Windows Terminal, and any Windows shell
- Created `install.bat` for native Windows Command Prompt support
- No need to figure out which shell you're using - the universal command adapts automatically

**Windows Users: Use this single command:**
```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

### Automatic Local Java 23 Installation
- **No admin/sudo required**: Installs Java 23 JRE locally to `~/.dmtools/jre` if system Java is not found or version < 23
- **Cross-platform support**: Works on macOS (Intel/Apple Silicon), Linux (x64/ARM64), and Windows (x64/ARM64)
- **Smart detection**: Checks for system Java first, only installs locally if needed
- **Testing flag**: Use `DMTOOLS_FORCE_LOCAL_JAVA=true` to force local Java installation for testing

---

## 🐛 Bug Fixes

### Installation Script Fixes
- **Fixed bundled Java verification** (aa2eb6e): Installation verification now correctly displays Java version from bundled installation instead of showing "Unable to locate a Java Runtime" error
- **Fixed macOS JRE directory structure** (5901ee6): Bundled Java now works correctly on macOS with `Contents/Home/bin/java` path structure
- **Fixed fish shell PATH configuration** (688119c): Fish shell users now get automatic PATH configuration even if config file doesn't exist
- **Improved Java version detection** (4634121): Added validation to ensure version string is actually a version number (starts with digit)
- **Improved download reliability** (c5b97f9): Increased download timeout to 300 seconds for large JAR files, better error handling with curl exit codes

---

## 📚 Documentation Improvements

### Updated Installation Guide
- New [INSTALL_INSTRUCTIONS.md](INSTALL_INSTRUCTIONS.md) with comprehensive troubleshooting
- Clear instructions for each platform: macOS, Linux, Windows (Git Bash, PowerShell, cmd.exe)
- Added troubleshooting sections for common issues:
  - Fish shell "Unknown command: dmtools"
  - Windows "irm is not recognized"
  - Windows "curl is not recognized"
  - Force local Java installation for testing

### Updated README
- Updated main [README.md](README.md) with universal Windows installation command
- Clear separation of installation commands by platform
- Direct link to detailed installation instructions

---

## 🔧 Installation Commands by Platform

### macOS / Linux / Git Bash (Windows)
```bash
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install | bash
```

### Windows (Universal - Works Everywhere)
```cmd
curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o "%TEMP%\dmtools-install.bat" && "%TEMP%\dmtools-install.bat"
```

### Windows PowerShell (Alternative)
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "& {[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-Expression ((New-Object Net.WebClient).DownloadString('https://github.com/IstiN/dmtools/releases/latest/download/install.ps1'))}"
```

---

## ✅ Verification

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

## 🙏 Thank You

Special thanks to all users who reported installation issues and helped test the fixes across different platforms!

---

## Full Changelog

- aa2eb6e - fix: use bundled Java in installation verification
- 688119c - feat: add Windows cmd.exe support and fix fish shell
- 5901ee6 - fix: support macOS JRE directory structure
- 4634121 - fix: improve Java detection and add DMTOOLS_FORCE_LOCAL_JAVA flag
- c5b97f9 - fix: improve download_file function in install.sh
- 9debc36 - jql modifications

**For complete documentation, see [docs/](docs/)**
