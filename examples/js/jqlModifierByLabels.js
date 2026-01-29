/**
 * Dynamic JQL Modifier - Filter Test Cases by Ticket Labels
 * Modifies existingTestCasesJql based on the current story ticket's labels
 */
function action(params) {
    try {
        console.log("=== JQL Modifier: Filter by Labels ===");

        const ticket = params.ticket || {};
        const jobParams = params.jobParams || {};
        let jql = params.existingTestCasesJql || jobParams.existingTestCasesJql;

        if (!jql) {
            console.warn("No existingTestCasesJql provided");
            return { existingTestCasesJql: jql };
        }

        console.log("Original JQL:", jql);
        console.log("Ticket key:", ticket.key);
        console.log("Ticket labels:", JSON.stringify(ticket.labels));

        const labels = ticket.labels || [];

        // If ticket has "web" label, exclude mobile test cases
        if (labels.includes("web")) {
            console.log("✓ Ticket has 'web' label, excluding mobile test cases");
            jql += " and labels not in (mobile)";
        }

        // If ticket has "mobile" label, exclude web test cases
        if (labels.includes("mobile")) {
            console.log("✓ Ticket has 'mobile' label, excluding web test cases");
            jql += " and labels not in (web)";
        }

        // If ticket has "api" label, only include API test cases
        if (labels.includes("api")) {
            console.log("✓ Ticket has 'api' label, filtering for API test cases");
            jql += " and labels in (api)";
        }

        console.log("Modified JQL:", jql);

        return { existingTestCasesJql: jql };

    } catch (error) {
        console.error("Error in JQL modifier:", error);
        return { existingTestCasesJql: params.existingTestCasesJql };
    }
}
