// Example: Processing raw CSV data to generate insights for the orchestrator
function generatePresentationJs(paramsForJs, javaClient) {
    try {
        javaClient.jsLogInfo("[CsvAnalysis] Starting presentation generation from CSV data.");

        // Helper function to process the CSV data and aggregate insights
        function reportPreparationFromCsv() {
            const csvData = `"ID","Start Time","End Time","Respondent","Email","Overall Score","Product Quality","User Experience","Pricing","Support","Recommendation","What did you like most?","Suggestions for improvement"
"1","3/15/2024 09:23","3/15/2024 09:28","anonymous",,"6","Very satisfied","Neither satisfied nor dissatisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Very satisfied","The interface is intuitive and the core features work well, though some advanced options could be more accessible. Great to see active development.",
"2","3/15/2024 09:27","3/15/2024 09:31","anonymous",,"7","Somewhat satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Neither satisfied nor dissatisfied","Somewhat satisfied","transparent communication from the team","we're already beta testing this feature"
"3","3/15/2024 09:23","3/15/2024 09:33","anonymous",,"7","Neither satisfied nor dissatisfied","Somewhat satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Somewhat satisfied","Good feature set for target users, responsive customer service","The product team is great, but not all key stakeholders from enterprise clients are involved in feedback sessions."
"4","3/15/2024 09:23","3/15/2024 09:34","anonymous",,"8","Very satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Somewhat dissatisfied","Very satisfied","Clean design, logical workflow, excellent presentation of data.","Focus on user engagement rather than just feature announcements. This should be a user community meeting, not a product demo. Could encourage more diverse participation."
"5","3/15/2024 09:35","3/15/2024 09:36","anonymous",,"6","Very satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Neither satisfied nor dissatisfied","Somewhat satisfied",,
"6","3/15/2024 09:35","3/15/2024 09:38","anonymous",,"8","Somewhat satisfied","Somewhat satisfied","Very satisfied","Somewhat dissatisfied","Very satisfied","Integration with existing tools",
"7","3/15/2024 09:29","3/15/2024 09:38","anonymous",,"8","Very satisfied","Somewhat satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Very satisfied",,
"8","3/15/2024 09:34","3/15/2024 09:38","anonymous",,"8","Very satisfied","Very satisfied","Very satisfied","Very satisfied","Very satisfied","Regular product updates and roadmap transparency","Enable offline mode functionality."
"9","3/15/2024 13:33","3/15/2024 13:35","anonymous",,"5","Neither satisfied nor dissatisfied","Somewhat dissatisfied","Neither satisfied nor dissatisfied","Somewhat dissatisfied","Somewhat satisfied",,"Better quality assurance before releases"
"10","3/16/2024 10:16","3/16/2024 10:23","anonymous",,"7","Somewhat satisfied","Somewhat satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Somewhat satisfied","Creates a sense of being part of an innovative ecosystem, with shared challenges and opportunities.","Would be valuable to hear from power users about their workflow solutions, and include product management representatives"
"11","3/16/2024 11:12","3/16/2024 11:23","anonymous",,"7","Very satisfied","Somewhat satisfied","Somewhat satisfied","Neither satisfied nor dissatisfied","Very satisfied",,
"12","3/28/2024 15:42","3/28/2024 15:44","anonymous",,"8","Very satisfied","Very satisfied","Very satisfied","Very satisfied","Very satisfied",,
"13","3/28/2024 17:03","3/28/2024 17:05","anonymous",,"7","Very satisfied","Very satisfied","Very satisfied","Somewhat satisfied","Somewhat satisfied","Access to comprehensive product updates","Would like more details on performance metrics and market adoption rates"
"14","3/28/2024 17:13","3/28/2024 17:13","anonymous",,"7","Very satisfied","Somewhat satisfied","Somewhat satisfied","Somewhat satisfied","Very satisfied",,
`;
            
            // A more robust CSV parsing function
            function parseCsvLine(line) {
                const result = [];
                let current = '';
                let inQuote = false;
                for (let i = 0; i < line.length; i++) {
                    const char = line[i];
                    if (char === '"') {
                        inQuote = !inQuote;
                    } else if (char === ',' && !inQuote) {
                        result.push(current);
                        current = '';
                    } else {
                        current += char;
                    }
                }
                result.push(current);
                return result;
            }

            const lines = csvData.trim().split('\n');
            const headers = parseCsvLine(lines[0]).map(h => h.replace(/"/g, ''));
            const likedMostIndex = headers.indexOf('What did you like most?');
            const suggestionsIndex = headers.indexOf('Suggestions for improvement');

            let positiveMoments = [];
            let recommendations = [];

            for (let i = 1; i < lines.length; i++) {
                const values = parseCsvLine(lines[i]);
                
                if (values[likedMostIndex]) {
                    const liked = values[likedMostIndex].replace(/"/g, '').trim();
                    if (liked) positiveMoments.push(liked);
                }

                if (values[suggestionsIndex]) {
                    const suggestion = values[suggestionsIndex].replace(/"/g, '').trim();
                    if (suggestion) recommendations.push(suggestion);
                }
            }

            let report = "## Positive Feedback Highlights\n";
            report += positiveMoments.map(p => `- ${p}`).join('\n');
            report += "\n\n## Key Improvement Suggestions\n";
            report += recommendations.map(r => `- ${r}`).join('\n');
            
            return report;
        }

        const aggregatedData = reportPreparationFromCsv();
        javaClient.jsLogInfo("[CsvAnalysis] CSV data processed. Report length: " + aggregatedData.length);
        
        const orchestratorParams = {
            topic: "Analysis of User Feedback Survey",
            audience: "Product and UX Teams",
            presenterName: "Feedback Analysis Bot",
            requestDataList: [
                {
                    userRequest: "Create two slides with insights: one summarizing the positive moments from user feedback, and another showing the most popular recommendations for improvement.",
                    additionalData: aggregatedData
                }
            ]
        };

        const orchestratorParamsJson = JSON.stringify(orchestratorParams);
        javaClient.jsLogInfo("[CsvAnalysis] Calling orchestrator with aggregated CSV data.");
        
        const presentationJsonString = javaClient.runPresentationOrchestrator(orchestratorParamsJson);
        javaClient.jsLogInfo("[CsvAnalysis] Received presentation from orchestrator.");
        
        return JSON.parse(presentationJsonString);

    } catch (e) {
        javaClient.jsLogErrorWithException("[CsvAnalysis] Error: " + e.message, e.stack);
        return { error: true, message: e.message, details: { stack: e.stack } };
    }
} 