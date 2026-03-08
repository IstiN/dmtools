# DMTools v1.7.147 Release Notes

## Bug Fixes

### `run` command: extra CLI args now forwarded to Java (commit `2c216107`)

**Problem**: `dmtools run file.json "$ENCODED_CONFIG" --ciRunUrl "$URL"` silently
dropped `--ciRunUrl` (and any other `--key value` flags after the encoded config).
The old shell script only forwarded `$JSON_FILE` and `$ENCODED_PARAM` to Java.
`RunCommandProcessor` never saw the override → `ciRunUrl` was `null` → the
"Processing started. CI Run: …" comment was never posted to the Jira ticket.

**Fix**: Shell `run` case now collects `EXTRA_RUN_ARGS=("${ARGS[@]:1}")` and passes
all remaining args to Java. `RunCommandProcessor` already handled `--key value` pairs
correctly; it just wasn't receiving them.

Additionally, **stderr is no longer suppressed** for `run` commands: the old
`execute_java_command` wrapper redirected stderr to `/dev/null` in non-debug mode,
hiding stack traces and "Execution result is null" messages from CI logs.

### Jira field/project resolution no longer serves stale disk cache (commit `3b403b8d`)

`/field` and `/project` requests now set `ignoreCache=true` so they always fetch
fresh data. This eliminates the need for `JIRA_CLEAR_CACHE=true` when custom field
IDs change.

## New Features

### Configurable CLI command whitelist (commit `8a6f6f0a`)

`cli_execute_command` now supports opt-in extra commands via `CLI_ALLOWED_COMMANDS`:

```bash
# dmtools.env
CLI_ALLOWED_COMMANDS=find,ls,cat,pytest,python3,pip3,curl,ffmpeg,bash,run-cursor-agent.sh
```

```json
// agent JSON (envVariables)
"envVariables": { "CLI_ALLOWED_COMMANDS": "pytest,python3" }
```

Script basenames are matched without path prefix, so `./run-cursor-agent.sh` and
`/abs/path/run-cursor-agent.sh` both match `run-cursor-agent.sh` in the allowed list.

## Upgrade Notes

CI workflows caching dmtools by version must update the cache key from
`dmtools-v1.7.146` → `dmtools-v1.7.147` to pick up these fixes.
