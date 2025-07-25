# Expert Job - attachResponseAsFile Parameter

## Overview

A new parameter `attachResponseAsFile` has been added to the Expert job to control whether AI responses are attached as files to tracker tickets.

## Usage

### Parameter Details

- **Name**: `attachResponseAsFile`
- **Type**: Boolean
- **Default**: `true`
- **Required**: No

### When `attachResponseAsFile` is `true` (default):
- AI response is attached as a file to the ticket (e.g., `TeamAssistantAgent_final_answer.txt`)
- Additional stats and result files are also attached from source code, confluence, and tracker searches
- Comments are posted to the ticket as usual

### When `attachResponseAsFile` is `false`:
- No files are attached to the ticket
- Only comments are posted to the ticket with the AI response
- Useful when you want to keep tickets clean and avoid file clutter

## Example Usage

### API Call with file attachments enabled (default):
```json
{
  "jobName": "Expert",
  "params": {
    "request": "Analyze this ticket for technical debt",
    "inputJql": "key = DMC-123",
    "initiator": "developer@company.com",
    "attachResponseAsFile": true
  }
}
```

### API Call with file attachments disabled:
```json
{
  "jobName": "Expert", 
  "params": {
    "request": "Analyze this ticket for technical debt",
    "inputJql": "key = DMC-123", 
    "initiator": "developer@company.com",
    "attachResponseAsFile": false
  }
}
```

### Job Configuration Example:
```json
{
  "name": "Clean Expert Analysis",
  "description": "Expert analysis without file attachments",
  "jobType": "Expert",
  "jobParameters": {
    "request": "Provide technical analysis",
    "inputJql": "project = DMC AND type = Story",
    "initiator": "team@company.com",
    "attachResponseAsFile": false
  }
}
```

## Benefits

1. **Cleaner Tickets**: Reduce file clutter in tickets when only comments are needed
2. **Flexible Output**: Choose between detailed file attachments or simple comments  
3. **Storage Savings**: Avoid unnecessary file storage when attachments aren't needed
4. **Better UX**: Streamlined ticket view for users who prefer comment-only responses

## Implementation Details

The parameter controls:
- Main AI response file attachment (`_final_answer.txt`)
- Source code search stats files (`_stats.json`, `_result.txt`)
- Confluence search stats files
- Tracker search stats files

All controlled by the single `attachResponseAsFile` parameter for consistency. 