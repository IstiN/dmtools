package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;
    private final AuthDebugFilter authDebugFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    private final String activeProfile;

    // Optional OAuth2 components - may not be available if OAuth2 is not configured
    @Autowired(required = false)
    private CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired(required = false)
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired(required = false)
    private CustomOidcUserService customOidcUserService;

    public SecurityConfig(EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler, 
                         AuthDebugFilter authDebugFilter, 
                         JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler,
                         @Value("${spring.profiles.active:default}") String activeProfile) {
        this.enhancedOAuth2AuthenticationSuccessHandler = enhancedOAuth2AuthenticationSuccessHandler;
        this.authDebugFilter = authDebugFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2AuthenticationFailureHandler = customOAuth2AuthenticationFailureHandler;
        this.activeProfile = activeProfile;
        logger.info("SecurityConfig initialized with custom OAuth2 handlers and resolver.");
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins for API access
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Apply to all API endpoints
        source.registerCorsConfiguration("/api/**", configuration);
        
        logger.info("🌐 CORS configured to allow all origins for /api/** endpoints");
        
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("🔧 Configuring SecurityFilterChain for active profile: {}", activeProfile);

        // Use IF_REQUIRED session management to support both JWT and OAuth2 session-based auth
        logger.info("🛡️ Applying HYBRID security settings (supports both JWT and OAuth2 session-based auth)");
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/local-login", "/api/auth/user", "/error").permitAll()
                    .requestMatchers("/api/oauth/**").permitAll()  // Allow OAuth proxy endpoints
                    .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                    .requestMatchers("/", "/test-*.html").permitAll()
                    .requestMatchers("/styleguide/**", "/css/**", "/js/**", "/img/**", "/components/**", "/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/chat/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Configure OAuth2 login only if ClientRegistrationRepository is available
        if (clientRegistrationRepository != null) {
            logger.info("🔐 OAuth2 ClientRegistrationRepository found - configuring OAuth2 login");
            http.oauth2Login(oauth2 -> {
                if (customOAuth2AuthorizationRequestResolver != null) {
                    oauth2.authorizationEndpoint(authorization -> authorization
                            .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                    );
                }
                oauth2.userInfoEndpoint(userInfo -> {
                    if (customOAuth2UserService != null) {
                        userInfo.userService(customOAuth2UserService);
                    }
                    if (customOidcUserService != null) {
                        userInfo.oidcUserService(customOidcUserService);
                    }
                })
                .successHandler(enhancedOAuth2AuthenticationSuccessHandler)
                .failureHandler(customOAuth2AuthenticationFailureHandler);
            });
        } else {
            logger.warn("⚠️ OAuth2 ClientRegistrationRepository not found - OAuth2 login disabled");
        }

        // Configure logout
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "jwt")
                .permitAll()
        );

        return http.build();
    }
}