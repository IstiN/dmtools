/**
 * Simple Assign For Review Action
 * Assigns ticket to initiator and moves to "In Review" status
 */

function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const initiatorId = params.initiator;
        
        console.log("Processing ticket:", ticketKey);
        
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
            message: `Ticket ${ticketKey} assigned and moved to In Review`
        };
        
    } catch (error) {
        console.error("❌ Error:", error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
