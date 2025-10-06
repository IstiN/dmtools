/**
 * Check WIP Label Pre-Action
 * Checks if ticket has a work-in-progress label and stops processing if found
 * Returns false to stop processing, true to continue
 */

/**
 * Pre-action function to check for WIP label
 * 
 * @param {Object} params - Parameters from Teammate job
 * @param {Object} params.ticket - Jira ticket object
 * @param {Object} params.metadata - Job metadata containing contextId
 * @returns {boolean} false to stop processing, true to continue
 */
function action(params) {
    try {
        const ticket = params.ticket;
        const metadata = params.metadata;
        
        if (!ticket || !metadata || !metadata.contextId) {
            console.log('No contextId in metadata, continuing with processing');
            return true;
        }
        
        // Dynamically generate WIP label from contextId
        const wipLabel = metadata.contextId + '_wip';
        const ticketKey = ticket.key;
        
        // Get ticket labels
        const labels = ticket.fields && ticket.fields.labels ? ticket.fields.labels : [];
        
        // Check if WIP label exists
        if (labels.includes(wipLabel)) {
            console.log('⏸️  Ticket ' + ticketKey + ' has WIP label "' + wipLabel + '" - skipping processing');
            
            // Post comment to ticket explaining why it was skipped
            try {
                jira_post_comment({
                    key: ticketKey,
                    comment: 'h3. *Processing Skipped*\n\n' +
                            'This ticket has the *' + wipLabel + '* label indicating work is in progress.\n' +
                            'Processing will be skipped until the label is removed.\n\n' +
                            '_Remove the label to allow automated processing._'
                });
                console.log('Posted skip notification comment to ' + ticketKey);
            } catch (commentError) {
                console.warn('Failed to post skip comment:', commentError);
            }
            
            return false; // Stop processing
        }
        
        console.log('✅ Ticket ' + ticketKey + ' does not have WIP label "' + wipLabel + '" - continuing with processing');
        return true; // Continue processing
        
    } catch (error) {
        console.error('❌ Error in WIP label check:', error);
        // On error, continue processing to avoid blocking legitimate workflows
        console.warn('Continuing with processing despite error in WIP check');
        return true;
    }
}

