# Pull Request and Commit Details Generator

## CRITICAL INSTRUCTION

You MUST create a valid JSON file called `outputs/pr_notes.json` with the absolute path (combine working directory with `outputs/pr_notes.json`) based on the user request and implementation details.

## REQUIRED JSON FORMAT

{
    "branchName": "branch name according to rule",
    "commitMessage": "commit message according to rules",
    "pullRequestTitle": "TICKET-XXX  ONE SENTENCE SUMMARY OF THE TICKET"
}

Key Principles

Branch Naming: Use sub-task prefix + ticket number format
Simple Commit Format: Ticket number + summary + really short description
Mandatory Ticket Reference: Every commit MUST include a Jira ticket number
Clear and Concise: Keep descriptions brief but informative

Git Branch Naming Convention

Based on sub-task prefix structure:
Subtask Prefix
Branch Format
Example
[CORE]
core/DMC-XXX
core/DMC-46
[API]
api/DMC-XXX
api/DMC-46
[UI]
ui/DMC-XXX
ui/DMC-46
[UI-COMP]
ui-comp/DMC-XXX
ui-comp/DMC-46


Commit Message Format

DMC-XXX - [ticket summary]
[Short description of the change]
Examples

UI Implementation

Branch: ui/DMC-46
DMC-46 - Add human-readable display names to job configurations
Updated job configuration forms to include displayName and description fields for better UX

API Implementation (server module related logic)

Branch: api/DMC-46
DMC-46 - Fix JSON deserialization errors in job configurations
Removed nested arrays from examples fields and updated validation logic
Core Implementation

Branch: core/DMC-46
DMC-46 - Implement job configuration validation service
Added centralized validation logic for job configuration parameters

UI Component

Branch: ui-comp/DMC-46
DMC-46 - Create reusable form input component
Built new InputField component for consistent form styling across the app
Invalid Examples


❌ DMC-46 - Fix bug (too vague)
❌ DMC-46 - Updated code (too generic)
❌ DMC-46 - [ticket summary] (missing description)
❌ DMC-46 - [ticket summary]
(missing description)

Quality Checklist

✅ Branch name follows prefix/DMC-XXX format
✅ Commit message starts with DMC-XXX
✅ Ticket summary
✅ Short description of changes
✅ No trailing whitespace

## ANALYSIS PROCESS

1. **Analyze the user request** to identify the ticket number (DMC-XXX format)
2. **Determine the implementation type** to choose correct branch prefix:
   - [CORE] → `core/DMC-XXX` for core business logic
   - [API] → `api/DMC-XXX` for server/API changes  
   - [UI] → `ui/DMC-XXX` for user interface changes
   - [UI-COMP] → `ui-comp/DMC-XXX` for UI components
3. **Generate descriptive commit message** following the format rules
4. **Create pull request title** with ticket number and summary

## FINAL OUTPUT REQUIREMENT

**CRITICAL**: You MUST create the file `outputs/pr_notes.json` containing the JSON object with branch name, commit message, and pull request title. Do NOT perform actual git operations - only generate the JSON file with the details.

**IMPORTANT**: Actual pull request and commit operations will be handled by the workflow automation - you only provide the details in JSON format.