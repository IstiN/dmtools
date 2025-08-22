package com.github.istin.dmtools.github;

import com.github.istin.dmtools.common.networking.GenericRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import okhttp3.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for GitHub workflow operations - testing multiple HTTP approaches.
 * 
 * This class tests different HTTP client implementations to find what works in Google Cloud.
 */
public class GitHubWorkflowUtils {
    
    private static final Logger logger = LogManager.getLogger(GitHubWorkflowUtils.class);
    
    /**
     * Triggers a GitHub workflow using multiple HTTP client approaches to find what works in cloud.
     * Tests different approaches sequentially until one succeeds.
     * 
     * @param github The GitHub client instance
     * @param owner Repository owner
     * @param repo Repository name  
     * @param workflowId Workflow ID or filename
     * @param request Request payload
     * @throws IOException if all approaches fail
     */
    public static void triggerWorkflow(GitHub github, String owner, String repo, String workflowId, String request) throws IOException {
        logger.info("🧪 Testing multiple HTTP approaches for GitHub workflow trigger");
        
        // Prepare common data
        String processedRequest = github.processLargePayload(request);
        JSONObject requestBody = new JSONObject();
        requestBody.put("ref", "main");
        requestBody.put("inputs", new JSONObject().put("user_request", processedRequest));
        String requestBodyStr = requestBody.toString();
        String triggerUrl = String.format("https://api.github.com/repos/%s/%s/actions/workflows/%s/dispatches", owner, repo, workflowId);
        
        logger.info("📊 Request size: {} characters", requestBodyStr.length());
        
        // Try approaches in order
        String[] approaches = {"OkHttp", "Native HttpClient", "URLConnection"};
        Exception lastException = null;
        
        for (String approach : approaches) {
            try {
                logger.info("🔄 Trying approach: {}", approach);
                
                switch (approach) {
                    case "OkHttp":
                        triggerWorkflowOkHttp(github, triggerUrl, requestBodyStr);
                        break;
                    case "Native HttpClient":
                        triggerWorkflowNativeHttp(github, triggerUrl, requestBodyStr);
                        break;
                    case "URLConnection":
                        triggerWorkflowUrlConnection(github, triggerUrl, requestBodyStr);
                        break;
                }
                
                logger.info("✅ SUCCESS: {} approach worked!", approach);
                return; // Success - exit
                
            } catch (Exception e) {
                logger.warn("❌ FAILED: {} approach failed: {}", approach, e.getMessage());
                lastException = e;
            }
        }
        
        // All approaches failed
        logger.error("💥 ALL APPROACHES FAILED - no working HTTP client found");
        throw new IOException("All HTTP approaches failed. Last error: " + 
            (lastException != null ? lastException.getMessage() : "Unknown"), lastException);
    }
    
    /**
     * Approach 1: OkHttp with basic configuration
     */
    private static void triggerWorkflowOkHttp(GitHub github, String triggerUrl, String requestBodyStr) throws IOException {
        logger.info("🔧 OkHttp: Creating basic OkHttp client");
        
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(github.getTimeout(), TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(requestBodyStr, mediaType);
        
        Request request = new Request.Builder()
            .url(triggerUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + github.getAuthorization())
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "DMTools")
            .build();
        
        logger.info("🌐 OkHttp: Sending request to {}", triggerUrl);
        
        try (Response response = client.newCall(request).execute()) {
            logger.info("📨 OkHttp: Response {} {}", response.code(), 
                response.body() != null ? "with body" : "no body");
            
            if (response.code() < 200 || response.code() >= 300) {
                String responseBody = response.body() != null ? response.body().string() : "";
                throw new IOException("OkHttp HTTP " + response.code() + ": " + responseBody);
            }
        }
    }
    
    /**
     * Approach 2: Native Java HttpClient (similar to the working triggerWorkflowSimpleHttp)
     */
    private static void triggerWorkflowNativeHttp(GitHub github, String triggerUrl, String requestBodyStr) throws IOException {
        logger.info("🔧 Native: Creating Java HttpClient");
        
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(triggerUrl))
                .timeout(Duration.ofSeconds(github.getTimeout()))
                .header("Authorization", "Bearer " + github.getAuthorization())
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "DMTools")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr))
                .build();
            
            logger.info("🌐 Native: Sending request to {}", triggerUrl);
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                HttpResponse.BodyHandlers.ofString());
            
            logger.info("📨 Native: Response {} {}", response.statusCode(), 
                response.body() != null ? "with body" : "no body");
            
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Native HTTP " + response.statusCode() + ": " + response.body());
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Native HTTP request interrupted", e);
        }
    }
    
    /**
     * Approach 3: URLConnection (most basic approach)
     */
    private static void triggerWorkflowUrlConnection(GitHub github, String triggerUrl, String requestBodyStr) throws IOException {
        logger.info("🔧 URLConnection: Creating basic URLConnection");
        
        URL url = new URL(triggerUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Configure connection
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(github.getTimeout() * 1000); // Convert to milliseconds
            
            // Set headers
            connection.setRequestProperty("Authorization", "Bearer " + github.getAuthorization());
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "DMTools");
            
            logger.info("🌐 URLConnection: Sending request to {}", triggerUrl);
            
            // Write request body
            try (OutputStream outputStream = connection.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(requestBodyStr);
                writer.flush();
            }
            
            // Get response
            int responseCode = connection.getResponseCode();
            logger.info("📨 URLConnection: Response {}", responseCode);
            
            if (responseCode < 200 || responseCode >= 300) {
                // Read error response if available
                String errorResponse = "";
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            errorResponse = sb.toString();
                        }
                    }
                }
                throw new IOException("URLConnection HTTP " + responseCode + ": " + errorResponse);
            }
            
        } finally {
            connection.disconnect();
        }
    }
}