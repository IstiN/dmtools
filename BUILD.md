# Build Configuration Guide

This guide explains how to build dmtools for different environments with appropriate configurations.

## Build Profiles

### Development Build
Uses your actual development credentials from `config.properties`:

```bash
# Standard development build
./gradlew bootJar

# With explicit profile (same as above)
./gradlew bootJar -PbuildProfile=development

# Clean development build  
./gradlew clean bootJar
```

### Production Build
Uses production configuration with environment variable placeholders:

```bash
# Production build (used by deployment script)
./gradlew bootJar -PbuildProfile=production

# Clean production build
./gradlew clean bootJar -PbuildProfile=production
```

## Configuration Files

### Development Configuration
- **`config.properties`** - Contains actual development credentials and settings
- Used for local development and testing
- Should NOT be committed with real credentials

### Production Configuration  
- **`config.prod.properties`** - Contains environment variable placeholders
- Used for App Engine deployment
- Safe to commit to version control
- Actual values provided via App Engine environment variables

## Configuration Management

### Key Differences

| Setting | Development | Production |
|---------|-------------|------------|
| API Keys | Hardcoded values | Environment variables |
| Logging | More verbose | Optimized for performance |
| Database URLs | Local/test instances | Production instances |
| AI Retry Settings | Same values | Same values |

### Production Environment Variables

Set these in `app.yaml` for production deployment:

```yaml
env_variables:
  GEMINI_API_KEY: "your-production-gemini-key"
  OPEN_AI_API_KEY: "your-production-openai-key"  
  JIRA_BASE_PATH: "https://your-company.atlassian.net"
  JIRA_LOGIN_PASS_TOKEN: "your-production-jira-token"
  MS_APPLICATION_ID: "your-microsoft-app-id"
  MS_SECRET_VALUE: "your-microsoft-secret"
  MS_SECRET_KEY: "your-microsoft-secret-key"
  MS_TENANT_ID: "your-microsoft-tenant-id"
```

## Build Process

### Development Build Process
1. Uses `config.properties` as-is
2. Packages with development credentials
3. Suitable for local testing

### Production Build Process  
1. Copies `config.prod.properties` → `config.properties` in JAR
2. Replaces hardcoded values with environment variable placeholders
3. App Engine provides actual values at runtime
4. Secure and scalable for production

## Deployment

### Local Development
```bash
./gradlew bootJar
java -jar dmtools-appengine.jar
```

### Production Deployment
```bash
./deploy-production.sh
```

The deployment script automatically:
- Builds with production profile
- Validates configuration
- Deploys to App Engine
- Provides deployment status

## Security Best Practices

1. **Never commit real credentials** to version control
2. **Use environment variables** for production secrets
3. **Review `config.prod.properties`** to ensure no hardcoded secrets
4. **Rotate credentials regularly** in production environment
5. **Use different credentials** for development vs production

## Troubleshooting

### Wrong Configuration in JAR
If you see development credentials in production:
```bash
# Verify you're using production build
./gradlew clean bootJar -PbuildProfile=production

# Check JAR contents
unzip -p dmtools-appengine.jar BOOT-INF/classes/config.properties | head -10
```

### Missing Environment Variables
Check App Engine logs if configuration values are missing:
```bash
gcloud app logs read --service=default | grep -i "config\|property\|missing"
``` 