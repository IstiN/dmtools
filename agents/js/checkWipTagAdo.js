/**
 * Check WIP Tag Pre-Action (ADO version)
 * Checks if work item has a work-in-progress tag and stops processing if found
 * Returns false to stop processing, true to continue
 */

/**
 * Pre-action function to check for WIP tag
 * 
 * @param {Object} params - Parameters from Teammate job
 * @param {Object} params.ticket - ADO work item object
 * @param {Object} params.metadata - Job metadata containing contextId
 * @returns {boolean} false to stop processing, true to continue
 */
function action(params) {
    try {
        const workItem = params.ticket;
        const metadata = params.metadata;
        
        if (!workItem || !metadata || !metadata.contextId) {
            console.log('No contextId in metadata, continuing with processing');
            return true;
        }
        
        // Dynamically generate WIP tag from contextId
        const wipTag = metadata.contextId + '_wip';
        const workItemId = workItem.id || workItem.key;
        
        // Get work item tags (ADO uses semicolon-separated tags in System.Tags field)
        const tagsString = workItem.fields && workItem.fields['System.Tags'] 
            ? workItem.fields['System.Tags'] 
            : '';
        const tags = tagsString ? tagsString.split(';').map(t => t.trim()).filter(t => t) : [];
        
        // Check if WIP tag exists
        if (tags.includes(wipTag)) {
            console.log('⏸️  Work item ' + workItemId + ' has WIP tag "' + wipTag + '" - skipping processing');
            
            // Post comment to work item explaining why it was skipped
            try {
                ado_post_comment({
                    id: workItemId,
                    comment: '<h3><strong>Processing Skipped</strong></h3>\n\n' +
                            '<p>This work item has the <strong>' + wipTag + '</strong> tag indicating work is in progress.</p>\n' +
                            '<p>Processing will be skipped until the tag is removed.</p>\n\n' +
                            '<p><em>Remove the tag to allow automated processing.</em></p>'
                });
                console.log('Posted skip notification comment to work item ' + workItemId);
            } catch (commentError) {
                console.warn('Failed to post skip comment:', commentError);
            }
            
            return false; // Stop processing
        }
        
        console.log('✅ Work item ' + workItemId + ' does not have WIP tag "' + wipTag + '" - continuing with processing');
        return true; // Continue processing
        
    } catch (error) {
        console.error('❌ Error in WIP tag check:', error);
        // On error, continue processing to avoid blocking legitimate workflows
        console.warn('Continuing with processing despite error in WIP check');
        return true;
    }
}

