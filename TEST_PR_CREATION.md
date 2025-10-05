# PR Creation Test Guide

This guide explains how to test the PR creation functionality in isolation using JSRunner.

## Files Created

1. **`test-pr-creation.json`** - JSRunner configuration that calls `developTicketAndCreatePR.js`
2. **`test-pr-simulation.sh`** - Script to set up the test environment
3. **`TEST_PR_CREATION.md`** - This guide

## Test Setup

The test simulates the full workflow:
- Creates fake code changes (to have something to commit)
- Creates `outputs/response.md` with complex markdown (simulates cursor-agent output)
- Stages the changes
- Runs the JavaScript post-processing function

## How to Run the Test

### Step 1: Set up the test environment
```bash
./test-pr-simulation.sh
```

This script will:
- Create `outputs/` directory
- Generate `outputs/response.md` with complex markdown (code blocks, special chars, etc.)
- Create `test-change-marker.txt` as a fake code change
- Stage the changes for commit

### Step 2: Run the JSRunner test
```bash
dmtools run test-pr-creation.json
```

This will execute the `developTicketAndCreatePR.js` function with test data:
- Ticket: `DMC-TEST-PR`
- Summary: `[CORE] Test PR Creation with Complex Markdown`

### Step 3: Verify the results

The script should:
1. ✅ Configure git author
2. ✅ Generate unique branch name (e.g., `core/DMC-TEST-PR`)
3. ✅ Create branch
4. ✅ Stage changes (`git add .`)
5. ✅ Commit with ticket summary
6. ✅ Push to remote
7. ✅ Create PR using `outputs/pr_body.md`
8. ✅ Post comment to Jira
9. ✅ Move ticket to "In Review"
10. ✅ Add "ai_developed" label

### Step 4: Clean up after test
```bash
# Reset git changes
git reset HEAD test-change-marker.txt
git checkout test-change-marker.txt  # if it existed before
rm -f test-change-marker.txt

# Delete the test branch (if created)
git branch -D core/DMC-TEST-PR

# Clean up outputs
rm -f outputs/response.md outputs/pr_body.md

# Optional: Delete the test PR on GitHub if it was created
# gh pr close <PR_NUMBER> --delete-branch
```

## What This Tests

### Shell Escaping
The `outputs/response.md` contains:
- Code blocks with backticks
- Special characters: `$`, `"`, `'`, `\`
- Newlines and formatting
- Complex markdown structures

### Git Operations
- Branch creation with collision detection
- File staging and commit
- Remote push

### PR Creation
- Title escaping (removes newlines)
- Body passed via `outputs/pr_body.md` file
- URL extraction from `gh pr create` output

### Jira Integration
- Comment posting with PR details
- Status transition
- Label management

## Expected Output

You should see logs like:
```
Processing development workflow for ticket: DMC-TEST-PR
Extracted issue type: core
✅ Configured git author as AI Teammate
Using branch name: core/DMC-TEST-PR
Creating branch: core/DMC-TEST-PR
Staging changes...
Committing changes...
Pushing to remote...
Creating Pull Request...
Using PR body file: outputs/pr_body.md
✅ Pull Request created: https://github.com/IstiN/dmtools/pull/XXX
✅ Moved ticket to In Review status
✅ Posted PR comment to DMC-TEST-PR
✅ Development workflow completed successfully
```

## Troubleshooting

### "No changes to commit"
Make sure you ran `test-pr-simulation.sh` first to create test changes.

### "gh: command not found"
Install GitHub CLI: `brew install gh` (macOS) or see https://cli.github.com/

### "Failed to configure git author"
The `cli_execute_command` MCP tool might not be exposed. Check `JobJavaScriptBridge.java`.

### PR created but no URL
Check the raw output of `gh pr create` in the logs to see what GitHub returned.

## Notes

- This test creates a **real branch and PR** on GitHub
- Make sure to clean up after testing
- The test uses real Jira integration if configured
- Set `GH_TOKEN` environment variable for GitHub authentication
