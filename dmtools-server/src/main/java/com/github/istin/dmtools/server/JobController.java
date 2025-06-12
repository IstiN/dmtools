package com.github.istin.dmtools.server;

import com.github.istin.dmtools.job.JobParams;
import com.github.istin.dmtools.common.utils.PropertyReader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Job Management", description = "API for executing jobs and managing configuration")
public class JobController {

    private final JobService jobService;
    private final PropertyReader propertyReader;

    @Autowired
    public JobController(JobService jobService, PropertyReader propertyReader) {
        this.jobService = jobService;
        this.propertyReader = propertyReader;
    }

    @PostMapping("/executeJob")
    @Operation(summary = "Execute a job", description = "Executes a job with the provided parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job executed successfully",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<String> executeJob(
            @Parameter(description = "Job parameters as JSON", required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"name\":\"exampleJob\",\"parameters\":{\"param1\":\"value1\"}}")))
            @RequestBody String requestBody) {
        try {
            JSONObject jsonRequest = new JSONObject(requestBody);
            JobParams jobParams = new JobParams(jsonRequest);

            jobService.executeJob(jobParams);
            return ResponseEntity.ok("Job '" + jobParams.getName() + "' executed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error executing job: " + e.getMessage());
        }
    }

    @GetMapping("/config")
    @Operation(summary = "Get configuration", description = "Retrieves non-sensitive application properties and configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getConfiguration() {
        try {
            Map<String, String> allProperties = propertyReader.getAllProperties();
            // Filter out sensitive properties
            Map<String, String> safeProperties = allProperties.entrySet().stream()
                .filter(entry -> {
                    String key = entry.getKey().toLowerCase();
                    // Filter out properties containing sensitive information
                    return !key.contains("token") &&
                           !key.contains("auth") &&
                           !key.contains("key") &&
                           !key.contains("secret") &&
                           !key.contains("password") &&
                           !key.contains("credential");
                })
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
            return ResponseEntity.ok(safeProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
} 