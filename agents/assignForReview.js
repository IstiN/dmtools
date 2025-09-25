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
        
        // Use common assignForReview function
        return assignForReview(ticketKey, initiatorId);
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
