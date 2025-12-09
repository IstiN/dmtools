/**
 * Common Azure DevOps Helper Functions
 * Shared utilities for ADO work item operations
 */

const { ADO_STATUSES, ADO_LABELS } = require('../config.js');

/**
 * Assign work item to initiator and move to "Resolved" state with AI-generated tag
 * This is the common post-processing logic used by multiple agents
 * 
 * @param {string} workItemId - The ADO work item ID
 * @param {string} initiatorEmail - Email of the person to assign the work item to
 * @param {string} wipLabel - Optional WIP label/tag to remove after processing
 * @returns {Object} Result object with success status and message
 */
function assignForReview(workItemId, initiatorEmail, wipLabel) {
    try {
        console.log("Processing work item:", workItemId);
        
        // Assign to initiator
        // Note: ADO requires email, not accountId like Jira
        ado_assign_work_item({
            id: workItemId,
            userEmail: initiatorEmail
        });
        
        // Move to Resolved state (indicating AI processing is complete and ready for review)
        ado_move_to_state({
            id: workItemId,
            state: ADO_STATUSES.RESOLVED
        });

        // Add AI-generated tag and remove WIP tag
        // Note: ADO uses tags instead of labels, semicolon-separated
        try {
            // Get current work item to read existing tags
            const workItemResult = ado_get_work_item({
                id: workItemId,
                fields: ['System.Tags']
            });
            
            // Parse result - could be string JSON or object
            let workItem = workItemResult;
            if (typeof workItemResult === 'string') {
                workItem = JSON.parse(workItemResult);
            }
            
            const currentTags = workItem && workItem.fields && workItem.fields['System.Tags'] 
                ? workItem.fields['System.Tags'] 
                : '';
            
            // Parse existing tags
            const tagArray = currentTags ? currentTags.split(';').map(t => t.trim()).filter(t => t) : [];
            
            // Add AI-generated tag if not already present
            if (!tagArray.includes(ADO_LABELS.AI_GENERATED)) {
                tagArray.push(ADO_LABELS.AI_GENERATED);
                console.log('Added AI-generated tag to work item ' + workItemId);
            }
            
            // Remove WIP tag if provided
            if (wipLabel) {
                const wipIndex = tagArray.indexOf(wipLabel);
                if (wipIndex > -1) {
                    tagArray.splice(wipIndex, 1);
                    console.log('Removed WIP tag "' + wipLabel + '" from work item ' + workItemId);
                }
            }
            
            // Update tags using the new MCP tool
            const tagsString = tagArray.join(';');
            ado_update_tags({
                id: workItemId,
                tags: tagsString
            });
            
            console.log('Updated tags on work item ' + workItemId + ': ' + tagsString);
            
        } catch (tagError) {
            console.warn('Failed to update tags:', tagError);
            // Don't fail the whole operation if tags fail
        }
        
        console.log("✅ Assigned to initiator and moved to Resolved");
        
        return {
            success: true,
            message: `Work item ${workItemId} assigned and moved to Resolved`
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
 * Extract work item ID from ADO API response
 * 
 * @param {string|Object} result - ADO API response
 * @returns {string|null} Extracted work item ID or null if not found
 */
function extractWorkItemId(result) {
    if (!result) {
        return null;
    }
    if (typeof result === 'string') {
        try {
            const parsed = JSON.parse(result);
            return parsed && parsed.id ? parsed.id.toString() : null;
        } catch (error) {
            return null;
        }
    }
    if (typeof result === 'object') {
        if (typeof result.id === 'number' || typeof result.id === 'string') {
            return result.id.toString();
        }
    }
    return null;
}

/**
 * Set priority on an ADO work item
 * 
 * @param {string} workItemId - The ADO work item ID
 * @param {number} priority - Priority number (1=Critical, 2=High, 3=Medium, 4=Low)
 * @returns {boolean} True if successful, false otherwise
 */
function setWorkItemPriority(workItemId, priority) {
    if (!workItemId || priority === undefined || priority === null) {
        return false;
    }
    
    try {
        ado_update_work_item({
            id: workItemId,
            params: {
                fields: {
                    'Microsoft.VSTS.Common.Priority': priority
                }
            }
        });
        console.log('Set priority ' + priority + ' on work item ' + workItemId);
        return true;
    } catch (priorityError) {
        console.error('Failed to set priority on work item ' + workItemId + ':', priorityError);
        return false;
    }
}

/**
 * Add a tag to an ADO work item
 * 
 * @param {string} workItemId - The ADO work item ID
 * @param {string} tag - Tag to add
 * @returns {boolean} True if successful, false otherwise
 */
function addTag(workItemId, tag) {
    if (!workItemId || !tag) {
        return false;
    }
    
    try {
        // Get current work item to read existing tags
        const workItem = ado_get_work_item({
            id: workItemId,
            fields: ['System.Tags']
        });
        
        const currentTags = workItem && workItem.fields && workItem.fields['System.Tags'] 
            ? workItem.fields['System.Tags'] 
            : '';
        
        // Parse existing tags
        const tagArray = currentTags ? currentTags.split(';').map(t => t.trim()).filter(t => t) : [];
        
        // Add new tag if not already present
        if (!tagArray.includes(tag)) {
            tagArray.push(tag);
            
            const tagsString = tagArray.join(';');
            ado_update_tags({
                id: workItemId,
                tags: tagsString
            });
            
            console.log('Added tag "' + tag + '" to work item ' + workItemId);
            return true;
        } else {
            console.log('Tag "' + tag + '" already exists on work item ' + workItemId);
            return true;
        }
    } catch (error) {
        console.error('Failed to add tag to work item ' + workItemId + ':', error);
        return false;
    }
}

/**
 * Remove a tag from an ADO work item
 * 
 * @param {string} workItemId - The ADO work item ID
 * @param {string} tag - Tag to remove
 * @returns {boolean} True if successful, false otherwise
 */
function removeTag(workItemId, tag) {
    if (!workItemId || !tag) {
        return false;
    }
    
    try {
        // Get current work item to read existing tags
        const workItem = ado_get_work_item({
            id: workItemId,
            fields: ['System.Tags']
        });
        
        const currentTags = workItem && workItem.fields && workItem.fields['System.Tags'] 
            ? workItem.fields['System.Tags'] 
            : '';
        
        // Parse existing tags
        const tagArray = currentTags ? currentTags.split(';').map(t => t.trim()).filter(t => t) : [];
        
        // Remove tag
        const filteredTags = tagArray.filter(t => t !== tag);
        
        if (filteredTags.length !== tagArray.length) {
            const tagsString = filteredTags.join(';');
            ado_update_tags({
                id: workItemId,
                tags: tagsString
            });
            
            console.log('Removed tag "' + tag + '" from work item ' + workItemId);
            return true;
        } else {
            console.log('Tag "' + tag + '" not found on work item ' + workItemId);
            return true;
        }
    } catch (error) {
        console.error('Failed to remove tag from work item ' + workItemId + ':', error);
        return false;
    }
}

// Export functions for use by other modules
module.exports = {
    assignForReview,
    extractWorkItemId,
    setWorkItemPriority,
    addTag,
    removeTag
};

