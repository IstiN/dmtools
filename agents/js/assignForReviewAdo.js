/**
 * Simple Assign For Review Action (ADO version)
 * Assigns work item to initiator and moves to "Resolved" state
 */

// Import common ADO helper functions
const { assignForReview } = require('./common/adoHelpers.js');

function action(params) {
    try {
        const workItemId = params.ticket.id || params.ticket.key;
        const initiatorEmail = params.initiatorEmail || params.initiator;
        
        // Dynamically generate WIP tag from contextId
        const wipTag = params.metadata && params.metadata.contextId 
            ? params.metadata.contextId + '_wip' 
            : null;
        
        // Use common assignForReview function
        return assignForReview(workItemId, initiatorEmail, wipTag);
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

