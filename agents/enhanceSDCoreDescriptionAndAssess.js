/**
 * Enhance SD CORE Description and Assess Action
 * Enhances SD CORE ticket descriptions with technical details, assesses implementation needs, 
 * and updates diagram field based on AI-generated content
 */

// Import common helper functions
const { assignForReview, extractTicketKey } = require('./common/jiraHelpers.js');

/**
 * Parse AI response for SD CORE enhancement
 * Expects JSON object with description, coreSubtaskCreation boolean, and diagram string
 * 
 * @param {Object|string} response - AI response (should be JSON object)
 * @returns {Object} Parsed enhancement data or null if invalid
 */
function parseSDCoreEnhancementResponse(response) {
    // Handle string responses (parse JSON)
    if (typeof response === 'string') {
        try {
            response = JSON.parse(response);
        } catch (error) {
            console.error('Invalid JSON from AI:', error);
            return null;
        }
    }
    
    // Validate expected structure
    if (!response || typeof response !== 'object') {
        console.warn('AI response is not an object, got:', typeof response);
        return null;
    }
    
    // Validate required fields
    if (typeof response.description !== 'string' || 
        typeof response.coreSubtaskCreation !== 'boolean' ||
        typeof response.diagram !== 'string') {
        console.error('AI response missing required fields or wrong types:', response);
        return null;
    }
    
    // Basic validation for mermaid diagram
    if (!response.diagram.trim()) {
        console.warn('Empty diagram provided');
        response.diagram = 'graph TD\n    A[SD CORE Enhancement] --> B[Technical Implementation]';
    }
    
    return response;
}

/**
 * Update SD CORE ticket with enhanced content
 * 
 * @param {string} ticketKey - The ticket key to update
 * @param {Object} enhancementData - Parsed enhancement data from AI
 * @returns {Object} Update result
 */
function updateSDCoreTicket(ticketKey, enhancementData) {
    const results = {
        descriptionUpdated: false,
        diagramUpdated: false,
        labelAdded: false,
        errors: []
    };
    
    try {
        // Update ticket description
        jira_update_description({
            key: ticketKey,
            description: enhancementData.description
        });
        results.descriptionUpdated = true;
        console.log('✅ Updated description for ' + ticketKey);
    } catch (error) {
        console.error('Failed to update description for ' + ticketKey + ':', error);
        results.errors.push('Description update failed: ' + error.toString());
    }
    
    try {
        // Update Diagrams field with mermaid diagram wrapped in code tags for better visualization
        const wrappedDiagram = '{code:mermaid}\n' + enhancementData.diagram + '\n{code}';
        jira_update_field({
            key: ticketKey,
            field: 'Diagrams',
            value: wrappedDiagram
        });
        results.diagramUpdated = true;
        console.log('✅ Updated Diagrams field for ' + ticketKey);
    } catch (error) {
        console.error('Failed to update Diagrams field for ' + ticketKey + ':', error);
        results.errors.push('Diagrams field update failed: ' + error.toString());
    }
    
    try {
        // Add implementation assessment label if needed
        if (enhancementData.coreSubtaskCreation) {
            jira_add_label({
                key: ticketKey,
                label: 'needs_core_implementation'
            });
            results.labelAdded = true;
            console.log('✅ Added needs_core_implementation label to ' + ticketKey);
        } else {
            console.log('ℹ️ No additional core implementation needed for ' + ticketKey);
        }
    } catch (error) {
        console.error('Failed to add implementation label to ' + ticketKey + ':', error);
        results.errors.push('Label addition failed: ' + error.toString());
    }
    
    return results;
}


function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const initiatorId = params.initiator;

        console.log("Processing SD CORE enhancement for ticket:", ticketKey);
        
        // Parse AI enhancement response
        const enhancementData = parseSDCoreEnhancementResponse(params.response);
        if (!enhancementData) {
            const errorMsg = 'Invalid AI response format for SD CORE enhancement';
            console.error(errorMsg);
            
            return {
                success: false,
                error: errorMsg
            };
        }

        // Update SD CORE ticket with enhanced content
        const updateResults = updateSDCoreTicket(ticketKey, enhancementData);

        // Use common assignForReview function for post-processing
        const assignResult = assignForReview(ticketKey, initiatorId);
        
        if (!assignResult.success) {
            return assignResult;
        }
        
        const successCount = (updateResults.descriptionUpdated ? 1 : 0) + 
                           (updateResults.diagramUpdated ? 1 : 0) + 
                           (updateResults.labelAdded ? 1 : 0);
        
        return {
            success: true,
            message: `Ticket ${ticketKey} enhanced, assigned, moved to In Review. Updates: ${successCount}/3 successful`,
            enhancementData: enhancementData,
            updateResults: updateResults
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        
        return {
            success: false,
            error: error.toString()
        };
    }
}
