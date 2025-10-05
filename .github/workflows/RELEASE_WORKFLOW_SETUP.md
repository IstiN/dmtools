# Release Workflow Setup

## Problem: Why GITHUB_TOKEN Doesn't Trigger Other Workflows

When a GitHub Actions workflow uses the default `GITHUB_TOKEN` to push commits or tags, it **will not trigger other workflows**. This is a security feature to prevent recursive workflow execution.

## Solution: Use a Personal Access Token (PAT)

To allow the `release.yml` workflow to trigger `fatjar_release.yml`, you need to set up a Personal Access Token.

### Steps:

1. **Create a Personal Access Token (Classic)**:
   - Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Click "Generate new token (classic)"
   - Give it a name: `DMTools Release Token`
   - Set expiration (or "No expiration" for permanent)
   - Select scopes:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `workflow` (Update GitHub Action workflows)
   - Click "Generate token"
   - **Copy the token immediately** (you won't see it again!)

2. **Add Token as Repository Secret**:
   - Go to your repository → Settings → Secrets and variables → Actions
   - Click "New repository secret"
   - Name: `PAT_TOKEN`
   - Value: Paste the token you copied
   - Click "Add secret"

3. **Test the Workflow**:
   - Go to Actions → Release DMTools → Run workflow
   - Leave version empty (or specify custom version)
   - Click "Run workflow"
   - After completion, check that `fatjar_release.yml` was triggered automatically

## How It Works

```yaml
# Before (won't trigger other workflows)
- uses: actions/checkout@v4
  with:
    token: ${{ secrets.GITHUB_TOKEN }}

# After (will trigger other workflows)
- uses: actions/checkout@v4
  with:
    token: ${{ secrets.PAT_TOKEN }}
```

When the workflow pushes with PAT_TOKEN, GitHub treats it as a regular user push, which triggers the `fatjar_release.yml` workflow on tag push.

## Workflow Chain

1. **Manual trigger** → `release.yml` runs
2. **release.yml** → Updates version, commits, creates tag, pushes (using PAT)
3. **Tag push** → Automatically triggers `fatjar_release.yml`
4. **fatjar_release.yml** → Builds JAR, runs tests, creates GitHub release

## Alternative: GitHub App Token

For better security, you can also use a GitHub App token instead of PAT. This is more secure because:
- More granular permissions
- No user association
- Auditable

See: https://github.com/actions/create-github-app-token
