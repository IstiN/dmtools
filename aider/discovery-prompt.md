# Aider File Discovery Assistant Instructions

## PHASE 1: FILE DISCOVERY AND ANALYSIS

**IMPORTANT**: This is the DISCOVERY phase only. You are NOT implementing code, just analyzing and creating a comprehensive file list.

**READ THE USER REQUEST FILE(`aider-outputs/user-request.txt`)** to understand what code changes are needed.

## DISCOVERY INSTRUCTIONS:

Your job is to perform comprehensive analysis to identify ALL files that will be affected by the requested changes:

1. **Analyze the Request**: Understand the full scope of changes needed
2. **Map Dependencies**: Identify all related files and their relationships
3. **Create Comprehensive File List**: Generate a detailed JSON structure
4. **Save Discovery Results**: Store analysis in `aider-outputs/affected-files.json`

## ANALYSIS SCOPE:

Consider ALL of these file types and relationships:

### Core Application Files:
- **Entity classes** and their relationships (JPA annotations, foreign keys)
- **Repository interfaces** and implementations (Spring Data JPA)
- **Service classes** and business logic implementations
- **Controller endpoints** and REST API definitions
- **DTO classes** for data transfer
- **Exception classes** and error handling
- **Configuration files** (application.yml, properties)

### Supporting Files:
- **Database migration files** (Flyway SQL scripts if schema changes needed)
- **Test files** (unit tests, integration tests)
- **OpenAPI/Swagger** specification files
- **Documentation files** (if API changes affect docs)

### Dependencies and Relationships:
- **Parent-child entity relationships**
- **Service layer dependencies**
- **Controller-service-repository chains**
- **Cross-cutting concerns** (security, validation, logging)

## DISCOVERY COMMANDS SEQUENCE:

Execute these commands to perform thorough analysis:

1. **Read user request**: `/add aider-outputs/user-request.txt`
2. **Understand scope**: Analyze the request thoroughly
3. **Map file relationships**: Identify all interconnected components
4. **Generate file list**: Create comprehensive JSON structure

## REQUIRED OUTPUT FORMAT:

You MUST create `aider-outputs/affected-files.json` with this exact structure:

```json
{
  "ticket_number": "DMC-XXX",
  "request_summary": "Brief description of the requested change",
  "affected_files": {
    "to_modify": [
      "path/to/existing/file1.java",
      "path/to/existing/file2.java"
    ],
    "to_create": [
      "path/to/new/file1.java", 
      "path/to/new/file2.java"
    ],
    "to_reference": [
      "path/to/context/file1.java",
      "path/to/context/file2.java"
    ],
    "tests_needed": [
      "path/to/test/file1Test.java",
      "path/to/test/file2Test.java"
    ]
  },
  "dependencies": [
    "Entity A depends on Entity B (foreign key relationship)",
    "Service X uses Repository Y",
    "Controller Z calls Service X"
  ],
  "implementation_notes": [
    "Consider validation rules for new fields",
    "Database migration needed for schema changes",
    "API documentation updates required"
  ],
  "estimated_complexity": "LOW|MEDIUM|HIGH",
  "potential_risks": [
    "Breaking change to existing API",
    "Data migration required"
  ]
}
```

## DISCOVERY QUALITY CHECKLIST:

Before completing discovery, verify:
- ✅ All entity relationships are mapped
- ✅ Service layer dependencies are identified
- ✅ Controller endpoints are considered
- ✅ Test files are included
- ✅ Database changes are noted
- ✅ Configuration impacts are assessed
- ✅ API documentation needs are identified

## FINAL DISCOVERY RESPONSE:

Wrap your discovery analysis in these tags:

<AIDER_RESPONSE>
# File Discovery Analysis Complete

## 📋 Discovery Summary

**Ticket**: DMC-XXX
**Request**: [Brief description]
**Complexity**: [LOW/MEDIUM/HIGH]

## 📁 Files Identified

### To Modify: X files
- List key files to be modified

### To Create: X files  
- List new files to be created

### For Context: X files
- List files needed for understanding

### Tests Needed: X files
- List test files to update/create

## 🔗 Key Dependencies
- Main dependency relationships identified

## ⚠️ Implementation Notes
- Critical considerations for implementation phase

## ✅ Discovery Status
File discovery analysis complete. Comprehensive file list saved to `aider-outputs/affected-files.json`.

**Next Step**: Use the implementation prompt with the generated file list to proceed with actual code changes.
</AIDER_RESPONSE>

## WORKFLOW INTEGRATION:

- This discovery phase creates the foundation for Phase 2 implementation
- The generated JSON file will be used by the implementation prompt
- DO NOT implement any code changes in this phase
- Focus solely on analysis and planning
