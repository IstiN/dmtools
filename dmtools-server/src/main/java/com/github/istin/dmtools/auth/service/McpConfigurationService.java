package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.Integration;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.McpConfigurationRepository;
import com.github.istin.dmtools.dto.AccessCodeResponse;
import com.github.istin.dmtools.dto.CreateMcpConfigurationRequest;
import com.github.istin.dmtools.dto.McpConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing MCP configurations.
 * Handles CRUD operations, validation, and access code generation.
 */
@Service
@Transactional
public class McpConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(McpConfigurationService.class);

    private final McpConfigurationRepository mcpConfigurationRepository;
    private final IntegrationService integrationService;
    private final UserService userService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public McpConfigurationService(
            McpConfigurationRepository mcpConfigurationRepository,
            IntegrationService integrationService,
            UserService userService) {
        this.mcpConfigurationRepository = mcpConfigurationRepository;
        this.integrationService = integrationService;
        this.userService = userService;
    }

    /**
     * Get all MCP configurations for a user.
     *
     * @param userId The user ID
     * @return List of MCP configuration DTOs
     */
    @Transactional(readOnly = true)
    public List<McpConfigurationDto> getUserConfigurations(String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<McpConfiguration> configurations = mcpConfigurationRepository.findByUserOrderByCreatedAtDesc(user);
        return configurations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific MCP configuration by ID for a user.
     *
     * @param configId The configuration ID
     * @param userId The user ID
     * @return Optional MCP configuration DTO
     */
    @Transactional(readOnly = true)
    public Optional<McpConfigurationDto> getUserConfiguration(String configId, String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        return mcpConfigurationRepository.findByIdAndUser(configId, user)
                .map(this::convertToDto);
    }

    /**
     * Create a new MCP configuration.
     *
     * @param request The creation request
     * @param userId The user ID
     * @return Created MCP configuration DTO
     */
    public McpConfigurationDto createConfiguration(CreateMcpConfigurationRequest request, String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validate configuration name is unique for the user
        if (mcpConfigurationRepository.existsByNameAndUser(request.getName(), user)) {
            throw new IllegalArgumentException("Configuration name already exists: " + request.getName());
        }

        // Validate that all integration IDs exist and belong to the user
        validateIntegrationIds(request.getIntegrationIds(), user);

        McpConfiguration configuration = new McpConfiguration();
        configuration.setName(request.getName());
        configuration.setUser(user);
        configuration.setIntegrationIds(request.getIntegrationIds());

        configuration = mcpConfigurationRepository.save(configuration);
        logger.info("Created MCP configuration {} for user {}", configuration.getId(), userId);
        
        return convertToDto(configuration);
    }

    /**
     * Update an existing MCP configuration.
     *
     * @param configId The configuration ID
     * @param request The update request
     * @param userId The user ID
     * @return Updated MCP configuration DTO
     */
    public McpConfigurationDto updateConfiguration(String configId, CreateMcpConfigurationRequest request, String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        McpConfiguration configuration = mcpConfigurationRepository.findByIdAndUser(configId, user)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        // Validate configuration name is unique for the user (excluding current config)
        if (mcpConfigurationRepository.existsByNameAndUserAndIdNot(request.getName(), user, configId)) {
            throw new IllegalArgumentException("Configuration name already exists: " + request.getName());
        }

        // Validate that all integration IDs exist and belong to the user
        validateIntegrationIds(request.getIntegrationIds(), user);

        configuration.setName(request.getName());
        configuration.setIntegrationIds(request.getIntegrationIds());

        configuration = mcpConfigurationRepository.save(configuration);
        logger.info("Updated MCP configuration {} for user {}", configId, userId);
        
        return convertToDto(configuration);
    }

    /**
     * Delete an MCP configuration.
     *
     * @param configId The configuration ID
     * @param userId The user ID
     */
    public void deleteConfiguration(String configId, String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        McpConfiguration configuration = mcpConfigurationRepository.findByIdAndUser(configId, user)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        mcpConfigurationRepository.delete(configuration);
        logger.info("Deleted MCP configuration {} for user {}", configId, userId);
    }

    /**
     * Generate access code for an MCP configuration.
     *
     * @param configId The configuration ID
     * @param userId The user ID
     * @param format The output format (cursor, json, shell)
     * @return Access code response
     */
    @Transactional(readOnly = true)
    public AccessCodeResponse generateAccessCode(String configId, String userId, String format) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        McpConfiguration configuration = mcpConfigurationRepository.findByIdAndUser(configId, user)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        String endpointUrl = baseUrl + "/mcp/config/" + configId;
        String code = generateCodeForFormat(configuration, endpointUrl, format);
        String instructions = generateInstructions(configuration, format);

        return new AccessCodeResponse(
                configuration.getName(),
                format,
                code,
                endpointUrl,
                instructions
        );
    }

    /**
     * Validate that integration IDs exist and belong to the user.
     */
    private void validateIntegrationIds(List<String> integrationIds, User user) {
        for (String integrationId : integrationIds) {
            try {
                // Try to get the integration - this method includes access validation
                integrationService.getIntegrationById(integrationId, user.getId(), false);
            } catch (IllegalArgumentException e) {
                // Re-throw with more specific message for MCP context
                throw new IllegalArgumentException("Integration not accessible: " + integrationId + ". " + e.getMessage());
            }
        }
    }

    /**
     * Generate configuration code based on format.
     */
    private String generateCodeForFormat(McpConfiguration configuration, String endpointUrl, String format) {
        switch (format.toLowerCase()) {
            case "cursor":
                return generateCursorConfig(configuration, endpointUrl);
            case "json":
                return generateJsonConfig(configuration, endpointUrl);
            case "shell":
                return generateShellConfig(configuration, endpointUrl);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    /**
     * Generate Cursor IDE configuration.
     */
    private String generateCursorConfig(McpConfiguration configuration, String endpointUrl) {
        return String.format("""
                {
                  "mcpServers": {
                    "%s": {
                      "command": "npx",
                      "args": ["-y", "@modelcontextprotocol/server-fetch", "%s"]
                    }
                  }
                }""", 
                configuration.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"),
                endpointUrl);
    }

    /**
     * Generate JSON configuration.
     */
    private String generateJsonConfig(McpConfiguration configuration, String endpointUrl) {
        return String.format("""
                {
                  "name": "%s",
                  "endpoint": "%s",
                  "protocol": "MCP",
                  "version": "2025-03-26",
                  "integrations": %s
                }""",
                configuration.getName(),
                endpointUrl,
                configuration.getIntegrationIds());
    }

    /**
     * Generate shell script configuration.
     */
    private String generateShellConfig(McpConfiguration configuration, String endpointUrl) {
        return String.format("""
                #!/bin/bash
                # MCP Configuration: %s
                # Generated: %s
                
                export MCP_SERVER_NAME="%s"
                export MCP_SERVER_URL="%s"
                
                # Run MCP server
                npx -y @modelcontextprotocol/server-fetch "$MCP_SERVER_URL"
                """,
                configuration.getName(),
                java.time.LocalDateTime.now(),
                configuration.getName(),
                endpointUrl);
    }

    /**
     * Generate setup instructions based on format.
     */
    private String generateInstructions(McpConfiguration configuration, String format) {
        switch (format.toLowerCase()) {
            case "cursor":
                return String.format("""
                        1. Copy the JSON configuration above
                        2. Open Cursor IDE settings (Cmd/Ctrl + ,)
                        3. Search for "MCP" or navigate to Extensions > MCP
                        4. Paste the configuration in the MCP Servers section
                        5. Restart Cursor IDE
                        6. Your DMTools integrations (%s) will be available via MCP
                        """, String.join(", ", configuration.getIntegrationIds()));
            case "json":
                return "Use this JSON configuration with any MCP-compatible client that supports JSON configuration files.";
            case "shell":
                return String.format("""
                        1. Save the script above to a file (e.g., dmtools_mcp_%s.sh)
                        2. Make it executable: chmod +x dmtools_mcp_%s.sh
                        3. Run: ./dmtools_mcp_%s.sh
                        """,
                        configuration.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"),
                        configuration.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"),
                        configuration.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"));
            default:
                return "Follow your MCP client's documentation for configuration setup.";
        }
    }

    /**
     * Convert entity to DTO.
     */
    private McpConfigurationDto convertToDto(McpConfiguration configuration) {
        return new McpConfigurationDto(
                configuration.getId(),
                configuration.getName(),
                configuration.getUser().getId(),
                configuration.getIntegrationIds(),
                configuration.getCreatedAt(),
                configuration.getUpdatedAt()
        );
    }
} 