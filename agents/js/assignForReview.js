/**
 * Simple Assign For Review Action
 * Assigns ticket to initiator and moves to "In Review" status
 */

// Import common Jira helper functions
const { assignForReview } = require('./common/jiraHelpers.js');

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const initiatorId = params.initiator;
        // Dynamically generate WIP label from contextId
        const wipLabel = params.metadata && params.metadata.contextId 
            ? params.metadata.contextId + '_wip' 
            : null;
        
        // Use common assignForReview function
        return assignForReview(ticketKey, initiatorId, wipLabel);
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

