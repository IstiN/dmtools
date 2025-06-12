# DMTools Server Properties Configuration

This document describes the configuration properties specific to the DMTools Server module.

## Example Configuration Files

### Development Configuration (`application.properties`)
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/
server.ssl.enabled=false

# Database Configuration
spring.datasource.url=jdbc:h2:./data/dmtools-db;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 Console (for development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# OAuth2 Configuration
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=read:user,user:email
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/login/oauth2/code/github

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
jwt.header=Authorization
jwt.prefix=Bearer 

# Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
springdoc.swagger-ui.tagsSorter=alpha

# Logging
logging.level.root=INFO
logging.level.com.github.istin.dmtools=DEBUG
logging.file.name=logs/dmtools.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=600s
spring.cache.cache-names=jobs,config,users

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized
management.health.db.enabled=true

# Job Execution
dmtools.job.execution.thread-pool-size=10
dmtools.job.execution.timeout=3600000
dmtools.job.retry.max-attempts=3
dmtools.job.retry.delay=5000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
dmtools.upload.temp-dir=./temp

# Rate Limiting
dmtools.rate-limit.enabled=true
dmtools.rate-limit.requests-per-second=10
dmtools.rate-limit.burst-size=20
```

### Production Configuration (`application-prod.properties`)
```properties
# Server Configuration
server.port=${PORT}
server.servlet.context-path=/
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

# Database Configuration
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# OAuth2 Configuration
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=read:user,user:email
spring.security.oauth2.client.registration.github.redirect-uri=${GITHUB_REDIRECT_URI}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
jwt.header=Authorization
jwt.prefix=Bearer 

# Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
springdoc.swagger-ui.tagsSorter=alpha

# Logging
logging.level.root=WARN
logging.level.com.github.istin.dmtools=INFO
logging.file.name=${LOG_FILE_PATH}
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=3600s
spring.cache.cache-names=jobs,config,users

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized
management.health.db.enabled=true

# Job Execution
dmtools.job.execution.thread-pool-size=${JOB_THREAD_POOL_SIZE}
dmtools.job.execution.timeout=${JOB_TIMEOUT}
dmtools.job.retry.max-attempts=${JOB_RETRY_ATTEMPTS}
dmtools.job.retry.delay=${JOB_RETRY_DELAY}

# File Upload
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE}
dmtools.upload.temp-dir=${TEMP_DIR}

# Rate Limiting
dmtools.rate-limit.enabled=true
dmtools.rate-limit.requests-per-second=${RATE_LIMIT_REQUESTS}
dmtools.rate-limit.burst-size=${RATE_LIMIT_BURST}
```

## Required Environment Variables

For production deployment, set these environment variables:

```bash
# Server Configuration
export PORT="8080"
export SSL_KEYSTORE_PATH="/path/to/keystore.p12"
export SSL_KEYSTORE_PASSWORD="your-keystore-password"

# Database Configuration
export DB_URL="jdbc:postgresql://localhost:5432/dmtools"
export DB_USERNAME="dbuser"
export DB_PASSWORD="dbpassword"

# OAuth2 Configuration
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"
export GITHUB_REDIRECT_URI="https://your-domain.com/login/oauth2/code/github"

# JWT Configuration
export JWT_SECRET="your-jwt-secret"
export JWT_EXPIRATION="86400000"

# Logging
export LOG_FILE_PATH="/var/log/dmtools/app.log"

# Job Execution
export JOB_THREAD_POOL_SIZE="20"
export JOB_TIMEOUT="3600000"
export JOB_RETRY_ATTEMPTS="3"
export JOB_RETRY_DELAY="5000"

# File Upload
export MAX_FILE_SIZE="10MB"
export MAX_REQUEST_SIZE="10MB"
export TEMP_DIR="/var/tmp/dmtools"

# Rate Limiting
export RATE_LIMIT_REQUESTS="10"
export RATE_LIMIT_BURST="20"
```

## Server Configuration

### Basic Server Settings
- `server.port`: Server port number (default: 8080)
- `server.servlet.context-path`: Application context path
- `server.ssl.enabled`: Enable SSL (true/false)
- `server.ssl.key-store`: SSL keystore location
- `server.ssl.key-store-password`: SSL keystore password
- `server.ssl.key-store-type`: SSL keystore type (e.g., PKCS12)

### Spring Security
- `spring.security.oauth2.client.registration.github.client-id`: GitHub OAuth client ID
  - How to get: GitHub Settings -> Developer Settings -> OAuth Apps -> New OAuth App
- `spring.security.oauth2.client.registration.github.client-secret`: GitHub OAuth client secret
- `spring.security.oauth2.client.registration.github.scope`: OAuth scopes (e.g., "read:user,repo")

### Database Configuration
- `spring.datasource.url`: Database connection URL
- `spring.datasource.username`: Database username
- `spring.datasource.password`: Database password
- `spring.jpa.hibernate.ddl-auto`: Hibernate DDL mode (e.g., "update")
- `spring.jpa.show-sql`: Show SQL queries in logs (true/false)

### JWT Configuration
- `jwt.secret`: JWT signing key
  - How to generate: Use a secure random generator to create a base64-encoded key
- `jwt.expiration`: Token expiration time in milliseconds
- `jwt.header`: HTTP header name for JWT (default: "Authorization")
- `jwt.prefix`: JWT prefix in header (default: "Bearer ")

### Swagger/OpenAPI
- `springdoc.api-docs.path`: OpenAPI documentation path (default: "/v3/api-docs")
- `springdoc.swagger-ui.path`: Swagger UI path (default: "/swagger-ui.html")
- `springdoc.swagger-ui.operationsSorter`: Operations sorting method
- `springdoc.swagger-ui.tagsSorter`: Tags sorting method

### Logging
- `logging.level.root`: Root logging level
- `logging.level.com.github.istin.dmtools`: Application logging level
- `logging.file.name`: Log file location
- `logging.pattern.console`: Console log pattern
- `logging.pattern.file`: File log pattern

### Cache Configuration
- `spring.cache.type`: Cache type (e.g., "caffeine")
- `spring.cache.caffeine.spec`: Caffeine cache specification
- `spring.cache.cache-names`: Cache names

### Actuator
- `management.endpoints.web.exposure.include`: Exposed actuator endpoints
- `management.endpoint.health.show-details`: Show detailed health information
- `management.health.db.enabled`: Enable database health check

## Application-Specific Settings

### Job Execution
- `dmtools.job.execution.thread-pool-size`: Thread pool size for job execution
- `dmtools.job.execution.timeout`: Job execution timeout in milliseconds
- `dmtools.job.retry.max-attempts`: Maximum retry attempts for failed jobs
- `dmtools.job.retry.delay`: Delay between retries in milliseconds

### File Upload
- `spring.servlet.multipart.max-file-size`: Maximum file size
- `spring.servlet.multipart.max-request-size`: Maximum request size
- `dmtools.upload.temp-dir`: Temporary directory for file uploads

### Rate Limiting
- `dmtools.rate-limit.enabled`: Enable rate limiting
- `dmtools.rate-limit.requests-per-second`: Maximum requests per second
- `dmtools.rate-limit.burst-size`: Burst size for rate limiting

## OAuth2 Provider Configuration

### GitHub OAuth2 Configuration

1. Go to GitHub Settings > Developer Settings > OAuth Apps > New OAuth App
2. Fill in the application details:
   - Application name: DMTools
   - Homepage URL: https://your-domain.com
   - Authorization callback URL: https://your-domain.com/login/oauth2/code/github
3. After registration, you'll receive:
   - Client ID
   - Client Secret
4. Configure in `application-prod.properties`:
```properties
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=read:user,user:email
spring.security.oauth2.client.registration.github.redirect-uri=https://your-domain.com/login/oauth2/code/github
```

### Google OAuth2 Configuration

1. Go to Google Cloud Console > APIs & Services > Credentials
2. Create a new OAuth 2.0 Client ID:
   - Application type: Web application
   - Name: DMTools
   - Authorized JavaScript origins: https://your-domain.com
   - Authorized redirect URIs: https://your-domain.com/login/oauth2/code/google
3. After creation, you'll receive:
   - Client ID
   - Client Secret
4. Configure in `application-prod.properties`:
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=https://your-domain.com/login/oauth2/code/google
```

### Microsoft OAuth2 Configuration

1. Go to Azure Portal > Azure Active Directory > App registrations
2. Create a new registration:
   - Name: DMTools
   - Supported account types: Single tenant
   - Redirect URI: Web, https://your-domain.com/login/oauth2/code/microsoft
3. After registration, you'll receive:
   - Application (client) ID
   - Client Secret (create one in Certificates & secrets)
4. Configure in `application-prod.properties`:
```properties
spring.security.oauth2.client.registration.microsoft.client-id=${MICROSOFT_CLIENT_ID}
spring.security.oauth2.client.registration.microsoft.client-secret=${MICROSOFT_CLIENT_SECRET}
spring.security.oauth2.client.registration.microsoft.scope=openid,profile,email
spring.security.oauth2.client.registration.microsoft.redirect-uri=https://your-domain.com/login/oauth2/code/microsoft
```

### Required Environment Variables for OAuth2

Add these to your production environment:

```bash
# GitHub OAuth2
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"

# Google OAuth2
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# Microsoft OAuth2
export MICROSOFT_CLIENT_ID="your-microsoft-client-id"
export MICROSOFT_CLIENT_SECRET="your-microsoft-client-secret"
```

### Security Best Practices

1. Always use HTTPS in production
2. Store OAuth2 credentials securely (environment variables or secret management service)
3. Use appropriate scopes (request only necessary permissions)
4. Regularly rotate client secrets
5. Monitor OAuth2 usage and implement rate limiting
6. Set up proper error handling and logging for OAuth2 flows
7. Implement CSRF protection
8. Use secure session management
9. Set appropriate session timeouts
10. Implement proper logout handling

### Troubleshooting OAuth2

Common issues and solutions:

1. Invalid redirect URI
   - Ensure the redirect URI matches exactly what's configured in the OAuth2 provider
   - Check for trailing slashes and protocol (http vs https)

2. Invalid client credentials
   - Verify client ID and secret are correct
   - Check if credentials are properly escaped in environment variables

3. Scope issues
   - Verify requested scopes are approved in the OAuth2 provider
   - Check if user has granted necessary permissions

4. CORS issues
   - Ensure proper CORS configuration for your domain
   - Check if the OAuth2 provider allows requests from your domain

5. SSL/TLS issues
   - Verify SSL certificate is valid
   - Check if the OAuth2 provider accepts your SSL certificate

## Usage Notes

1. All sensitive properties (passwords, keys, secrets) should be stored in environment variables or a secure configuration management system.
2. Never commit property files containing sensitive information to version control.
3. Use `application-local.properties` for local development overrides.
4. The system will first check for properties in the configuration file, then fall back to environment variables.
5. For production deployment, use environment variables or a secure configuration management system.
6. Keep the JWT secret key secure and rotate it periodically.
7. Configure appropriate CORS settings for your deployment environment.
8. Adjust logging levels based on the environment (development/production). 