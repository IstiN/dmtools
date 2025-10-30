/**
 * Create Solution Design Tickets and Assign For Review Action
 * Creates solution design subtasks based on AI module analysis and assigns parent ticket for review
 */

// Import common helper functions
const { assignForReview, extractTicketKey } = require('./common/jiraHelpers.js');
const { ISSUE_TYPES, PRIORITIES, LABELS, SOLUTION_DESIGN_MODULES } = require('./config.js');

/**
 * Parse AI response for module analysis
 * Expects JSON object with core, api, ui boolean flags and description
 * 
 * @param {Object|string} response - AI response (should be JSON object)
 * @returns {Object} Parsed module analysis or null if invalid
 */
function parseModuleAnalysisResponse(response) {
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
    if (typeof response.core !== 'boolean' || 
        typeof response.api !== 'boolean' || 
        typeof response.ui !== 'boolean' ||
        typeof response.description !== 'string') {
        console.error('AI response missing required fields or wrong types:', response);
        return null;
    }
    
    return response;
}

/**
 * Create solution design subtasks based on module analysis
 * 
 * @param {Object} moduleAnalysis - Parsed module analysis from AI
 * @param {string} parentKey - Parent ticket key
 * @param {string} parentSummary - Parent ticket summary for naming
 * @returns {Array} Array of created ticket information
 */
function createSolutionDesignTickets(moduleAnalysis, parentKey, parentSummary) {
    const projectKey = parentKey.split('-')[0];
    const createdTickets = [];
    
    SOLUTION_DESIGN_MODULES.forEach(function(module) {
        if (moduleAnalysis[module.flag]) {
            const summary = module.prefix + ' ' + parentSummary;
            const description = 'Details are in [' + parentKey + '|https://dmtools.atlassian.net/browse/' + parentKey + '|smart-link] \n\n' +
                               '*Module Analysis:* ' + moduleAnalysis.description;
            
            try {
                // Create subtask using the dedicated parent method
                const result = jira_create_ticket_with_parent({
                    project: projectKey,
                    issueType: ISSUE_TYPES.SUBTASK,
                    summary: summary,
                    description: description,
                    parentKey: parentKey
                });
                
                const createdKey = extractTicketKey(result);
                
                if (createdKey) {
                    // Set priority using dedicated method
                    try {
                        jira_set_priority({
                            key: createdKey,
                            priority: PRIORITIES.MEDIUM
                        });
                    } catch (priorityError) {
                        console.warn('Failed to set priority on ' + createdKey + ':', priorityError);
                    }
                    
                    // Add module-specific label
                    try {
                        jira_add_label({
                            key: createdKey,
                            label: module.label
                        });
                    } catch (labelError) {
                        console.warn('Failed to add label ' + module.label + ' to ' + createdKey + ':', labelError);
                    }
                }
                
                createdTickets.push({
                    module: module.flag,
                    summary: summary,
                    key: createdKey,
                    success: true
                });
                
                console.log('Created ' + module.prefix + ' subtask: ' + (createdKey || '(unknown key)'));
                
            } catch (error) {
                console.error('Failed to create ' + module.prefix + ' subtask:', error);
                createdTickets.push({
                    module: module.flag,
                    summary: summary,
                    error: error.toString(),
                    success: false
                });
            }
        }
    });
    
    return createdTickets;
}

/**
 * Post a summary comment to the parent ticket
 * 
 * @param {string} parentKey - Parent ticket key
 * @param {Object} moduleAnalysis - Module analysis results
 * @param {Array} createdTickets - Array of created tickets
 */
function postSummaryComment(parentKey, moduleAnalysis, createdTickets) {
    try {
        const successfulTickets = createdTickets.filter(function(ticket) { return ticket.success; });
        const failedTickets = createdTickets.filter(function(ticket) { return !ticket.success; });
        
        let comment = 'h3. *Solution Design Analysis Results*\n\n';
        comment += '*Module Analysis:* ' + moduleAnalysis.description + '\n\n';
        comment += '*Modules Requiring Implementation:*\n';
        comment += '* *Core:* ' + (moduleAnalysis.core ? 'Yes' : 'No') + '\n';
        comment += '* *API:* ' + (moduleAnalysis.api ? 'Yes' : 'No') + '\n';
        comment += '* *UI:* ' + (moduleAnalysis.ui ? 'Yes' : 'No') + '\n\n';
        
        if (successfulTickets.length > 0) {
            comment += '*Created Solution Design Tickets:*\n';
            successfulTickets.forEach(function(ticket) {
                comment += '* [' + ticket.key + '|https://dmtools.atlassian.net/browse/' + ticket.key + '] - ' + ticket.summary + '\n';
            });
            comment += '\n';
        }
        
        if (failedTickets.length > 0) {
            comment += '*Failed to Create:*\n';
            failedTickets.forEach(function(ticket) {
                comment += '* ' + ticket.summary + ' - Error: ' + ticket.error + '\n';
            });
            comment += '\n';
        }
        
        comment += '*Total Created:* ' + successfulTickets.length + ' solution design tickets';
        
        jira_post_comment({
            key: parentKey,
            comment: comment
        });
        
        console.log('Posted summary comment to ' + parentKey);
        
    } catch (error) {
        console.error('Failed to post summary comment:', error);
    }
}

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const ticketSummary = params.ticket.fields.summary;
        const initiatorId = params.initiator;
        // Dynamically generate WIP label from contextId
        const wipLabel = params.metadata && params.metadata.contextId 
            ? params.metadata.contextId + '_wip' 
            : null;

        console.log("Processing solution design creation for ticket:", ticketKey);
        
        // Parse AI module analysis response
        const moduleAnalysis = parseModuleAnalysisResponse(params.response);
        if (!moduleAnalysis) {
            const errorMsg = 'Invalid AI response format for module analysis';
            console.error(errorMsg);
            
            // Post error comment to ticket
            try {
                jira_post_comment({
                    key: ticketKey,
                    comment: '*Error:* ' + errorMsg + '. Please check logs for details and retry the workflow.'
                });
            } catch (commentError) {
                console.error('Failed to post error comment:', commentError);
            }
            
            return {
                success: false,
                error: errorMsg
            };
        }

        // Create solution design tickets based on analysis
        const createdTickets = createSolutionDesignTickets(moduleAnalysis, ticketKey, ticketSummary);
        
        // Post summary comment with analysis results
        postSummaryComment(ticketKey, moduleAnalysis, createdTickets);

        // Add solution design label
        try {
            jira_add_label({
                key: ticketKey,
                label: LABELS.AI_SOLUTION_DESIGN_CREATED
            });
        } catch (labelError) {
            console.warn('Failed to add ai_solution_design_created label:', labelError);
        }

        // Use common assignForReview function for post-processing
        const assignResult = assignForReview(ticketKey, initiatorId, wipLabel);
        
        if (!assignResult.success) {
            return assignResult;
        }
        
        const successfulTickets = createdTickets.filter(function(ticket) { return ticket.success; });
        
        return {
            success: true,
            message: `Ticket ${ticketKey} assigned, moved to In Review, created ${successfulTickets.length} solution design subtasks`,
            moduleAnalysis: moduleAnalysis,
            createdTickets: createdTickets
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        
        // Try to post error comment to ticket
        try {
            if (params && params.ticket && params.ticket.key) {
                jira_post_comment({
                    key: params.ticket.key,
                    comment: '*Workflow Error:* ' + error.toString() + '. Please check server logs for details.'
                });
            }
        } catch (commentError) {
            console.error('Failed to post error comment:', commentError);
        }
        
        return {
            success: false,
            error: error.toString()
        };
    }
}

