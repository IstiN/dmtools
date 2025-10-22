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
     
     **Core Chat Permissions:**
     - `User.Read` - Read user profile
     - `Chat.Create` - Create new chats
     - `Chat.Read` - Read user's chats
     - `Chat.ReadBasic` - Read basic chat info
     - `Chat.ReadWrite` - Read and write chats
     - `ChatMessage.Read` - Read chat messages
     - `ChatMessage.Send` - Send chat messages (requires admin consent)
     
     **SharePoint/File Access (for recordings and attachments):**
     - `Files.Read` - Read user's files
     - `Files.Read.All` - Read all files (requires admin consent)
     - `Sites.Read.All` - Read SharePoint sites (requires admin consent, recommended for full recording/transcript access)
     
     **Email Integration:**
     - `Mail.Read` - Read user's mail
     - `Mail.ReadBasic` - Read basic mail info
     
     **Team/Channel Access:**
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
export TEAMS_SCOPES="User.Read Chat.Create Chat.Read Chat.ReadBasic Chat.ReadWrite ChatMessage.Read Files.Read Sites.Read.All Channel.ReadBasic.All Team.ReadBasic.All openid profile email offline_access"
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

### Understanding Tool Naming Convention

Teams tools follow a consistent naming pattern:

- Tools ending in `_raw` return full JSON objects from the API
- Tools without `_raw` return simplified, human-readable output
- Simplified output filters out noisy system messages (member joins/leaves, pins, etc.)
- Simplified output includes: author, body, date, reactions, mentions, attachments

### Common Operations

#### List Recent Chats (Simplified)

```bash
# Get 50 most recent chats (default)
dmtools teams_recent_chats

# Get all recent chats
dmtools teams_recent_chats 0

# Get only 1-on-1 chats
dmtools teams_recent_chats 10 "oneOnOne"
```

#### List All Chats (Simplified)

```bash
# Get 50 chats (default)
dmtools teams_chats

# Get all chats
dmtools teams_chats 0

# Get raw JSON data
dmtools teams_chats_raw 50
```

#### Get Chat Messages (Simplified)

```bash
# Get 100 messages (default) from chat by name
dmtools teams_messages "Project Team"

# Get specific number of messages
dmtools teams_messages "Project Team" 50

# Get all messages (0 = unlimited)
dmtools teams_messages "Project Team" 0

# Get messages since a specific date (ISO 8601 format)
dmtools teams_messages_since "Project Team" "2025-10-21T00:00:00Z"
```

#### Get Raw Messages

```bash
# Get raw JSON messages by chat ID
dmtools teams_messages_by_chat_id_raw "19:xxx@thread.v2" 100

# Get raw messages by chat name
dmtools teams_messages_raw "Project Team" 50
```

#### Send Message to Chat

```bash
# Send by chat name
dmtools teams_send_message "Project Team" "Hello from MCP!"

# Send by chat ID
dmtools teams_send_message_by_id "19:xxx@thread.v2" "Hello!"
```

#### Personal Notes (Self Chat)

```bash
# Get your personal notes
dmtools teams_myself_messages

# Send note to yourself
dmtools teams_send_myself_message "Remember to check this later"
```

#### List Joined Teams

```bash
dmtools teams_get_joined_teams_raw
```

#### Get Team Channels

```bash
dmtools teams_get_team_channels_raw "team-id"
```

#### Get Channel Messages

```bash
dmtools teams_get_channel_messages_by_name_raw "Team Name" "General" 50
```

## Available MCP Tools

### Authentication Tools

- `teams_auth_start` - Start device code authentication
- `teams_auth_complete` - Complete device code authentication
- `teams_auth_status` - Check authentication status

### Chat Operations

**List Chats:**
- `teams_chats_raw` - List chats with full JSON (limit parameter: 0 for all, default: 50)
- `teams_chats` - List chats with simplified output (limit parameter: 0 for all, default: 50)
- `teams_recent_chats` - Get recent chats sorted by activity with optional chat type filter (default limit: 50)

**Find Chat:**
- `teams_chat_by_name_raw` - Find a chat by topic or participant name

**Get Messages:**
- `teams_messages_by_chat_id_raw` - Get raw messages from chat by ID (supports optional OData filter)
- `teams_messages_raw` - Get raw messages from chat by name
- `teams_messages` - Get simplified messages from chat by name (filters noisy system messages)
- `teams_messages_since_by_id` - Get messages since a specific date by chat ID (ISO 8601)
- `teams_messages_since` - Get messages since a specific date by chat name (ISO 8601)

**Send Messages:**
- `teams_send_message_by_id` - Send message to chat by ID
- `teams_send_message` - Send message to chat by name

**Personal Notes (Self Chat):**
- `teams_myself_messages_raw` - Get personal notes (raw JSON)
- `teams_myself_messages` - Get personal notes (simplified)
- `teams_send_myself_message` - Send note to yourself

### File Download Operations

- `teams_download_file` - Download file from Teams (Graph API or SharePoint URL)
- `teams_get_message_hosted_contents` - Get hosted contents for a message
- `teams_search_user_drive_files` - Search files in user's OneDrive

### Recording and Transcript Operations

- `teams_get_call_transcripts` - Get transcripts for a call/meeting
- `teams_get_recording_transcripts` - Get transcript metadata for a recording
- `teams_list_recording_transcripts` - List available transcripts for a recording
- `teams_extract_transcript_from_sharepoint` - Extract transcript info by parsing SharePoint HTML
- `teams_download_recording_transcript` - Download transcript (VTT) file from Teams recording

### Team Operations

- `teams_get_joined_teams_raw` - List teams user is member of
- `teams_find_team_by_name_raw` - Find team by name
- `teams_get_team_channels_raw` - Get channels in a team
- `teams_find_channel_by_name_raw` - Find channel in team by name
- `teams_get_channel_messages_by_name_raw` - Get channel messages by team and channel names

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

## Key Features

### Message Filtering

Simplified message output (`teams_messages`, `teams_messages_since`, etc.) automatically filters out noisy system messages:

**Filtered Events:**
- Member join/leave notifications
- Message pins/unpins
- Call start/end notifications
- App installations

**Preserved Events:**
- Meeting recordings
- Meeting transcripts
- Poll results (Adaptive Cards)
- User messages

### Limit Parameters

Most tools support a `limit` parameter for controlling result count:

- **Default behavior**: Returns a reasonable default (50 for chats, 100 for messages)
- **Specific limit**: Pass a number to get exactly that many results
- **Get all**: Pass `0` to retrieve unlimited results

**Examples:**
```bash
# Default (50 chats)
dmtools teams_recent_chats

# Specific limit (10 chats)
dmtools teams_recent_chats 10

# All chats (0 = unlimited)
dmtools teams_recent_chats 0

# Same pattern for messages
dmtools teams_messages "chat-name"      # Default: 100
dmtools teams_messages "chat-name" 50   # Limit: 50
dmtools teams_messages "chat-name" 0    # All messages
```

### Smart Pagination

Message tools use smart pagination with early exit:

- **`teams_messages_since`**: Stops fetching when encountering messages older than the specified date
- **Performer pattern**: Efficiently processes messages on-the-fly without loading everything into memory
- **Duplicate detection**: Automatically filters duplicate chats from API results

## Examples

### Automated Chat Monitoring

```bash
# Get recent messages from specific chat
dmtools teams_messages "Support Team" 10

# Get messages since yesterday
dmtools teams_messages_since "Support Team" "2025-10-20T00:00:00Z"

# Get all messages (no limit)
dmtools teams_messages "Support Team" 0
```

### Team Reporting

```bash
# List all teams
dmtools teams_get_joined_teams_raw

# Get channels for specific team
dmtools teams_get_team_channels_raw "your-team-id"

# Get recent channel messages
dmtools teams_get_channel_messages_by_name_raw "Team Name" "General" 50
```

### Automated Notifications

```bash
# Send notification to team chat
dmtools teams_send_message "Deployment Updates" "Deploy completed successfully!"

# Send note to yourself
dmtools teams_send_myself_message "TODO: Review PRs"
```

### Working with Recordings and Transcripts

```bash
# Search for messages with recordings in a chat
dmtools teams_messages "Meeting Chat" 100

# Download a recording from SharePoint URL
dmtools teams_download_file "https://company-my.sharepoint.com/:v:/p/..." "/tmp/recording.mp4"

# Extract transcript info from SharePoint page
dmtools teams_extract_transcript_from_sharepoint "https://company-my.sharepoint.com/..."

# Download transcript
dmtools teams_download_recording_transcript "driveId" "itemId" "transcriptId" "/tmp/transcript.vtt"
```

## Additional Resources

- [Microsoft Graph API Documentation](https://docs.microsoft.com/en-us/graph/api/resources/teams-api-overview)
- [Azure App Registration Guide](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app)
- [OAuth 2.0 Device Code Flow](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code)



