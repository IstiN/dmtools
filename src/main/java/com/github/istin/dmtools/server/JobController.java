package com.github.istin.dmtools.server;

import com.github.istin.dmtools.job.JobParams;
import com.github.istin.dmtools.common.utils.PropertyReader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final PropertyReader propertyReader;

    @Autowired
    public JobController(JobService jobService, PropertyReader propertyReader) {
        this.jobService = jobService;
        this.propertyReader = propertyReader;
    }

    @PostMapping("/executeJob")
    public ResponseEntity<String> executeJob(@RequestBody String requestBody) {
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
    public ResponseEntity<Map<String, String>> getConfiguration() {
        try {
            Map<String, String> properties = propertyReader.getAllProperties();
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
} 