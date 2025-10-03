/**
 * File Reading Examples for DMTools JavaScript Post-Processing Functions
 * 
 * This file demonstrates how to use the file_read() function to access files
 * generated during job execution (e.g., CLI command outputs, AI responses).
 * 
 * The file_read() function is available in both Expert and Teammate jobs
 * through the JobJavaScriptBridge MCP tools infrastructure.
 */

// ============================================================================
// Example 1: Reading CLI Command Output
// ============================================================================
// Use case: Read the main response from cursor-agent or aider CLI execution
function readCliOutput(params) {
    // Read the main CLI response file
    var mainResponse = file_read("outputs/response.md");
    
    if (mainResponse === null) {
        console.log("Warning: CLI output file not found");
        return "No response available";
    }
    
    console.log("Successfully read CLI output: " + mainResponse.length + " characters");
    return mainResponse;
}

// ============================================================================
// Example 2: Reading Job Input Request
// ============================================================================
// Use case: Access the original request sent to the AI/CLI tool
function readJobRequest(params) {
    // Construct path to input request file using ticket key
    var ticketKey = params.ticket.key;
    var requestPath = "input/" + ticketKey + "/request.md";
    
    var requestContent = file_read(requestPath);
    
    if (requestContent === null) {
        console.log("Error: Could not read request file for " + ticketKey);
        return null;
    }
    
    return requestContent;
}

// ============================================================================
// Example 3: Reading and Parsing JSON Analysis
// ============================================================================
// Use case: Parse JSON analysis file to make intelligent decisions
function analyzeJsonResults(params) {
    // Read JSON analysis file
    var analysisJson = file_read("outputs/analysis.json");
    
    if (analysisJson === null) {
        console.log("No analysis file found");
        return { success: false, message: "Analysis file not available" };
    }
    
    // Parse JSON content
    try {
        var analysis = JSON.parse(analysisJson);
        
        // Make decisions based on analysis
        if (analysis.riskLevel === "high") {
            console.log("High risk detected - updating ticket priority");
            
            // Update Jira ticket priority
            jira_update_field({
                key: params.ticket.key,
                field: "priority",
                value: { name: "Critical" }
            });
        }
        
        if (analysis.issues && analysis.issues.length > 0) {
            // Post comment with issues found
            var issuesList = analysis.issues.map(function(issue, index) {
                return (index + 1) + ". " + issue;
            }).join("\n");
            
            jira_post_comment({
                key: params.ticket.key,
                comment: "h3. Issues Found\n\n" + issuesList
            });
        }
        
        return {
            success: true,
            riskLevel: analysis.riskLevel,
            issueCount: analysis.issues ? analysis.issues.length : 0
        };
        
    } catch (error) {
        console.log("Error parsing JSON: " + error);
        return { success: false, error: "Failed to parse analysis JSON" };
    }
}

// ============================================================================
// Example 4: Cross-Validation Between Multiple Files
// ============================================================================
// Use case: Compare multiple generated files for consistency
function crossValidateOutputs(params) {
    // Read multiple output files
    var mainResponse = file_read("outputs/response.md");
    var validationReport = file_read("outputs/validation.txt");
    var testResults = file_read("outputs/test-results.json");
    
    // Graceful null handling
    if (mainResponse === null || validationReport === null || testResults === null) {
        console.log("Warning: Some output files are missing");
        return {
            status: "incomplete",
            message: "Not all output files were generated"
        };
    }
    
    // Parse test results
    var tests = JSON.parse(testResults);
    
    // Check if validation passed
    var validationPassed = validationReport.indexOf("PASSED") >= 0;
    var allTestsPassed = tests.passed === tests.total;
    
    // Cross-validate results
    if (validationPassed && allTestsPassed) {
        // Move ticket to Done
        jira_move_to_status({
            key: params.ticket.key,
            statusName: "Done"
        });
        
        return {
            status: "success",
            validation: "passed",
            tests: tests.passed + "/" + tests.total
        };
    } else {
        // Keep ticket in progress with details
        var comment = "h3. Validation Results\n\n" +
                     "* Validation: " + (validationPassed ? "PASSED" : "FAILED") + "\n" +
                     "* Tests: " + tests.passed + "/" + tests.total + " passed\n\n" +
                     "See attached files for details.";
        
        jira_post_comment({
            key: params.ticket.key,
            comment: comment
        });
        
        return {
            status: "failed",
            validation: validationPassed ? "passed" : "failed",
            tests: tests.passed + "/" + tests.total
        };
    }
}

// ============================================================================
// Example 5: Aggregating Information from Multiple Files
// ============================================================================
// Use case: Combine data from multiple files into a single report
function aggregateResults(params) {
    // Read all relevant files
    var cliOutput = file_read("outputs/response.md");
    var metrics = file_read("outputs/metrics.json");
    var changelog = file_read("outputs/changelog.md");
    
    // Build aggregated report
    var report = "h2. Automated Processing Results\n\n";
    
    if (cliOutput !== null) {
        report += "h3. CLI Output\n\n{code}\n" + 
                 cliOutput.substring(0, Math.min(500, cliOutput.length)) + 
                 "\n{code}\n\n";
    }
    
    if (metrics !== null) {
        try {
            var metricsData = JSON.parse(metrics);
            report += "h3. Metrics\n\n";
            report += "* Files Changed: " + metricsData.filesChanged + "\n";
            report += "* Lines Added: " + metricsData.linesAdded + "\n";
            report += "* Lines Removed: " + metricsData.linesRemoved + "\n\n";
        } catch (error) {
            console.log("Could not parse metrics: " + error);
        }
    }
    
    if (changelog !== null) {
        report += "h3. Changelog\n\n" + changelog + "\n\n";
    }
    
    // Post aggregated report as comment
    jira_post_comment({
        key: params.ticket.key,
        comment: report
    });
    
    return { success: true, reportLength: report.length };
}

// ============================================================================
// Example 6: Error Handling Patterns
// ============================================================================
// Use case: Robust error handling for file operations
function robustFileReading(params) {
    // Try to read file with comprehensive error handling
    function safeReadFile(filePath) {
        try {
            var content = file_read(filePath);
            
            if (content === null) {
                console.log("File not found: " + filePath);
                return { success: false, error: "File not found" };
            }
            
            if (content.length === 0) {
                console.log("Warning: File is empty: " + filePath);
                return { success: true, content: "", warning: "Empty file" };
            }
            
            return { success: true, content: content };
            
        } catch (error) {
            console.log("Error reading file " + filePath + ": " + error);
            return { success: false, error: error.toString() };
        }
    }
    
    // Read multiple files with error handling
    var response = safeReadFile("outputs/response.md");
    var analysis = safeReadFile("outputs/analysis.json");
    
    // Process based on what's available
    if (response.success && analysis.success) {
        // Both files available - full processing
        return {
            status: "complete",
            responseLength: response.content.length,
            analysisAvailable: true
        };
    } else if (response.success) {
        // Only response available - partial processing
        return {
            status: "partial",
            responseLength: response.content.length,
            analysisAvailable: false,
            message: "Analysis file not found, proceeding with response only"
        };
    } else {
        // No files available - graceful degradation
        return {
            status: "failed",
            message: "No output files available for processing"
        };
    }
}

// ============================================================================
// Example 7: Conditional Processing Based on File Contents
// ============================================================================
// Use case: Different actions based on file content analysis
function conditionalProcessing(params) {
    // Read CLI output
    var cliOutput = file_read("outputs/response.md");
    
    if (cliOutput === null) {
        console.log("No CLI output found");
        return { processed: false };
    }
    
    // Analyze output content
    var hasErrors = cliOutput.indexOf("ERROR") >= 0 || cliOutput.indexOf("FAILED") >= 0;
    var hasWarnings = cliOutput.indexOf("WARNING") >= 0 || cliOutput.indexOf("WARN") >= 0;
    var isSuccess = cliOutput.indexOf("SUCCESS") >= 0 || cliOutput.indexOf("PASSED") >= 0;
    
    // Take different actions based on analysis
    if (hasErrors) {
        // Errors detected - flag for review
        jira_update_field({
            key: params.ticket.key,
            field: "labels",
            value: ["needs-review", "errors-detected"]
        });
        
        jira_update_field({
            key: params.ticket.key,
            field: "priority",
            value: { name: "High" }
        });
        
        jira_post_comment({
            key: params.ticket.key,
            comment: "h3. Errors Detected\n\nAutomated processing detected errors. Manual review required.\n\nSee outputs/response.md for details."
        });
        
        return { status: "error", action: "flagged for review" };
        
    } else if (hasWarnings) {
        // Warnings detected - note but continue
        jira_post_comment({
            key: params.ticket.key,
            comment: "h3. Warnings Detected\n\nProcessing completed with warnings. Review recommended.\n\nSee outputs/response.md for details."
        });
        
        return { status: "warning", action: "noted in comments" };
        
    } else if (isSuccess) {
        // Success - move to done
        jira_move_to_status({
            key: params.ticket.key,
            statusName: "Done"
        });
        
        jira_post_comment({
            key: params.ticket.key,
            comment: "h3. Processing Complete\n\nAutomated processing completed successfully."
        });
        
        return { status: "success", action: "moved to done" };
        
    } else {
        // Unclear status - flag for review
        jira_post_comment({
            key: params.ticket.key,
            comment: "h3. Processing Complete\n\nAutomated processing finished. Manual review recommended to verify results."
        });
        
        return { status: "unknown", action: "noted in comments" };
    }
}

// ============================================================================
// Example 8: Working with Different File Formats
// ============================================================================
// Use case: Handle various file formats (MD, JSON, YAML, XML, CSV, etc.)
function multiFormatProcessing(params) {
    var results = {};
    
    // Markdown file
    var mdContent = file_read("outputs/report.md");
    if (mdContent !== null) {
        results.markdownLength = mdContent.length;
        results.markdownLines = mdContent.split("\n").length;
    }
    
    // JSON file
    var jsonContent = file_read("outputs/data.json");
    if (jsonContent !== null) {
        try {
            results.jsonData = JSON.parse(jsonContent);
        } catch (error) {
            results.jsonError = "Failed to parse JSON";
        }
    }
    
    // YAML file (parse as text)
    var yamlContent = file_read("outputs/config.yaml");
    if (yamlContent !== null) {
        results.yamlLines = yamlContent.split("\n").length;
        // Simple YAML key extraction (for basic cases)
        var keyMatches = yamlContent.match(/^([a-zA-Z_][a-zA-Z0-9_]*):$/gm);
        results.yamlKeys = keyMatches ? keyMatches.length : 0;
    }
    
    // CSV file
    var csvContent = file_read("outputs/results.csv");
    if (csvContent !== null) {
        var lines = csvContent.split("\n");
        results.csvRows = lines.length - 1; // Exclude header
        if (lines.length > 0) {
            results.csvColumns = lines[0].split(",").length;
        }
    }
    
    // Log file
    var logContent = file_read("outputs/execution.log");
    if (logContent !== null) {
        results.logLines = logContent.split("\n").length;
        results.logErrors = (logContent.match(/ERROR/g) || []).length;
        results.logWarnings = (logContent.match(/WARN/g) || []).length;
    }
    
    return results;
}

// ============================================================================
// Main Action Function - Entry Point
// ============================================================================
/**
 * Main action function that gets called by the job execution framework.
 * Demonstrates a complete workflow combining file reading with MCP tools.
 * 
 * @param params Job parameters including ticket, response, and custom parameters
 * @returns Result object with processing status and details
 */
function action(params) {
    console.log("Starting file reading workflow for ticket: " + params.ticket.key);
    
    // Read main CLI response
    var cliResponse = file_read("outputs/response.md");
    
    // Read input request for context
    var inputRequest = file_read("input/" + params.ticket.key + "/request.md");
    
    // Read analysis if available
    var analysisJson = file_read("outputs/analysis.json");
    var analysis = null;
    if (analysisJson !== null) {
        try {
            analysis = JSON.parse(analysisJson);
        } catch (error) {
            console.log("Could not parse analysis JSON: " + error);
        }
    }
    
    // Graceful handling when files are missing
    if (cliResponse === null) {
        console.log("No CLI response found - using params.response as fallback");
        cliResponse = params.response || "No response available";
    }
    
    // Build comprehensive report
    var comment = "h2. Automated Processing Results\n\n";
    
    // Add response summary
    comment += "h3. Response Summary\n\n";
    comment += "* Response length: " + cliResponse.length + " characters\n";
    comment += "* Lines: " + cliResponse.split("\n").length + "\n\n";
    
    // Add analysis if available
    if (analysis !== null) {
        comment += "h3. Analysis\n\n";
        comment += "* Risk Level: " + (analysis.riskLevel || "N/A") + "\n";
        comment += "* Issues Found: " + (analysis.issues ? analysis.issues.length : 0) + "\n\n";
    }
    
    // Post comment with results
    jira_post_comment({
        key: params.ticket.key,
        comment: comment
    });
    
    // Return processing results
    return {
        success: true,
        ticket: params.ticket.key,
        responseLength: cliResponse.length,
        analysisAvailable: analysis !== null,
        filesProcessed: [
            cliResponse !== null ? "response.md" : null,
            inputRequest !== null ? "request.md" : null,
            analysis !== null ? "analysis.json" : null
        ].filter(function(f) { return f !== null; })
    };
}

// Export for testing (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        action: action,
        readCliOutput: readCliOutput,
        readJobRequest: readJobRequest,
        analyzeJsonResults: analyzeJsonResults,
        crossValidateOutputs: crossValidateOutputs,
        aggregateResults: aggregateResults,
        robustFileReading: robustFileReading,
        conditionalProcessing: conditionalProcessing,
        multiFormatProcessing: multiFormatProcessing
    };
}

