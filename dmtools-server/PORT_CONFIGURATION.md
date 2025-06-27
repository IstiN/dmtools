# Port and Host Configuration Guide

## Overview
The DMTools server port and host configuration is now centralized in `application.properties`. All URLs throughout the application automatically update when you change the port.

## Quick Port Change

### Method 1: Edit application.properties
```properties
# Change this line in dmtools-server/src/main/resources/application.properties
server.port=3000  # or any port you want
```

### Method 2: Use Environment Variable
```bash
PORT=3000 ./gradlew :dmtools-server:bootRun
```

### Method 3: Use Spring Profile
```bash
SPRING_PROFILES_ACTIVE=port80 sudo ./gradlew :dmtools-server:bootRun
```

## Common Port Configurations

| Port | Usage | Requirements |
|------|-------|-------------|
| `80` | Standard HTTP | Requires `sudo` on Mac/Linux |
| `3000` | Common dev port | No special requirements |
| `8000` | Alternative dev port | No special requirements |
| `8080` | Default (Spring Boot) | No special requirements |

## What Gets Auto-Updated

When you change `server.port`, these configurations automatically update:

✅ **OAuth2 Redirect URIs**
- Google OAuth: `/login/oauth2/code/google`
- GitHub OAuth: `/login/oauth2/code/github` 
- Microsoft OAuth: `/login/oauth2/code/microsoft`

✅ **Swagger/OpenAPI Configuration**
- Local server URL in Swagger UI
- API documentation base URL

✅ **Application Startup**
- Browser auto-open URL
- Console messages

✅ **Job Runner Messages**
- Web interface URL prompts

## Advanced Configuration

### Custom Host/Protocol
```properties
# For custom domains or HTTPS
app.base-url=https://my-domain.com:8443
```

### Environment-Specific Overrides
```bash
# Production
APP_BASE_URL=https://dmtools.mydomain.com ./gradlew :dmtools-server:bootRun

# Development with custom port
PORT=3000 ./gradlew :dmtools-server:bootRun
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `app.base-url` | `http://localhost:${server.port}` | Complete base URL |
| `app.host` | `localhost` | Host name |
| `app.port` | `${server.port}` | Port (mirrors server.port) |

## Examples

### Run on Port 80 (Standard HTTP)
```bash
# Temporary
sudo PORT=80 ./gradlew :dmtools-server:bootRun

# Or use the pre-configured profile
sudo SPRING_PROFILES_ACTIVE=port80 ./gradlew :dmtools-server:bootRun
```

### Run on Port 3000
```bash
PORT=3000 ./gradlew :dmtools-server:bootRun
```

### Run with Custom Base URL
```bash
APP_BASE_URL=https://localhost:8443 ./gradlew :dmtools-server:bootRun
```

## Benefits

- ✅ **Single source of truth** - change port in one place
- ✅ **No hardcoded URLs** - everything updates automatically
- ✅ **Environment-friendly** - easy to override for different environments
- ✅ **OAuth2 compatible** - redirect URIs update automatically
- ✅ **Documentation in sync** - Swagger URLs stay current 