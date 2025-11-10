# Job Configuration Examples

Example job configuration files for running JavaScript functions via dmtools.

## Usage

```bash
# Using dmtools.sh
./dmtools.sh run examples/js/job-configs/teams-kb-sync.json

# Or if dmtools is in your PATH
dmtools run examples/js/job-configs/teams-kb-sync.json
```

## Available Configurations

### teams-kb-sync.json
Syncs a Teams chat to the Knowledge Base with automatic inbox processing.

**Before running**: Edit the file and update:
- `chatName`: Your Teams chat name (e.g., "Project Team")
- `kbOutputPath`: Your KB directory path (or use `DMTOOLS_KB_OUTPUT_PATH` env var)

**What it does**:
1. Fetches Teams messages since last sync
2. For first sync: uses ASC sorting and splits into batches of 100
3. For incremental sync: uses DESC sorting for newest messages
4. Processes all unprocessed inbox files
5. Commits and pushes changes to git

### kb-inbox-only.json
Processes only inbox files without fetching Teams messages.

**Use case**: When you manually drop files into `inbox/raw/[source]/` folders.

**Before running**: Edit `kbOutputPath` or use environment variable.

## Environment Variables

Instead of hardcoding `kbOutputPath`, you can set:

```bash
export DMTOOLS_KB_OUTPUT_PATH="/Users/username/my-knowledge-base"
```

Then simplify your config:

```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project Team"
    }
  }
}
```

## Scheduled Execution

### Via Cron

Add to crontab (`crontab -e`):

```bash
# Sync Teams messages every hour
0 * * * * cd /path/to/dmtools && ./dmtools.sh run examples/js/job-configs/teams-kb-sync.json

# Process inbox files every 30 minutes
*/30 * * * * cd /path/to/dmtools && ./dmtools.sh run examples/js/job-configs/kb-inbox-only.json
```

### Via systemd Timer (Linux)

Create `/etc/systemd/system/teams-kb-sync.service`:

```ini
[Unit]
Description=Teams KB Sync Service
After=network.target

[Service]
Type=oneshot
User=youruser
WorkingDirectory=/path/to/dmtools
ExecStart=/path/to/dmtools/dmtools.sh run examples/js/job-configs/teams-kb-sync.json
Environment="DMTOOLS_KB_OUTPUT_PATH=/path/to/kb"
```

Create `/etc/systemd/system/teams-kb-sync.timer`:

```ini
[Unit]
Description=Teams KB Sync Timer
Requires=teams-kb-sync.service

[Timer]
OnCalendar=hourly
Persistent=true

[Install]
WantedBy=timers.target
```

Enable and start:

```bash
sudo systemctl enable teams-kb-sync.timer
sudo systemctl start teams-kb-sync.timer
```

## Debugging

Add `--debug` flag to see detailed logs:

```bash
./dmtools.sh run examples/js/job-configs/teams-kb-sync.json --debug
```

## Multiple Teams Chats

Create separate config files for each chat:

**teams-project-a.json**:
```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project A Team"
    }
  }
}
```

**teams-project-b.json**:
```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "examples/js/teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "Project B Team"
    }
  }
}
```

Run them:
```bash
./dmtools.sh run examples/js/job-configs/teams-project-a.json
./dmtools.sh run examples/js/job-configs/teams-project-b.json
```

Or create a wrapper script to run them all.

