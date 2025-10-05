# Production Deployment Guide

This guide explains how to deploy dmtools to Google App Engine with production configuration.

## Configuration Structure

### 1. Spring Boot Configuration
- **`application.properties`** - Base Spring Boot configuration for all environments
- **`application-prod.properties`** - Production-specific Spring Boot settings

### 2. Application Configuration  
- **`config.properties`** - Development configuration with full settings
- **`config.prod.properties`** - Production configuration with environment variable placeholders

### 3. App Engine Configuration (`app.yaml`)
- Runtime: Java 21
- Instance class: F2 (512MB memory) 
- Environment variables for production credentials

### 4. Build System
- Gradle automatically selects the appropriate config file based on build profile
- Production builds use `config.prod.properties` as the source for `config.properties`

## Before Deployment

### 1. Configure Production Environment Variables
Edit `app.yaml` and set your production credentials:

```yaml
env_variables:
  SPRING_PROFILES_ACTIVE: "prod"
  GEMINI_API_KEY: "your-actual-gemini-api-key"
  # Uncomment and set other variables as needed:
  # DIAL_API_KEY: "your-dial-key"
  # JIRA_BASE_PATH: "https://your-company.atlassian.net"
  # JIRA_LOGIN_PASS_TOKEN: "your-jira-token"
  # MS_APPLICATION_ID: "your-microsoft-app-id"
  # MS_SECRET_VALUE: "your-microsoft-secret"
  # MS_SECRET_KEY: "your-microsoft-secret-key"
  # MS_TENANT_ID: "your-microsoft-tenant-id"
```

### 2. Verify Configuration
- Ensure you have access to the Google Cloud project
- Verify `gcloud` is authenticated: `gcloud auth list`
- Check project is set: `gcloud config get-value project`

## Deployment

### Option 1: Using the Deployment Script
```bash
./deploy-production.sh
```

The script will:
- Check if GEMINI_API_KEY is properly set
- Build the application
- Deploy to App Engine
- Provide deployment status and URLs

### Option 2: Manual Deployment
```bash
# Build the application
./gradlew clean bootJar

# Deploy to App Engine
gcloud app deploy app.yaml --quiet
```

## Post-Deployment

### Verification
```bash
# Check application status
curl https://your-project.lm.r.appspot.com/api/v1/chat/health

# View logs
gcloud app logs read --service=default --limit=50

# Open in browser
gcloud app browse
```

### Monitoring
- **Health endpoint**: `/api/v1/chat/health`
- **Management endpoint**: `/actuator/health`
- **Logs**: `gcloud app logs tail --service=default`

## Production Features

### Performance Optimizations
- F2 instance class (512MB memory)
- Optimized JVM settings with G1GC
- Connection timeouts configured for App Engine
- Minimal debug logging

### AI Configuration
- Retry mechanism: 3 attempts with 20-second delay steps
- Token limit: 120,000 for prompt chunks
- Singleton AI instances to prevent initialization loops

### Security
- Security auto-configuration disabled for App Engine
- Environment-based API key configuration
- Separate production configuration file

## Troubleshooting

### Common Issues
1. **503 Service Unavailable**: Check health check configuration and startup logs
2. **Memory Issues**: Consider upgrading to F4 instance class if needed
3. **Timeout Issues**: Adjust health check timeouts in `app.yaml`

### Debug Mode
To enable debug logging temporarily, update `application-prod.properties`:
```properties
logging.level.com.github.istin.dmtools=DEBUG
```

Then redeploy the application. 