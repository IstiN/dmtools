/**
 * Simple JQL Modifier for Testing
 * Just appends a label filter to verify mechanism works
 */
function action(params) {
    const jql = params.existingTestCasesJql || "";
    const modifiedJql = jql + " and labels = ai_generated";

    console.log("Original JQL:", jql);
    console.log("Modified JQL:", modifiedJql);

    return { existingTestCasesJql: modifiedJql };
}
