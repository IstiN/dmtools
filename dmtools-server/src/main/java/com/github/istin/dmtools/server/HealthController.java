package com.github.istin.dmtools.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/_ah/health")
    public String appEngineHealth() {
        return "OK";
    }
} 