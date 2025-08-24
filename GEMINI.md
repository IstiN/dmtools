# DMTools Project - AI Assistant Instructions

## CRITICAL JAVA VERSION RULE - MUST FOLLOW

**🚨 ABSOLUTELY FORBIDDEN TO CHANGE JAVA VERSION 🚨**

- **Current Java Version:** JavaVersion.VERSION_23 (Java 23)
- **NEVER change Java version in any build.gradle files**  
- **NEVER change java-version in any GitHub workflow files**
- **NEVER suggest upgrading or downgrading Java version**

### If Java Version Change is Required:

1. **STOP IMMEDIATELY** - Do not make any changes
2. **Create response.md file** explaining:
   - Why Java version change is absolutely necessary
   - What specific functionality requires different version
   - Impact analysis of the proposed change
   - Alternative solutions that don't require version change
3. **Java version change must be explicitly mentioned in user task request** - only proceed if user specifically requested Java version modification

### Current Java Configuration (DO NOT MODIFY):

```gradle
// All build.gradle files MUST use:
java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23  
}
```

```yaml
# All GitHub workflows MUST use:
- name: Set up JDK 23
  uses: actions/setup-java@v4
  with:
    java-version: '23'
    distribution: 'temurin'
```

## Project Structure & Architecture

### Multi-Module Architecture:
- **dmtools-core**: Standalone business logic, integrations, AI framework
  - NEVER depends on dmtools-server
  - Uses Dagger for dependency injection
  - Can run as CLI or library
- **dmtools-server**: Spring Boot web services, REST APIs, UI  
  - Depends on dmtools-core
  - Uses Spring for dependency injection
  - Web-only functionality

### Critical Separation Rules:
- Core module NEVER imports server classes
- Core module NEVER depends on Spring Boot
- Server module can import and use core classes
- Maintain clean dependency direction: server → core (never reverse)

## Coding Standards

### Java Development:
- **Object-Oriented Programming**: Must be OOP driven
- **No Code Duplication**: Eliminate all code duplication
- **Unit Testing**: ALL business logic must have comprehensive unit tests
- **Error Handling**: Proper exception handling for all external calls
- **Thread Safety**: Consider and document thread safety implications

### Testing Requirements:
- Use `./gradlew test --tests "ClassName"` for specific class testing
- Use `./gradlew :dmtools-core:test` for core module tests  
- Use `./gradlew :dmtools-server:test` for server module tests
- Use `@SpringBootTest`, `@WebMvcTest`, `@MockBean` for Spring tests
- Use `@WithMockUser` for authentication testing
- Prevent `UnnecessaryStubbingException` with `lenient()` or `@Mock(lenient = true)`

### Spring Security & OAuth2 Patterns:
```java
// OAuth2 testing setup:
@SpringBootTest
@AutoConfigureTestDatabase  
@TestPropertySource(properties = {
    "auth.enabled-providers=test",
    "auth.permitted-email-domains=test.com"
})

// Mock OAuth2 user:
OAuth2User mockUser = mock(OAuth2User.class);
when(mockUser.getAttribute("email")).thenReturn("test@test.com");

// ClientRegistration testing - ALWAYS provide test registration:
List<ClientRegistration> registrations = Arrays.asList(testRegistration);
```

## File Structure & Dependencies

### Build Configuration:
- **DO NOT modify** Java version in build.gradle files
- **DO NOT add unnecessary dependencies** - most already exist
- Only add dependencies if specifically required by task
- Use existing ApplicationConfiguration for core module
- Use Spring configuration bridging for server module

### GitHub Actions & Workflows:
- Use `.github/actions/setup-environment` for DMTools-specific setup
- Java 23, Gradle cache, Node.js setup included
- Never hardcode Java versions in workflows
- Reusable workflows should remain language-agnostic

## AI Assistant Behavior Rules

### When Making Changes:
1. **Read existing code patterns** before implementing
2. **Follow existing architectural decisions** 
3. **Use existing utilities and frameworks**
4. **Maintain consistency** with current codebase
5. **Add comprehensive unit tests** for all new functionality

### What NOT to Do:
- ❌ Change Java versions (JavaVersion.VERSION_21 → VERSION_23 is forbidden)
- ❌ Add unnecessary external dependencies  
- ❌ Mix Core and Server concerns
- ❌ Skip unit test coverage
- ❌ Hardcode configuration values
- ❌ Create duplicate functionality

### Testing & Quality Assurance:
- Run `./gradlew clean test :dmtools-core:shadowJar -x integrationTest` before submitting
- Fix all compilation errors and test failures
- Ensure consistent code style and formatting
- Document any complex logic or architectural decisions

## Project Context

This is a multi-module Java project using:
- **Spring Boot 3.x** for web services
- **Gradle** for build management  
- **JUnit 5** for testing
- **Mockito** for mocking
- **GitHub Actions** for CI/CD
- **Docker** for deployment

The project focuses on development tools and AI-assisted workflows with strict architectural boundaries and comprehensive testing requirements.

---

**Remember: JAVA 23 VERSION IS MANDATORY - DO NOT CHANGE UNDER ANY CIRCUMSTANCES WITHOUT EXPLICIT EXPLANATION AND APPROVAL**
