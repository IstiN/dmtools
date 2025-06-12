package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final AuthDebugFilter authDebugFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, AuthDebugFilter authDebugFilter, CustomOAuth2UserService customOAuth2UserService) {
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.authDebugFilter = authDebugFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        logger.info("SecurityConfig initialized with custom OAuth2AuthenticationSuccessHandler and AuthDebugFilter.");
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        // Add detailed logging for the token response
        restTemplate.getInterceptors().add((request, body, execution) -> {
            logger.debug("[OAuth2] Request to: {}", request.getURI());
            logger.debug("[OAuth2] Request headers: {}", request.getHeaders());
            logger.debug("[OAuth2] Request body: {}", new String(body));
            var response = execution.execute(request, body);
            logger.debug("[OAuth2] Response status: {}", response.getStatusCode());
            logger.debug("[OAuth2] Response headers: {}", response.getHeaders());
            // Read response body for logging
            byte[] responseBody = response.getBody().readAllBytes();
            logger.debug("[OAuth2] Response body: {}", new String(responseBody));
            // Return a new response with the consumed body
            return new org.springframework.http.client.ClientHttpResponse() {
                @Override
                public org.springframework.http.HttpStatus getStatusCode() throws java.io.IOException {
                    // Convert HttpStatusCode to HttpStatus for compatibility
                    return org.springframework.http.HttpStatus.valueOf(response.getRawStatusCode());
                }
                @Override
                @SuppressWarnings("removal")
                public int getRawStatusCode() throws java.io.IOException {
                    return response.getRawStatusCode();
                }
                @Override
                public String getStatusText() throws java.io.IOException {
                    return response.getStatusText();
                }
                @Override
                public void close() {
                    response.close();
                }
                @Override
                public org.springframework.http.HttpHeaders getHeaders() {
                    return response.getHeaders();
                }
                @Override
                public java.io.InputStream getBody() {
                    return new java.io.ByteArrayInputStream(responseBody);
                }
            };
        });
        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        logger.info("Configuring SecurityFilterChain...");
        http
                .addFilterBefore(authDebugFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> {
                    logger.info("Configuring CORS...");
                    cors.configurationSource(request -> {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.applyPermitDefaultValues();
                        return configuration;
                    });
                })
                .authorizeHttpRequests(authorizeRequests -> {
                    logger.info("Configuring authorization requests...");
                    authorizeRequests
                            .requestMatchers("/", "/index.html", "/login", "/oauth2/authorization/**", "/login/oauth2/code/*", "/error", "/css/**", "/js/**", "/img/**", "/styleguide/**", "/components/**", "/api/config", "/is-local", "/api/v1/chat/health", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/test-oauth2.html").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2Login(oauth2Login -> {
                    logger.info("Configuring OAuth2 login...");
                    oauth2Login
                            .authorizationEndpoint(authorizationEndpoint -> {
                                logger.info("Configuring authorization endpoint base URI to /oauth2/authorization");
                                authorizationEndpoint.baseUri("/oauth2/authorization");
                            })
                            .tokenEndpoint(token -> token.accessTokenResponseClient(accessTokenResponseClient()))
                            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                            .successHandler(oAuth2AuthenticationSuccessHandler)
                            .failureUrl("/?error=true");
                    logger.info("OAuth2 login configured with success handler and failure URL.");
                })
                .sessionManagement(session -> {
                    logger.info("Configuring session management...");
                    // Explicitly set the session creation policy
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
                    // Set session fixation protection
                    session.sessionFixation().migrateSession();
                    // Configure maximum sessions
                    session.maximumSessions(1)
                           .maxSessionsPreventsLogin(false);
                    logger.info("Session management configured with ALWAYS creation policy and session fixation protection.");
                })
                .logout(logout -> {
                    logger.info("Configuring logout...");
                    logout
                            .logoutSuccessUrl("/")
                            .permitAll();
                })
                .exceptionHandling(e -> {
                    logger.info("Configuring exception handling...");
                    e.authenticationEntryPoint((request, response, authException) -> {
                        logger.warn("Authentication failed for request to {}: {}. Entry point triggered.", request.getRequestURI(), authException.getMessage());
                        // For AJAX requests, return 401
                        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                            logger.info("Request is AJAX. Sending 401 Unauthorized.");
                            response.sendError(401, "Unauthorized");
                        } else {
                            // For regular requests, redirect to home page
                            logger.info("Regular request. Redirecting to /");
                            response.sendRedirect("/");
                        }
                    });
                })
                .csrf(AbstractHttpConfigurer::disable);
        logger.info("SecurityFilterChain configuration complete.");
        return http.build();
    }
}