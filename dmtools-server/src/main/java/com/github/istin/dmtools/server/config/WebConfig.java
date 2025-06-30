package com.github.istin.dmtools.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Remove default welcome page mapping to prevent conflicts with our WebController
        // Spring Boot by default maps "/" to "index.html" if it exists
        // Since we deleted index.html but want custom redirect behavior, we override this
    }
} 