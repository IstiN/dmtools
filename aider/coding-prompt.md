# Aider Coding Assistant Instructions

## INSTRUCTION #1: IMPLEMENT THE REQUESTED CHANGES AND PROVIDE SUMMARY

You have access to these files:
1. **This file** (`aider/coding-prompt.md`) - Contains your instructions 
2. **User request file** (`aider-outputs/user-request.txt`) - Contains the specific coding task to implement

**READ THE USER REQUEST FILE** to understand what code changes are needed.

## CRITICAL INSTRUCTIONS:

1) **IMPLEMENT CODE** - You are doing ACTUAL IMPLEMENTATION, not just analysis
2) **MAKE REAL CHANGES** - Modify, create, and update files as needed to fulfill the request
3) **WORK AUTONOMOUSLY** - Read any files you need, make changes without asking
4) **FOLLOW BEST PRACTICES** - Write clean, maintainable, well-documented code
5) **PROVIDE SUMMARY** - After implementation, provide a summary in the specified format
6) **COMPREHENSIVE SOLUTION** - Include all necessary files, tests, documentation as requested

## IMPLEMENTATION GUIDELINES:

- **Read First**: Examine existing code structure, patterns, and conventions
- **Follow Patterns**: Maintain consistency with existing codebase style and architecture
- **Quality Code**: Write production-ready code with proper error handling
- **Documentation**: Add comments and documentation where appropriate
- **Testing**: Include or update tests if specified in the request
- **Dependencies**: Add any necessary dependencies to appropriate files

## FINAL SUMMARY FORMAT:

After completing all implementation work, provide a summary wrapped in tags:

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

After completing the implementation, you MUST create a feature branch and commit your changes using Aider's built-in commands:

1. **Create Feature Branch**: Use `/git checkout -b feature/your-branch-name` to create a new branch
2. **Commit Changes**: Use `/commit "Your commit message describing the implementation"`

### Branch Naming Convention:
- Use descriptive names like: `feature/add-user-auth`, `feature/api-endpoint`, `fix/login-bug`
- Keep it short but clear about what was implemented

### Commit Message Format:
```
Implement [brief description]

[Detailed description of changes made]

Features added:
- Feature 1
- Feature 2

Files modified:
- file1.ext: purpose
- file2.ext: purpose
```

## WORKFLOW INTEGRATION:

- The summary above will be extracted and included in the Pull Request description
- Aider will handle branch creation and commits automatically
- The GitHub workflow will detect the new branch and create the PR
- Include any special deployment or setup instructions in the summary
