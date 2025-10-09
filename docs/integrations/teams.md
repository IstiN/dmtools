# Microsoft Teams Integration

## Overview

The Microsoft Teams integration provides full access to Teams chats, messages, teams, and channels through MCP tools. It supports both read and write operations, enabling automation of Teams workflows.

## Prerequisites

1. **Azure App Registration**
   - Register an application in [Azure Portal](https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationsListBlade)
   - Note your Application (client) ID
   - Configure as a "Mobile and desktop application" (not a web app)
   - Set redirect URI to `http://localhost:8080` for browser auth, or leave empty for device code flow

2. **API Permissions**
   - Add the following Microsoft Graph API **Delegated** permissions:
     - `User.Read` - Read user profile
     - `Chat.Create` - Create new chats
     - `Chat.Read` - Read user's chats
     - `Chat.ReadBasic` - Read basic chat info
     - `Chat.ReadWrite` - Read and write chats
     - `ChatMessage.Read` - Read chat messages
     - `ChatMessage.Send` - Send chat messages
     - `Mail.Read` - Read user's mail
     - `Mail.ReadBasic` - Read basic mail info
     - `Team.ReadBasic.All` - Read team info (requires admin consent)
     - `Channel.ReadBasic.All` - Read channel info (requires admin consent)

3. **Admin Consent** (Optional)
   - If you need team/channel access (`Team.ReadBasic.All`, `Channel.ReadBasic.All`), request admin consent
   - For personal chats only, these permissions are not required

## Configuration

### Environment Variables

Configure the Teams integration using these environment variables:

```bash
# Required
export TEAMS_CLIENT_ID="your-azure-app-client-id"

# Optional (with defaults)
export TEAMS_TENANT_ID="common"  # or your specific tenant ID
export TEAMS_AUTH_METHOD="device"  # "device", "browser", or "refresh_token"
export TEAMS_AUTH_PORT="8080"  # for browser auth only
export TEAMS_TOKEN_CACHE_PATH="./teams.token"  # where to cache tokens
export TEAMS_REFRESH_TOKEN=""  # pre-configured refresh token (optional)

# Scopes (defaults shown, override if needed)
export TEAMS_SCOPES="User.Read Chat.Create Chat.Read Chat.ReadBasic Chat.ReadWrite ChatMessage.Read ChatMessage.Send Mail.Read Mail.ReadBasic openid profile email offline_access"
```

### Authentication Methods

#### 1. Device Code Flow (Recommended)

Best for remote/headless environments or when port 8080 is in use:

```bash
export TEAMS_CLIENT_ID="your-azure-app-client-id"
export TEAMS_AUTH_METHOD="device"

# Start authentication via MCP
./dmtools.sh mcp teams_auth_start

# Output will show:
# {
#   "verification_url": "https://microsoft.com/devicelogin",
#   "user_code": "ABC12345",
#   "expires_in": 900,
#   "message": "Please visit https://microsoft.com/devicelogin and enter code: ABC12345"
# }

# Visit the URL, enter the code, approve permissions

# Complete authentication
./dmtools.sh mcp teams_auth_complete

# Save the refresh token from output:
export TEAMS_REFRESH_TOKEN="0.A..."
```

#### 2. Browser Flow

Opens a browser window automatically:

```bash
export TEAMS_CLIENT_ID="your-client-id"
export TEAMS_AUTH_METHOD="browser"
export TEAMS_AUTH_PORT="8080"
```

#### 3. Refresh Token Flow

Use a pre-configured refresh token (from previous auth):

```bash
export TEAMS_CLIENT_ID="your-client-id"
export TEAMS_REFRESH_TOKEN="0.A..."
export TEAMS_AUTH_METHOD="refresh_token"
```

## Usage

### Check Authentication Status

```bash
./dmtools.sh mcp teams_auth_status
```

### List Available Teams Tools

```bash
./dmtools.sh mcp list teams
```

### Common Operations

#### List Chats

```bash
./dmtools.sh mcp teams_get_chats
```

#### Find Chat by Name

```bash
./dmtools.sh mcp teams_find_chat_by_name --data '{"chatName":"Project Team"}'
```

#### Get Chat Messages

```bash
./dmtools.sh mcp teams_get_messages --data '{"chatId":"19:xxx@thread.v2", "limit":50}'
```

#### Send Message to Chat

```bash
./dmtools.sh mcp teams_send_message --data '{"chatId":"19:xxx@thread.v2", "content":"Hello from MCP!"}'
```

#### List Joined Teams

```bash
./dmtools.sh mcp teams_get_joined_teams
```

#### Get Team Channels

```bash
./dmtools.sh mcp teams_get_team_channels --data '{"teamId":"team-id"}'
```

## Available MCP Tools

### Authentication Tools

- `teams_auth_start` - Start device code authentication
- `teams_auth_complete` - Complete device code authentication
- `teams_auth_status` - Check authentication status

### Chat Operations

- `teams_get_chats` - List all user chats
- `teams_find_chat_by_name` - Find chat by name
- `teams_get_messages` - Get messages from chat by ID
- `teams_get_messages_by_name` - Get messages from chat by name
- `teams_send_message` - Send message to chat by ID
- `teams_send_message_by_name` - Send message to chat by name

### Team Operations

- `teams_get_joined_teams` - List teams user is member of
- `teams_find_team_by_name` - Find team by name
- `teams_get_team_channels` - Get channels in a team
- `teams_find_channel_by_name` - Find channel in team by name
- `teams_get_channel_messages_by_name` - Get channel messages

## Architecture

### Components

- **BasicTeamsClient** - Singleton client reading configuration from environment
- **TeamsClient** - Core Teams REST client with MCP tool annotations
- **TeamsAuthTools** - Separate authentication management tools
- **MicrosoftGraphRestClient** - Base OAuth 2.0 client for Graph API

### Token Management

- Access tokens are cached automatically
- Refresh tokens are used to renew access tokens
- Token cache file is configurable via `TEAMS_TOKEN_CACHE_PATH`
- Tokens are automatically refreshed before expiration

## Troubleshooting

### "TEAMS_CLIENT_ID not configured"

Set the `TEAMS_CLIENT_ID` environment variable with your Azure App Registration client ID.

### "Need admin approval"

Some permissions require admin consent:
- `Team.ReadBasic.All` - for reading team information
- `Channel.ReadBasic.All` - for reading channel information

Either:
1. Remove these permissions from your Azure App Registration
2. Request admin consent from your Azure AD administrator
3. Use personal account (if available) instead of organization account

### "Invalid refresh token"

Your refresh token has expired or is invalid. Re-authenticate:

```bash
./dmtools.sh mcp teams_auth_start
# Complete the flow
./dmtools.sh mcp teams_auth_complete
```

### "Port 8080 already in use"

Switch to device code authentication method:

```bash
export TEAMS_AUTH_METHOD="device"
```

## Security Best Practices

1. **Never commit tokens** - Add `.token` files to `.gitignore`
2. **Use environment variables** - Don't hardcode credentials
3. **Limit permissions** - Only request necessary scopes
4. **Rotate tokens** - Periodically re-authenticate
5. **Secure storage** - Protect token cache files with proper file permissions

## Examples

### Automated Chat Monitoring

```bash
# Get recent messages from specific chat
./dmtools.sh mcp teams_get_messages_by_name \
  --data '{"chatName":"Support Team", "limit":10}'
```

### Team Reporting

```bash
# List all teams
./dmtools.sh mcp teams_get_joined_teams

# Get channels for specific team
./dmtools.sh mcp teams_get_team_channels \
  --data '{"teamId":"your-team-id"}'
```

### Automated Notifications

```bash
# Send notification to team chat
./dmtools.sh mcp teams_send_message_by_name \
  --data '{"chatName":"Deployment Updates", "content":"Deploy completed successfully!"}'
```

## Additional Resources

- [Microsoft Graph API Documentation](https://docs.microsoft.com/en-us/graph/api/resources/teams-api-overview)
- [Azure App Registration Guide](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app)
- [OAuth 2.0 Device Code Flow](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code)



