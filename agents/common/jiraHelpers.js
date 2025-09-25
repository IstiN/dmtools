/**
 * Common Jira Helper Functions
 * Shared utilities for Jira ticket operations
 */

/**
 * Assign ticket to initiator and move to "In Review" status with AI-generated label
 * This is the common post-processing logic used by multiple agents
 * 
 * @param {string} ticketKey - The Jira ticket key
 * @param {string} initiatorId - Account ID of the person to assign the ticket to
 * @returns {Object} Result object with success status and message
 */
function assignForReview(ticketKey, initiatorId) {
    try {
        console.log("Processing ticket:", ticketKey);
        
        // Assign to initiator
        jira_assign_ticket_to({
            key: ticketKey,
            accountId: initiatorId
        });
        
        // Move to In Review status
        jira_move_to_status({
            key: ticketKey,
            statusName: "In Review"
        });

        // Add AI-generated label
        jira_add_label({
            key: ticketKey,
            label: 'ai_generated'
        });
        
        console.log("✅ Assigned to initiator and moved to In Review");
        
        return {
            success: true,
            message: `Ticket ${ticketKey} assigned and moved to In Review`
        };
        
    } catch (error) {
        console.error("❌ Error in assignForReview:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

/**
 * Extract ticket key from Jira API response
 * 
 * @param {string|Object} result - Jira API response
 * @returns {string|null} Extracted ticket key or null if not found
 */
function extractTicketKey(result) {
    if (!result) {
        return null;
    }
    if (typeof result === 'string') {
        try {
            const parsed = JSON.parse(result);
            return parsed && parsed.key ? parsed.key : null;
        } catch (error) {
            return null;
        }
    }
    if (typeof result === 'object' && typeof result.key === 'string') {
        return result.key;
    }
    return null;
}

/**
 * Set priority on a Jira ticket using the appropriate API
 * 
 * @param {string} ticketKey - The Jira ticket key
 * @param {string} priority - Priority name (e.g., 'Low', 'Medium', 'High')
 * @returns {boolean} True if successful, false otherwise
 */
function setTicketPriority(ticketKey, priority) {
    if (!ticketKey || !priority) {
        return false;
    }
    
    try {
        jira_set_priority({
            key: ticketKey,
            priority: priority
        });
        console.log('Set priority ' + priority + ' on ticket ' + ticketKey);
        return true;
    } catch (priorityError) {
        console.error('Failed to set priority on ticket ' + ticketKey + ':', priorityError);
        return false;
    }
}

// Export functions for use by other modules
module.exports = {
    assignForReview,
    extractTicketKey,
    setTicketPriority
};
