package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.dto.JobTypeDto;
import com.github.istin.dmtools.expert.ExpertParams;
import com.github.istin.dmtools.job.Params;
import com.github.istin.dmtools.qa.TestCasesGeneratorParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobConfigurationLoader to ensure JSON configurations 
 * match the actual Java parameter classes.
 */
@SpringBootTest
class JobConfigurationLoaderTest {

    @Autowired
    private JobConfigurationLoader jobConfigurationLoader;

    private List<JobTypeDto> allJobTypes;

    @BeforeEach
    void setUp() {
        allJobTypes = jobConfigurationLoader.getAllJobTypes();
    }

    @Test
    void testJobConfigurationLoaderInitialization() {
        assertNotNull(jobConfigurationLoader, "JobConfigurationLoader should be initialized");
        assertNotNull(allJobTypes, "Job types should be loaded");
        assertFalse(allJobTypes.isEmpty(), "Should have at least one job type");
    }

    @Test
    void testExpertJobTypeExists() {
        assertTrue(jobConfigurationLoader.hasJobType("expert"), "Expert job type should exist");
        
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        assertNotNull(expertJob, "Expert job should not be null");
        assertEquals("Expert", expertJob.getType(), "Expert job type should be 'Expert'");
        assertNotNull(expertJob.getConfigParams(), "Expert job should have config parameters");
        assertFalse(expertJob.getConfigParams().isEmpty(), "Expert job should have at least one parameter");
    }

    @Test
    void testTestCasesGeneratorJobTypeExists() {
        assertTrue(jobConfigurationLoader.hasJobType("testcases-generator"), 
                "TestCasesGenerator job type should exist");
        
        JobTypeDto testCasesJob = jobConfigurationLoader.getJobType("testcases-generator");
        assertNotNull(testCasesJob, "TestCasesGenerator job should not be null");
        assertEquals("TestCasesGenerator", testCasesJob.getType(), 
                "TestCasesGenerator job type should be 'TestCasesGenerator'");
        assertNotNull(testCasesJob.getConfigParams(), "TestCasesGenerator job should have config parameters");
        assertFalse(testCasesJob.getConfigParams().isEmpty(), 
                "TestCasesGenerator job should have at least one parameter");
    }

    @Test
    void testExpertJobOutputTypeEnumValues() {
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        
        JobTypeDto.ConfigParamDefinition outputTypeParam = expertJob.getConfigParams().stream()
                .filter(param -> "outputType".equals(param.getKey()))
                .findFirst()
                .orElse(null);
                
        assertNotNull(outputTypeParam, "Expert job should have outputType parameter");
        assertNotNull(outputTypeParam.getOptions(), "outputType should have options");
        
        List<String> expectedValues = Arrays.asList("comment", "field", "creation");
        assertEquals(expectedValues, outputTypeParam.getOptions(), 
                "outputType enum values should match Params.OutputType enum");
    }

    @Test
    void testTestCasesGeneratorJobOutputTypeEnumValues() {
        JobTypeDto testCasesJob = jobConfigurationLoader.getJobType("testcases-generator");
        
        JobTypeDto.ConfigParamDefinition outputTypeParam = testCasesJob.getConfigParams().stream()
                .filter(param -> "outputType".equals(param.getKey()))
                .findFirst()
                .orElse(null);
                
        assertNotNull(outputTypeParam, "TestCasesGenerator job should have outputType parameter");
        assertNotNull(outputTypeParam.getOptions(), "outputType should have options");
        
        List<String> expectedValues = Arrays.asList("comment", "field", "creation");
        assertEquals(expectedValues, outputTypeParam.getOptions(), 
                "outputType enum values should match Params.OutputType enum");
    }

    @Test
    void testExpertJobRequiredParameters() {
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        
        // Test required parameters exist
        List<String> requiredParamKeys = expertJob.getConfigParams().stream()
                .filter(JobTypeDto.ConfigParamDefinition::isRequired)
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .collect(Collectors.toList());
                
        assertTrue(requiredParamKeys.contains("request"), 
                "Expert job should have required 'request' parameter");
        assertTrue(requiredParamKeys.contains("inputJql"), 
                "Expert job should have required 'inputJql' parameter");
        assertTrue(requiredParamKeys.contains("initiator"), 
                "Expert job should have required 'initiator' parameter");
    }

    @Test
    void testTestCasesGeneratorJobRequiredParameters() {
        JobTypeDto testCasesJob = jobConfigurationLoader.getJobType("testcases-generator");
        
        // Test required parameters exist
        List<String> requiredParamKeys = testCasesJob.getConfigParams().stream()
                .filter(JobTypeDto.ConfigParamDefinition::isRequired)
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .collect(Collectors.toList());
                
        assertTrue(requiredParamKeys.contains("inputJql"), 
                "TestCasesGenerator job should have required 'inputJql' parameter");
        assertTrue(requiredParamKeys.contains("initiator"), 
                "TestCasesGenerator job should have required 'initiator' parameter");
    }

    @Test
    void testExpertJobParametersMatchJavaClass() {
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        
        // Get all parameter keys from JSON configuration
        Set<String> jsonParamKeys = expertJob.getConfigParams().stream()
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .collect(Collectors.toSet());
        
        // Get all field names from ExpertParams and base Params classes
        Set<String> javaParamKeys = getAllFieldNames(ExpertParams.class);
        
        // Check that all Java parameters have corresponding JSON configuration
        for (String javaParam : javaParamKeys) {
            assertTrue(jsonParamKeys.contains(javaParam), 
                    String.format("JSON configuration missing parameter '%s' from ExpertParams class", javaParam));
        }
    }

    @Test
    void testTestCasesGeneratorJobParametersMatchJavaClass() {
        JobTypeDto testCasesJob = jobConfigurationLoader.getJobType("testcases-generator");
        
        // Get all parameter keys from JSON configuration
        Set<String> jsonParamKeys = testCasesJob.getConfigParams().stream()
                .map(JobTypeDto.ConfigParamDefinition::getKey)
                .collect(Collectors.toSet());
        
        // Get all field names from TestCasesGeneratorParams and base Params classes
        Set<String> javaParamKeys = getAllFieldNames(TestCasesGeneratorParams.class);
        
        // Check that all Java parameters have corresponding JSON configuration
        for (String javaParam : javaParamKeys) {
            assertTrue(jsonParamKeys.contains(javaParam), 
                    String.format("JSON configuration missing parameter '%s' from TestCasesGeneratorParams class", 
                            javaParam));
        }
    }

    @Test
    void testConfluencePagesParameterIsArrayType() {
        // Test Expert job
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        JobTypeDto.ConfigParamDefinition confluencePagesParam = expertJob.getConfigParams().stream()
                .filter(param -> "confluencePages".equals(param.getKey()))
                .findFirst()
                .orElse(null);
                
        assertNotNull(confluencePagesParam, "Expert job should have confluencePages parameter");
        assertEquals("array", confluencePagesParam.getType(), 
                "confluencePages should be array type in Expert job");
        
        // Test TestCasesGenerator job
        JobTypeDto testCasesJob = jobConfigurationLoader.getJobType("testcases-generator");
        confluencePagesParam = testCasesJob.getConfigParams().stream()
                .filter(param -> "confluencePages".equals(param.getKey()))
                .findFirst()
                .orElse(null);
                
        assertNotNull(confluencePagesParam, "TestCasesGenerator job should have confluencePages parameter");
        assertEquals("array", confluencePagesParam.getType(), 
                "confluencePages should be array type in TestCasesGenerator job");
    }

    @Test
    void testInvalidJobTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, 
                () -> jobConfigurationLoader.getJobType("nonexistent"), 
                "Should throw exception for non-existent job type");
    }

    @Test
    void testJobCategoriesAreValid() {
        Set<String> allCategories = jobConfigurationLoader.getAllCategories();
        assertNotNull(allCategories, "Categories should not be null");
        assertFalse(allCategories.isEmpty(), "Should have at least one category");
        
        // Check that expected categories exist
        assertTrue(allCategories.contains("AI"), "Should have AI category");
        assertTrue(allCategories.contains("Analysis"), "Should have Analysis category");
        assertTrue(allCategories.contains("Testing"), "Should have Testing category");
    }

    @Test
    void testRequiredIntegrationsAreValid() {
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        List<String> requiredIntegrations = expertJob.getRequiredIntegrations();
        
        assertNotNull(requiredIntegrations, "Required integrations should not be null");
        assertTrue(requiredIntegrations.contains("TrackerClient"), 
                "Expert job should require TrackerClient integration");
        assertTrue(requiredIntegrations.contains("AI"), 
                "Expert job should require AI integration");
    }

    @Test
    void testExecutionModesAreValid() {
        for (JobTypeDto jobType : allJobTypes) {
            List<String> executionModes = jobType.getExecutionModes();
            assertNotNull(executionModes, 
                    String.format("Execution modes should not be null for job type %s", jobType.getType()));
            assertFalse(executionModes.isEmpty(), 
                    String.format("Should have at least one execution mode for job type %s", jobType.getType()));
            
            // Check that execution modes are valid
            for (String mode : executionModes) {
                assertTrue(Arrays.asList("STANDALONE", "SERVER_MANAGED").contains(mode), 
                        String.format("Invalid execution mode '%s' for job type %s", mode, jobType.getType()));
            }
        }
    }

    @Test
    void testParameterValidationConstraints() {
        JobTypeDto expertJob = jobConfigurationLoader.getJobType("expert");
        
        // Test email parameter validation
        JobTypeDto.ConfigParamDefinition initiatorParam = expertJob.getConfigParams().stream()
                .filter(param -> "initiator".equals(param.getKey()))
                .findFirst()
                .orElse(null);
                
        assertNotNull(initiatorParam, "Should have initiator parameter");
        assertEquals("email", initiatorParam.getType(), "Initiator should be email type");
        assertTrue(initiatorParam.isRequired(), "Initiator should be required");
    }

    /**
     * Helper method to get all field names from a class and its superclasses.
     * This is used to compare JSON configuration parameters with Java class fields.
     */
    private Set<String> getAllFieldNames(Class<?> clazz) {
        Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> !name.startsWith("$") && !name.equals("serialVersionUID"))
                .collect(Collectors.toSet());
        
        // Add fields from superclass (Params)
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            fieldNames.addAll(getAllFieldNames(clazz.getSuperclass()));
        }
        
        return fieldNames;
    }

    @Test
    void testJobsByCategory() {
        List<JobTypeDto> aiJobs = jobConfigurationLoader.getJobsByCategory("AI");
        assertNotNull(aiJobs, "AI jobs should not be null");
        assertFalse(aiJobs.isEmpty(), "Should have at least one AI job");
        
        for (JobTypeDto job : aiJobs) {
            assertTrue(job.getCategories().contains("AI"), 
                    String.format("Job %s should be in AI category", job.getType()));
        }
    }

    @Test
    void testLocalizationSupport() {
        // Test with different locales
        JobTypeDto expertJobEn = jobConfigurationLoader.getJobType("expert", "en");
        JobTypeDto expertJobRu = jobConfigurationLoader.getJobType("expert", "ru");
        
        assertNotNull(expertJobEn, "Expert job should be available in English");
        assertNotNull(expertJobRu, "Expert job should be available in Russian");
        
        // Both should have the same type and parameters
        assertEquals(expertJobEn.getType(), expertJobRu.getType(), 
                "Job type should be the same across locales");
        assertEquals(expertJobEn.getConfigParams().size(), expertJobRu.getConfigParams().size(), 
                "Parameter count should be the same across locales");
    }

    @Test
    void testDefaultValuesAreValid() {
        for (JobTypeDto jobType : allJobTypes) {
            for (JobTypeDto.ConfigParamDefinition param : jobType.getConfigParams()) {
                String defaultValue = param.getDefaultValue();
                
                // If parameter has a default value, it should be valid for its type
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    switch (param.getType()) {
                        case "boolean":
                            assertTrue(Arrays.asList("true", "false").contains(defaultValue.toLowerCase()), 
                                    String.format("Invalid boolean default value '%s' for parameter %s in job %s", 
                                            defaultValue, param.getKey(), jobType.getType()));
                            break;
                        case "number":
                            assertDoesNotThrow(() -> Double.parseDouble(defaultValue), 
                                    String.format("Invalid number default value '%s' for parameter %s in job %s", 
                                            defaultValue, param.getKey(), jobType.getType()));
                            break;
                        case "email":
                            assertTrue(defaultValue.contains("@") || defaultValue.isEmpty(), 
                                    String.format("Invalid email default value '%s' for parameter %s in job %s", 
                                            defaultValue, param.getKey(), jobType.getType()));
                            break;
                    }
                }
            }
        }
    }
} 