/**
 * Create Questions and Assign For Review Action (ADO version)
 * Creates child work items based on AI-generated questions and assigns parent work item for review
 */

// Import common helper functions
const { assignForReview, extractWorkItemId, addTag } = require('./common/adoHelpers.js');
const { parseQuestionsResponse, buildSummary, buildDescription } = require('./common/aiResponseParser.js');
const { ADO_LABELS } = require('./config.js');

/**
 * Process AI response and create question work items
 * @param {string|Array} response - AI response (JSON array of questions)
 * @param {string} parentId - Parent work item ID
 * @param {string} project - ADO project key
 * @returns {Array} Array of created work item objects
 */
function processQuestionWorkItems(response, parentId, project) {
    const questions = parseQuestionsResponse(response);
    if (questions.length === 0) {
        console.log('No follow-up questions detected, skipping work item creation.');
        return [];
    }

    const createdWorkItems = [];

    questions.forEach(function(question, index) {
        // Ensure question.summary is a string before processing
        let rawSummary = question.summary;
        if (Array.isArray(rawSummary)) {
            console.warn('question.summary is an array, converting to string:', rawSummary);
            rawSummary = rawSummary.join(' ');
        }
        if (typeof rawSummary !== 'string') {
            console.warn('question.summary is not a string, converting:', typeof rawSummary, rawSummary);
            rawSummary = String(rawSummary);
        }
        
        let summary = buildSummary(rawSummary, index);
        const description = buildDescription(question);
        
        // Double-check summary is a string after buildSummary
        if (Array.isArray(summary)) {
            console.warn('buildSummary returned an array, converting to string:', summary);
            summary = summary.join(' ');
        }
        if (typeof summary !== 'string') {
            console.warn('buildSummary returned non-string, converting:', typeof summary, summary);
            summary = String(summary);
        }
        
        console.log('Creating work item with title (length=' + summary.length + '):', summary.substring(0, 100));
        
        try {
            // Prepare additional fields for ADO work item creation
            const additionalFields = {};
            
            // Add priority if provided (ADO uses numeric priority: 1=highest, 2=high, 3=medium, 4=low)
            if (question.priority) {
                additionalFields["Microsoft.VSTS.Common.Priority"] = question.priority;
            }
            
            // Create work item with title and description as separate parameters
            // Note: ado_create_work_item requires title and description as separate params
            const result = ado_create_work_item({
                project: project,
                workItemType: "Task",
                title: summary,
                description: description,
                fieldsJson: additionalFields  // Additional fields like priority
            });
            
            const createdId = extractWorkItemId(result);
            
            if (createdId) {
                // Set parent relationship using work item link
                // Note: sourceId is child (created task), targetId is parent (story)
                // For child -> parent link, we use "parent" relationship type
                try {
                    ado_link_work_items({
                        sourceId: createdId,
                        targetId: parentId,
                        relationship: "parent" // Child -> Parent link (maps to System.LinkTypes.Hierarchy-Reverse)
                    });
                    console.log('Linked work item ' + createdId + ' to parent ' + parentId);
                } catch (linkError) {
                    console.warn('Failed to link work item ' + createdId + ' to parent ' + parentId + ':', linkError);
                }
                
                createdWorkItems.push({
                    summary: summary,
                    priority: question.priority,
                    id: createdId
                });
                console.log('Created question work item ' + createdId + ' with priority ' + (question.priority || 'default') + ' for summary "' + summary + '"');
            }
        } catch (error) {
            console.error('Failed to create question work item for summary "' + summary + '":', error);
            createdWorkItems.push({
                summary: summary,
                priority: question.priority,
                error: error.toString()
            });
        }
    });

    return createdWorkItems;
}

/**
 * Main action function
 * @param {Object} params - Action parameters from Teammate
 * @returns {Object} Result object with success status
 */
function action(params) {
    try {
        // Extract work item ID from ticket
        const workItemId = String(params.ticket.id || params.ticket.key || '');
        const initiatorEmail = params.initiatorEmail || params.initiator;
        
        // Get project name from ticket's getProject() method or fallback to field access
        let project = null;
        
        // Try calling the getProject() method if available (WorkItem has this method)
        if (typeof params.ticket.getProject === 'function') {
            project = params.ticket.getProject();
        }
        
        // Fallback: try getting from fields directly
        if (!project && params.ticket.fields) {
            project = params.ticket.fields["System.TeamProject"] || 
                     params.ticket.fields["System.AreaPath"]?.split('\\')[0];
        }
        
        // Last resort: extract from URL
        if (!project && params.ticket.url) {
            const urlMatch = params.ticket.url.match(/\/([^\/]+)\/_apis\/wit\/workitems/);
            if (urlMatch) {
                project = urlMatch[1];
            }
        }
        
        // Dynamically generate WIP tag from contextId
        const wipTag = params.metadata && params.metadata.contextId 
            ? params.metadata.contextId + '_wip' 
            : null;

        console.log("Processing work item:", workItemId);
        console.log("Project:", project);
        console.log("Initiator:", initiatorEmail);
        
        if (!workItemId || workItemId === '') {
            console.error("Work item ID is missing");
            return {
                success: false,
                error: "Work item ID not available"
            };
        }
        
        if (!project) {
            console.error("Unable to determine project from work item");
            // Log available ticket properties for debugging
            console.error("Available ticket properties:", Object.keys(params.ticket).join(', '));
            return {
                success: false,
                error: "Project information not available. Please ensure work item has System.TeamProject or System.AreaPath field."
            };
        }
        
        const createdQuestionWorkItems = processQuestionWorkItems(params.response, workItemId, project);

        // Add AI-questions-asked tag
        try {
            addTag(workItemId, ADO_LABELS.AI_QUESTIONS_ASKED);
            console.log('Added AI questions asked tag to work item ' + workItemId);
        } catch (tagError) {
            console.warn('Failed to add AI questions asked tag:', tagError);
        }

        // Use common assignForReview function for post-processing
        const assignResult = assignForReview(workItemId, initiatorEmail, wipTag);
        
        if (!assignResult.success) {
            return assignResult;
        }
        
        return {
            success: true,
            message: `Work item ${workItemId} assigned, moved to Resolved, created ${createdQuestionWorkItems.length} question work items`,
            createdQuestions: createdQuestionWorkItems
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

