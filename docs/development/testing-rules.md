# DMTools Test Fixing Rules

These are project-specific rules and context for automatically fixing test failures in the DMTools project.

## Project Architecture Context

### Core vs Server Module Structure
DMTools follows a **multi-module architecture** with clear separation:
- **dmtools-core**: Standalone business logic, integrations, AI framework, job execution
- **dmtools-server**: Spring Boot web services, REST APIs, authentication, UI

**Critical**: Core module NEVER depends on server module. Only server can depend on core.

### Common Test Failure Patterns

#### 1. Spring Security Configuration Issues
- **Problem**: Authentication/authorization configuration changes breaking tests
- **Solution**: Check SecurityConfig.java for proper OAuth2 and local auth setup
- **Files to check**: 
  - `dmtools-server/src/main/java/com/github/istin/dmtools/auth/SecurityConfig.java`
  - `dmtools-server/src/main/java/com/github/istin/dmtools/auth/config/AuthConfigProperties.java`

#### 2. OAuth2 Service Implementation Problems
- **Problem**: CustomOAuth2UserService email domain validation failures
- **Solution**: Ensure proper email domain checking and user service configuration
- **Files to check**:
  - `dmtools-server/src/main/java/com/github/istin/dmtools/auth/security/CustomOAuth2UserServiceImpl.java`
  - Test files in `dmtools-server/src/test/java/com/github/istin/dmtools/auth/`

#### 3. Configuration Properties Issues
- **Problem**: @ConfigurationProperties not properly bound or missing environment variables
- **Solution**: Check AuthConfigProperties and ensure proper Spring configuration
- **Environment Variables**: AUTH_ENABLED_PROVIDERS, AUTH_PERMITTED_EMAIL_DOMAINS, ADMIN_USERNAME, ADMIN_PASSWORD

#### 4. Dependency Injection Problems
- **Problem**: Missing @Component, @Service, @Configuration annotations or circular dependencies
- **Solution**: Ensure proper Dagger (core) vs Spring (server) dependency injection patterns
- **Pattern**: Core uses Dagger, Server uses Spring and bridges to core

#### 5. Test Configuration Issues
- **Problem**: Missing test-specific configuration or conflicting beans
- **Solution**: Check @TestConfiguration classes and test application properties
- **Files to check**: `application-test.properties`, `@TestConfiguration` classes

#### 6. Missing Imports and Compilation Errors
- **Problem**: Import statements missing after code changes
- **Solution**: Add proper imports for Spring Security, OAuth2, and custom classes
- **Common imports**: 
  - Spring Security: `org.springframework.security.*`
  - OAuth2: `org.springframework.security.oauth2.*`
  - Project classes: `com.github.istin.dmtools.*`

## Test Fixing Best Practices

### Spring Boot Tests
1. **Use @SpringBootTest** for integration tests that need full application context
2. **Use @WebMvcTest** for testing only the web layer
3. **Use @MockBean** to mock Spring-managed beans
4. **Check test slices**: Ensure you're using the right test slice annotation

### Security Tests
1. **Mock Authentication**: Use @WithMockUser or custom security context
2. **OAuth2 Testing**: Mock OAuth2 user details and authentication
3. **Configuration Testing**: Test both enabled and disabled auth modes

### Configuration Tests
1. **Property Binding**: Test @ConfigurationProperties with @EnableConfigurationProperties
2. **Conditional Beans**: Test @ConditionalOnProperty behavior
3. **Profile-specific**: Test different application profiles

### Common Fix Patterns

#### Missing Bean Configuration
```java
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public AuthConfigProperties authConfigProperties() {
        return new AuthConfigProperties();
    }
}
```

#### OAuth2 Test Setup
```java
@WithMockUser
@Test
public void testOAuth2Authentication() {
    // Test OAuth2 authentication flow
}
```

#### Security Context Mocking
```java
@MockBean
private CustomOAuth2UserServiceImpl oauth2UserService;

@Test
public void testSecurityConfiguration() {
    // Mock security components
}
```

## Project-Specific Context

### Authentication Modes
- **Local Standalone**: Using ADMIN_USERNAME/ADMIN_PASSWORD
- **OAuth2**: Google, GitHub, etc. based on AUTH_ENABLED_PROVIDERS
- **Email Filtering**: AUTH_PERMITTED_EMAIL_DOMAINS for domain restriction

### Key Configuration Classes
- `AuthConfigProperties`: Main configuration for authentication modes
- `SecurityConfig`: Spring Security configuration with dynamic OAuth2 setup
- `AuthController`: REST endpoints for authentication
- `CustomOAuth2UserServiceImpl`: Email domain validation service

### Test Structure
- Core tests: `dmtools-core/src/test/java/`
- Server tests: `dmtools-server/src/test/java/`
- Focus on: Authentication, Security, Configuration, and OAuth2 service tests

### Build System
- **Gradle**: Multi-module build with Java 21
- **Test Command**: `./gradlew test`
- **Module-specific**: `./gradlew :dmtools-server:test`

## Debugging Test Failures

### Check These First
1. **Application Properties**: Ensure test-specific properties are correct
2. **Mock Configuration**: Verify all required beans are mocked properly
3. **Security Context**: Check if security context is properly set up for tests
4. **Import Statements**: Verify all necessary imports are present
5. **Annotation Usage**: Check for correct test annotations (@Test, @SpringBootTest, etc.)

### Common Error Messages and Solutions

#### "No qualifying bean of type..."
- **Solution**: Add @MockBean or @TestConfiguration to provide missing beans

#### "Authentication object cannot be null"
- **Solution**: Add @WithMockUser or set up security context in test

#### "OAuth2AuthenticationException"
- **Solution**: Mock OAuth2 user service or authentication details

#### "ConfigurationProperties not found"
- **Solution**: Add @EnableConfigurationProperties or provide test configuration

#### "CircularReferenceException"  
- **Solution**: Check dependency injection setup, may need to break circular dependencies

## Anti-Analysis-Paralysis Rules for OAuth2 Test Fixes

**CRITICAL**: When fixing OAuth2/WebMvcTest compilation errors, follow these EXACT steps:

### Step 1: Fix @WebMvcTest Syntax (FIRST)
```java
// ❌ WRONG - causes compilation error
@WebMvcTest(AuthConfigurationController.class, properties = "auth.enabled-providers=google")

// ✅ CORRECT - separate annotations
@WebMvcTest(AuthConfigurationController.class)
@TestPropertySource(properties = "auth.enabled-providers=google")
```

### Step 2: Choose ONE Approach (NO OVERTHINKING)
**Decision Rule**: Look at test class name and pick immediately:
- If name contains "OAuth2" or "Google" → Enable OAuth2 (Option A)
- If name contains "NoProvider" or "Empty" → Disable OAuth2 (Option B)  
- If unsure → Pick Option B (simpler)

**Option A - Enable OAuth2:**
```java
@WebMvcTest(AuthConfigurationController.class)
@TestPropertySource(properties = "auth.enabled-providers=google")
@Import({SecurityConfig.class, AuthConfigProperties.class})

@TestConfiguration
static class TestConfig {
    @Bean @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration
            .withRegistrationId("google")
            .clientId("test-client")
            .clientSecret("test-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/login/oauth2/code/google")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v2/userinfo")
            .build();
        return new InMemoryClientRegistrationRepository(registration);
    }
}
```

**Option B - Disable OAuth2:**
```java
@WebMvcTest(AuthConfigurationController.class)
@TestPropertySource(properties = "auth.enabled-providers=")
```

### Step 3: STOP Writing Comments About "What Should Be Tested"
- ❌ Don't analyze what the test "should" do in comments
- ❌ Don't write paragraphs about different approaches
- ❌ Don't second-guess the approach once chosen
- ✅ Fix compilation error, run test, done

### Step 4: Common OAuth2 Imports (Add If Missing)
```java
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
```

## Success Criteria for Auto-Fix

1. **All tests pass**: The primary goal
2. **No new failures**: Don't break existing working tests
3. **Clean imports**: Remove unused imports, add missing ones
4. **Proper mocking**: Use appropriate mocking strategies for external dependencies
5. **Security context**: Properly handle authentication in security tests
6. **Configuration coverage**: Ensure all configuration scenarios are tested
7. **NO ANALYSIS PARALYSIS**: Fix quickly, don't overthink

## Example Fix Commands

If you need to run tests locally to verify fixes:
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :dmtools-server:test

# Run specific test class
./gradlew :dmtools-server:test --tests "AuthConfigurationControllerTest"

# Run with debug info
./gradlew test --info --stacktrace
```
