# Aider Implementation Assistant Instructions

## PHASE 2: IMPLEMENTATION USING DISCOVERED FILES

**IMPORTANT**: This is the IMPLEMENTATION phase. You are doing ACTUAL CODE IMPLEMENTATION using the file discovery from Phase 1.

## PREREQUISITES:

1. **Discovery Phase Complete**: File discovery must be completed first
2. **JSON File Available**: `aider-outputs/affected-files.json` must exist
3. **Ticket Validated**: DMC-XXX ticket number already confirmed

## CRITICAL INSTRUCTIONS:

1) **DISCOVERY CONTEXT READY** - All discovered files are already loaded in your context
2) **REVIEW DISCOVERY RESULTS** - Check `aider-outputs/affected-files.json` for the analysis
3) **IMPLEMENT CODE** - Make actual code changes following the discovery analysis
4) **WORK SYSTEMATICALLY** - Follow the planned approach from discovery
5) **FOLLOW BEST PRACTICES** - Write clean, maintainable, well-documented code
6) **COMPREHENSIVE SOLUTION** - Include all necessary files, tests, documentation

## IMPLEMENTATION WORKFLOW:

### Step 1: Review Discovery Results
The discovery analysis and all identified files are already in your context:
- `aider-outputs/affected-files.json` - Complete discovery analysis
- All files from `to_modify` array - Ready for modification
- All files from `to_reference` array - Available for understanding context
- All files from `tests_needed` array - Ready for test updates

### Step 2: Understand the Scope
Review the discovery JSON to understand:
- **Request Summary**: What needs to be implemented
- **Files to Modify**: Existing files that need changes
- **Files to Create**: New files to be created
- **Dependencies**: Relationships between components
- **Implementation Notes**: Key considerations and risks

### Step 3: Implement Changes
Follow the implementation plan from discovery:
- **Read existing patterns** and maintain consistency
- **Create new files** as identified in discovery
- **Modify existing files** according to requirements
- **Update tests** to cover new functionality

## IMPLEMENTATION GUIDELINES:

### Code Quality:
- **Follow Existing Patterns**: Maintain consistency with codebase style and architecture
- **Production-Ready Code**: Include proper error handling and validation
- **Documentation**: Add JavaDoc comments and inline documentation
- **Testing**: Update or create tests as identified in discovery
- **Dependencies**: Add any necessary dependencies to appropriate files

### Change Management:
- **Incremental Changes**: Make logical, related changes together
- **Validation**: Ensure all changes work together cohesively
- **Database Changes**: Include migration scripts if schema changes are needed
- **API Changes**: Update OpenAPI/Swagger documentation if endpoints change

## MANDATORY TICKET INTEGRATION:

All implementation must follow DMTools standards using the ticket from discovery:

### Branch Naming Convention:
Based on sub-task prefix structure:

| Sub-task Prefix | Branch Format | Example |
|----------------|---------------|---------|
| **[CORE]** | core/DMC-XXX | core/DMC-46 |
| **[API]** | api/DMC-XXX | api/DMC-46 |
| **[UI]** | ui/DMC-XXX | ui/DMC-46 |
| **[UI-COMP]** | ui-comp/DMC-XXX | ui-comp/DMC-46 |

### Commit Message Format:
```
DMC-XXX - [ticket summary]
[Short description of the change]
```

## FINAL SUMMARY FORMAT:

Your final response must be wrapped in these tags:

<AIDER_RESPONSE>
# Implementation Summary

## 🚀 Changes Made

### Files Modified:
- `path/to/file1.ext` - Description of changes made
- `path/to/file2.ext` - Description of changes made

### Files Created:
- `path/to/newfile.ext` - Purpose and functionality

### Key Features Implemented:
- Feature 1: Description
- Feature 2: Description

## 🔧 Technical Details

### Implementation Approach:
Brief explanation of the solution approach and design decisions based on discovery analysis.

### Dependencies Added:
- dependency-name: version (purpose)

### Configuration Changes:
- Config file changes if any

### Database Changes:
- Schema modifications (migrations created)

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

## COMPLETE GIT WORKFLOW SEQUENCE:

After completing all implementation work, execute these commands:

1. **Check modifications**: `/run git status`
2. **Create feature branch**: `/git checkout -b [prefix]/DMC-XXX`
3. **Stage changes**: `/run git add .`
4. **Create commit**: `/commit "DMC-XXX - [implementation summary]\n[Short description of changes]"`
5. **Push changes**: `/run git push -u origin [prefix]/DMC-XXX`

## QUALITY CHECKLIST:

Before completing implementation, verify:
- ✅ All files from discovery are properly handled
- ✅ Code follows existing patterns and conventions
- ✅ Tests are updated/created as planned
- ✅ Error handling is comprehensive
- ✅ Documentation is updated
- ✅ Branch name follows prefix/DMC-XXX format
- ✅ Commit message follows DMTools standards

## WORKFLOW INTEGRATION:

- This phase uses the comprehensive file list from Phase 1
- All identified dependencies and relationships are considered
- Changes are made systematically based on the discovery analysis
- The implementation follows the planned approach from discovery
- Git workflow follows DMTools standards with proper ticket linkage
