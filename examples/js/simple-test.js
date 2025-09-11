/**
 * Simple MCP Test Script
 * 
 * This is a basic test script to verify that the JavaScript MCP bridge is working correctly.
 * Use this as a starting point for your own JavaScript post-actions.
 */

function action(params) {
    try {
        console.log("=== SIMPLE MCP TEST ===");
        console.log("Ticket key:", params.ticket.key);
        console.log("Ticket title:", params.ticket.title);
        
        const results = {};
        
        // Test 1: Add a simple label
        console.log("Test 1: Adding test label...");
        const labelResult = jira_update_field({
            key: params.ticket.key,
            field: "labels",
            value: ["js-mcp-test", "automated"]
        });
        results.labelUpdate = labelResult;
        
        // Test 2: Post a comment
        console.log("Test 2: Posting test comment...");
        const commentResult = jira_post_comment({
            key: params.ticket.key,
            comment: `✅ JavaScript MCP bridge test successful! 
            
Executed at: ${new Date().toISOString()}
Ticket: ${params.ticket.key}
Title: ${params.ticket.title || 'N/A'}

This comment was posted by the JavaScript MCP bridge to verify functionality.`
        });
        results.comment = commentResult;
        
        // Test 3: Get ticket information
        console.log("Test 3: Retrieving ticket information...");
        const ticketInfo = jira_get_ticket({
            key: params.ticket.key,
            fields: ["summary", "status", "labels", "description"]
        });
        results.ticketInfo = ticketInfo;
        
        // Test 4: AI integration test (if available)
        console.log("Test 4: Testing AI integration...");
        try {
            const aiResponse = gemini_ai_chat({
                message: `Briefly analyze this ticket: ${params.ticket.key} - ${params.ticket.title}. Provide a one-sentence summary.`
            });
            results.aiAnalysis = aiResponse;
        } catch (aiError) {
            results.aiAnalysis = { error: "AI not available: " + aiError.toString() };
        }
        
        console.log("=== ALL TESTS COMPLETED ===");
        
        return {
            success: true,
            message: `JavaScript MCP bridge test completed successfully for ${params.ticket.key}`,
            timestamp: new Date().toISOString(),
            results: results,
            testsRun: 4,
            summary: {
                labelUpdate: results.labelUpdate ? "✅ Success" : "❌ Failed",
                commentPosted: results.comment ? "✅ Success" : "❌ Failed", 
                ticketRetrieval: results.ticketInfo ? "✅ Success" : "❌ Failed",
                aiIntegration: results.aiAnalysis && !results.aiAnalysis.error ? "✅ Success" : "⚠️ Unavailable"
            }
        };
        
    } catch (error) {
        console.error("Test failed:", error);
        return {
            success: false,
            error: error.toString(),
            message: "JavaScript MCP bridge test failed",
            timestamp: new Date().toISOString()
        };
    }
}

