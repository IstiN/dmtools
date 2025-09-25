/**
 * AI Response Parser
 * Simple utilities for AI responses - expecting stable, well-formatted responses
 */

/**
 * Parse AI response containing questions array
 * Expects clean JSON array format from AI
 * 
 * @param {Array|string} response - AI response (should be JSON array)
 * @returns {Array} Array of question objects
 */
function parseQuestionsResponse(response) {
    // Handle string responses (parse JSON)
    if (typeof response === 'string') {
        try {
            response = JSON.parse(response);
        } catch (error) {
            console.error('Invalid JSON from AI:', error);
            return [];
        }
    }
    
    // Expect array format
    if (!Array.isArray(response)) {
        console.warn('AI response is not an array, got:', typeof response);
        return [];
    }
    
    return response; // Trust AI to provide correct format
}

/**
 * Build ticket summary with length constraint
 * 
 * @param {string} summary - Summary from AI
 * @param {number} index - Index for fallback numbering
 * @returns {string} Summary (truncated if needed)
 */
function buildSummary(summary, index) {
    const text = summary || 'Follow-up question #' + (index + 1);
    return text.length <= 120 ? text : text.slice(0, 117) + '...';
}

/**
 * Build ticket description
 * 
 * @param {Object} question - Question object
 * @returns {string} Description text
 */
function buildDescription(question) {
    return question.description || 'Follow template from instructions.';
}

// Export functions for use by other modules
module.exports = {
    parseQuestionsResponse,
    buildSummary,
    buildDescription
};
