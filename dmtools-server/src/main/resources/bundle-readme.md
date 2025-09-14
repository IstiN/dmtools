# DMTools Standalone Bundle

## 🚀 Quick Start

```bash
# Simply run the launcher
./run.sh        # macOS/Linux
run.cmd         # Windows
```

Then open http://localhost:8080 and login with `admin` / `admin`

## ⚠️ Security Warnings

### macOS "java Not Opened" Warning

If you see this warning, it's because the embedded Java isn't code-signed by Apple.

**Quick Fix:**
```bash
# Remove quarantine (run this once)
xattr -dr com.apple.quarantine .
# Then run normally
./run.sh
```

**Alternative:**
- Right-click `run.sh` → Open → Open
- Or go to System Preferences → Privacy & Security → Allow

### Windows Defender Warning

If Windows blocks execution:
1. Click "More info" → "Run anyway"
2. Or add this folder to Windows Defender exclusions

## 📋 What's Included

- **DMTools Server** (448MB) - Complete application
- **Java 23 Runtime** (125MB) - No installation needed
- **All Dependencies** - Works offline
- **H2 Database** - Local data storage

## 🔧 Configuration

```bash
# Custom port
./run.sh --server.port=9090

# Custom database location  
./run.sh --spring.datasource.url=jdbc:h2:./my-data/dmtools-db

# More memory
./run.sh -Xmx4g
```

## 🆘 Need Help?

- **Documentation:** https://github.com/IstiN/dmtools
- **Issues:** https://github.com/IstiN/dmtools/issues
- **API Docs:** http://localhost:8080/swagger-ui.html (after startup)

## 🔒 Default Credentials

- **Username:** admin
- **Password:** admin

⚠️ **Change these in production!**
