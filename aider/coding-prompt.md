# Aider Coding Assistant Instructions

## INSTRUCTION #1: IMPLEMENT THE REQUESTED CHANGES AND PROVIDE SUMMARY

**IMPORTANT** YOU HAVE ACCESS TO ALL FILES WHICH ARE MANDATORY TO SOLVE THE USER REQUEST.

get /context of what must be changed and do /whole changes of files to reduce mistakes

**READ THE USER REQUEST FILE(`aider-outputs/user-request.txt`)** to understand what code changes are needed. 

## TWO-PHASE IMPLEMENTATION APPROACH:

### PHASE 1: FILE DISCOVERY AND ANALYSIS
**Before making any changes**, perform a comprehensive analysis to identify all files that will be affected:

1. **Analyze the Request**: Understand the scope of changes needed
2. **Map Dependencies**: Identify all related files (entities, repositories, services, controllers, tests, etc.)
3. **Create File List**: Generate a comprehensive list of affected files in JSON format
4. **Save to Discovery File**: Store the list in `aider-outputs/affected-files.json`

**Discovery Command Sequence:**
```
/ask Please analyze the user request and create a comprehensive list of all files that will need to be modified, created, or referenced for this implementation. Consider:
- Entity classes and their relationships
- Repository interfaces and implementations  
- Service classes and business logic
- Controller endpoints and DTOs
- Exception classes
- Configuration files
- Test files
- Database migration files (if needed)
- Documentation files

Save this analysis as a JSON file with the following structure:
{
  "request_summary": "Brief description of the change",
  "affected_files": {
    "to_modify": ["list of existing files to modify"],
    "to_create": ["list of new files to create"], 
    "to_reference": ["list of files to read for context"],
    "tests_needed": ["list of test files to update/create"]
  },
  "dependencies": ["list of file dependencies and relationships"],
  "implementation_notes": ["key considerations for implementation"]
}
```

### PHASE 2: IMPLEMENTATION
**After file discovery**, proceed with actual implementation:

1. **Load File List**: Read `aider-outputs/affected-files.json`
2. **Add Files to Context**: Use `/add` to include all files from the discovery phase
3. **Implement Changes**: Make all necessary modifications following the analysis
4. **Verify Completeness**: Ensure all identified files are properly handled

## MANDATORY TICKET REQUIREMENT:

**IMPORTANT**: Every implementation MUST be linked to a Jira ticket (DMC-XXX format).

1. **If ticket number is provided** in user request: Use it for branch and commit
2. **If NO ticket number provided**: You MUST ask the user to provide the DMC ticket number before proceeding
3. **Never proceed without a valid DMC-XXX ticket number**

Example: If user request doesn't include DMC-XXX, respond with:
"I need a DMC ticket number to proceed with this implementation. Please provide the ticket number (format: DMC-XXX) for this work."

## CRITICAL INSTRUCTIONS:

1) **FOLLOW TWO-PHASE APPROACH** - First discover files, then implement
2) **IMPLEMENT CODE** - You are doing ACTUAL IMPLEMENTATION, not just analysis
3) **USE FILE DISCOVERY** - Add files to context based on the discovery phase analysis
4) **MAKE REAL CHANGES** - Modify, create, and update files as needed to fulfill the request
5) **WORK AUTONOMOUSLY** - Read any files you need, make changes without asking
6) **FOLLOW BEST PRACTICES** - Write clean, maintainable, well-documented code
7) **PROVIDE SUMMARY** - After implementation, provide a summary in the specified format
8) **COMPREHENSIVE SOLUTION** - Include all necessary files, tests, documentation as requested

## IMPLEMENTATION GUIDELINES:

### Phase 1 - Discovery:
- **Analyze Scope**: Thoroughly understand the full impact of the requested changes
- **Map File Dependencies**: Identify all interconnected files and relationships
- **Document Analysis**: Create comprehensive JSON file list for Phase 2
- **Validate Coverage**: Ensure no critical files are missed in discovery

### Phase 2 - Implementation:
- **Load Discovery Results**: Read `aider-outputs/affected-files.json` before starting
- **Add Files Systematically**: Use `/add` for all files identified in discovery phase
- **Read First**: Examine existing code structure, patterns, and conventions
- **Follow Patterns**: Maintain consistency with existing codebase style and architecture
- **Quality Code**: Write production-ready code with proper error handling
- **Documentation**: Add comments and documentation where appropriate
- **Testing**: Include or update tests if specified in the request
- **Dependencies**: Add any necessary dependencies to appropriate files

## FINAL SUMMARY FORMAT:

Your final response must be wrapped to <AIDER_RESPONSE> even it's success or not:

<AIDER_RESPONSE>
# Implementation Summary

## 🚀 Changes Made

### Files Modified:
- `path/to/file1.ext` - Description of changes
- `path/to/file2.ext` - Description of changes

### Files Created:
- `path/to/newfile.ext` - Purpose and functionality

### Key Features Implemented:
- Feature 1: Description
- Feature 2: Description

## 🔧 Technical Details

### Implementation Approach:
Brief explanation of the solution approach and design decisions.

### Dependencies Added:
- dependency-name: version (purpose)

### Configuration Changes:
- Config file changes if any

## ✅ Verification Steps

### How to Test:
1. Step 1 to verify the implementation
2. Step 2 to test functionality
3. Step 3 to validate integration

### Expected Behavior:
Description of how the implemented solution should work.

## 📝 Additional Notes

Any important notes, considerations, or follow-up tasks.
</AIDER_RESPONSE>

## GIT WORKFLOW INTEGRATION:

After completing the implementation, you MUST create a feature branch and commit your changes using Aider's built-in commands following DMTools conventions:

1. **Create Feature Branch**: Use `/git checkout -b [prefix]/DMC-XXX` format
2. **Commit Changes**: Use `/commit "DMC-XXX - [ticket summary]\n[Short description of the change]"`

### Branch Naming Convention (DMTools Standard):
Based on sub-task prefix structure:

| Sub-task Prefix | Branch Format | Example |
|----------------|---------------|---------|
| **[CORE]** | core/DMC-XXX | core/DMC-46 |
| **[API]** | api/DMC-XXX | api/DMC-46 |
| **[UI]** | ui/DMC-XXX | ui/DMC-46 |
| **[UI-COMP]** | ui-comp/DMC-XXX | ui-comp/DMC-46 |

### Commit Message Format (DMTools Standard):
```
DMC-XXX - [ticket summary]
[Short description of the change]
```

### Examples:

**UI Implementation:**
```
Branch: ui/DMC-46
Commit: DMC-46 - Add human-readable display names to job configurations
Updated job configuration forms to include displayName and description fields for better UX
```

**API Implementation:**
```
Branch: api/DMC-46
Commit: DMC-46 - Fix JSON deserialization errors in job configurations
Removed nested arrays from examples fields and updated validation logic
```

**Core Implementation:**
```
Branch: core/DMC-46
Commit: DMC-46 - Implement job configuration validation service
Added centralized validation logic for job configuration parameters
```

## QUALITY CHECKLIST (DMTools Standards):

Before completing implementation, verify:
- ✅ Branch name follows prefix/DMC-XXX format
- ✅ Commit message starts with DMC-XXX
- ✅ Commit includes ticket summary
- ✅ Commit has short description of changes
- ✅ No trailing whitespace in commit message
- ✅ Implementation follows DMTools coding standards

## WORKFLOW INTEGRATION:

- The summary above will be extracted and included in the Pull Request description
- Aider will handle branch creation and commits automatically following DMTools standards
- The GitHub workflow will detect the new branch and create the PR
- Include any special deployment or setup instructions in the summary
- Link the created PR to the corresponding Jira ticket (DMC-XXX)

## COMPLETE WORKFLOW SEQUENCE:

You MUST follow this complete sequence for every implementation:

### PHASE 1 - DISCOVERY:
1. **Read user request**: `/add aider-outputs/user-request.txt`
2. **Perform analysis**: Use `/ask` command to analyze scope and create file discovery JSON
3. **Create discovery file**: Save comprehensive file list to `aider-outputs/affected-files.json`

### PHASE 2 - IMPLEMENTATION:
4. **Load discovery results**: `/add aider-outputs/affected-files.json`  
5. **Add discovered files**: Use `/add` for all files identified in discovery phase
6. **Implement changes**: Make all necessary code modifications
7. **Verify completeness**: Ensure all requirements are met

### PHASE 3 - GIT WORKFLOW:
8. **Check what was modified**: `/run git status` to show what files were changed
9. **Create feature branch**: `/git checkout -b [prefix]/DMC-XXX` (use appropriate prefix: core, api, ui, ui-comp)
10. **Stage all changes**: `/run git add .` to add all modified files
11. **Create commit**: `/commit "DMC-XXX - [your implementation summary]\n[Short description of changes]"`
12. **Push changes**: `/run git push -u origin [prefix]/DMC-XXX`

These commands ensure comprehensive discovery and proper Git tracking following DMTools standards.