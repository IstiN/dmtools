package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.server.exception.JobHasActiveExecutionsException;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import com.github.istin.dmtools.server.service.WebhookExamplesService;
import com.github.istin.dmtools.server.service.WebhookKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobConfigurationController.class)
public class JobConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobConfigurationService jobConfigurationService;
    @MockBean
    private UserService userService;
    @MockBean
    private WebhookKeyService webhookKeyService;
    @MockBean
    private WebhookExamplesService webhookExamplesService;
    @MockBean
    private JobExecutionController jobExecutionController;

    private String jobConfigId = "test-job-config-id";
    private String userId = "test-user-id";

    @BeforeEach
    void setUp() {
        // Mock getUserId method in controller to return a fixed user ID
        // This is a workaround as @WithMockUser doesn't directly set the 'sub' attribute for OAuth2User
        // For simplicity in this test, we'll assume getUserId correctly extracts 'userId'
        // In a real application, you might use a custom security context or more advanced Spring Security testing utilities
        when(userService.findByEmail(anyString())).thenReturn(java.util.Optional.of(new com.github.istin.dmtools.auth.model.User() {{ setId(userId); }}));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteJobConfiguration_Success() throws Exception {
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), any(String.class)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/v1/job-configurations/{id}", jobConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // 204 No Content
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteJobConfiguration_NotFound() throws Exception {
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), any(String.class)))
                .thenReturn(false);

        mockMvc.perform(delete("/api/v1/job-configurations/{id}", jobConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 Not Found
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteJobConfiguration_Conflict_ActiveExecutions() throws Exception {
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), any(String.class)))
                .thenThrow(new JobHasActiveExecutionsException("Cannot delete job with active executions."));

        mockMvc.perform(delete("/api/v1/job-configurations/{id}", jobConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()); // 409 Conflict
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteJobConfiguration_InternalServerError() throws Exception {
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), any(String.class)))
                .thenThrow(new RuntimeException("Simulated internal error."));

        mockMvc.perform(delete("/api/v1/job-configurations/{id}", jobConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // 500 Internal Server Error
    }
}
