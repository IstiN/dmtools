# Test Simulation for developTicketAndCreatePR.js

## Purpose
Test the JavaScript post-processing function locally without actually running the full workflow.

## Prerequisites
1. Build dmtools: `./gradlew clean build -x test`
2. Make sure you're in the repository root
3. Have git configured locally

## Option 1: Test with JSRunner (Recommended)

### Step 1: Make a simple test change
```bash
# Create a simple test file to simulate development work
echo "# Test Development for DMC-544" > TEST_FILE_DMC544.md
echo "This is a test file created by AI Teammate simulation" >> TEST_FILE_DMC544.md
```

### Step 2: Run the JavaScript via dmtools CLI
```bash
# Using dmtools CLI with JSRunner job
./dmtools.sh run jsrunner test-develop-pr.json
```

This will:
1. Execute the `developTicketAndCreatePR.js` function
2. Extract issue type from ticket summary ("[Feature]" → "feature")
3. Configure git author as "AI Teammate"
4. Create branch `feature/DMC-544` (or `feature/DMC-544_1` if it exists)
5. Stage the TEST_FILE_DMC544.md file
6. Commit with message "DMC-544 [Feature] File Path Injection for AI Teammate JSON Configuration"
7. Push to origin (if you have permissions)
8. Attempt to create PR via `gh pr create`
9. Move ticket to "In Review" (requires Jira access)
10. Post comment to Jira with PR link

## Option 2: Dry Run Mode (Test Without Git Operations)

If you want to test the logic without actually doing git operations, you can modify the test JSON to use a mock ticket:

```bash
# Create a test version that only validates logic
cat > test-develop-pr-dry.json << 'EOF'
{
  "jsPath": "agents/developTicketAndCreatePR.js",
  "ticket": {
    "key": "TEST-123",
    "fields": {
      "summary": "[Bug] Test ticket for dry run",
      "description": "Test description"
    }
  },
  "response": "## Test Response\nThis is a test.",
  "initiator": "test-user"
}
EOF

# Run with dry run (will fail on git operations but you can see the logic)
./dmtools.sh run jsrunner test-develop-pr-dry.json
```

## Expected Behavior

### Success Case
```
Processing development workflow for ticket: DMC-544
Extracted issue type: feature
✅ Configured git author as AI Teammate
Using branch name: feature/DMC-544
Staging changes...
Committing changes...
Pushing to remote...
Creating Pull Request...
✅ Pull Request created: https://github.com/...
✅ Moved ticket to In Review status
✅ Posted PR comment to DMC-544
✅ Development workflow completed successfully
```

### Failure Case (Before Fix)
```
Processing development workflow for ticket: DMC-544
Extracted issue type: feature
Failed to configure git author: ReferenceError: cli_execute_command is not defined
Development Workflow Error
Stage: Git Configuration
Error: Failed to configure git author
```

## Cleanup After Test
```bash
# Delete test file
rm -f TEST_FILE_DMC544.md

# Delete test branch if created
git branch -D feature/DMC-544

# Remove from remote if pushed (careful!)
# git push origin --delete feature/DMC-544
```

## Verification Steps

1. **Before the fix** (without "cli" in integrations):
   - Error: `ReferenceError: cli_execute_command is not defined`
   - Stage: Git Configuration

2. **After the fix** (with "cli" in integrations):
   - Should successfully configure git author
   - Should proceed through all git operations
   - May fail later if no changes to commit or GitHub auth issues

## Troubleshooting

### Error: "cli_execute_command is not defined"
- **Cause**: CLI integration not exposed to JavaScript
- **Fix**: Added "cli" to integrations set in `JobJavaScriptBridge.java`
- **Rebuild**: `./gradlew clean build -x test`

### Error: "No changes to commit"
- **Cause**: No files were modified before running the script
- **Fix**: Create TEST_FILE_DMC544.md as shown in Step 1

### Error: "gh: command not found"
- **Cause**: GitHub CLI not installed
- **Fix**: Install gh CLI: `brew install gh` (macOS) or see https://cli.github.com/

### Error: "Authentication failed"
- **Cause**: PAT_TOKEN not set or gh not authenticated
- **Fix**: 
  - Set PAT_TOKEN: `export PAT_TOKEN=your_github_token`
  - Or authenticate gh: `gh auth login`

## Notes

- The script requires PAT_TOKEN or GH_TOKEN environment variable for PR creation
- Git operations will actually execute - use a test branch if concerned
- Jira operations require proper credentials in dmtools.env
- Consider using a test Jira ticket key if you don't want to modify DMC-544

