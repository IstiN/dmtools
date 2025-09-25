/**
 * Create Questions and Assign For Review Action
 * Creates subtasks based on AI-generated questions and assigns parent ticket for review
 */

// Import common helper functions
const { assignForReview, extractTicketKey } = require('./common/jiraHelpers.js');
const { parseQuestionsResponse, buildSummary, buildDescription } = require('./common/aiResponseParser.js');

function processQuestionTickets(response, parentKey) {
    const questions = parseQuestionsResponse(response);
    if (questions.length === 0) {
        console.log('No follow-up questions detected, skipping ticket creation.');
        return [];
    }

    const projectKey = parentKey.split('-')[0];
    const createdTickets = [];

    questions.forEach(function(question, index) {
        const summary = buildSummary(question.summary, index);
        const description = buildDescription(question);
        try {
            // Create ticket with priority set directly
            const fieldsJson = {
                summary: summary,
                description: description,
                issuetype: { name: 'Subtask' },
                parent: { key: parentKey }
            };
            
            // Add priority if provided
            if (question.priority) {
                fieldsJson.priority = { name: question.priority };
            }
            
            const result = jira_create_ticket_with_json({
                project: projectKey,
                fieldsJson: fieldsJson
            });
            const createdKey = extractTicketKey(result);
            
            createdTickets.push({
                summary: summary,
                priority: question.priority,
                key: createdKey
            });
            console.log('Created question subtask ' + (createdKey || '(unknown key)') + ' with priority ' + (question.priority || 'default') + ' for summary "' + summary + '"');
        } catch (error) {
            console.error('Failed to create question subtask for summary "' + summary + '":', error);
            createdTickets.push({
                summary: summary,
                priority: question.priority,
                error: error.toString()
            });
        }
    });

    return createdTickets;
}

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const initiatorId = params.initiator;

        console.log("Processing ticket:", ticketKey);
        const createdQuestionTickets = processQuestionTickets(params.response, ticketKey);

        // Add AI-generated label
        jira_add_label({
            key: ticketKey,
            label: 'ai_questions_asked'
        });

        // Use common assignForReview function for post-processing
        const assignResult = assignForReview(ticketKey, initiatorId);
        
        if (!assignResult.success) {
            return assignResult;
        }
        
        return {
            success: true,
            message: `Ticket ${ticketKey} assigned, moved to In Review, created ${createdQuestionTickets.length} question subtasks`,
            createdQuestions: createdQuestionTickets
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
