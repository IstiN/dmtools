# DMtools MCP Tools Reference

This is the complete list of available MCP tools, generated directly from the running DMtools instance.

## How to Use

### From Command Line
```bash
# List all tools
dmtools list

# Execute a tool
dmtools <tool_name> [arguments]

# Examples
dmtools jira_get_ticket PROJ-123
dmtools gemini_ai_chat "Hello"
```

### From JavaScript Agents
All MCP tools are directly accessible as JavaScript functions:

```javascript
// Direct function calls
const ticket = jira_get_ticket("PROJ-123");
const response = gemini_ai_chat("Analyze this");
file_write("output.txt", "content");
```

## Available Tools

{"tools": [
  {
    "name": "cli_execute_command",
    "inputSchema": {
      "type": "object",
      "required": ["command"],
      "properties": {
        "workingDirectory": {
          "type": "string",
          "description": "Working directory for command execution. Defaults to repository root if not specified. Use absolute path or path relative to current directory.",
          "example": "/path/to/repo"
        },
        "command": {
          "type": "string",
          "description": "CLI command to execute. Must start with whitelisted command (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az).",
          "example": "git commit -m 'Automated update'"
        }
      }
    },
    "description": "Execute CLI commands (git, gh, dmtools, npm, yarn, docker, kubectl, terraform, ansible, aws, gcloud, az) from JavaScript post-actions. Returns command output as string. Commands execute synchronously with environment variables inherited from workflow context. Only whitelisted commands allowed for security."
  },
  {
    "name": "teams_chats_raw",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {"limit": {
        "type": "number",
        "description": "Maximum number of chats (0 for all, default: 50)",
        "example": "50"
      }}
    },
    "description": "List chats for the current user with topic, type, and participant information (returns raw JSON)"
  },
  {
    "name": "teams_chats",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {"limit": {
        "type": "number",
        "description": "Maximum number of chats (0 for all, default: 50)",
        "example": "50"
      }}
    },
    "description": "List chats showing only chat/contact names, last message (truncated to 100 chars), and date"
  },
  {
    "name": "teams_recent_chats",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {
        "limit": {
          "type": "number",
          "description": "Maximum number of recent chats (0 for all, default: 50)",
          "example": "50"
        },
        "chatType": {
          "type": "string",
          "description": "Filter by chat type: 'oneOnOne', 'group', 'meeting', or 'all' (default: 'all')",
          "example": "oneOnOne"
        }
      }
    },
    "description": "Get recent chats sorted by last activity showing chat/contact names, last message with author, and date. Shows 'new: true' for unread messages. Filter by type: 'oneOnOne' for 1-on-1 chats, 'group' for group chats, 'meeting' for meeting chats, or 'all' (default). Only shows chats with activity in the last 90 days."
  },
  {
    "name": "teams_messages_by_chat_id_raw",
    "inputSchema": {
      "type": "object",
      "required": ["chatId"],
      "properties": {
        "chatId": {
          "type": "string",
          "description": "The chat ID"
        },
        "limit": {
          "type": "number",
          "description": "Maximum number of messages (0 for all, default: 100)",
          "example": "100"
        },
        "filter": {
          "type": "string",
          "description": "Optional OData filter (e.g., 'lastModifiedDateTime gt 2025-01-01T00:00:00Z')",
          "example": "lastModifiedDateTime gt 2025-01-01T00:00:00Z"
        }
      }
    },
    "description": "Get messages from a chat by ID with optional server-side filtering. Use $filter syntax with lastModifiedDateTime: 'lastModifiedDateTime gt 2025-01-01T00:00:00Z' (returns raw JSON). Note: createdDateTime is not supported in filters."
  },
  {
    "name": "teams_chat_by_name_raw",
    "inputSchema": {
      "type": "object",
      "required": ["chatName"],
      "properties": {"chatName": {
        "type": "string",
        "description": "The chat topic or participant name to search for"
      }}
    },
    "description": "Find a chat by topic/name or participant name (case-insensitive partial match). Works for group chats and 1-on-1 chats. (returns raw JSON)"
  },
  {
    "name": "teams_messages_raw",
    "inputSchema": {
      "type": "object",
      "required": ["chatName"],
      "properties": {
        "limit": {
          "type": "number",
          "description": "Maximum number of messages (0 for all, default: 100)",
          "example": "100"
        },
        "chatName": {
          "type": "string",
          "description": "The chat name to search for"
        }
      }
    },
    "description": "Get messages from a chat by name (combines find + get messages) (returns raw JSON)"
  },
  {
    "name": "teams_messages",
    "inputSchema": {
      "type": "object",
      "required": ["chatName"],
      "properties": {
        "limit": {
          "type": "number",
          "description": "Maximum number of messages (0 for all, default: 100)",
          "example": "100"
        },
        "chatName": {
          "type": "string",
          "description": "The chat name to search for"
        },
        "sorting": {
          "type": "string",
          "description": "Sort order: 'asc' for oldest first, 'desc' for newest first (default: 'desc')",
          "example": "desc"
        }
      }
    },
    "description": "Get messages from a chat with simplified output showing only: author, body, date, reactions, mentions, and attachments"
  },
  {
    "name": "teams_messages_since_by_id",
    "inputSchema": {
      "type": "object",
      "required": [
        "chatId",
        "sinceDate"
      ],
      "properties": {
        "chatId": {
          "type": "string",
          "description": "The chat ID"
        },
        "sinceDate": {
          "type": "string",
          "description": "ISO 8601 date string (e.g., '2025-10-08T00:00:00Z')",
          "example": "2025-10-08T00:00:00Z"
        },
        "sorting": {
          "type": "string",
          "description": "Sort order: 'asc' for oldest first, 'desc' for newest first (default: 'desc')",
          "example": "desc"
        }
      }
    },
    "description": "Get messages from a chat starting from a specific date (ISO 8601 format, e.g., '2025-10-08T00:00:00Z'). Returns simplified format. Uses smart pagination with early exit for performance."
  },
  {
    "name": "teams_messages_since",
    "inputSchema": {
      "type": "object",
      "required": [
        "chatName",
        "sinceDate"
      ],
      "properties": {
        "chatName": {
          "type": "string",
          "description": "The chat name to search for"
        },
        "sinceDate": {
          "type": "string",
          "description": "ISO 8601 date string (e.g., '2025-10-08T00:00:00Z')",
          "example": "2025-10-08T00:00:00Z"
        },
        "sorting": {
          "type": "string",
          "description": "Sort order: 'asc' for oldest first, 'desc' for newest first (default: 'desc')",
          "example": "desc"
        }
      }
    },
    "description": "Get messages from a chat by name starting from a specific date (ISO 8601 format). Returns simplified format. Uses smart pagination with early exit for performance."
  },
  {
    "name": "teams_send_message_by_id",
    "inputSchema": {
      "type": "object",
      "required": [
        "chatId",
        "content"
      ],
      "properties": {
        "chatId": {
          "type": "string",
          "description": "The chat ID"
        },
        "content": {
          "type": "string",
          "description": "Message content (plain text or HTML)"
        }
      }
    },
    "description": "Send a message to a chat by ID (returns raw JSON)"
  },
  {
    "name": "teams_send_message",
    "inputSchema": {
      "type": "object",
      "required": [
        "chatName",
        "content"
      ],
      "properties": {
        "chatName": {
          "type": "string",
          "description": "The chat topic or participant name"
        },
        "content": {
          "type": "string",
          "description": "Message content (plain text or HTML)"
        }
      }
    },
    "description": "Send a message to a chat by name or participant name (finds chat, then sends message)"
  },
  {
    "name": "teams_myself_messages_raw",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {"limit": {
        "type": "number",
        "description": "Maximum number of messages (0 for all, default: 100)",
        "example": "100"
      }}
    },
    "description": "Get messages from your personal self chat (notes to yourself) with full raw data"
  },
  {
    "name": "teams_myself_messages",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {"limit": {
        "type": "number",
        "description": "Maximum number of messages (0 for all, default: 100)",
        "example": "100"
      }}
    },
    "description": "Get messages from your personal self chat (notes to yourself) with simplified output"
  },
  {
    "name": "teams_send_myself_message",
    "inputSchema": {
      "type": "object",
      "required": ["content"],
      "properties": {"content": {
        "type": "string",
        "description": "Message content (plain text or HTML)"
      }}
    },
    "description": "Send a message to your personal self chat (notes to yourself)"
  },
  {
    "name": "teams_download_file",
    "inputSchema": {
      "type": "object",
      "required": [
        "url",
        "outputPath"
      ],
      "properties": {
        "url": {
          "type": "string",
          "description": "Graph API URL or SharePoint sharing URL",
          "example": "https://graph.microsoft.com/v1.0/chats/.../hostedContents/.../$value"
        },
        "outputPath": {
          "type": "string",
          "description": "Local file path to save to",
          "example": "/tmp/file.ext"
        }
      }
    },
    "description": "Download a file from Teams (Graph API hostedContents or SharePoint sharing URL). Auto-detects URL type and uses appropriate method."
  },
  {
    "name": "teams_get_message_hosted_contents",
    "inputSchema": {
      "type": "object",
      "required": [
        "chatId",
        "messageId"
      ],
      "properties": {
        "chatId": {
          "type": "string",
          "description": "Chat ID"
        },
        "messageId": {
          "type": "string",
          "description": "Message ID"
        }
      }
    },
    "description": "Get hosted contents (files/transcripts) for a specific message. Returns list of files with download URLs."
  },
  {
    "name": "teams_get_call_transcripts",
    "inputSchema": {
      "type": "object",
      "required": ["callId"],
      "properties": {"callId": {
        "type": "string",
        "description": "Call ID from the meeting"
      }}
    },
    "description": "Get transcripts for a call/meeting using Call Records API. Returns list of transcripts with download URLs."
  },
  {
    "name": "teams_search_user_drive_files",
    "inputSchema": {
      "type": "object",
      "required": [
        "userId",
        "searchQuery"
      ],
      "properties": {
        "userId": {
          "type": "string",
          "description": "User ID to search OneDrive"
        },
        "searchQuery": {
          "type": "string",
          "description": "Search term (meeting name, 'transcript', '.vtt', etc.)"
        }
      }
    },
    "description": "Search for files in a user's OneDrive (e.g., meeting transcripts/recordings). Returns list of files with download URLs."
  },
  {
    "name": "teams_get_recording_transcripts",
    "inputSchema": {
      "type": "object",
      "required": [
        "driveId",
        "itemId"
      ],
      "properties": {
        "itemId": {
          "type": "string",
          "description": "Item ID of the recording file"
        },
        "driveId": {
          "type": "string",
          "description": "Drive ID from the recording file"
        }
      }
    },
    "description": "Get transcript metadata for a recording file. Returns list of available transcripts with download URLs."
  },
  {
    "name": "teams_list_recording_transcripts",
    "inputSchema": {
      "type": "object",
      "required": [
        "driveId",
        "itemId"
      ],
      "properties": {
        "itemId": {
          "type": "string",
          "description": "Recording item ID"
        },
        "driveId": {
          "type": "string",
          "description": "Drive ID"
        }
      }
    },
    "description": "List available transcripts for a recording file. Returns transcript IDs that can be downloaded."
  },
  {
    "name": "teams_extract_transcript_from_sharepoint",
    "inputSchema": {
      "type": "object",
      "required": ["webUrl"],
      "properties": {"webUrl": {
        "type": "string",
        "description": "SharePoint webUrl of the recording"
      }}
    },
    "description": "Extract transcript information by parsing SharePoint HTML page. Useful for finding transcript IDs."
  },
  {
    "name": "teams_download_recording_transcript",
    "inputSchema": {
      "type": "object",
      "required": [
        "driveId",
        "itemId",
        "transcriptId",
        "outputPath"
      ],
      "properties": {
        "itemId": {
          "type": "string",
          "description": "Recording item ID"
        },
        "driveId": {
          "type": "string",
          "description": "Drive ID"
        },
        "outputPath": {
          "type": "string",
          "description": "Local file path to save"
        },
        "transcriptId": {
          "type": "string",
          "description": "Transcript ID (UUID)"
        }
      }
    },
    "description": "Download transcript (VTT) file from a Teams recording using SharePoint API. Requires driveId, itemId, and transcriptId."
  },
  {
    "name": "teams_get_joined_teams_raw",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {}
    },
    "description": "List teams the user is a member of (returns raw JSON)"
  },
  {
    "name": "teams_get_team_channels_raw",
    "inputSchema": {
      "type": "object",
      "required": ["teamId"],
      "properties": {"teamId": {
        "type": "string",
        "description": "The team ID"
      }}
    },
    "description": "Get channels in a specific team (returns raw JSON)"
  },
  {
    "name": "teams_find_team_by_name_raw",
    "inputSchema": {
      "type": "object",
      "required": ["teamName"],
      "properties": {"teamName": {
        "type": "string",
        "description": "The team name to search for"
      }}
    },
    "description": "Find a team by display name (case-insensitive partial match) (returns raw JSON)"
  },
  {
    "name": "teams_find_channel_by_name_raw",
    "inputSchema": {
      "type": "object",
      "required": [
        "teamId",
        "channelName"
      ],
      "properties": {
        "channelName": {
          "type": "string",
          "description": "The channel name to search for"
        },
        "teamId": {
          "type": "string",
          "description": "The team ID"
        }
      }
    },
    "description": "Find a channel by name within a team (case-insensitive partial match) (returns raw JSON)"
  },
  {
    "name": "teams_get_channel_messages_by_name_raw",
    "inputSchema": {
      "type": "object",
      "required": [
        "teamName",
        "channelName"
      ],
      "properties": {
        "teamName": {
          "type": "string",
          "description": "The team name to search for"
        },
        "limit": {
          "type": "number",
          "description": "Maximum number of messages (0 for all, default: 100)",
          "example": "100"
        },
        "channelName": {
          "type": "string",
          "description": "The channel name to search for"
        }
      }
    },
    "description": "Get messages from a channel by team and channel names (returns raw JSON)"
  },
  {
    "name": "jira_xray_create_precondition",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "summary"
      ],
      "properties": {
        "summary": {
          "type": "string",
          "description": "Precondition summary",
          "example": "System is ready for testing"
        },
        "project": {
          "type": "string",
          "description": "Project key (e.g., 'TP')",
          "example": "TP"
        },
        "description": {
          "type": "string",
          "description": "Precondition description",
          "example": "All system components are initialized"
        },
        "steps": {
          "type": "string",
          "description": "Optional JSON array of steps in format [{\"action\": \"...\", \"data\": \"...\", \"result\": \"...\"}]. Will be converted to definition format."
        }
      }
    },
    "description": "Create a Precondition issue in Xray with optional steps (converted to definition). Returns the created ticket key."
  },
  {
    "name": "jira_xray_search_tickets",
    "inputSchema": {
      "type": "object",
      "required": ["searchQueryJQL"],
      "properties": {
        "fields": {
          "type": "array",
          "description": "Array of field names to retrieve (e.g., ['summary', 'description', 'status'])",
          "example": "summary,description,status"
        },
        "searchQueryJQL": {
          "type": "string",
          "description": "JQL search query (e.g., 'project = TP AND issueType = Test')",
          "example": "project = TP AND issueType = Test"
        }
      }
    },
    "description": "Search for Jira tickets using JQL query and enrich Test/Precondition issues with X-ray test steps and preconditions. Returns list of tickets with X-ray data."
  },
  {
    "name": "jira_xray_get_test_details",
    "inputSchema": {
      "type": "object",
      "required": ["testKey"],
      "properties": {"testKey": {
        "type": "string",
        "description": "Jira ticket key (e.g., 'TP-909')",
        "example": "TP-909"
      }}
    },
    "description": "Get test details including steps and preconditions using X-ray GraphQL API. Returns JSONObject with test details."
  },
  {
    "name": "jira_xray_get_test_steps",
    "inputSchema": {
      "type": "object",
      "required": ["testKey"],
      "properties": {"testKey": {
        "type": "string",
        "description": "Jira ticket key (e.g., 'TP-909')",
        "example": "TP-909"
      }}
    },
    "description": "Get test steps for a test issue using X-ray GraphQL API. Returns JSONArray of test steps."
  },
  {
    "name": "jira_xray_get_preconditions",
    "inputSchema": {
      "type": "object",
      "required": ["testKey"],
      "properties": {"testKey": {
        "type": "string",
        "description": "Jira ticket key (e.g., 'TP-909')",
        "example": "TP-909"
      }}
    },
    "description": "Get preconditions for a test issue using X-ray GraphQL API. Returns JSONArray of precondition objects."
  },
  {
    "name": "jira_xray_get_precondition_details",
    "inputSchema": {
      "type": "object",
      "required": ["preconditionKey"],
      "properties": {"preconditionKey": {
        "type": "string",
        "description": "Jira ticket key (e.g., 'TP-910')",
        "example": "TP-910"
      }}
    },
    "description": "Get Precondition details including definition using X-ray GraphQL API. Returns JSONObject with precondition details."
  },
  {
    "name": "jira_xray_add_test_step",
    "inputSchema": {
      "type": "object",
      "required": [
        "issueId",
        "action"
      ],
      "properties": {
        "result": {
          "type": "string",
          "description": "Step expected result (e.g., 'Username accepted')",
          "example": "Username accepted"
        },
        "action": {
          "type": "string",
          "description": "Step action (e.g., 'Enter username')",
          "example": "Enter username"
        },
        "issueId": {
          "type": "string",
          "description": "Jira issue ID (e.g., '12345')",
          "example": "12345"
        },
        "data": {
          "type": "string",
          "description": "Step data (e.g., 'test_user')",
          "example": "test_user"
        }
      }
    },
    "description": "Add a single test step to a test issue using X-ray GraphQL API. Returns JSONObject with created step details."
  },
  {
    "name": "jira_xray_add_test_steps",
    "inputSchema": {
      "type": "object",
      "required": [
        "issueId",
        "steps"
      ],
      "properties": {
        "steps": {
          "type": "object",
          "description": "JSON array string of step objects, each with 'action', 'data', and 'result' fields (e.g., '[{\"action\":\"Enter username\",\"data\":\"test_user\",\"result\":\"Username accepted\"}]')",
          "example": "[{\"action\":\"Enter username\",\"data\":\"test_user\",\"result\":\"Username accepted\"}]"
        },
        "issueId": {
          "type": "string",
          "description": "Jira issue ID (e.g., '12345')",
          "example": "12345"
        }
      }
    },
    "description": "Add multiple test steps to a test issue using X-ray GraphQL API. Returns JSONArray of created step objects."
  },
  {
    "name": "jira_xray_add_precondition_to_test",
    "inputSchema": {
      "type": "object",
      "required": [
        "testIssueId",
        "preconditionIssueId"
      ],
      "properties": {
        "testIssueId": {
          "type": "string",
          "description": "Jira issue ID of the test (e.g., '12345')",
          "example": "12345"
        },
        "preconditionIssueId": {
          "type": "string",
          "description": "Jira issue ID of the precondition (e.g., '12346')",
          "example": "12346"
        }
      }
    },
    "description": "Add a single precondition to a test issue using X-ray GraphQL API. Returns JSONObject with result."
  },
  {
    "name": "jira_xray_add_preconditions_to_test",
    "inputSchema": {
      "type": "object",
      "required": [
        "testIssueId",
        "preconditionIssueIds"
      ],
      "properties": {
        "preconditionIssueIds": {
          "type": "object",
          "description": "JSON array string of precondition issue IDs (e.g., '[\"12346\", \"12347\"]')",
          "example": "[\"12346\", \"12347\"]"
        },
        "testIssueId": {
          "type": "string",
          "description": "Jira issue ID of the test (e.g., '12345')",
          "example": "12345"
        }
      }
    },
    "description": "Add multiple preconditions to a test issue using X-ray GraphQL API. Returns JSONArray of results."
  },
  {
    "name": "file_read",
    "inputSchema": {
      "type": "object",
      "required": ["path"],
      "properties": {"path": {
        "type": "string",
        "description": "File path relative to working directory or absolute path within working directory",
        "example": "outputs/response.md"
      }}
    },
    "description": "Read file content from working directory (supports input/ and outputs/ folders). Returns file content as string or null if file doesn't exist or is inaccessible. All file formats supported as UTF-8 text."
  },
  {
    "name": "file_write",
    "inputSchema": {
      "type": "object",
      "required": [
        "path",
        "content"
      ],
      "properties": {
        "path": {
          "type": "string",
          "description": "File path relative to working directory or absolute path within working directory",
          "example": "inbox/raw/teams_messages/1729766400000-messages.json"
        },
        "content": {
          "type": "string",
          "description": "Content to write to the file as UTF-8 string",
          "example": "{\"messages\": []}"
        }
      }
    },
    "description": "Write content to file in working directory. Creates parent directories automatically. Returns success message or null on failure."
  },
  {
    "name": "file_validate_json",
    "inputSchema": {
      "type": "object",
      "required": ["json"],
      "properties": {"json": {
        "type": "string",
        "description": "JSON string to validate",
        "example": "{\"key\": \"value\"}"
      }}
    },
    "description": "Validate JSON string and return detailed error information if invalid. Returns JSON string with validation result: {\"valid\": true} for valid JSON, or {\"valid\": false, \"error\": \"error message\", \"line\": line_number, \"column\": column_number, \"position\": character_position, \"context\": \"context around error\"} for invalid JSON."
  },
  {
    "name": "file_validate_json_file",
    "inputSchema": {
      "type": "object",
      "required": ["path"],
      "properties": {"path": {
        "type": "string",
        "description": "File path relative to working directory or absolute path within working directory",
        "example": "outputs/response.json"
      }}
    },
    "description": "Validate JSON file and return detailed error information if invalid. Reads file from working directory and validates its JSON content. Returns JSON string with validation result including file path."
  },
  {
    "name": "figma_get_screen_source",
    "inputSchema": {
      "type": "object",
      "required": ["url"],
      "properties": {"url": {
        "type": "string",
        "description": "Figma design URL with node-id parameter",
        "example": "https://www.figma.com/file/abc123/Design?node-id=1%3A2"
      }}
    },
    "description": "Get screen source content by URL. Returns the image URL for the specified Figma design node."
  },
  {
    "name": "figma_download_node_image",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeId"
      ],
      "properties": {
        "format": {
          "type": "string",
          "description": "Image format: png or jpg"
        },
        "scale": {
          "type": "number",
          "description": "Scale factor: 1, 2, or 4"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL"
        },
        "nodeId": {
          "type": "string",
          "description": "Node ID to download"
        }
      }
    },
    "description": "Download image of specific node/component. Useful for visual preview of design pieces before processing structure."
  },
  {
    "name": "figma_download_image_of_file",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "Figma design URL to download as image file",
        "example": "https://www.figma.com/file/abc123/Design?node-id=1%3A2"
      }}
    },
    "description": "Download image by URL as File type. Converts Figma design URL to downloadable image file."
  },
  {
    "name": "figma_get_icons",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "Figma design URL to extract visual elements from",
        "example": "https://www.figma.com/file/abc123/Design"
      }}
    },
    "description": "Find and extract all exportable visual elements (vectors, shapes, graphics, text) from Figma design by URL. Focuses on actual visual elements to avoid complex component references."
  },
  {
    "name": "figma_download_image_as_file",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeId",
        "format"
      ],
      "properties": {
        "format": {
          "type": "string",
          "description": "Export format",
          "example": "png"
        },
        "nodeId": {
          "type": "string",
          "description": "Node ID to export (from figma_get_icons result)",
          "example": "123:456"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL to extract file ID from",
          "example": "https://www.figma.com/file/abc123/Design"
        }
      }
    },
    "description": "Download image as file by node ID and format. Use this after figma_get_icons to download actual icon files."
  },
  {
    "name": "figma_get_svg_content",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeId"
      ],
      "properties": {
        "nodeId": {
          "type": "string",
          "description": "Node ID to export as SVG (from figma_get_icons result)",
          "example": "123:456"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL to extract file ID from",
          "example": "https://www.figma.com/file/abc123/Design"
        }
      }
    },
    "description": "Get SVG content as text by node ID. Use this after figma_get_icons to get SVG code for vector icons."
  },
  {
    "name": "figma_get_node_details",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeIds"
      ],
      "properties": {
        "nodeIds": {
          "type": "string",
          "description": "Comma-separated node IDs (max 10)"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL"
        }
      }
    },
    "description": "Get detailed properties for specific node(s) including colors, fonts, text, dimensions, and styles. Returns small focused response."
  },
  {
    "name": "figma_get_text_content",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeIds"
      ],
      "properties": {
        "nodeIds": {
          "type": "string",
          "description": "Comma-separated text node IDs (max 20)"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL"
        }
      }
    },
    "description": "Extract text content from text nodes. Returns map of nodeId to text content."
  },
  {
    "name": "figma_get_styles",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "Figma design URL"
      }}
    },
    "description": "Get design tokens (colors, text styles) defined in Figma file."
  },
  {
    "name": "figma_get_layers",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "Figma design URL with node-id"
      }}
    },
    "description": "Get first-level layers (direct children) to understand structure. Returns layer names, IDs, types, sizes. Essential first step before getting details."
  },
  {
    "name": "figma_get_layers_batch",
    "inputSchema": {
      "type": "object",
      "required": [
        "href",
        "nodeIds"
      ],
      "properties": {
        "nodeIds": {
          "type": "string",
          "description": "Comma-separated node IDs (max 10)"
        },
        "href": {
          "type": "string",
          "description": "Figma design URL"
        }
      }
    },
    "description": "Get layers for multiple nodes at once. More efficient for analyzing multiple screens/containers. Returns map of nodeId to layers."
  },
  {
    "name": "figma_get_node_children",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "Figma design URL with node-id"
      }}
    },
    "description": "Get immediate children IDs and basic info for a node. Non-recursive, returns only direct children."
  },
  {
    "name": "jira_delete_ticket",
    "inputSchema": {
      "type": "object",
      "required": ["key"],
      "properties": {"key": {
        "type": "string",
        "description": "The Jira ticket key to delete",
        "example": "PRJ-123"
      }}
    },
    "description": "Delete a Jira ticket by key"
  },
  {
    "name": "jira_get_account_by_email",
    "inputSchema": {
      "type": "object",
      "required": ["email"],
      "properties": {"email": {
        "type": "string",
        "description": "The Jira Email",
        "example": "email@email.com"
      }}
    },
    "description": "Gets account details by email"
  },
  {
    "name": "jira_assign_ticket_to",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "accountId"
      ],
      "properties": {
        "accountId": {
          "type": "string",
          "description": "The Jira account ID to assign to. If you know email use first jira_get_account_by_email tools to get account ID",
          "example": "123457:2a123456-40e8-49d6-8ddc-6852e518451f"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to assign",
          "example": "PRJ-123"
        }
      }
    },
    "description": "Assigns a Jira ticket to user"
  },
  {
    "name": "jira_add_label",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "label"
      ],
      "properties": {
        "key": {
          "type": "string",
          "description": "The Jira ticket key to assign",
          "example": "PRJ-123"
        },
        "label": {
          "type": "string",
          "description": "The label to be added to ticket",
          "example": "custom_label"
        }
      }
    },
    "description": "Adding label to specific ticket key"
  },
  {
    "name": "jira_search_by_jql",
    "inputSchema": {
      "type": "object",
      "required": ["jql"],
      "properties": {
        "jql": {
          "type": "string",
          "description": "JQL query string to search tickets",
          "example": "project = DEMO AND status = Open"
        },
        "fields": {
          "type": "array",
          "description": "Optional array of field names to include in response",
          "example": "[\"summary\", \"status\", \"assignee\"]"
        }
      }
    },
    "description": "Search for Jira tickets using JQL and returns all results"
  },
  {
    "name": "jira_search_with_pagination",
    "inputSchema": {
      "type": "object",
      "required": [
        "jql",
        "startAt",
        "fields"
      ],
      "properties": {
        "jql": {
          "type": "string",
          "description": "JQL query string to search for tickets",
          "example": "project = PROJ AND status = Open"
        },
        "fields": {
          "type": "array",
          "description": "Array of field names to include in the response",
          "example": "['summary', 'status', 'assignee']"
        },
        "startAt": {
          "type": "number",
          "description": "Starting index for pagination (0-based)",
          "example": "0"
        }
      }
    },
    "description": "[Deprecated] Search for Jira tickets using JQL with pagination support"
  },
  {
    "name": "jira_search_by_page",
    "inputSchema": {
      "type": "object",
      "required": [
        "jql",
        "nextPageToken",
        "fields"
      ],
      "properties": {
        "jql": {
          "type": "string",
          "description": "JQL query string to search for tickets",
          "example": "project = PROJ AND status = Open"
        },
        "fields": {
          "type": "array",
          "description": "Array of field names to include in the response",
          "example": "['summary', 'status', 'assignee']"
        },
        "nextPageToken": {
          "type": "string",
          "description": "Next Page Token from previous response, empty by default for 1 page",
          "example": "AasvvasasaSASdada"
        }
      }
    },
    "description": "Search for Jira tickets using JQL with paging support"
  },
  {
    "name": "jira_get_my_profile",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {}
    },
    "description": "Get the current user's profile information from Jira"
  },
  {
    "name": "jira_get_user_profile",
    "inputSchema": {
      "type": "object",
      "required": ["userId"],
      "properties": {"userId": {
        "type": "string",
        "description": "The user ID to get profile for"
      }}
    },
    "description": "Get a specific user's profile information from Jira"
  },
  {
    "name": "jira_get_ticket",
    "inputSchema": {
      "type": "object",
      "required": ["key"],
      "properties": {
        "fields": {
          "type": "array",
          "description": "Optional array of fields to include in the response"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to retrieve"
        }
      }
    },
    "description": "Get a specific Jira ticket by key with optional field filtering"
  },
  {
    "name": "jira_get_subtasks",
    "inputSchema": {
      "type": "object",
      "required": ["key"],
      "properties": {"key": {
        "type": "string",
        "description": "The parent ticket key to get subtasks for"
      }}
    },
    "description": "Get all subtasks of a specific Jira ticket using jql: parent = PRJ-123 and issueType in (subtask, sub-task, 'sub task')"
  },
  {
    "name": "jira_post_comment_if_not_exists",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "comment"
      ],
      "properties": {
        "key": {
          "type": "string",
          "description": "The Jira ticket key to post comment to"
        },
        "comment": {
          "type": "string",
          "description": "The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        }
      }
    },
    "description": "Post a comment to a Jira ticket only if it doesn't already exist. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists"
  },
  {
    "name": "jira_get_comments",
    "inputSchema": {
      "type": "object",
      "required": ["key"],
      "properties": {
        "ticket": {
          "type": "object",
          "description": "Optional ticket object for cache validation"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to get comments for"
        }
      }
    },
    "description": "Get all comments for a specific Jira ticket"
  },
  {
    "name": "jira_post_comment",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "comment"
      ],
      "properties": {
        "key": {
          "type": "string",
          "description": "The Jira ticket key to post comment to"
        },
        "comment": {
          "type": "string",
          "description": "The comment text to post (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        }
      }
    },
    "description": "Post a comment to a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists"
  },
  {
    "name": "jira_get_fix_versions",
    "inputSchema": {
      "type": "object",
      "required": ["project"],
      "properties": {"project": {
        "type": "string",
        "description": "The Jira project key to get fix versions for"
      }}
    },
    "description": "Get all fix versions for a specific Jira project"
  },
  {
    "name": "jira_get_components",
    "inputSchema": {
      "type": "object",
      "required": ["project"],
      "properties": {"project": {
        "type": "string",
        "description": "The Jira project key to get components for"
      }}
    },
    "description": "Get all components for a specific Jira project"
  },
  {
    "name": "jira_get_project_statuses",
    "inputSchema": {
      "type": "object",
      "required": ["project"],
      "properties": {"project": {
        "type": "string",
        "description": "The Jira project key to get statuses for"
      }}
    },
    "description": "Get all statuses for a specific Jira project"
  },
  {
    "name": "jira_create_ticket_with_parent",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "issueType",
        "summary",
        "description",
        "parentKey"
      ],
      "properties": {
        "issueType": {
          "type": "string",
          "description": "The type of issue to create (e.g., Bug, Story, Task)"
        },
        "summary": {
          "type": "string",
          "description": "The ticket summary/title"
        },
        "project": {
          "type": "string",
          "description": "The Jira project key to create the ticket in"
        },
        "description": {
          "type": "string",
          "description": "The ticket description"
        },
        "parentKey": {
          "type": "string",
          "description": "The key of the parent ticket"
        }
      }
    },
    "description": "Create a new Jira ticket with a parent relationship"
  },
  {
    "name": "jira_create_ticket_basic",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "issueType",
        "summary",
        "description"
      ],
      "properties": {
        "issueType": {
          "type": "string",
          "description": "The type of issue to create (e.g., Bug, Story, Task)"
        },
        "summary": {
          "type": "string",
          "description": "The ticket summary/title (e.g., Fix login issue)"
        },
        "project": {
          "type": "string",
          "description": "The Jira project key to create the ticket in (e.g., PROJ)"
        },
        "description": {
          "type": "string",
          "description": "The ticket description. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists"
        }
      }
    },
    "description": "Create a new Jira ticket with basic fields (project, issue type, summary, description)"
  },
  {
    "name": "jira_create_ticket_with_json",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "fieldsJson"
      ],
      "properties": {
        "project": {
          "type": "string",
          "description": "The Jira project key to create the ticket in (e.g., PROJ)"
        },
        "fieldsJson": {
          "type": "object",
          "description": "JSON object containing ticket fields in Jira format (e.g., {\"summary\": \"Ticket Summary\", \"description\": \"Ticket Description\", \"issuetype\": {\"name\": \"Task\"}, \"priority\": {\"name\": \"High\"}}), Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists"
        }
      }
    },
    "description": "Create a new Jira ticket with custom fields using JSON configuration"
  },
  {
    "name": "jira_update_description",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "description"
      ],
      "properties": {
        "description": {
          "type": "string",
          "description": "The new description text (supports Jira markup: h2. headings, *bold*, {code}code{code}, * lists)"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to update"
        }
      }
    },
    "description": "Update the description of a Jira ticket. Supports Jira markup syntax: h2. for headings, *text* for bold, {code}text{code} for inline code, * for bullet lists"
  },
  {
    "name": "jira_update_ticket_parent",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "parentKey"
      ],
      "properties": {
        "key": {
          "type": "string",
          "description": "The Jira ticket key to update"
        },
        "parentKey": {
          "type": "string",
          "description": "The key of the new parent ticket"
        }
      }
    },
    "description": "Update the parent of a Jira ticket. Can be used for setting up epic relationships and parent-child relationships for subtasks"
  },
  {
    "name": "jira_update_ticket",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "params"
      ],
      "properties": {
        "params": {
          "type": "object",
          "description": "JSON object containing update parameters in Jira format (e.g., {\"fields\": {\"summary\": \"New Summary\", \"parent\": {\"key\": \"PROJ-123\"}}})"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to update"
        }
      }
    },
    "description": "Update a Jira ticket using JSON parameters following the standard Jira REST API format"
  },
  {
    "name": "jira_update_field",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "field",
        "value"
      ],
      "properties": {
        "field": {
          "type": "string",
          "description": "The field to update. Use field name (e.g., 'Dependencies') to update ALL fields with that name, or custom field ID (e.g., 'customfield_10091') to update specific field"
        },
        "value": {
          "type": "object",
          "description": "The new value for the field(s)"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to update"
        }
      }
    },
    "description": "Update field(s) in a Jira ticket. When using field names (e.g., 'Dependencies'), updates ALL fields with that name. When using custom field IDs (e.g., 'customfield_10091'), updates only that specific field."
  },
  {
    "name": "jira_update_all_fields_with_name",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "fieldName",
        "value"
      ],
      "properties": {
        "value": {
          "type": "object",
          "description": "The new value for the fields"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to update"
        },
        "fieldName": {
          "type": "string",
          "description": "The user-friendly field name (e.g., 'Dependencies')"
        }
      }
    },
    "description": "Update ALL fields with the same name in a Jira ticket. Useful when there are multiple custom fields with the same display name."
  },
  {
    "name": "jira_get_all_fields_with_name",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "fieldName"
      ],
      "properties": {
        "project": {
          "type": "string",
          "description": "The Jira project key"
        },
        "fieldName": {
          "type": "string",
          "description": "The user-friendly field name"
        }
      }
    },
    "description": "Get all custom field IDs that have the same display name in a Jira project"
  },
  {
    "name": "jira_execute_request",
    "inputSchema": {
      "type": "object",
      "required": ["url"],
      "properties": {"url": {
        "type": "string",
        "description": "The Jira API URL to execute"
      }}
    },
    "description": "Execute a custom HTTP GET request to Jira API with auth. Can be used to perform any jira get requests which are required auth."
  },
  {
    "name": "jira_attach_file_to_ticket",
    "inputSchema": {
      "type": "object",
      "required": [
        "ticketKey",
        "name",
        "filePath"
      ],
      "properties": {
        "name": {
          "type": "string",
          "description": "The name of the file to attach",
          "example": "document.pdf"
        },
        "ticketKey": {
          "type": "string",
          "description": "The Jira ticket key to attach the file to",
          "example": "PRJ-123"
        },
        "contentType": {
          "type": "string",
          "description": "The content type of the file (e.g., 'application/pdf', 'image/png'). If not provided, defaults to 'image/*'",
          "example": "application/pdf"
        },
        "filePath": {
          "type": "string",
          "description": "Absolute path to the file on disk",
          "example": "/tmp/document.pdf"
        }
      }
    },
    "description": "Attach a file to a Jira ticket from a local file path. The file will only be attached if a file with the same name doesn't already exist"
  },
  {
    "name": "jira_get_transitions",
    "inputSchema": {
      "type": "object",
      "required": ["key"],
      "properties": {"key": {
        "type": "string",
        "description": "The Jira ticket key to get transitions for"
      }}
    },
    "description": "Get all available transitions(statuses, workflows) for a Jira ticket"
  },
  {
    "name": "jira_move_to_status",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "statusName"
      ],
      "properties": {
        "statusName": {
          "type": "string",
          "description": "The target status name"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to move"
        }
      }
    },
    "description": "Move a Jira ticket to a specific status (workflow, transition)"
  },
  {
    "name": "jira_move_to_status_with_resolution",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "statusName",
        "resolution"
      ],
      "properties": {
        "statusName": {
          "type": "string",
          "description": "The target status name"
        },
        "resolution": {
          "type": "string",
          "description": "The resolution to set"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to move"
        }
      }
    },
    "description": "Move a Jira ticket to a specific status (workflow, transition) with resolution"
  },
  {
    "name": "jira_clear_field",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "field"
      ],
      "properties": {
        "field": {
          "type": "string",
          "description": "The field name to clear"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to clear field from"
        }
      }
    },
    "description": "Clear (delete value) a specific field value in a Jira ticket"
  },
  {
    "name": "jira_set_fix_version",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "fixVersion"
      ],
      "properties": {
        "fixVersion": {
          "type": "string",
          "description": "The fix version name to set"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to set fix version for"
        }
      }
    },
    "description": "Set the fix version for a Jira ticket"
  },
  {
    "name": "jira_add_fix_version",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "fixVersion"
      ],
      "properties": {
        "fixVersion": {
          "type": "string",
          "description": "The fix version name to add"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to add fix version to"
        }
      }
    },
    "description": "Add a fix version to a Jira ticket (without removing existing ones)"
  },
  {
    "name": "jira_set_priority",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "priority"
      ],
      "properties": {
        "priority": {
          "type": "string",
          "description": "The priority name to set"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to set priority for"
        }
      }
    },
    "description": "Set the priority for a Jira ticket"
  },
  {
    "name": "jira_remove_fix_version",
    "inputSchema": {
      "type": "object",
      "required": [
        "key",
        "fixVersion"
      ],
      "properties": {
        "fixVersion": {
          "type": "string",
          "description": "The fix version name to remove"
        },
        "key": {
          "type": "string",
          "description": "The Jira ticket key to remove fix version from"
        }
      }
    },
    "description": "Remove a fix version from a Jira ticket"
  },
  {
    "name": "jira_download_attachment",
    "inputSchema": {
      "type": "object",
      "required": ["href"],
      "properties": {"href": {
        "type": "string",
        "description": "The attachment URL to download"
      }}
    },
    "description": "Download a Jira attachment by URL and save it as a file"
  },
  {
    "name": "jira_get_fields",
    "inputSchema": {
      "type": "object",
      "required": ["project"],
      "properties": {"project": {
        "type": "string",
        "description": "The Jira project key to get fields for"
      }}
    },
    "description": "Get all available fields for a Jira project"
  },
  {
    "name": "jira_get_issue_types",
    "inputSchema": {
      "type": "object",
      "required": ["project"],
      "properties": {"project": {
        "type": "string",
        "description": "The Jira project key to get issue types for"
      }}
    },
    "description": "Get all available issue types for a specific Jira project"
  },
  {
    "name": "jira_get_field_custom_code",
    "inputSchema": {
      "type": "object",
      "required": [
        "project",
        "fieldName"
      ],
      "properties": {
        "project": {
          "type": "string",
          "description": "The Jira project key"
        },
        "fieldName": {
          "type": "string",
          "description": "The human-readable field name"
        }
      }
    },
    "description": "Get the custom field code for a human friendly field name in a Jira project"
  },
  {
    "name": "jira_get_issue_link_types",
    "inputSchema": {
      "type": "object",
      "required": [],
      "properties": {}
    },
    "description": "Get all available issue link types/relationships in Jira"
  },
  {
    "name": "jira_link_issues",
    "inputSchema": {
      "type": "object",
      "required": [
        "sourceKey",
        "anotherKey",
        "relationship"
      ],
      "properties": {
        "sourceKey": {
          "type": "string",
          "description": "The source issue key"
        },
        "relationship": {
          "type": "string",
          "description": "The relationship type name"
        },
        "anotherKey": {
          "type": "string",
          "description": "The target issue key"
        }
      }
    },
    "description": "Link two Jira issues with a specific relationship type"
  }
]}

## Notes

- Tools are organized by integration (jira, ado, teams, figma, etc.)
- All tools support direct invocation from JavaScript agents
- Parameters and descriptions are shown in the list above
- For detailed parameter information, use: `dmtools <tool_name> --help`

---

*This reference is generated from the actual MCPToolRegistry and reflects the current build.*
