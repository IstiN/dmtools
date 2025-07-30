package com.github.istin.dmtools.server.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@EntityScan(basePackages = {
    "com.github.istin.dmtools.auth.model",
    "com.github.istin.dmtools.server.model"
})
@EnableJpaRepositories(basePackages = {
    "com.github.istin.dmtools.auth.repository", 
    "com.github.istin.dmtools.server.repository"
})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
public class TestJpaConfiguration {
} 