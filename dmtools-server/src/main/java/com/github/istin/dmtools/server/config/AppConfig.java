package com.github.istin.dmtools.server.config;

import com.github.istin.dmtools.common.utils.PropertyReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PropertyReader propertyReader() {
        return new PropertyReader();
    }
} 