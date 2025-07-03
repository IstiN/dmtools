package com.github.istin.dmtools.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.Scopes;
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
                        .description("DMTools Server API for presentation generation, job execution, and development management tools.\n\n" +
                                "**Authentication Required**: Most endpoints require authentication. Please authenticate using one of the OAuth2 providers or JWT token below.")
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
                ))
                .components(new Components()
                        .addSecuritySchemes("oauth2_google", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Google OAuth2 Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(baseUrl + "/oauth2/authorization/google")
                                                .tokenUrl(baseUrl + "/login/oauth2/code/google")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "Read user profile")
                                                        .addString("email", "Read user email")))))
                        .addSecuritySchemes("oauth2_github", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("GitHub OAuth2 Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(baseUrl + "/oauth2/authorization/github")
                                                .tokenUrl(baseUrl + "/login/oauth2/code/github")
                                                .scopes(new Scopes()
                                                        .addString("read:user", "Read user profile")
                                                        .addString("user:email", "Read user email")))))
                        .addSecuritySchemes("oauth2_microsoft", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Microsoft OAuth2 Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(baseUrl + "/oauth2/authorization/microsoft")
                                                .tokenUrl(baseUrl + "/login/oauth2/code/microsoft")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "Read user profile")
                                                        .addString("email", "Read user email")))))
                        .addSecuritySchemes("bearer_jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token Authentication")))
                .addSecurityItem(new SecurityRequirement().addList("oauth2_google"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2_github"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2_microsoft"))
                .addSecurityItem(new SecurityRequirement().addList("bearer_jwt"));
    }
} 