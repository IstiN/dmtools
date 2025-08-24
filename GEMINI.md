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

## How to Work with Java in DMTools (CRITICAL INSTRUCTIONS)

### 🔥 MANDATORY Java Commands - ALWAYS USE THESE:

#### **Fast Development Commands (use these most often):**
```bash
# Fast compile check (incremental, no clean):
./gradlew compileJava testClasses

# Test specific class (fastest):
./gradlew test --tests "ClassName"

# Test specific method:
./gradlew test --tests "ClassName.methodName"

# Compile and test specific module:
./gradlew :dmtools-core:compileJava :dmtools-core:test
./gradlew :dmtools-server:compileJava :dmtools-server:test
```

#### **Full Test Command (use before submitting):**
```bash
# Complete test run (incremental build):
./gradlew test :dmtools-core:shadowJar -x integrationTest

# Only use clean when necessary (slower but thorough):
./gradlew clean test :dmtools-core:shadowJar -x integrationTest
```

#### **When to Use Clean:**
- After major dependency changes
- When build cache seems corrupted
- Before final submission to ensure clean build
- **NOT for regular development** (wastes time recompiling everything)

### ⚠️ CRITICAL Environment Requirements:

#### **Java Version MUST be 23:**
- Current project uses JavaVersion.VERSION_23
- All workflows use JDK 23 with temurin distribution
- NEVER change this to Java 21 or any other version

#### **Multi-Module Project Structure:**
```
dmtools/
├── dmtools-core/          # Business logic (no Spring)
├── dmtools-server/        # Web services (Spring Boot)
├── dmtools-mcp-annotations/  # Annotations
└── dmtools-annotation-processor/  # Processing
```

#### **Required JVM Arguments for Testing:**
```bash
# These are automatically configured, but important to know:
-Dnet.bytebuddy.experimental=true
-XX:+EnableDynamicAgentLoading
```

### 🚫 COMMON MISTAKES TO AVOID:

#### **Wrong Commands (DO NOT USE):**
```bash
❌ java -cp ... com.github.istin...  # Don't use java directly
❌ javac src/main/java/...           # Don't use javac directly  
❌ ./gradlew run                     # Use bootRun for server
❌ ./gradlew build --parallel        # Can cause issues
❌ mvn test                          # This is Gradle project
```

#### **Correct Commands (ALWAYS USE):**
```bash
✅ ./gradlew test                    # Standard testing (incremental)
✅ ./gradlew compileJava             # Compile all modules (fast)
✅ ./gradlew classes testClasses     # Quick compile (fastest)
✅ ./gradlew test --tests "Class"    # Test specific class
✅ ./gradlew :module:test            # Test specific module
```

### 🔧 Working with Code Changes:

#### **Fast Development Workflow:**
1. **Make your code changes**
2. **Quick compile check**: `./gradlew compileJava testClasses` (fastest)
3. **Test specific changes**: `./gradlew test --tests "YourClass"` 
4. **Test affected module**: `./gradlew :dmtools-core:test` or `:dmtools-server:test`
5. **Before submitting**: `./gradlew test :dmtools-core:shadowJar -x integrationTest`

#### **If Compilation Fails:**
```bash
# Check what's wrong (incremental):
./gradlew compileJava --info --stacktrace

# Only use clean if build cache is corrupted:
./gradlew clean compileJava
```

#### **If Tests Fail:**
```bash
# Test specific failing class (fastest):
./gradlew test --tests "FailingClassName" --info

# Run with detailed failure info:
./gradlew test --info --stacktrace

# Focus on specific module:
./gradlew :dmtools-server:test --info
```

#### **Performance Tips:**
- **Use incremental builds** - avoid `clean` unless necessary
- **Test specific classes** instead of full test suite during development  
- **Focus on compilation** before running tests
- **Use module-specific commands** to reduce build time

### 📦 Understanding Module Dependencies:

```
dmtools-server → dmtools-core  ✅ (allowed)
dmtools-core → dmtools-server  ❌ (forbidden)
```

- **dmtools-core**: Standalone, uses Dagger, no Spring
- **dmtools-server**: Web layer, uses Spring Boot, depends on core

### 🧪 Testing Patterns You MUST Follow:

#### **Spring Security Tests:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
    "auth.enabled-providers=test",
    "auth.permitted-email-domains=test.com"
})
```

#### **OAuth2 Mocking:**
```java
OAuth2User mockUser = mock(OAuth2User.class);
when(mockUser.getAttribute("email")).thenReturn("test@test.com");
```

#### **Prevent Mockito Issues:**
```java
@Mock(lenient = true)
private SomeService someService;

// OR use lenient() in setup:
lenient().when(mockService.method()).thenReturn(value);
```

## AI Assistant Behavior Rules

### When Making Changes:
1. **ALWAYS use the Java commands specified above**
2. **Read existing code patterns** before implementing
3. **Follow existing architectural decisions** 
4. **Use existing utilities and frameworks**
5. **Test incrementally** using the commands provided
6. **Add comprehensive unit tests** for all new functionality

### What NOT to Do:
- ❌ Change Java versions (JavaVersion.VERSION_23 is mandatory)
- ❌ Use `java` or `javac` directly - use Gradle commands
- ❌ Add unnecessary external dependencies  
- ❌ Mix Core and Server concerns
- ❌ Skip unit test coverage
- ❌ Run commands without `./gradlew` prefix
- ❌ Use Maven commands (this is Gradle project)

### MANDATORY Before Finishing:
```bash
# ALWAYS run this final command before submitting (incremental):
./gradlew test :dmtools-core:shadowJar -x integrationTest

# Use clean only if you want to ensure completely fresh build:
./gradlew clean test :dmtools-core:shadowJar -x integrationTest
```

If this command fails, **FIX THE ISSUES** before submitting your changes.

**Performance Note**: Prefer incremental builds during development. Only use `clean` when:
- Making major dependency changes
- Preparing final submission  
- Build cache appears corrupted

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
