# Teams KB Inbox Processor

Automated processor for Teams messages and manual Knowledge Base (KB) inbox files. This script fetches Teams messages since the last sync, saves them to the inbox, processes all unprocessed files, and commits changes to git.

## Overview

The Teams KB Inbox Processor provides two modes of operation:

1. **Manual Processing** (existing): Use `kb_build` to process specific files directly
2. **Automatic Inbox Processing** (new): Drop files into `inbox/raw/[source]/` and run this processor

## Features

- ✅ Fetches Teams messages automatically since last sync
- ✅ First-sync batching: Splits large initial syncs into batches of 100 messages (oldest first)
- ✅ Incremental sync: Efficient fetching of newest messages only
- ✅ Processes files in place (no copying)
- ✅ Supports multiple sources (Teams + manual files)
- ✅ Tracks processed files to avoid re-processing
- ✅ Commits and pushes changes to git automatically
- ✅ Portable - can be reused in other repositories

## Architecture

### Folder Structure

```
<KB_OUTPUT_PATH>/
  inbox/
    raw/
      teams_messages/           # Teams source (auto-created)
        1729766400000-batch-1-of-5-messages.json  # First sync batches
        1729766400001-batch-2-of-5-messages.json
        1729766500000-messages.json               # Incremental sync
      meeting_notes/            # Manual source (you create)
        transcript.txt
      project_docs/             # Another manual source
        design.md
    analyzed/                   # Tracking (auto-created by KB)
      teams_messages/
        1729766400000-batch-1-of-5-messages_analyzed.json
        1729766500000-messages_analyzed.json
      meeting_notes/
        transcript_analyzed.json
  topics/                       # KB structure (auto-created)
  people/                       # KB structure (auto-created)
  source_config.json            # Tracks last sync per source
```

### Processing Flow

1. **Fetch Teams Messages** → Gets messages via `kb_get` and Teams MCP tools
   - **First sync**: Uses `teams_messages` with `limit: 0` and `sorting: 'asc'` (all messages, oldest first - reversed programmatically), splits into batches of 100
   - **Incremental sync**: Uses `teams_messages_since` with `sorting: 'desc'` (newest first), single file
   - **Note**: Microsoft Teams API only supports DESC order; ASC is achieved by fetching all messages and reversing them
2. **Save to Inbox** → Writes JSON to `inbox/raw/[sanitized_chat_name]/[timestamp]-messages.json` (or batch files)
3. **Process Inbox** → Calls `kb_process_inbox` to scan and process all unprocessed files
4. **Commit & Push** → Stages, commits, and pushes changes with summary

### Source Name Sanitization

Chat names are sanitized for use as source names:
- Convert to lowercase
- Replace non-alphanumeric characters with underscore

**Examples:**
- `"Project Team"` → `"project_team"`
- `"Sales & Marketing"` → `"sales___marketing"`
- `"Dev-Team 2025"` → `"dev_team_2025"`

## Configuration

### Required Environment Variables

- `DMTOOLS_KB_OUTPUT_PATH`: Path to your KB directory (optional if passed as parameter)

### Job Parameters

- `chatName` (optional): Teams chat name to fetch messages from
- `kbOutputPath` (optional): Override KB output path
- `generateDescriptions` (optional, default: true): Generate AI descriptions after processing
- `smartAggregation` (optional, default: true): Only regenerate descriptions if Q/A/N changed

## Usage

### Quick Start with dmtools CLI (Recommended)

The easiest way to run the processor:

```bash
# Edit the job config first
# examples/js/job-configs/teams-kb-sync.json

# Then run it
./dmtools.sh run examples/js/job-configs/teams-kb-sync.json

# Or if dmtools is in your PATH
dmtools run examples/js/job-configs/teams-kb-sync.json
```

**Example job config** (`examples/js/job-configs/teams-kb-sync.json`):
```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project Team",
      "kbOutputPath": "/path/to/kb"
    }
  }
}
```

See `examples/js/job-configs/README.md` for more examples and scheduled execution setup.

### Via JSRunner Job

#### Process Teams Chat + Inbox Files

```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project Team",
      "kbOutputPath": "/path/to/kb"
    }
  }
}
```

#### Process Only Inbox Files (No Teams Fetch)

```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "kbOutputPath": "/path/to/kb"
    }
  }
}
```

#### Advanced: Control Description Generation

```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project Team",
      "kbOutputPath": "/path/to/kb",
      "generateDescriptions": false,
      "smartAggregation": true
    }
  }
}
```

**Parameter Details:**
- `generateDescriptions: false` → Skip AI description generation (faster, only process Q/A/N)
- `smartAggregation: true` → Only regenerate descriptions if Q/A/N changed (90-95% reduction in AI calls)
- `smartAggregation: false` → Always regenerate all descriptions (use for full refresh)

### Via Command Line (Java)

```bash
# With Teams chat processing
java -cp dmtools.jar com.github.istin.dmtools.job.JobRunner \
  --job-class com.github.istin.dmtools.js.JSRunner \
  --params '{
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project Team",
      "kbOutputPath": "/Users/username/my-kb"
    }
  }'

# Process only inbox files
java -cp dmtools.jar com.github.istin.dmtools.job.JobRunner \
  --job-class com.github.istin.dmtools.js.JSRunner \
  --params '{
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "kbOutputPath": "/Users/username/my-kb"
    }
  }'
```

### Scheduled Execution

Set up a cron job or scheduled task to run periodically:

```bash
# Example: Every hour
0 * * * * cd /path/to/dmtools && java -cp dmtools.jar com.github.istin.dmtools.job.JobRunner --job-class com.github.istin.dmtools.js.JSRunner --params '{"jsPath":"examples/js/teams-kb-inbox-processor.js","jobParams":{"chatName":"Project Team"}}'
```

## Manual File Drop

To process files manually (meeting transcripts, documents, etc.):

1. **Create source folder**: `mkdir -p <KB_PATH>/inbox/raw/[source_name]`
2. **Drop your file**: Copy file to `<KB_PATH>/inbox/raw/[source_name]/yourfile.txt`
3. **Run processor**: Execute the script (with or without `chatName`)

**Example:**

```bash
# Create source folder
mkdir -p ~/my-kb/inbox/raw/meeting_transcripts

# Drop file
cp ~/Downloads/team-meeting-notes.txt ~/my-kb/inbox/raw/meeting_transcripts/

# Process (will pick up the file automatically)
java -cp dmtools.jar com.github.istin.dmtools.job.JobRunner \
  --job-class com.github.istin.dmtools.js.JSRunner \
  --params '{"jsPath":"examples/js/teams-kb-inbox-processor.js","jobParams":{"kbOutputPath":"~/my-kb"}}'
```

The source name (`meeting_transcripts` in this example) will be used to track and organize the processed content in the KB.

## How It Works

### Unprocessed File Detection

The processor checks if a file has been processed by looking for a corresponding analyzed file:

- Input file: `inbox/raw/teams_messages/1729766400000-messages.json`
- Analyzed tracking: `inbox/analyzed/teams_messages/1729766400000-messages_analyzed.json`

If the analyzed file exists, the input file is skipped. Otherwise, it's processed.

### In-Place Processing

Files are **not copied** from `inbox/raw/`. They stay in place and are processed directly:

1. File saved: `inbox/raw/teams_messages/1729766400000-messages.json`
2. KB reads and analyzes the file in place
3. Tracking file created: `inbox/analyzed/teams_messages/1729766400000-messages_analyzed.json`
4. KB structure updated: `topics/`, `people/`, etc.
5. Original file remains in `inbox/raw/teams_messages/`

### Dual Mode Support

The existing `kb_build` manual processing mode continues to work:

```javascript
// Manual processing (still works)
kb_build({
  source_name: "confluence_docs",
  input_file: "/path/to/export.json",
  date_time: "2024-10-23T12:00:00Z",
  output_path: "/path/to/kb"
});
```

The new `kb_process_inbox` provides automatic batch processing:

```javascript
// Automatic inbox processing (new)
kb_process_inbox({
  output_path: "/path/to/kb"
});
```

## Output

The script returns a JSON object with processing results:

```json
{
  "success": true,
  "message": "Processed 5 files, skipped 2 files",
  "teamsMessages": 150,
  "processed": 5,
  "skipped": 2,
  "sources": ["teams_messages", "meeting_notes", "project_docs"]
}
```

### Git Commit Message Format

Commits include a summary of processed files:

```
KB: Processed 5 files from 3 sources (including 150 Teams messages from project_team)
```

## Troubleshooting

### No Files Processed

**Issue**: Script runs but no files are processed

**Solutions:**
1. Check if files exist in `inbox/raw/[source]/`
2. Verify files haven't been processed already (check `inbox/analyzed/[source]/`)
3. Check KB path is correct (logs will show resolved path)
4. Ensure files are regular files (not directories)

### Teams Messages Not Fetched

**Issue**: Teams messages are not being fetched

**Solutions:**
1. Verify `chatName` parameter is provided
2. Check Teams authentication (run `dmtools teams_chats` to test)
3. Verify chat name is correct and accessible
4. Check network connectivity

### Git Operations Failed

**Issue**: Files processed but git commit/push fails

**Solutions:**
1. Ensure git is configured (`git config user.name` and `user.email`)
2. Check git repository is initialized
3. Verify remote is configured (`git remote -v`)
4. Ensure you have push permissions
5. Check for merge conflicts

### File Write Permission Denied

**Issue**: Cannot write to inbox folder

**Solutions:**
1. Check folder permissions: `ls -la <KB_PATH>/inbox/raw/`
2. Create folder manually: `mkdir -p <KB_PATH>/inbox/raw/[source]`
3. Verify KB path is correct
4. Check disk space

### KB Processing Errors

**Issue**: Files not being analyzed properly

**Solutions:**
1. Check file format (JSON for Teams, text for documents)
2. Verify file encoding is UTF-8
3. Check AI service is configured and accessible
4. Review KB logs for detailed error messages
5. Try processing manually first with `kb_build` to isolate issue

## Advanced Usage

### Custom Source Names

You can organize manual files by creating custom source folders:

```bash
# Technical documentation
mkdir -p ~/my-kb/inbox/raw/tech_docs

# Product requirements  
mkdir -p ~/my-kb/inbox/raw/product_reqs

# Customer feedback
mkdir -p ~/my-kb/inbox/raw/customer_feedback
```

Each source folder creates a separate namespace in the KB, making it easy to filter and query content by source.

### Multiple Teams Chats

To process multiple Teams chats, run the script multiple times with different chat names:

```bash
# Chat 1
java -cp dmtools.jar ... --params '{"jobParams":{"chatName":"Project Team"}}'

# Chat 2
java -cp dmtools.jar ... --params '{"jobParams":{"chatName":"Sales Team"}}'
```

Or create a wrapper script that iterates through multiple chats.

### Re-processing Files

To re-process a file that was already processed:

1. Delete the analyzed tracking file: `rm <KB_PATH>/inbox/analyzed/[source]/[file]_analyzed.json`
2. Optionally clean the source: Use `kb_build` with `clean_source: "true"`
3. Run the processor again

## See Also

- [KB Architecture Documentation](../../dmtools-core/docs/kb/architecture.md)
- [KB Usage Examples](../../dmtools-core/docs/kb/usage-examples.md)
- [Teams Integration Guide](../../docs/integrations/teams.md)
- [JavaScript Actions Guide](../../docs/ai-teammate/javascript-actions.md)

## License

Part of the dmtools project.

