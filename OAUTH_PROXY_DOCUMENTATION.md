# OAuth Proxy - Multi-Client Authentication

## 🎯 Overview

The OAuth Proxy feature allows multiple client applications (web, mobile, desktop) to authenticate through your dmtools service as a proxy, enabling flexible redirect URIs while maintaining centralized OAuth management.

## 🔄 How It Works

### Traditional OAuth Flow (Limitation)
```
Client App → OAuth Provider → dmtools.com → dmtools.com/dashboard
```
**Problem**: Fixed redirect to dmtools.com only

### New OAuth Proxy Flow (Solution)
```
1. Client App → dmtools.com/api/oauth/initiate
2. dmtools.com → OAuth Provider
3. User authenticates
4. Provider → dmtools.com (with state)
5. dmtools.com → Client App (with temp code)
6. Client App → dmtools.com/api/oauth/exchange
7. Client App receives JWT token
```
**Solution**: Supports any client redirect URI!

## 🛠️ API Endpoints

### 1. Initiate OAuth Flow
```
POST /api/oauth/initiate
Content-Type: application/json

{
  "provider": "google|microsoft|github",
  "client_redirect_uri": "https://myapp.com/auth/callback",
  "client_type": "web|mobile|desktop",
  "environment": "dev|staging|prod"
}
```

**Response:**
```json
{
  "auth_url": "https://accounts.google.com/oauth/authorize?...",
  "state": "oauth_proxy_12345-abcd-...",
  "expires_in": 300
}
```

### 2. Exchange Code for Token
```
POST /api/oauth/exchange
Content-Type: application/json

{
  "code": "temp_code_from_callback",
  "state": "oauth_proxy_12345-abcd-..."
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 3. Get Supported Providers
```
GET /api/oauth/providers
```

**Response:**
```json
{
  "providers": ["google", "microsoft", "github"],
  "client_types": ["web", "mobile", "desktop"],
  "environments": ["dev", "staging", "prod"]
}
```

## 💻 Client Integration Examples

### Flutter Web Application
```dart
class OAuthService {
  static const String BASE_URL = 'https://yourapi.com';
  
  Future<String?> loginWithGoogle() async {
    // Step 1: Initiate OAuth
    final response = await http.post(
      Uri.parse('$BASE_URL/api/oauth/initiate'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'provider': 'google',
        'client_redirect_uri': '${window.location.origin}/auth/callback',
        'client_type': 'web',
        'environment': 'prod'
      }),
    );
    
    final data = jsonDecode(response.body);
    
    // Step 2: Redirect to OAuth provider
    window.location.href = data['auth_url'];
    
    return data['state'];
  }
  
  Future<String?> handleCallback(String code, String state) async {
    // Step 3: Exchange code for token
    final response = await http.post(
      Uri.parse('$BASE_URL/api/oauth/exchange'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'code': code,
        'state': state,
      }),
    );
    
    final data = jsonDecode(response.body);
    return data['access_token'];
  }
}
```

### Mobile Apps (Android/iOS with Custom URI Schemes)
```dart
Future<String?> loginWithGoogle() async {
  // Step 1: Initiate OAuth
  final response = await http.post(
    Uri.parse('$BASE_URL/api/oauth/initiate'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'provider': 'google',
      'client_redirect_uri': 'myapp://auth/callback', // Custom URI scheme
      'client_type': 'mobile',
      'environment': 'prod'
    }),
  );
  
  final data = jsonDecode(response.body);
  
  // Step 2: Open system browser
  await launchUrl(Uri.parse(data['auth_url']));
  
  return data['state'];
}

// Handle deep link when user returns to app
void handleDeepLink(String url) {
  final uri = Uri.parse(url);
  final code = uri.queryParameters['code'];
  final state = uri.queryParameters['state'];
  
  if (code != null && state != null) {
    exchangeCodeForToken(code, state);
  }
}
```

### Desktop Applications
```javascript
// Electron or similar desktop app
const { shell } = require('electron');

async function loginWithGoogle() {
  // Step 1: Initiate OAuth
  const response = await fetch('https://yourapi.com/api/oauth/initiate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      provider: 'google',
      client_redirect_uri: 'http://localhost:3000/auth/callback',
      client_type: 'desktop',
      environment: 'prod'
    })
  });
  
  const data = await response.json();
  
  // Step 2: Open external browser
  shell.openExternal(data.auth_url);
  
  // Step 3: Listen for callback on local server
  startLocalServer(data.state);
}
```

## 🔧 Environment Configuration

### Development
```properties
# application-dev.properties
app.base-url=http://localhost:8080
spring.profiles.active=dev
```

### Staging
```properties
# application-staging.properties
app.base-url=https://staging-api.yourapp.com
spring.profiles.active=staging
```

### Production
```properties
# application-prod.properties
app.base-url=https://api.yourapp.com
spring.profiles.active=prod
```

## 🔐 Security Features

- **State Validation**: Each OAuth flow uses a unique state parameter
- **Time-Limited Codes**: Temporary codes expire in 5 minutes
- **Single-Use Codes**: Codes are deleted after exchange
- **Client Validation**: Only registered client redirect URIs are allowed
- **Environment Isolation**: Different environments use different base URLs

## 🧪 Testing

Visit `/test-oauth-proxy.html` to test the OAuth proxy functionality with a visual interface.

## 🚀 Production Setup

### With Redis (Recommended)
```gradle
// Add to build.gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

```properties
# application-prod.properties
spring.redis.host=your-redis-host
spring.redis.port=6379
spring.redis.password=your-redis-password
```

### OAuth Provider Configuration

#### Google
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create OAuth 2.0 Client ID
3. Add authorized redirect URI: `https://yourapi.com/login/oauth2/code/google`

#### Microsoft
1. Go to [Azure Portal](https://portal.azure.com/)
2. Create App Registration
3. Add redirect URI: `https://yourapi.com/login/oauth2/code/microsoft`

#### GitHub
1. Go to GitHub Settings > Developer settings > OAuth Apps
2. Create OAuth App
3. Set authorization callback URL: `https://yourapi.com/login/oauth2/code/github`

## 🔍 Troubleshooting

### Common Issues

1. **"Invalid or expired state"**
   - Check that state parameter matches
   - Ensure request is made within 5 minutes

2. **"Invalid or expired code"**
   - Temporary codes expire in 5 minutes
   - Codes can only be used once

3. **"Unknown OAuth provider"**
   - Ensure provider is one of: google, microsoft, github
   - Check application.properties configuration

4. **CORS Issues**
   - Configure CORS in SecurityConfig for web clients
   - Use server-side redirects for mobile/desktop

## 📈 Benefits

✅ **Multi-client support**: Web, mobile, desktop apps  
✅ **Custom URI schemes**: `myapp://auth/callback` for mobile  
✅ **Environment flexibility**: Dev, staging, prod  
✅ **Backward compatibility**: Existing OAuth flows continue to work  
✅ **Security**: Time-limited, single-use authorization codes  
✅ **Scalability**: Redis-based temporary storage (production)  
✅ **Centralized management**: Single OAuth configuration for all clients 