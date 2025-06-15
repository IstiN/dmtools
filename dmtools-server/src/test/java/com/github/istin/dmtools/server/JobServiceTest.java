package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.model.Metadata;
import com.github.istin.dmtools.job.Job;
import com.github.istin.dmtools.job.JobParams;
import com.github.istin.dmtools.job.Params;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    @Spy
    @InjectMocks
    private JobService jobService;
    
    @Mock
    private Job mockJob;
    
    @Mock
    private JobParams mockJobParams;
    
    @Mock
    private Params mockParams;
    
    @Mock
    private AI mockAI;
    
    @Mock
    private Metadata mockMetadata;

    @BeforeEach
    void setUp() {
        // Create a list with just our mock job
        List<Job> testJobs = new ArrayList<>();
        testJobs.add(mockJob);
        
        // Replace the JOBS list in JobService with our test list
        ReflectionTestUtils.setField(jobService, "JOBS", testJobs);
    }

    @Test
    void executeJob_WithValidJobName_ShouldExecuteJob() throws Exception {
        // Arrange
        when(mockJob.getName()).thenReturn("testJob");
        when(mockJobParams.getName()).thenReturn("testJob");
        when(mockJobParams.getParamsByClass(any())).thenReturn(mockParams);
        when(mockParams.getMetadata()).thenReturn(mockMetadata);
        when(mockJob.getAi()).thenReturn(mockAI);
        
        // Act
        jobService.executeJob(mockJobParams);
        
        // Assert
        verify(mockJob).runJob(mockParams);
        verify(mockMetadata).init(mockJob);
        verify(mockAI).setMetadata(mockMetadata);
    }

    @Test
    void executeJob_WithInvalidJobName_ShouldThrowException() {
        // Arrange
        when(mockJob.getName()).thenReturn("validJob");
        when(mockJobParams.getName()).thenReturn("invalidJob");
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jobService.executeJob(mockJobParams);
        });
        
        assertEquals("Job with name 'invalidJob' not found.", exception.getMessage());
    }

    @Test
    void executeJob_WithNullMetadata_ShouldNotCallInit() throws Exception {
        // Arrange
        when(mockJob.getName()).thenReturn("testJob");
        when(mockJobParams.getName()).thenReturn("testJob");
        when(mockJobParams.getParamsByClass(any())).thenReturn(mockParams);
        when(mockParams.getMetadata()).thenReturn(null);
        when(mockJob.getAi()).thenReturn(mockAI);
        
        // Act
        jobService.executeJob(mockJobParams);
        
        // Assert
        verify(mockJob).runJob(mockParams);
        verify(mockAI, never()).setMetadata(any());
    }

    @Test
    void executeJob_WithNullAI_ShouldNotSetMetadata() throws Exception {
        // Arrange
        when(mockJob.getName()).thenReturn("testJob");
        when(mockJobParams.getName()).thenReturn("testJob");
        when(mockJobParams.getParamsByClass(any())).thenReturn(mockParams);
        when(mockParams.getMetadata()).thenReturn(mockMetadata);
        when(mockJob.getAi()).thenReturn(null);
        
        // Act
        jobService.executeJob(mockJobParams);
        
        // Assert
        verify(mockJob).runJob(mockParams);
        verify(mockMetadata).init(mockJob);
    }

    @Test
    void executeJob_WithNonParamsObject_ShouldNotInitMetadata() throws Exception {
        // Arrange
        when(mockJob.getName()).thenReturn("testJob");
        when(mockJobParams.getName()).thenReturn("testJob");
        when(mockJobParams.getParamsByClass(any())).thenReturn(new Object());
        
        // Act
        jobService.executeJob(mockJobParams);
        
        // Assert
        verify(mockJob).runJob(any());
        verify(mockMetadata, never()).init(any());
    }
    
    // Helper class for ReflectionTestUtils
    private static class ReflectionTestUtils {
        public static void setField(Object target, String fieldName, Object value) {
            try {
                java.lang.reflect.Field field = JobService.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field", e);
            }
        }
    }
} 