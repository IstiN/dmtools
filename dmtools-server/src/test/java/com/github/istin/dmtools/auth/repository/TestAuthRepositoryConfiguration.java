package com.github.istin.dmtools.auth.repository;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@TestConfiguration
@EntityScan(basePackages = {
    "com.github.istin.dmtools.auth.model",
    "com.github.istin.dmtools.server.model"
})
@EnableJpaRepositories(basePackages = {
    "com.github.istin.dmtools.auth.repository", 
    "com.github.istin.dmtools.server.repository"
})
public class TestAuthRepositoryConfiguration {
}
