package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.server.exception.JobHasActiveExecutionsException;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import com.github.istin.dmtools.server.service.WebhookExamplesService;
import com.github.istin.dmtools.server.service.WebhookKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobConfigurationControllerTest {

    @Mock
    private JobConfigurationService jobConfigurationService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private WebhookKeyService webhookKeyService;
    
    @Mock
    private WebhookExamplesService webhookExamplesService;
    
    @Mock
    private JobExecutionController jobExecutionController;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private JobConfigurationController controller;

    private String jobConfigId = "test-job-config-id";
    private String userId = "test-user-id";

    @BeforeEach
    void setUp() {
        // Setup authentication mocks
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn(userId);
    }

    @Test
    void deleteJobConfiguration_Success() {
        // Arrange
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), eq(userId)))
                .thenReturn(true);

        // Act
        ResponseEntity<Void> response = controller.deleteJobConfiguration(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(jobConfigurationService).deleteJobConfiguration(eq(jobConfigId), eq(userId));
    }

    @Test
    void deleteJobConfiguration_NotFound() {
        // Arrange
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), eq(userId)))
                .thenReturn(false);

        // Act
        ResponseEntity<Void> response = controller.deleteJobConfiguration(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(jobConfigurationService).deleteJobConfiguration(eq(jobConfigId), eq(userId));
    }

    @Test
    void deleteJobConfiguration_Conflict_ActiveExecutions() {
        // Arrange
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), eq(userId)))
                .thenThrow(new JobHasActiveExecutionsException("Cannot delete job with active executions."));

        // Act
        ResponseEntity<Void> response = controller.deleteJobConfiguration(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(jobConfigurationService).deleteJobConfiguration(eq(jobConfigId), eq(userId));
    }

    @Test
    void deleteJobConfiguration_InternalServerError() {
        // Arrange
        when(jobConfigurationService.deleteJobConfiguration(eq(jobConfigId), eq(userId)))
                .thenThrow(new RuntimeException("Simulated internal error."));

        // Act
        ResponseEntity<Void> response = controller.deleteJobConfiguration(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(jobConfigurationService).deleteJobConfiguration(eq(jobConfigId), eq(userId));
    }
}