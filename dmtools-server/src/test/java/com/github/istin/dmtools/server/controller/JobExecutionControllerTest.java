package com.github.istin.dmtools.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.dto.JobTypeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JobExecutionController API endpoints.
 * Verifies that the API correctly returns job configurations and parameters.
 */
@SpringBootTest
@AutoConfigureWebMvc
class JobExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetJobTypesEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent, "Response content should not be null");
        assertFalse(responseContent.isEmpty(), "Response content should not be empty");

        // Parse the response
        JobTypeDto[] jobTypes = objectMapper.readValue(responseContent, JobTypeDto[].class);
        assertNotNull(jobTypes, "Job types array should not be null");
        assertTrue(jobTypes.length >= 2, "Should have at least 2 job types (Expert and TestCasesGenerator)");

        // Verify Expert job exists
        JobTypeDto expertJob = Arrays.stream(jobTypes)
                .filter(job -> "Expert".equals(job.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(expertJob, "Expert job should be present");

        // Verify TestCasesGenerator job exists
        JobTypeDto testCasesJob = Arrays.stream(jobTypes)
                .filter(job -> "TestCasesGenerator".equals(job.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(testCasesJob, "TestCasesGenerator job should be present");
    }

    @Test
    void testExpertJobConfigurationStructure() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        JobTypeDto expertJob = Arrays.stream(jobTypes)
                .filter(job -> "Expert".equals(job.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expert job not found"));

        // Verify basic structure
        assertEquals("Expert", expertJob.getType());
        assertNotNull(expertJob.getDisplayName());
        assertNotNull(expertJob.getCategories());
        assertNotNull(expertJob.getExecutionModes());
        assertNotNull(expertJob.getRequiredIntegrations());
        assertNotNull(expertJob.getConfigParams());

        // Verify categories
        assertTrue(expertJob.getCategories().contains("AI"));
        assertTrue(expertJob.getCategories().contains("Analysis"));

        // Verify execution modes
        assertTrue(expertJob.getExecutionModes().contains("STANDALONE"));
        assertTrue(expertJob.getExecutionModes().contains("SERVER_MANAGED"));

        // Verify required integrations
        assertTrue(expertJob.getRequiredIntegrations().contains("TrackerClient"));
        assertTrue(expertJob.getRequiredIntegrations().contains("AI"));
    }

    @Test
    void testExpertJobParameterConfiguration() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        JobTypeDto expertJob = Arrays.stream(jobTypes)
                .filter(job -> "Expert".equals(job.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expert job not found"));

        List<JobTypeDto.ConfigParamDefinition> params = expertJob.getConfigParams();
        assertNotNull(params);
        assertFalse(params.isEmpty());

        // Verify required parameters exist
        assertParameterExists(params, "request", true);
        assertParameterExists(params, "inputJql", true);
        assertParameterExists(params, "initiator", true);

        // Verify outputType parameter has correct enum values
        JobTypeDto.ConfigParamDefinition outputTypeParam = params.stream()
                .filter(p -> "outputType".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("outputType parameter not found"));

        assertNotNull(outputTypeParam.getOptions());
        List<String> expectedOptions = Arrays.asList("comment", "field", "creation");
        assertEquals(expectedOptions, outputTypeParam.getOptions());

        // Verify confluencePages is array type
        JobTypeDto.ConfigParamDefinition confluencePagesParam = params.stream()
                .filter(p -> "confluencePages".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("confluencePages parameter not found"));

        assertEquals("array", confluencePagesParam.getType());
        assertFalse(confluencePagesParam.isRequired());
    }

    @Test
    void testTestCasesGeneratorJobParameterConfiguration() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        JobTypeDto testCasesJob = Arrays.stream(jobTypes)
                .filter(job -> "TestCasesGenerator".equals(job.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("TestCasesGenerator job not found"));

        List<JobTypeDto.ConfigParamDefinition> params = testCasesJob.getConfigParams();
        assertNotNull(params);
        assertFalse(params.isEmpty());

        // Verify required parameters exist
        assertParameterExists(params, "inputJql", true);
        assertParameterExists(params, "initiator", true);

        // Verify outputType parameter has correct enum values
        JobTypeDto.ConfigParamDefinition outputTypeParam = params.stream()
                .filter(p -> "outputType".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("outputType parameter not found"));

        assertNotNull(outputTypeParam.getOptions());
        List<String> expectedOptions = Arrays.asList("comment", "field", "creation");
        assertEquals(expectedOptions, outputTypeParam.getOptions());

        // Verify confluencePages is array type
        JobTypeDto.ConfigParamDefinition confluencePagesParam = params.stream()
                .filter(p -> "confluencePages".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("confluencePages parameter not found"));

        assertEquals("array", confluencePagesParam.getType());
        assertFalse(confluencePagesParam.isRequired());

        // Verify specific TestCasesGenerator parameters
        assertParameterExists(params, "relatedTestCasesRules", false);
        assertParameterExists(params, "testCaseIssueType", false);
        assertParameterExists(params, "testCaseLinkRelationship", false);
    }

    @Test
    void testNoInvalidParametersPresent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        JobTypeDto expertJob = Arrays.stream(jobTypes)
                .filter(job -> "Expert".equals(job.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expert job not found"));

        // Verify invalid parameters are NOT present
        List<String> paramKeys = expertJob.getConfigParams().stream()
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .toList();

        assertFalse(paramKeys.contains("tempFolder"), 
                "Expert job should not have tempFolder parameter");
        assertFalse(paramKeys.contains("attachmentsFolder"), 
                "Expert job should not have attachmentsFolder parameter");

        JobTypeDto testCasesJob = Arrays.stream(jobTypes)
                .filter(job -> "TestCasesGenerator".equals(job.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("TestCasesGenerator job not found"));

        List<String> testCasesParamKeys = testCasesJob.getConfigParams().stream()
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .toList();

        // Verify invalid TestCasesGenerator parameters are NOT present
        assertFalse(testCasesParamKeys.contains("testCaseType"), 
                "TestCasesGenerator job should not have testCaseType parameter");
        assertFalse(testCasesParamKeys.contains("maxTestCasesPerTicket"), 
                "TestCasesGenerator job should not have maxTestCasesPerTicket parameter");
        assertFalse(testCasesParamKeys.contains("includeNegativeTestCases"), 
                "TestCasesGenerator job should not have includeNegativeTestCases parameter");
        assertFalse(testCasesParamKeys.contains("includeEdgeCases"), 
                "TestCasesGenerator job should not have includeEdgeCases parameter");
        assertFalse(testCasesParamKeys.contains("generateAutomationScript"), 
                "TestCasesGenerator job should not have generateAutomationScript parameter");
    }

    @Test
    void testResponseContainsAllBaseParameters() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        for (JobTypeDto jobType : jobTypes) {
            List<String> paramKeys = jobType.getConfigParams().stream()
                    .map(JobTypeDto.ConfigParamDefinition::getKey)
                    .toList();

            // Verify base Params class fields are present
            assertTrue(paramKeys.contains("confluencePages"), 
                    String.format("Job %s should have confluencePages parameter", jobType.getType()));
            assertTrue(paramKeys.contains("isCodeAsSource"), 
                    String.format("Job %s should have isCodeAsSource parameter", jobType.getType()));
            assertTrue(paramKeys.contains("isConfluenceAsSource"), 
                    String.format("Job %s should have isConfluenceAsSource parameter", jobType.getType()));
            assertTrue(paramKeys.contains("isTrackerAsSource"), 
                    String.format("Job %s should have isTrackerAsSource parameter", jobType.getType()));
            assertTrue(paramKeys.contains("transformConfluencePagesToMarkdown"), 
                    String.format("Job %s should have transformConfluencePagesToMarkdown parameter", jobType.getType()));
        }
    }

    @Test
    void testParameterTypesAreValid() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/jobs/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JobTypeDto[] jobTypes = objectMapper.readValue(
                result.getResponse().getContentAsString(), JobTypeDto[].class);

        List<String> validTypes = Arrays.asList(
                "text", "textarea", "password", "email", "number", 
                "boolean", "select", "array", "file"
        );

        for (JobTypeDto jobType : jobTypes) {
            for (JobTypeDto.ConfigParamDefinition param : jobType.getConfigParams()) {
                assertTrue(validTypes.contains(param.getType()), 
                        String.format("Invalid parameter type '%s' for parameter '%s' in job '%s'", 
                                param.getType(), param.getKey(), jobType.getType()));
            }
        }
    }

    /**
     * Helper method to assert that a parameter exists with the expected required status.
     */
    private void assertParameterExists(List<JobTypeDto.ConfigParamDefinition> params, 
                                     String paramKey, boolean expectedRequired) {
        JobTypeDto.ConfigParamDefinition param = params.stream()
                .filter(p -> paramKey.equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Parameter '%s' not found", paramKey)));

        assertEquals(expectedRequired, param.isRequired(), 
                String.format("Parameter '%s' required status should be %b", paramKey, expectedRequired));
    }
} 