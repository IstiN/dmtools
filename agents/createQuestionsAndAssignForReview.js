/**
 * Simple Assign For Review Action
 * Assigns ticket to initiator and moves to "In Review" status
 */

const ALLOWED_PRIORITIES = ['Highest', 'High', 'Medium', 'Low', 'Lowest'];

function normalizePriority(priority) {
    if (!priority) {
        return 'Medium';
    }
    const normalized = String(priority).trim().toLowerCase();
    const matched = ALLOWED_PRIORITIES.find(value => value.toLowerCase() === normalized);
    return matched || 'Medium';
}

function parseQuestionsResponse(response) {
    if (response == null) {
        return [];
    }

    let raw = response;
    if (typeof raw === 'string') {
        raw = raw.trim();
        if (!raw) {
            return [];
        }
        try {
            raw = JSON.parse(raw);
        } catch (error) {
            console.error('Failed to parse response JSON:', error);
            return [];
        }
    }

    if (raw && !Array.isArray(raw) && Array.isArray(raw.questions)) {
        raw = raw.questions;
    }

    if (!Array.isArray(raw)) {
        console.warn('Response is not an array, skipping question ticket creation');
        return [];
    }

    return raw
        .map((item, index) => {
            if (!item || typeof item !== 'object') {
                console.warn('Question item at index ' + index + ' is not an object, skipping');
                return null;
            }
            const summary = typeof item.summary === 'string' ? item.summary.trim() : '';
            const description = typeof item.description === 'string' ? item.description.trim() : '';
            const priority = normalizePriority(item.priority);
            return {
                summary: summary,
                description: description,
                priority: priority
            };
        })
        .filter(Boolean);
}

function buildSummary(summary, index) {
    const fallback = 'Follow-up question #' + (index + 1);
    const candidate = summary && summary.length > 0 ? summary : fallback;
    if (candidate.length <= 120) {
        return candidate;
    }
    return candidate.slice(0, 117).trim() + '...';
}

function buildDescription(question) {
    const parts = [];
    if (question.priority) {
        parts.push('*Priority*: ' + question.priority);
    }
    if (question.description && question.description.length > 0) {
        parts.push(question.description);
    } else {
        parts.push('Follow template from instructions.');
    }
    return parts.join('\n\n');
}

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
            const result = jira_create_ticket_with_parent({
                project: projectKey,
                issueType: 'Subtask',
                summary: summary,
                description: description,
                parentKey: parentKey
            });
            const createdKey = extractTicketKey(result);
            
            // Set priority on the created ticket
            if (createdKey && question.priority) {
                try {
                    jira_set_priority({
                        key: createdKey,
                        priority: question.priority
                    });
                    console.log('Set priority ' + question.priority + ' on ticket ' + createdKey);
                } catch (priorityError) {
                    console.error('Failed to set priority on ticket ' + createdKey + ':', priorityError);
                }
            }
            
            createdTickets.push({
                summary: summary,
                priority: question.priority,
                key: createdKey
            });
            console.log('Created question subtask ' + (createdKey || '(unknown key)') + ' for summary "' + summary + '"');
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

        // Assign to initiator
        //initiatorId
        //userName: "uladzimir.klyshevich@gmail.com"
        jira_assign_ticket_to({
            key: ticketKey,
            accountId: initiatorId
        });
        
        // Move to In Review status
        jira_move_to_status({
            key: ticketKey,
            statusName: "In Review"
        });

        jira_add_label({
            key: ticketKey,
            label: 'ai_generated'
        });
        
        console.log("✅ Assigned to initiator and moved to In Review");
        
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
