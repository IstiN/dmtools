# Knowledge Base Usage Examples

## Table of Contents

1. [Basic Usage](#basic-usage)
2. [Processing Modes](#processing-modes)
3. [Source Management](#source-management)
4. [Incremental Updates](#incremental-updates)
5. [Advanced Features](#advanced-features)
6. [Common Workflows](#common-workflows)
7. [Troubleshooting](#troubleshooting)

## Basic Usage

### Building a Knowledge Base from Chat Messages

```bash
# Process Slack messages into a knowledge base
dmtools kb_build \
  --source_name "slack_general" \
  --input_file "/path/to/slack_export.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/knowledge-base"
```

**Input Format** (`slack_export.json`):
```json
{
  "messages": [
    {
      "author": "Alice Brown",
      "text": "How do we deploy to production?",
      "timestamp": "2024-01-15T09:00:00Z"
    },
    {
      "author": "Bob Smith",
      "text": "We use Jenkins. First, merge to main, then run the deploy pipeline.",
      "timestamp": "2024-01-15T09:05:00Z"
    }
  ]
}
```

**Output Structure**:
```
knowledge-base/
├── questions/
│   └── q_0001.md          # "How do we deploy to production?"
├── answers/
│   └── a_0001.md          # Jenkins deployment process
├── topics/
│   ├── deployment.md      # Topic file with Q&A references
│   └── deployment-desc.md # AI-generated description
├── people/
│   ├── Alice_Brown.md     # Profile with contributions
│   └── Bob_Smith.md       # Profile with contributions
└── stats/
    └── kb_statistics.json # Overall statistics
```

### Processing Plain Text Documents

```bash
# Process a transcript or document
dmtools kb_build \
  --source_name "team_meeting_2024_01" \
  --input_file "/path/to/meeting_transcript.txt" \
  --date_time "2024-01-15T14:00:00Z" \
  --output_path "/path/to/knowledge-base"
```

**Input Format** (`meeting_transcript.txt`):
```text
Team Meeting - January 15, 2024

Alice: What's the status of the new API?
Bob: It's ready for testing. We've implemented all endpoints.
Charlie: Great! When can we start integration testing?
Bob: Tomorrow morning. I'll send the documentation today.
```

The AI will automatically:
- Extract questions and answers
- Identify speakers as people
- Detect topics (API, testing, integration)
- Create relationships between entities

## Processing Modes

### FULL Mode (Default)

Complete processing with AI analysis and descriptions.

```bash
dmtools kb_build \
  --source_name "confluence_docs" \
  --input_file "/path/to/docs.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/kb"
```

**What happens**:
1. ✅ AI analyzes input → extracts Q/A/N
2. ✅ Builds file structure
3. ✅ AI generates descriptions for people and topics
4. ✅ Creates statistics and indexes

**Use when**: You want complete, searchable KB with AI-generated summaries

### PROCESS_ONLY Mode

Fast processing without AI descriptions (bulk data mode).

```bash
dmtools kb_process \
  --source_name "bulk_import_2024" \
  --input_file "/path/to/large_dataset.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/kb"
```

**What happens**:
1. ✅ AI analyzes input → extracts Q/A/N
2. ✅ Builds file structure
3. ❌ Skips AI descriptions (faster)
4. ✅ Creates statistics and indexes

**Use when**: Processing large volumes of data quickly

### AGGREGATE_ONLY Mode

Generate AI descriptions for existing KB structure.

```bash
dmtools kb_aggregate \
  --output_path "/path/to/kb"
```

**What happens**:
1. ❌ Skips input processing
2. ❌ Skips structure building
3. ✅ AI generates descriptions for existing entities
4. ✅ Updates statistics

**Use when**: You ran PROCESS_ONLY mode and now want AI descriptions

### Combined Workflow

```bash
# Step 1: Fast bulk import (no AI descriptions)
dmtools kb_process \
  --source_name "archive_2023" \
  --input_file "/path/to/archive.json" \
  --date_time "2023-12-31T23:59:59Z" \
  --output_path "/path/to/kb"

# Step 2: Generate AI descriptions later
dmtools kb_aggregate \
  --output_path "/path/to/kb"
```

## Source Management

### Adding Multiple Sources

```bash
# Add Slack messages
dmtools kb_build \
  --source_name "slack_general" \
  --input_file "/path/to/slack.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/kb"

# Add Teams messages (incremental)
dmtools kb_build \
  --source_name "teams_engineering" \
  --input_file "/path/to/teams.json" \
  --date_time "2024-01-16T10:00:00Z" \
  --output_path "/path/to/kb"

# Add Confluence docs (incremental)
dmtools kb_build \
  --source_name "confluence_wiki" \
  --input_file "/path/to/confluence.json" \
  --date_time "2024-01-17T10:00:00Z" \
  --output_path "/path/to/kb"
```

**Result**: All sources coexist in the same KB, each tracked independently.

### Checking Source Status

```bash
# Get last sync date for a source
dmtools kb_get \
  --source_name "slack_general" \
  --output_path "/path/to/kb"

# Output: "2024-01-15T10:00:00Z"
```

### Refreshing a Source (Clean & Rebuild)

```bash
# Clean and refresh Confluence page content
dmtools kb_build \
  --source_name "confluence_page_123" \
  --input_file "/path/to/updated_page.json" \
  --date_time "2024-01-20T10:00:00Z" \
  --output_path "/path/to/kb" \
  --clean_source "true"
```

**What happens**:
1. 🗑️ Deletes all Q/A/N from `confluence_page_123`
2. ✅ Processes new content
3. ✅ Regenerates person profiles (updated stats)
4. ✅ Regenerates topic statistics
5. ✅ Other sources remain untouched

**Use when**: 
- Confluence pages that change frequently
- Content that should be replaced, not merged
- Fixing incorrect data from a specific source

## Incremental Updates

### Daily Sync Pattern

```bash
#!/bin/bash
# daily_sync.sh - Run daily to update KB

SOURCE="slack_general"
KB_PATH="/data/knowledge-base"
EXPORT_PATH="/data/exports/slack_$(date +%Y%m%d).json"

# Export Slack messages from last 24 hours
# (your export logic here)

# Get last sync date
LAST_SYNC=$(dmtools kb_get --source_name "$SOURCE" --output_path "$KB_PATH")

# Process new messages (incremental)
dmtools kb_build \
  --source_name "$SOURCE" \
  --input_file "$EXPORT_PATH" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "$KB_PATH"

echo "KB updated. Last sync: $LAST_SYNC"
```

### Handling Large Backlogs

```bash
# Step 1: Process historical data quickly (no AI descriptions)
for file in archive/*.json; do
  dmtools kb_process \
    --source_name "archive_$(basename $file .json)" \
    --input_file "$file" \
    --date_time "2023-01-01T00:00:00Z" \
    --output_path "/path/to/kb"
done

# Step 2: Generate AI descriptions once
dmtools kb_aggregate --output_path "/path/to/kb"

# Step 3: Resume normal incremental updates
dmtools kb_build \
  --source_name "current_data" \
  --input_file "/path/to/current.json" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/path/to/kb"
```

## Advanced Features

### Custom AI Instructions

Add domain-specific guidance to AI agents:

```bash
dmtools kb_build \
  --source_name "medical_records" \
  --input_file "/path/to/records.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/kb" \
  --analysis_extra_instructions "Focus on medical terminology and patient safety topics" \
  --aggregation_extra_instructions "Emphasize clinical best practices in descriptions" \
  --qa_mapping_extra_instructions "Match questions to answers based on medical context"
```

### Programmatic Usage (Java)

```java
import com.github.istin.dmtools.common.kb.agent.KBOrchestrator;
import com.github.istin.dmtools.common.kb.params.KBOrchestratorParams;
import com.github.istin.dmtools.common.kb.model.KBResult;
import com.github.istin.dmtools.di.DaggerKnowledgeBaseComponent;

public class KBExample {
    public static void main(String[] args) throws Exception {
        // Initialize via Dagger
        var component = DaggerKnowledgeBaseComponent.create();
        var orchestrator = component.kbOrchestrator();
        
        // Configure parameters
        var params = new KBOrchestratorParams();
        params.setSourceName("my_source");
        params.setInputFile("/path/to/input.json");
        params.setDateTime("2024-01-15T10:00:00Z");
        params.setOutputPath("/path/to/kb");
        params.setCleanSourceBeforeProcessing(false);
        
        // Process
        KBResult result = orchestrator.run(params);
        
        // Check results
        System.out.println("Questions: " + result.getQuestionsCount());
        System.out.println("Answers: " + result.getAnswersCount());
        System.out.println("People: " + result.getPeopleCount());
        System.out.println("Topics: " + result.getTopicsCount());
    }
}
```

### Regenerating Structure

If you manually edit Q/A/N files, regenerate the structure:

```java
import java.nio.file.Paths;

var component = DaggerKnowledgeBaseComponent.create();
var orchestrator = component.kbOrchestrator();

// Regenerate topics, people profiles, and statistics
var result = orchestrator.regenerateStructureFromExistingFiles(
    Paths.get("/path/to/kb"),
    "manual_edit"
);
```

## Common Workflows

### Workflow 1: Team Chat Knowledge Base

```bash
# Daily automated sync
0 2 * * * /scripts/sync_slack.sh
0 3 * * * /scripts/sync_teams.sh

# sync_slack.sh
dmtools kb_build \
  --source_name "slack_$(date +%Y%m%d)" \
  --input_file "/exports/slack_daily.json" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/data/team_kb"

# sync_teams.sh
dmtools kb_build \
  --source_name "teams_$(date +%Y%m%d)" \
  --input_file "/exports/teams_daily.json" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/data/team_kb"
```

### Workflow 2: Documentation Wiki

```bash
# Weekly full refresh of wiki pages
for page in wiki_pages/*.json; do
  PAGE_ID=$(basename $page .json)
  
  dmtools kb_build \
    --source_name "wiki_$PAGE_ID" \
    --input_file "$page" \
    --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    --output_path "/data/wiki_kb" \
    --clean_source "true"  # Replace old content
done
```

### Workflow 3: Customer Support Tickets

```bash
# Process resolved tickets
dmtools kb_build \
  --source_name "support_tickets_$(date +%Y%m)" \
  --input_file "/exports/resolved_tickets.json" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/data/support_kb" \
  --analysis_extra_instructions "Focus on problem-solution pairs and customer pain points"
```

### Workflow 4: Meeting Transcripts

```bash
# Process weekly meeting transcripts
dmtools kb_build \
  --source_name "standup_$(date +%Y_week_%V)" \
  --input_file "/transcripts/standup_$(date +%Y%m%d).txt" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/data/meetings_kb" \
  --analysis_extra_instructions "Extract action items and decisions"
```

## Troubleshooting

### Issue: "Input file not found"

**Problem**: File path is incorrect or file doesn't exist.

**Solution**:
```bash
# Use absolute paths
dmtools kb_build \
  --input_file "/absolute/path/to/file.json" \
  ...

# Or verify file exists
ls -la /path/to/file.json
```

### Issue: "Expected BEGIN_OBJECT but was STRING"

**Problem**: AI returned non-JSON response (often due to rate limits or errors).

**Solution**:
1. Check AI service status
2. Verify API keys in `.env` or `dmtools.env`
3. Try again (temporary API issue)
4. Check AI model configuration

```bash
# Check environment
cat dmtools.env | grep -E "(DEFAULT_LLM|GEMINI|DIAL)"
```

### Issue: Statistics not updating after cleanup

**Problem**: Old statistics cached.

**Solution**: The system automatically regenerates statistics after cleanup. If issues persist:

```java
// Manually regenerate
var component = DaggerKnowledgeBaseComponent.create();
var orchestrator = component.kbOrchestrator();
orchestrator.regenerateStructureFromExistingFiles(
    Paths.get("/path/to/kb"),
    "manual_regen"
);
```

### Issue: Source cleanup deleted too much

**Problem**: Wrong source name specified.

**Prevention**:
```bash
# Always verify source name first
dmtools kb_get --source_name "my_source" --output_path "/path/to/kb"

# Use clean_source with caution
dmtools kb_build \
  --source_name "my_source" \
  --clean_source "true"  # Double-check source name!
  ...
```

### Issue: Out of memory with large files

**Problem**: Input file too large for single processing.

**Solution**: The system automatically chunks large inputs, but for very large files:

```bash
# Split large file into smaller chunks
split -l 10000 large_file.json chunk_

# Process each chunk
for chunk in chunk_*; do
  dmtools kb_build \
    --source_name "large_import_$chunk" \
    --input_file "$chunk" \
    --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    --output_path "/path/to/kb"
done
```

### Issue: Duplicate questions/answers

**Problem**: Same content processed multiple times.

**Solution**: Use source cleanup to replace:

```bash
# Clean and reprocess
dmtools kb_build \
  --source_name "my_source" \
  --input_file "/path/to/corrected_data.json" \
  --date_time "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --output_path "/path/to/kb" \
  --clean_source "true"
```

## Best Practices

### 1. Source Naming Convention

Use descriptive, consistent names:
```bash
# Good
slack_general_2024
teams_engineering_sprint_42
confluence_api_docs

# Bad
source1
data
test
```

### 2. Regular Backups

```bash
# Backup before major operations
tar -czf kb_backup_$(date +%Y%m%d).tar.gz /path/to/kb

# Restore if needed
tar -xzf kb_backup_20240115.tar.gz -C /restore/path
```

### 3. Incremental Over Full Refresh

Prefer incremental updates unless content truly needs replacement:
```bash
# Incremental (default) - preserves history
dmtools kb_build --source_name "my_source" ...

# Full refresh - only when necessary
dmtools kb_build --source_name "my_source" --clean_source "true" ...
```

### 4. Monitor Statistics

```bash
# Check KB growth
cat /path/to/kb/stats/kb_statistics.json | jq '.'

# Example output:
{
  "totalQuestions": 1234,
  "totalAnswers": 2345,
  "totalNotes": 567,
  "totalPeople": 45,
  "totalTopics": 123,
  "totalAreas": 12,
  "lastUpdated": "2024-01-15T10:00:00Z"
}
```

### 5. Use Processing Modes Appropriately

```bash
# Historical data: PROCESS_ONLY (fast)
dmtools kb_process --source_name "archive_2023" ...

# Current data: FULL (complete)
dmtools kb_build --source_name "current" ...

# Periodic: AGGREGATE_ONLY (update descriptions)
dmtools kb_aggregate --output_path "/path/to/kb"
```

## Next Steps

- See [Architecture](architecture.md) for system design details
- Check [API Reference](api-reference.md) for programmatic usage
- Review [Configuration](configuration.md) for environment setup

