package com.github.istin.dmtools.github;

import com.github.istin.dmtools.common.networking.GenericRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Utility class for GitHub workflow operations with enhanced cloud environment support.
 * 
 * This class contains the production-ready workflow triggering logic that has been
 * tested and verified to work in Google Cloud Run environments.
 */
public class GitHubWorkflowUtils {
    
    private static final Logger logger = LogManager.getLogger(GitHubWorkflowUtils.class);
    
    /**
     * Triggers a GitHub workflow dispatch with enhanced error handling and cloud support.
     * 
     * This method uses the enhanced AbstractRestClient with:
     * - Automatic retry logic for connection failures (max 3 attempts)
     * - Cloud environment timeout detection (120s vs 60s local)
     * - Proper handling of "broken pipe" and other connection errors
     * - Exponential backoff for retries
     * 
     * @param github The GitHub client instance
     * @param owner Repository owner
     * @param repo Repository name  
     * @param workflowId Workflow ID or filename
     * @param request Request payload
     * @throws IOException if the workflow trigger fails after all retries
     */
    public static void triggerWorkflow(GitHub github, String owner, String repo, String workflowId, String request) throws IOException {
        String triggerPath = github.path(String.format("repos/%s/%s/actions/workflows/%s/dispatches", owner, repo, workflowId));
        
        // Handle large payloads by implementing size limiting and compression
        String processedRequest = github.processLargePayload(request);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("ref", "main");
        requestBody.put("inputs", new JSONObject().put("user_request", processedRequest));
        
        // Log request size for debugging
        String requestBodyStr = requestBody.toString();
        int requestSize = requestBodyStr.length();
        logger.info("Triggering workflow {}/{}/{} with request size: {} characters", owner, repo, workflowId, requestSize);
        
        // Only log full request body if it's reasonably sized
        if (requestSize <= 1000) {
            logger.debug("Request body: {}", requestBodyStr);
        } else {
            logger.debug("Request body (truncated): {}...", requestBodyStr.substring(0, 500));
        }
        
        GenericRequest postRequest = new GenericRequest(github, triggerPath);
        postRequest.setBody(requestBodyStr);
        postRequest.setIgnoreCache(true);

        try {
            // Use enhanced POST method with automatic retry logic
            String response = github.post(postRequest);
            logger.info("Workflow trigger response: {}", response != null ? response : "No response (this is normal for GitHub workflow dispatches)");
            
            // GitHub workflow dispatch typically returns 204 No Content on success
            // The lack of a response body is normal and indicates success
            
        } catch (IOException e) {
            // Enhanced error handling for connection issues
            String errorMessage = e.getMessage();
            boolean isConnectionError = errorMessage != null && (
                errorMessage.toLowerCase().contains("broken pipe") ||
                errorMessage.toLowerCase().contains("connection reset") ||
                errorMessage.toLowerCase().contains("connection refused") ||
                errorMessage.toLowerCase().contains("timeout")
            );
            
            if (isConnectionError) {
                logger.warn("GitHub API connection issue detected: {}. This was handled by retry logic.", errorMessage);
                // The AbstractRestClient has already handled retries before reaching here
                throw e;
            }
            
            logger.error("Failed to trigger workflow: {}", errorMessage);
            
            // Check for specific "inputs are too large" error
            if (errorMessage != null && errorMessage.toLowerCase().contains("inputs are too large")) {
                logger.error("GitHub workflow inputs exceed size limit. Original request size: {} characters, processed size: {} characters", 
                    request.length(), processedRequest.length());
                throw new IOException("GitHub workflow inputs are too large even after compression. " +
                    "Consider reducing the input data size or implementing request chunking. " +
                    "Original size: " + request.length() + " chars, processed: " + processedRequest.length() + " chars", e);
            }
            
            // Provide specific guidance for common failure reasons
            String errorDetails = getWorkflowTriggerErrorDetails(e, owner, repo, workflowId);
            throw new IOException("Workflow trigger failed: " + errorMessage + ". " + errorDetails, e);
        } catch (Exception e) {
            logger.error("Unexpected error during workflow trigger: {}", e.getMessage(), e);
            String errorDetails = getWorkflowTriggerErrorDetails(e, owner, repo, workflowId);
            throw new IOException("Workflow trigger failed with unexpected error: " + e.getMessage() + ". " + errorDetails, e);
        }
    }
    
    /**
     * Provides detailed error information for workflow trigger failures.
     * 
     * @param exception The exception that occurred
     * @param owner Repository owner
     * @param repo Repository name
     * @param workflowId Workflow ID
     * @return Detailed error information and troubleshooting steps
     */
    private static String getWorkflowTriggerErrorDetails(Exception exception, String owner, String repo, String workflowId) {
        StringBuilder details = new StringBuilder();
        
        details.append("Troubleshooting steps:\n");
        details.append("1. Verify the workflow file exists: https://github.com/").append(owner).append("/").append(repo).append("/actions/workflows/").append(workflowId).append("\n");
        details.append("2. Check that the workflow has 'workflow_dispatch' trigger configured\n");
        details.append("3. Ensure the GitHub token has 'actions:write' permission\n");
        details.append("4. Verify the repository allows workflow dispatches\n");
        
        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("404")) {
                details.append("5. ERROR: Workflow not found - check the workflow ID/filename\n");
            } else if (message.contains("401") || message.contains("403")) {
                details.append("5. ERROR: Authentication/permission issue - check token permissions\n");
            } else if (message.contains("422")) {
                details.append("5. ERROR: Invalid request - check workflow_dispatch configuration\n");
            }
        }
        
        return details.toString();
    }
}
