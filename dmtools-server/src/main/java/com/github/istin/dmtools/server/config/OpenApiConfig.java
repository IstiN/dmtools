package com.github.istin.dmtools.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    // Basic configuration - OpenAPI will be auto-configured by SpringDoc
    // Swagger UI will be available at /swagger-ui.html
    // OpenAPI spec will be available at /v3/api-docs
    
    @Bean
    public OpenAPI dmToolsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DMTools API")
                        .description("DMTools Server API for presentation generation, job execution, and development management tools")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("DMTools Team")
                                .email("uladzimir.klyshevich@gmail.com")
                                .url("https://github.com/IstiN/dmtools"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url(baseUrl).description("Local server"),
                        new Server().url("https://api.dmtools.example.com").description("Production server")
                ));
    }
} 