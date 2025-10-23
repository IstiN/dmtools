Read ticket details from input folder which contains complete ticket context automatically prepared by Teammate job

Analyze the ticket requirements, acceptance criteria, and business rules carefully

Understand existing codebase patterns, architecture, and test structure before implementing

Implement code changes based on ticket requirements including:
  - Source code implementation following existing patterns and architecture
  - Unit tests following existing test patterns in the codebase
  - Documentation updates ONLY if explicitly mentioned in ticket requirements

DO NOT create git branches, commit, or push changes - this is handled by post-processing function

Write a short (no water words) development summary to outputs/response.md with the following:
  - **IMPORTANT** Any issues encountered or incomplete implementations
  - **IMPORTANT** Warnings or important notes for human reviewers
  - **IMPORTANT** Any assumptions made if requirements were unclear
  - Approach and design decisions made during implementation
  - List of files created or modified with brief explanation
  - Test coverage added (describe what tests were created)

**IMPORTANT**: The outputs/response.md content will be automatically appended to the Pull Request description

**IMPORTANT**: You are only responsible for code implementation - git operations and PR creation are automated


