/**
 * AI MCP Methods Examples (Gemini & Dial AI)
 * 
 * This file demonstrates all available AI MCP tools with example usage.
 * Use this as a reference for implementing AI operations in DMTools jobs.
 * 
 * Available in JavaScript context: 2 AI MCP tools
 * - gemini_ai_chat: Google Gemini AI integration
 * - dial_ai_chat: Dial AI integration
 * 
 * Usage: Copy relevant examples to your postJSAction parameter
 * 
 * ============================================================================
 * AI MODEL CONFIGURATION
 * ============================================================================
 * 
 * To configure AI models, set the following in dmtools.env:
 * 
 * 1. Google Gemini:
 *    GEMINI_API_KEY=your-api-key
 *    GEMINI_DEFAULT_MODEL=gemini-2.0-flash
 * 
 * 2. Dial AI:
 *    DIAL_AI_API_KEY=your-api-key
 *    DIAL_AI_BASE_PATH=https://your-dial-ai-endpoint.com
 * 
 * 3. AWS Bedrock (supports multiple model families):
 *    BEDROCK_REGION=eu-north-1
 *    BEDROCK_MODEL_ID=your-model-id
 *    BEDROCK_BEARER_TOKEN=your-aws-bearer-token
 *    # Or use alternative token name:
 *    AWS_BEARER_TOKEN_BEDROCK=your-aws-bearer-token
 *    BEDROCK_MAX_TOKENS=4096
 *    BEDROCK_TEMPERATURE=1.0
 * 
 * Available Bedrock Models:
 * 
 * - Amazon Nova (supports images):
 *   BEDROCK_MODEL_ID=arn:aws:bedrock:eu-north-1:713881790201:inference-profile/eu.amazon.nova-lite-v1:0
 *   # Or direct model ID:
 *   BEDROCK_MODEL_ID=eu.amazon.nova-lite-v1:0
 * 
 * - Qwen (text only):
 *   BEDROCK_MODEL_ID=qwen.qwen3-coder-480b-a35b-v1:0
 * 
 * - Claude (supports images):
 *   BEDROCK_MODEL_ID=anthropic.claude-3-5-sonnet-20241022-v2:0
 * 
 * - Mistral (some support images):
 *   BEDROCK_MODEL_ID=mistral.pixtral-large-2502-v1:0
 * 
 * To use Bedrock as default AI, set:
 * DEFAULT_LLM=aws_bedrock
 * # Or:
 * DEFAULT_LLM=bedrock
 * 
 * ============================================================================
 */

function action(params) {
    try {
        console.log("=== AI MCP EXAMPLES ===");
        console.log("Processing ticket:", params.ticket.key);
        
        const results = {};
        const ticketKey = params.ticket.key;
        const ticketTitle = params.ticket.title || "No title available";
        const ticketDescription = params.ticket.description || "No description available";
        
        // ===== GEMINI AI EXAMPLES =====
        
        console.log("1. Testing Gemini AI chat...");
        
        // Simple question
        const geminiSimple = gemini_ai_chat({
            message: "Hello! Can you help me with software development tasks?"
        });
        results.geminiSimple = geminiSimple;
        
        // Ticket analysis
        const geminiTicketAnalysis = gemini_ai_chat({
            message: `Please analyze this Jira ticket and provide insights:
            
Ticket: ${ticketKey}
Title: ${ticketTitle}
Description: ${ticketDescription}

Please provide:
1. A brief summary of what this ticket is about
2. Potential complexity assessment (Low/Medium/High)
3. Suggested next steps or considerations
4. Any potential risks or dependencies you can identify

Keep your response concise and actionable.`
        });
        results.geminiTicketAnalysis = geminiTicketAnalysis;
        
        // Code review request
        const geminiCodeReview = gemini_ai_chat({
            message: `As a senior software engineer, please provide a code review checklist for a ticket titled "${ticketTitle}". 

Consider:
- Code quality standards
- Testing requirements
- Documentation needs
- Performance considerations
- Security aspects

Provide a practical checklist that a developer can follow.`
        });
        results.geminiCodeReview = geminiCodeReview;
        
        // Test case generation
        const geminiTestCases = gemini_ai_chat({
            message: `Generate test cases for the following requirement:

Ticket: ${ticketKey}
Title: ${ticketTitle}
Description: ${ticketDescription}

Please provide:
1. 3-5 positive test cases
2. 2-3 negative test cases
3. 1-2 edge cases

Format as a simple list with clear test descriptions.`
        });
        results.geminiTestCases = geminiTestCases;
        
        // Documentation generation
        const geminiDocumentation = gemini_ai_chat({
            message: `Create user documentation for the feature described in this ticket:

Title: ${ticketTitle}
Description: ${ticketDescription}

Please provide:
1. Feature overview (2-3 sentences)
2. How to use it (step-by-step)
3. Common troubleshooting tips

Keep it user-friendly and concise.`
        });
        results.geminiDocumentation = geminiDocumentation;
        
        // ===== DIAL AI EXAMPLES =====
        
        console.log("2. Testing Dial AI chat...");
        
        // Simple question
        const dialSimple = dial_ai_chat({
            message: "Hello! Please introduce yourself and your capabilities."
        });
        results.dialSimple = dialSimple;
        
        // Technical analysis
        const dialTechnicalAnalysis = dial_ai_chat({
            message: `Analyze this software development ticket from a technical perspective:

Ticket ID: ${ticketKey}
Title: ${ticketTitle}
Description: ${ticketDescription}

Please provide:
1. Technical complexity assessment
2. Required skills/technologies
3. Estimated effort (in story points or hours)
4. Potential technical challenges
5. Recommended approach or architecture

Be specific and technical in your response.`
        });
        results.dialTechnicalAnalysis = dialTechnicalAnalysis;
        
        // Risk assessment
        const dialRiskAssessment = dial_ai_chat({
            message: `Perform a risk assessment for this development task:

Task: ${ticketTitle}
Details: ${ticketDescription}

Identify:
1. Technical risks
2. Business risks
3. Timeline risks
4. Mitigation strategies for each risk

Provide a structured risk analysis.`
        });
        results.dialRiskAssessment = dialRiskAssessment;
        
        // Architecture recommendations
        const dialArchitecture = dial_ai_chat({
            message: `Provide architectural recommendations for implementing this feature:

Feature: ${ticketTitle}
Requirements: ${ticketDescription}

Consider:
1. System design patterns
2. Database considerations
3. API design
4. Scalability factors
5. Integration points

Provide practical architectural guidance.`
        });
        results.dialArchitecture = dialArchitecture;
        
        // ===== COMPARATIVE ANALYSIS =====
        
        console.log("3. Performing comparative AI analysis...");
        
        // Ask both AIs the same question for comparison
        const comparisonQuestion = `What are the key factors to consider when implementing the feature: "${ticketTitle}"?`;
        
        const geminiComparison = gemini_ai_chat({
            message: comparisonQuestion
        });
        
        const dialComparison = dial_ai_chat({
            message: comparisonQuestion
        });
        
        results.comparison = {
            question: comparisonQuestion,
            geminiResponse: geminiComparison,
            dialResponse: dialComparison
        };
        
        // ===== WORKFLOW INTEGRATION EXAMPLES =====
        
        console.log("4. AI-powered workflow examples...");
        
        // Generate commit message
        const commitMessage = gemini_ai_chat({
            message: `Generate a conventional commit message for work done on this ticket:

Ticket: ${ticketKey} - ${ticketTitle}

The commit should follow conventional commit format (type(scope): description).
Consider the ticket title and provide an appropriate commit message.
Keep it concise and descriptive.`
        });
        results.commitMessage = commitMessage;
        
        // Generate release notes
        const releaseNotes = dial_ai_chat({
            message: `Generate release notes entry for this feature:

Feature: ${ticketTitle}
Description: ${ticketDescription}

Create a user-facing release note that explains:
1. What's new
2. How it benefits users
3. Any important notes

Keep it marketing-friendly but accurate.`
        });
        results.releaseNotes = releaseNotes;
        
        // ===== ERROR HANDLING AND EDGE CASES =====
        
        console.log("5. Testing AI error handling...");
        
        // Test with empty message (should handle gracefully)
        try {
            const emptyTest = gemini_ai_chat({
                message: ""
            });
            results.emptyMessageTest = emptyTest;
        } catch (error) {
            results.emptyMessageTest = { error: error.toString() };
        }
        
        // Test with very long message
        const longMessage = "Analyze this ticket: " + "Very detailed requirements. ".repeat(100);
        try {
            const longMessageTest = dial_ai_chat({
                message: longMessage.substring(0, 1000) + "..." // Truncate for safety
            });
            results.longMessageTest = longMessageTest;
        } catch (error) {
            results.longMessageTest = { error: error.toString() };
        }
        
        console.log("=== AI MCP EXAMPLES COMPLETED ===");
        
        return {
            success: true,
            message: `Successfully executed AI MCP examples for ticket ${ticketKey}`,
            results: results,
            totalOperations: Object.keys(results).length,
            aiProviders: ["Gemini AI", "Dial AI"],
            useCases: [
                "Ticket analysis and insights",
                "Code review assistance",
                "Test case generation",
                "Documentation creation",
                "Technical risk assessment",
                "Architecture recommendations",
                "Workflow automation",
                "Comparative AI analysis"
            ],
            notes: [
                "AI responses may vary between runs",
                "Consider rate limiting for production use",
                "Validate AI-generated content before use",
                "Both AI providers offer different strengths"
            ]
        };
        
    } catch (error) {
        console.error("Error in AI MCP examples:", error);
        return {
            success: false,
            error: error.toString(),
            message: "Failed to execute AI MCP examples",
            troubleshooting: [
                "Check AI service availability",
                "Verify API credentials",
                "Check network connectivity",
                "Review message content for policy violations"
            ]
        };
    }
}

