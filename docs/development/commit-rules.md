# Git Commit Message Rules

## 📋 **Mandatory Format**
```
<type>(DMC-<number>): <subject>

<body>

Resolves: DMC-<number>
```

## 🎯 **Types**
- **feat**: New feature
- **fix**: Bug fix  
- **docs**: Documentation changes
- **style**: Code formatting, semicolons, etc.
- **refactor**: Code refactoring without functionality changes
- **test**: Adding or updating tests
- **chore**: Maintenance, build tasks, dependency updates
- **ci**: CI/CD pipeline changes

## 🔢 **Ticket Number Rules**
- **MANDATORY**: Every commit MUST include ticket number in format `DMC-<number>`
- **Subject line**: Include ticket number after type: `fix(DMC-46): Fix API parameter mismatches`
- **Footer**: Include `Resolves: DMC-<number>` line at the end

## ✅ **Examples**

### Feature Commit
```
feat(DMC-46): Add human-readable display names to job configurations

- Added displayName and description fields to expert.json
- Added displayName and description fields to testcases-generator.json  
- Updated parameter descriptions for better UX
- Fixed nested array structures in examples

Resolves: DMC-46
```

### Bug Fix Commit
```
fix(DMC-46): Fix JSON deserialization errors in job configurations

- Removed nested arrays from examples fields
- Changed defaultValue from [] to null for array types
- Fixed confluencePages parameter type configuration

Resolves: DMC-46
```

### Documentation Update
```
docs(DMC-46): Add commit message rules with mandatory ticket references

- Created COMMIT_MESSAGE_RULES.md with format specifications
- Added examples for different commit types
- Enforced ticket number requirements

Resolves: DMC-46
```

## 🚫 **Invalid Examples**
```
❌ Add new feature (missing ticket number)
❌ fix: bug (missing ticket number)
❌ feat: new API endpoint (missing ticket number)
```

## 🔧 **Git Configuration**
Add this to your `.gitconfig` to enforce format:
```
[commit]
    template = .gitmessage
```

Create `.gitmessage` file:
```
# feat|fix|docs|style|refactor|test|chore|ci(DMC-XXX): Brief description

# Detailed explanation of changes
# - What was changed
# - Why it was changed
# - How it affects the system

Resolves: DMC-XXX
``` 