package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    private final EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;
    private final AuthDebugFilter authDebugFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    private final AuthConfigProperties authConfigProperties;
    private final String activeProfile;

    // Optional OAuth2 components - may not be available if OAuth2 is not configured
    @Autowired(required = false)
    @Lazy
    private CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;

    @Autowired(required = false)
    @Lazy
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired(required = false)
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired(required = false)
    private CustomOidcUserService customOidcUserService;

    public SecurityConfig(EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler, 
                         AuthDebugFilter authDebugFilter, 
                         JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler,
                         AuthConfigProperties authConfigProperties,
                         @Value("${spring.profiles.active:default}") String activeProfile) {
        this.enhancedOAuth2AuthenticationSuccessHandler = enhancedOAuth2AuthenticationSuccessHandler;
        this.authDebugFilter = authDebugFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2AuthenticationFailureHandler = customOAuth2AuthenticationFailureHandler;
        this.authConfigProperties = authConfigProperties;
        this.activeProfile = activeProfile;
        logger.info("SecurityConfig initialized with custom OAuth2 handlers and resolver.");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return username -> {
            if (authConfigProperties.isLocalStandaloneMode() && username.equals(authConfigProperties.getAdminUsername())) {
                return User.withUsername(authConfigProperties.getAdminUsername())
                        .password(passwordEncoder.encode(authConfigProperties.getAdminPassword()))
                        .roles("ADMIN", "USER")
                        .build();
            }
            // Fallback to an empty UserDetails or throw an exception if no other user service is configured
            // For now, we'll return a minimal user if not in standalone mode or not the admin user
            return User.withUsername(username)
                    .password(passwordEncoder.encode("dummy")) // Dummy password for non-admin users in non-standalone mode
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    @ConditionalOnProperty(name = "auth.enabled-providers", matchIfMissing = false)
    public ClientRegistrationRepository clientRegistrationRepository(@Autowired(required = false) List<ClientRegistration> clientRegistrations) {
        if (authConfigProperties.isLocalStandaloneMode()) {
            logger.warn("⚠️ Local standalone mode enabled. OAuth2 ClientRegistrationRepository disabled.");
            // Return null or don't create bean in standalone mode
            throw new IllegalStateException("ClientRegistrationRepository should not be created in standalone mode");
        }

        if (clientRegistrations == null || clientRegistrations.isEmpty()) {
            logger.warn("⚠️ No ClientRegistration beans found. OAuth2 ClientRegistrationRepository will be empty.");
            // Create a dummy registration to avoid empty repository
            ClientRegistration dummyRegistration = ClientRegistration.withRegistrationId("dummy")
                    .clientId("dummy")
                    .clientSecret("dummy")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri("http://localhost/dummy")
                    .tokenUri("http://localhost/dummy")
                    .redirectUri("http://localhost/dummy")
                    .userNameAttributeName("name")
                    .build();
            return new InMemoryClientRegistrationRepository(List.of(dummyRegistration));
        }

        Set<String> enabledProviders = authConfigProperties.getEnabledProvidersAsSet();
        List<ClientRegistration> filteredRegistrations = clientRegistrations.stream()
                .filter(reg -> enabledProviders.isEmpty() || enabledProviders.contains(reg.getRegistrationId()))
                .collect(Collectors.toList());

        if (filteredRegistrations.isEmpty()) {
            logger.warn("⚠️ No enabled OAuth2 providers found based on 'auth.enabled-providers'. OAuth2 login disabled.");
        } else {
            logger.info("🔐 Configured OAuth2 ClientRegistrationRepository with providers: {}",
                    filteredRegistrations.stream().map(ClientRegistration::getRegistrationId).collect(Collectors.joining(", ")));
        }
        return new InMemoryClientRegistrationRepository(filteredRegistrations);
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
        
        // Allow all origins for development/testing - adjust for production
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
        
        // Apply CORS to all endpoints, not just /api/**
        source.registerCorsConfiguration("/**", configuration);
        
        logger.info("🌐 CORS configured to allow all origins for ALL endpoints");
        
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("🔧 Configuring SecurityFilterChain for active profile: {}", activeProfile);

        // Use IF_REQUIRED session management to support both JWT and OAuth2 session-based auth
        logger.info("🛡️ Applying HYBRID security settings (supports both JWT and OAuth2 session-based auth)");
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // Configure security headers including HSTS for HTTPS
        http.headers(headers -> headers
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true))
            .frameOptions().deny()
            .contentTypeOptions().and());

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> {
                if (authConfigProperties.isLocalStandaloneMode()) {
                    logger.info("🔒 Local standalone mode enabled. Permitting /api/auth/local-login and /api/auth/config");
                    auth.requestMatchers("/api/auth/local-login", "/api/auth/config").permitAll();
                } else {
                    auth.requestMatchers("/api/auth/local-login").denyAll(); // Deny local login if not in standalone mode
                }
                auth
                    .requestMatchers("/api/auth/user", "/api/auth/public-test", "/error", "/api/auth/config").permitAll()
                    .requestMatchers("/api/oauth/**", "/api/oauth-proxy/**").permitAll()  // Allow OAuth proxy endpoints
                    .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                    .requestMatchers("/", "/test-*.html").permitAll()
                    .requestMatchers("/temp/**", "/styleguide/**", "/css/**", "/js/**", "/img/**", "/components/**").permitAll()  // Added /temp/** for test files
                    .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/chat/**").permitAll()
                    .requestMatchers("/mcp/stream/**").permitAll()  // Allow public access to MCP SSE streams
                    .requestMatchers("/api/files/download/**").permitAll()  // Allow public access to file downloads
                    .requestMatchers("/api/v1/job-configurations/*/webhook").permitAll()  // Allow webhook endpoints to use API key authentication
                    .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Configure OAuth2 login only if ClientRegistrationRepository is available and not in standalone mode
        if (!authConfigProperties.isLocalStandaloneMode() && clientRegistrationRepository != null) {
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
        } else if (!authConfigProperties.isLocalStandaloneMode()) {
            logger.warn("⚠️ OAuth2 ClientRegistrationRepository not found and not in standalone mode - OAuth2 login disabled");
        } else {
            logger.info("ℹ️ OAuth2 login explicitly disabled due to local standalone mode.");
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