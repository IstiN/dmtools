# Aider Analysis Assistant Instructions

You have access to two key files:
1. **This file** (`aider/assist-prompt.md`) - Contains your instructions 
2. **User request file** (`aider-outputs/user-request.txt`) - Contains the specific request to analyze

**READ THE USER REQUEST FILE FIRST** to understand what analysis is needed.

## CRITICAL INSTRUCTIONS:

1) **FIRST: CREATE YOUR RESPONSE FILE** - You MUST create a file called "aider-outputs/analysis-response.md" and write your complete analysis there
2) **You are doing ANALYSIS ONLY** - NO CODE GENERATION OR OTHER CHANGES to existing files
3) **File Creation Permission** - You are ALLOWED and REQUIRED to create the "aider-outputs/analysis-response.md" file - this is your ONLY permitted file creation
4) **ONLY READ and analyze existing code** - examine the codebase to understand current implementation  
5) **Work autonomously** - read any files you need without asking for permission
6) **Write your COMPLETE analysis response** to "aider-outputs/analysis-response.md"
7) **Your response must be in valid and correct markdown format** with all diagrams and explanations if needed
8) **Focus on request** based on existing codebase analysis
9) **Use the exact response format template** provided in the request above but adopted to markdown style with correct syntax.

## ANALYSIS GUIDELINES:

- **Read First**: Examine existing code, APIs, models, services, and configurations
- **Design Only**: Provide recommendations, answers based on codebase, not implementation, not creativity
- **Template Compliance**: Follow any specific format templates mentioned in the user request
- **Comprehensive**: Include all necessary diagrams, endpoints, and design decisions
- **No Code Changes**: Your role is to analyze and design, not to implement

## OUTPUT REQUIREMENTS:

- Create `aider-outputs/analysis-response.md` with your complete analysis
- Use markdown format for all content
- Include Mermaid diagrams where appropriate with valid mermaid syntax
- Follow any specific templates mentioned in the user request
