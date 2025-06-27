package com.github.istin.dmtools.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@SpringBootApplication(scanBasePackages = "com.github.istin.dmtools")
@EnableJpaRepositories(basePackages = "com.github.istin.dmtools.auth.repository")
@EntityScan(basePackages = "com.github.istin.dmtools.auth.model")
public class DmToolsServerApplication {

    public static void main(String[] args) {
        // For local development, you can run with -Denv=local
        SpringApplication.run(DmToolsServerApplication.class, args);
    }

    @Component
    static class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

        private final SystemCommandService systemCommandService;
        
        @org.springframework.beans.factory.annotation.Value("${app.base-url}")
        private String baseUrl;

        public ApplicationStartup(SystemCommandService systemCommandService) {
            this.systemCommandService = systemCommandService;
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            systemCommandService.openBrowser(baseUrl + "/settings.html");
        }
    }
} 