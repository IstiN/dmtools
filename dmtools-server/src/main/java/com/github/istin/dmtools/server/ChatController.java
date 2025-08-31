package com.github.istin.dmtools.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
@Tag(name = "Chat Controller", description = "AI Chat API with MCP Tools Integration")
public class ChatController {

    private static final Logger logger = LogManager.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @PostMapping("/completions")
    @Operation(
        summary = "Chat Completions with MCP Tools Support", 
        description = "Process chat messages with AI and optionally execute MCP (Multi-Cloud Platform) tools for enhanced functionality. " +
                     "When mcpConfigId is provided, the system can automatically select and execute relevant tools based on the conversation context."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Chat completion successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class),
                examples = @ExampleObject(
                    name = "Successful Response",
                    value = """
                        {
                          "content": "Based on the Jira ticket DMC-123, here are the key details: [Tool execution results and AI analysis]",
                          "success": true,
                          "error": null
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request - missing or empty messages",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class),
                examples = @ExampleObject(
                    name = "Bad Request",
                    value = """
                        {
                          "content": null,
                          "success": false,
                          "error": "Messages cannot be empty"
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error during chat processing",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class),
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                        {
                          "content": null,
                          "success": false,
                          "error": "Failed to process chat request: AI service unavailable"
                        }"""
                )
            )
        )
    })
    public ResponseEntity<ChatResponse> chatCompletions(
        @Parameter(
            description = "Chat request containing messages and optional MCP configuration. " +
                         "The mcpConfigId enables tool integration for enhanced AI responses.",
            required = true,
            schema = @Schema(implementation = ChatRequest.class),
            examples = {
                @ExampleObject(
                    name = "Basic Chat Request",
                    summary = "Simple chat without tools",
                    value = """
                        {
                          "messages": [
                            {
                              "role": "user",
                              "content": "Hello, how are you?",
                              "fileNames": null
                            }
                          ],
                          "model": "gpt-4",
                          "ai": "openai-integration",
                          "mcpConfigId": null
                        }"""
                ),
                @ExampleObject(
                    name = "Chat with MCP Tools",
                    summary = "Chat request with MCP tools integration",
                    value = """
                        {
                          "messages": [
                            {
                              "role": "user",
                              "content": "Get me information from ticket DMC-123 and create a summary",
                              "fileNames": null
                            }
                          ],
                          "model": "gpt-4",
                          "ai": "openai-integration",
                          "mcpConfigId": "mcp-config-jira-confluence"
                        }"""
                )
            }
        )
        @RequestBody ChatRequest request, 
        Authentication authentication
    ) {
        logger.info("Received chat completions request with {} messages", 
                   request.getMessages() != null ? request.getMessages().size() : 0);
        
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.error("Messages cannot be empty"));
        }
        
        // Get user ID from authentication, allowing null for backwards compatibility
        String userId = null;
        try {
            if (authentication != null) {
                userId = getUserId(authentication);
                logger.info("Processing chat request for user: {}", userId);
            } else {
                logger.info("Processing chat request without authentication context");
            }
        } catch (Exception e) {
            logger.warn("Failed to extract user ID from authentication: {}", e.getMessage());
        }
        
        ChatResponse response = chatService.chat(request, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping(value = "/completions-with-files", consumes = {"multipart/form-data"})
    @Operation(
        summary = "Chat Completions with File Upload and MCP Tools",
        description = "Process chat messages with uploaded files and optional MCP tools integration. " +
                     "Files are processed and their content is made available to the AI for analysis."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chat completion with files successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or file processing error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during processing",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        )
    })
    public ResponseEntity<ChatResponse> chatCompletionsWithFiles(
            @Parameter(
                description = "JSON string containing the chat request (same structure as /completions)",
                required = true,
                example = """
                    {
                      "messages": [
                        {
                          "role": "user",
                          "content": "Analyze the uploaded document and provide a summary",
                          "fileNames": null
                        }
                      ],
                      "model": "gpt-4",
                      "ai": "openai-integration",
                      "mcpConfigId": "mcp-config-document-analysis"
                    }"""
            )
            @RequestParam("chatRequest") String chatRequestJson,
            @Parameter(
                description = "List of files to upload and analyze (optional)",
                required = false
            )
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        
        logger.info("Received chat completions request with files: {} files attached", 
                   files != null ? files.size() : 0);
        
        try {
            // Parse the JSON chat request
            ChatRequest request = objectMapper.readValue(chatRequestJson, ChatRequest.class);
            
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ChatResponse.error("Messages cannot be empty"));
            }
            
            // Get user ID from authentication, allowing null for backwards compatibility
            String userId = null;
            try {
                if (authentication != null) {
                    userId = getUserId(authentication);
                    logger.info("Processing chat request with files for user: {}", userId);
                } else {
                    logger.info("Processing chat request with files without authentication context");
                }
            } catch (Exception e) {
                logger.warn("Failed to extract user ID from authentication: {}", e.getMessage());
            }
            
            // Process uploaded files and save them temporarily
            List<File> tempFiles = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                tempFiles = saveUploadedFiles(files);
                logger.info("Saved {} temporary files for processing", tempFiles.size());
            }
            
            ChatResponse response = chatService.chatWithFiles(request, tempFiles, userId);
            
            // Clean up temporary files
            cleanupTempFiles(tempFiles);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat request with files", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.error("Failed to process chat request with files: " + e.getMessage()));
        }
    }

    @PostMapping("/simple")
    @Operation(
        summary = "Simple Chat Message",
        description = "Send a simple text message to the AI without file upload or MCP tools integration. " +
                     "This is a simplified endpoint for basic chat functionality."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Simple chat successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class),
                examples = @ExampleObject(
                    name = "Simple Chat Response",
                    value = """
                        {
                          "content": "Hello! I'm here to help you. How can I assist you today?",
                          "success": true,
                          "error": null
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - empty message",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        )
    })
    public ResponseEntity<ChatResponse> simpleChat(
            @Parameter(
                description = "The text message to send to the AI",
                required = true,
                example = "Hello, how can you help me today?"
            )
            @RequestParam String message,
            @Parameter(
                description = "AI model to use (optional)",
                required = false,
                example = "gpt-4"
            )
            @RequestParam(required = false) String model,
            @Parameter(
                description = "AI integration to use (optional)",
                required = false,
                example = "openai-integration"
            )
            @RequestParam(required = false) String ai,
            Authentication authentication) {
        logger.info("Received simple chat request");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.error("Message cannot be empty"));
        }
        
        // Get user ID from authentication, allowing null for backwards compatibility
        String userId = null;
        try {
            if (authentication != null) {
                userId = getUserId(authentication);
                logger.info("Processing simple chat request for user: {}", userId);
            } else {
                logger.info("Processing simple chat request without authentication context");
            }
        } catch (Exception e) {
            logger.warn("Failed to extract user ID from authentication: {}", e.getMessage());
        }
        
        ChatResponse response = chatService.simpleChatMessage(message.trim(), model, ai, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Check if the chat service is running and accessible"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "Chat service is running"
                )
            )
        )
    })
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is running");
    }

    private List<File> saveUploadedFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                // Create a temporary file
                String originalFilename = multipartFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                
                Path tempFile = Files.createTempFile("chat_upload_", extension);
                Files.copy(multipartFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
                
                File file = tempFile.toFile();
                tempFiles.add(file);
                
                logger.info("Saved uploaded file: {} -> {}", originalFilename, tempFile);
            }
        }
        
        return tempFiles;
    }

    private void cleanupTempFiles(List<File> tempFiles) {
        for (File tempFile : tempFiles) {
            try {
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (deleted) {
                        logger.debug("Cleaned up temporary file: {}", tempFile.getPath());
                    } else {
                        logger.warn("Failed to delete temporary file: {}", tempFile.getPath());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error cleaning up temporary file: {}", tempFile.getPath(), e);
            }
        }
    }

    private String getUserId(Authentication authentication) {
        // Handle PlaceholderAuthentication during OAuth flow
        if (authentication instanceof com.github.istin.dmtools.auth.PlaceholderAuthentication) {
            throw new IllegalArgumentException("Authentication still in progress, cannot extract user ID");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("sub");
        } else if (principal instanceof UserDetails) {
            // For JWT authentication, the username is the email
            // We need to find the user by email and return the user ID
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmail(email)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
        } else if (principal instanceof String) {
            String principalStr = (String) principal;
            // Check if it's a placeholder string from PlaceholderAuthentication
            if (principalStr.startsWith("placeholder_")) {
                throw new IllegalArgumentException("Authentication still in progress, cannot extract user ID");
            }
            // For string principal, assume it's the user ID directly
            return principalStr;
        } else {
            throw new IllegalArgumentException("Unsupported principal type: " + 
                (principal != null ? principal.getClass().getName() : "null"));
        }
    }
} 