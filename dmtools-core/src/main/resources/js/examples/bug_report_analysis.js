// Example: Generating a presentation based on an open bugs report
function generatePresentationJs(paramsForJs, javaClient) {
    try {
        javaClient.jsLogInfo("[BugAnalysis] Starting presentation generation for open bugs report.");
        const parsedParams = JSON.parse(paramsForJs);
        const projectKey = parsedParams.projectKey || "PROJECT_X"; // Default if not provided, also can be hardcoded in script

        // Step 1: Define parameters for the bug report
        const trackerClient = javaClient.getTrackerClientInstance();
        const reportConfig = JSON.stringify({ reportName: `Open Bugs Report for ${projectKey}` });
        const openedBugsJQL = `project = ${projectKey} and issueType in (Bug) and status not in ('ready for testing', 'review', 'testing', 'accepted', 'completed')`;
        const startDate = "-90d"; // Look back period for the report context
        const timelinePeriod = null;
        const usePeriodForTimeline = false;
        const reportTypes = JSON.stringify(['CREATED_OVERVIEW', 'CREATED_DETAILS', 'CREATED_TIMELINE_BY_PRIORITY']);

        // Step 2: Generate the bug report data
        const bugReportHtml = javaClient.generateBugsReportWithTypes(
            trackerClient,
            reportConfig,
            openedBugsJQL,
            startDate,
            timelinePeriod,
            usePeriodForTimeline,
            reportTypes
        );
        javaClient.jsLogInfo("[BugAnalysis] Bug report data generated successfully.");

        // Step 3: Prepare the parameters for the presentation orchestrator
        const orchestratorParams = {
            topic: `Analysis of Currently Open Bugs in ${projectKey}`,
            audience: "Development Team and Project Managers",
            presenterName: "Bug Analysis Bot",
            requestDataList: [
                {
                    userRequest: "Based on the provided open bugs report, create a presentation summarizing the current situation. Include key metrics, trends, and a breakdown of bugs by priority and status.",
                    additionalData: bugReportHtml
                }
            ]
        };

        // Step 4: Call the presentation orchestrator
        const orchestratorResultJson = javaClient.runPresentationOrchestrator(JSON.stringify(orchestratorParams));
        javaClient.jsLogInfo("[BugAnalysis] Presentation orchestrator called successfully.");

        // Step 5: Return the final presentation JSON
        return JSON.parse(orchestratorResultJson);

    } catch (e) {
        javaClient.jsLogError("[BugAnalysis] An error occurred: " + e.message + "\\nStack: " + e.stack);
        // Return a structured error JSON
        return {
            error: "A critical error occurred during script execution.",
            message: e.message,
            stack: e.stack
        };
    }
} 