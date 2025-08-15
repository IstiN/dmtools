#!/bin/bash

# git-workflow.sh - Handle Git operations for automated implementations
# Creates branches, commits, and pushes following DMTools standards

set -e

# Parameters
ACTION="$1"              # check, create-branch, commit, push, or full-workflow
TICKET_NUMBER="$2"       # DMC-XXX format
COMMIT_MESSAGE="$3"      # Commit message
PR_TITLE="$4"           # Pull request title
BRANCH_PREFIX="$5"      # core, api, ui, ui-comp, etc.

echo "🔧 Git workflow: $ACTION"

# Function to validate ticket number format
validate_ticket() {
    local ticket="$1"
    if [[ ! "$ticket" =~ ^DMC-[0-9]+$ ]]; then
        echo "ERROR: Invalid ticket format '$ticket'. Expected format: DMC-XXX"
        exit 1
    fi
}

# Function to determine branch prefix from commit message or ticket
determine_branch_prefix() {
    local ticket="$1"
    local message="$2"
    
    # If prefix is explicitly provided, use it
    if [ -n "$BRANCH_PREFIX" ]; then
        echo "$BRANCH_PREFIX"
        return
    fi
    
    # Try to determine from commit message content
    local msg_lower=$(echo "$message" | tr '[:upper:]' '[:lower:]')
    
    if echo "$msg_lower" | grep -q -E "(ui|frontend|component|react|angular|vue)"; then
        echo "ui"
    elif echo "$msg_lower" | grep -q -E "(api|endpoint|controller|rest|graphql)"; then
        echo "api"
    elif echo "$msg_lower" | grep -q -E "(core|service|business|logic|entity|repository)"; then
        echo "core"
    else
        # Default to core for general changes
        echo "core"
    fi
}

# Function to check git status
check_git_status() {
    echo "📋 Checking Git status..."
    echo "=== Git Status ==="
    git status --porcelain || echo "No changes detected"
    
    echo "📋 Modified files:"
    git diff --name-only || echo "No modified files"
    
    echo "📋 Untracked files:"
    git ls-files --others --exclude-standard || echo "No untracked files"
    
    # Check if there are any changes to commit
    if ! git diff --quiet || ! git diff --cached --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
        echo "✅ Changes detected"
        return 0
    else
        echo "ℹ️ No changes to commit"
        return 1
    fi
}

# Function to create branch
create_branch() {
    local ticket="$1"
    local prefix="$2"
    local pr_title="$3"
    
    validate_ticket "$ticket"
    
    # Generate clean branch name
    local timestamp=$(date +%Y%m%d-%H%M%S)
    local run_id="${GITHUB_RUN_ID:-local}"
    
    if [ -n "$pr_title" ]; then
        local branch_base="$(echo "$pr_title" | tr '\n\r' ' ' | head -c 30 | tr ' ' '-' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9-]//g' | sed 's/--*/-/g' | sed 's/^-\|-$//g')"
    else
        local branch_base="implementation"
    fi
    
    local branch_name="${prefix}/${ticket}-${branch_base}-${timestamp}-${run_id}"
    
    # Ensure we don't exceed Git's branch name limits
    if [ ${#branch_name} -gt 250 ]; then
        branch_name="${prefix}/${ticket}-${timestamp}-${run_id}"
    fi
    
    echo "📋 Creating branch: $branch_name"
    
    # Create and switch to new branch
    git checkout -b "$branch_name"
    
    echo "BRANCH_NAME=$branch_name"
    return 0
}

# Function to create commit
create_commit() {
    local ticket="$1"
    local message="$2"
    
    validate_ticket "$ticket"
    
    # Add all changes
    git add .
    
    # Create commit message in DMTools format
    local commit_msg="${ticket} - ${message}"
    
    # Add additional context if available
    if [ -n "$GITHUB_RUN_ID" ]; then
        commit_msg="${commit_msg}

Implemented via automated workflow (Run: $GITHUB_RUN_ID)
Co-authored-by: CLI Agent <cli-agent@dmtools>"
    fi
    
    echo "📋 Creating commit with message:"
    echo "$commit_msg"
    
    git commit -m "$commit_msg"
    echo "✅ Commit created successfully"
    return 0
}

# Function to push branch
push_branch() {
    local branch_name="$1"
    
    if [ -z "$branch_name" ]; then
        branch_name=$(git branch --show-current)
    fi
    
    echo "📋 Pushing branch: $branch_name"
    git push origin "$branch_name"
    echo "✅ Branch pushed successfully"
    return 0
}

# Function to run full workflow
full_workflow() {
    local ticket="$1"
    local message="$2"
    local pr_title="$3"
    
    # Check if there are changes
    if ! check_git_status; then
        echo "ℹ️ No changes to commit, skipping Git workflow"
        echo "HAS_CHANGES=false"
        return 0
    fi
    
    # Configure git user for commits
    git config user.name "cli-agent[bot]"
    git config user.email "cli-agent-bot@dmtools.local"
    echo "✅ Git configured for CLI operations"
    
    # Determine branch prefix
    local prefix=$(determine_branch_prefix "$ticket" "$message")
    echo "📋 Using branch prefix: $prefix"
    
    # Create branch
    local branch_output=$(create_branch "$ticket" "$prefix" "$pr_title")
    local branch_name=$(echo "$branch_output" | grep "BRANCH_NAME=" | cut -d'=' -f2)
    
    if [ -z "$branch_name" ]; then
        echo "ERROR: Failed to extract branch name"
        exit 1
    fi
    
    # Create commit
    create_commit "$ticket" "$message"
    
    # Push branch
    push_branch "$branch_name"
    
    # Output results for GitHub Actions
    echo "BRANCH_NAME=$branch_name"
    echo "HAS_CHANGES=true"
    echo "PR_TITLE=${pr_title:-$message}"
    
    return 0
}

# Execute the requested action
case "$ACTION" in
    "check")
        check_git_status
        ;;
    "create-branch")
        if [ -z "$TICKET_NUMBER" ]; then
            echo "ERROR: Ticket number required for create-branch action"
            exit 1
        fi
        prefix=$(determine_branch_prefix "$TICKET_NUMBER" "$COMMIT_MESSAGE")
        create_branch "$TICKET_NUMBER" "$prefix" "$PR_TITLE"
        ;;
    "commit")
        if [ -z "$TICKET_NUMBER" ] || [ -z "$COMMIT_MESSAGE" ]; then
            echo "ERROR: Ticket number and commit message required for commit action"
            exit 1
        fi
        create_commit "$TICKET_NUMBER" "$COMMIT_MESSAGE"
        ;;
    "push")
        push_branch "$TICKET_NUMBER"  # In this case, TICKET_NUMBER is branch name
        ;;
    "full-workflow")
        if [ -z "$TICKET_NUMBER" ] || [ -z "$COMMIT_MESSAGE" ]; then
            echo "ERROR: Ticket number and commit message required for full-workflow action"
            exit 1
        fi
        full_workflow "$TICKET_NUMBER" "$COMMIT_MESSAGE" "$PR_TITLE"
        ;;
    *)
        echo "ERROR: Unknown action '$ACTION'"
        echo "Supported actions: check, create-branch, commit, push, full-workflow"
        exit 1
        ;;
esac
