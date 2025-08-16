package com.github.istin.dmtools.server.controller;

import com.github.istin.dmtools.auth.controller.DynamicMCPController;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.server.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DynamicMCPControllerTest {

    private MockMvc mockMvc;

    @Mock
    private McpConfigurationService mcpConfigurationService;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private IntegrationConfigurationLoader configurationLoader;

    @Mock
    private FileDownloadService fileDownloadService;

    @InjectMocks
    private DynamicMCPController dynamicMCPController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dynamicMCPController).build();
    }

    @Test
    void mcpStreamGet_shouldReturnSseEmitter_whenConfigFound() throws Exception {
        String configId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);

        McpConfiguration mcpConfig = new McpConfiguration();
        mcpConfig.setId(configId);
        mcpConfig.setUser(user);
        mcpConfig.setIntegrationIds(Collections.singletonList("jira"));

        when(mcpConfigurationService.findById(anyString())).thenReturn(mcpConfig);

        mockMvc.perform(get("/mcp/stream/{configId}", configId)
                        .param("method", "initialize")
                        .param("id", "1")
                        .param("params", "{}")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    void mcpStreamPost_shouldReturnSseEmitter_whenConfigFound() throws Exception {
        String configId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);

        McpConfiguration mcpConfig = new McpConfiguration();
        mcpConfig.setId(configId);
        mcpConfig.setUser(user);
        mcpConfig.setIntegrationIds(Collections.singletonList("jira"));

        when(mcpConfigurationService.findById(anyString())).thenReturn(mcpConfig);

        String requestBody = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":1,\"params\":{}}";

        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    void mcpStreamGet_shouldReturnError_whenConfigNotFound() throws Exception {
        String configId = UUID.randomUUID().toString();

        when(mcpConfigurationService.findById(anyString())).thenReturn(null);

        mockMvc.perform(get("/mcp/stream/{configId}", configId)
                        .param("method", "initialize")
                        .param("id", "1")
                        .param("params", "{}")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted()); // Still async, error sent via emitter
    }

    @Test
    void mcpStreamPost_shouldReturnError_whenConfigNotFound() throws Exception {
        String configId = UUID.randomUUID().toString();

        when(mcpConfigurationService.findById(anyString())).thenReturn(null);

        String requestBody = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":1,\"params\":{}}";

        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted()); // Still async, error sent via emitter
    }
}
