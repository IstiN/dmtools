# DMTools - Architecture Documentation

## Multi-Module System Architecture

DMTools is designed as a **multi-module Gradle project** with clear architectural boundaries between business logic and presentation layers.

```mermaid
graph TB
    subgraph "dmtools-server Module"
        A[Web UI Layer]
        B[REST API Layer]
        C[Security Layer]
        D[Spring Boot Configuration]
    end
    
    subgraph "dmtools-core Module"
        E[Job Orchestrator]
        F[Role-Based Services]
        G[AI Integration Framework]
        H[External Integrations]
        I[Business Logic Layer]
        J[Common Utilities]
    end
    
    subgraph "External Dependencies"
        K[Spring Boot]
        L[AI Providers]
        M[External APIs]
        N[Automation Tools]
    end
    
    A --> E
    B --> E
    C --> I
    D --> K
    
    E --> F
    F --> G
    F --> H
    G --> L
    H --> M
    I --> J
    
    F --> N
```

### Module Responsibilities

- **dmtools-server**: Web services, authentication, REST APIs, UI
- **dmtools-core**: Business logic, integrations, AI framework, job execution

## System Architecture Overview

DMTools follows a layered, modular architecture designed for scalability, maintainability, and extensibility. The system is built around a plugin-based job execution framework with AI integration at its core.

```mermaid
graph TB
    subgraph "Presentation Layer"
        A[Web UI]
        B[REST API]
        C[CLI Interface]
        D[Chrome Extension]
    end
    
    subgraph "Application Layer"
        E[Job Orchestrator]
        F[Role-Based Services]
        G[AI Agents]
        H[Report Generators]
    end
    
    subgraph "Business Logic Layer"
        I[Business Rules Engine]
        J[Workflow Engine]
        K[Context Orchestrator]
        L[Expert System]
    end
    
    subgraph "Integration Layer"
        M[Tracker Adapters]
        N[Source Control Adapters]
        O[AI Provider Adapters]
        P[Document Adapters]
    end
    
    subgraph "Data Layer"
        Q[Configuration Store]
        R[Cache Layer]
        S[File System]
        T[External APIs]
    end
    
    A --> E
    B --> E
    C --> E
    D --> E
    
    E --> F
    F --> G
    F --> H
    
    G --> I
    F --> J
    G --> K
    H --> L
    
    I --> M
    J --> N
    K --> O
    L --> P
    
    M --> Q
    N --> R
    O --> S
    P --> T
```

## Core Components

### 1. Job Execution Framework

The foundation of DMTools is its flexible job execution system that allows for pluggable, parameterized tasks.

```mermaid
classDiagram
    class Job {
        <<interface>>
        +getName() String
        +getParamsClass() Class
        +runJob(params Object) void
        +getAi() AI
    }
    
    class AbstractJob {
        <<abstract>>
        -ai AI
        -propertyReader PropertyReader
        +initAI() void
        +getTicketTracker() TrackerClient
    }
    
    class JobRunner {
        +main(args String[]) void
        +initMetadata(job Job, params Object) void
        -JOBS List~Job~
    }
    
    class JobParams {
        -name String
        -params JSONObject
        +getParamsByClass(clazz Class) Object
    }
    
    Job <|-- AbstractJob
    AbstractJob <|-- DevProductivityReport
    AbstractJob <|-- TestCasesGenerator
    AbstractJob <|-- CodeGenerator
    AbstractJob <|-- Expert
    
    JobRunner --> Job : executes
    JobRunner --> JobParams : uses
```

### 2. AI Integration Architecture

DMTools provides a flexible AI abstraction layer supporting multiple providers and specialized agents.

```mermaid
graph TD
    subgraph "AI Abstraction Layer"
        A[AI Interface]
        B[ConversationObserver]
        C[Metadata]
    end
    
    subgraph "AI Implementations"
        D[BasicOpenAI]
        E[BasicGeminiAI]
        F[JSAIClient]
        G[Custom AI Providers]
    end
    
    subgraph "Specialized Agents"
        H[TeamAssistantAgent]
        I[SummaryContextAgent]
        J[ContentMergeAgent]
        K[RequestDecompositionAgent]
        L[SourceImpactAssessmentAgent]
    end
    
    subgraph "Prompt Management"
        M[PromptManager]
        N[Template Engine]
        O[Prompt Templates]
    end
    
    A --> D
    A --> E
    A --> F
    A --> G
    
    D --> H
    E --> I
    F --> J
    G --> K
    G --> L
    
    H --> M
    I --> N
    J --> O
    K --> M
    L --> N
```

### 3. Integration Architecture

The system integrates with multiple external services through a unified adapter pattern.

```mermaid
graph LR
    subgraph "DMTools Core"
        A[TrackerClient Interface]
        B[SourceCode Interface]
        C[AI Interface]
    end
    
    subgraph "Issue Tracking"
        D[JiraClient]
        E[RallyClient]
    end
    
    subgraph "Source Control"
        F[GitHubClient]
        G[GitLabClient]
        H[BitbucketClient]
    end
    
    subgraph "Documentation"
        I[ConfluenceClient]
        J[NotionClient]
    end
    
    subgraph "Design & Mobile"
        K[FigmaClient]
        L[AppCenterClient]
    end
    
    subgraph "Cloud Services"
        M[FirebaseClient]
        N[AWSClient]
    end
    
    A --> D
    A --> E
    B --> F
    B --> G
    B --> H
    C --> I
    C --> J
    C --> K
    C --> L
    C --> M
    C --> N
```

### 4. Role-Based Module Architecture

Each role has dedicated modules with specific capabilities and workflows.

```mermaid
graph TB
    subgraph "Business Analyst"
        A[RequirementsCollector]
        B[UserStoryGenerator]
        C[BusinessAnalyticDORGeneration]
        D[BAProductivityReport]
    end
    
    subgraph "Developer"
        E[CodeGenerator]
        F[UnitTestsGenerator]
        G[CommitsTriage]
        H[DevProductivityReport]
    end
    
    subgraph "QA Engineer"
        I[TestCasesGenerator]
        J[AutomationTester]
        K[QAProductivityReport]
        L[TestExecutionFramework]
    end
    
    subgraph "Scrum Master"
        M[ScrumMasterDaily]
        N[SprintAnalytics]
        O[TeamMetrics]
    end
    
    subgraph "Solution Architect"
        P[SolutionArchitectureCreator]
        Q[DiagramsCreator]
        R[TechnicalSpecification]
    end
    
    subgraph "Shared Services"
        S[AI Service]
        T[Integration Service]
        U[Report Service]
        V[Context Service]
    end
    
    A --> S
    B --> S
    E --> S
    F --> S
    I --> S
    J --> S
    M --> S
    P --> S
    
    A --> T
    E --> T
    I --> T
    M --> T
    P --> T
    
    C --> U
    G --> U
    K --> U
    N --> U
    Q --> U
    
    B --> V
    F --> V
    J --> V
    O --> V
    R --> V
```

## Data Flow Architecture

### 1. Request Processing Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server/CLI
    participant J as Job Orchestrator
    participant A as AI Service
    participant I as Integration Service
    participant R as Report Generator
    
    C->>S: Submit Job Request
    S->>J: Parse & Validate Params
    J->>A: Initialize AI Context
    J->>I: Fetch External Data
    I-->>J: Return Integration Data
    J->>A: Process with AI
    A-->>J: Return AI Results
    J->>R: Generate Reports
    R-->>J: Return Generated Reports
    J-->>S: Job Completion Result
    S-->>C: Response with Artifacts
```

### 2. AI Processing Flow

```mermaid
graph TD
    A[Input Request] --> B{Requires AI?}
    B -->|Yes| C[Context Preparation]
    B -->|No| D[Direct Processing]
    
    C --> E[Chunk Preparation]
    E --> F[Prompt Template Selection]
    F --> G[AI Provider Selection]
    G --> H[AI Processing]
    H --> I[Response Validation]
    I --> J[Context Update]
    J --> K[Result Formatting]
    
    D --> K
    K --> L[Output Generation]
```

### 3. Integration Data Flow

```mermaid
graph LR
    subgraph "Data Sources"
        A[Jira Tickets]
        B[Git Commits]
        C[Confluence Pages]
        D[Test Results]
        E[Figma Designs]
    end
    
    subgraph "Processing Pipeline"
        F[Data Extraction]
        G[Data Transformation]
        H[Data Validation]
        I[Context Enrichment]
    end
    
    subgraph "AI Processing"
        J[Prompt Generation]
        K[AI Analysis]
        L[Response Processing]
    end
    
    subgraph "Output Generation"
        M[Report Generation]
        N[Documentation Creation]
        O[Code Generation]
        P[Artifact Storage]
    end
    
    A --> F
    B --> F
    C --> F
    D --> F
    E --> F
    
    F --> G
    G --> H
    H --> I
    
    I --> J
    J --> K
    K --> L
    
    L --> M
    L --> N
    L --> O
    M --> P
    N --> P
    O --> P
```

## Automation Architecture

### Web and Mobile Automation Framework

```mermaid
graph TB
    subgraph "Automation Orchestrator"
        A[AutomationTester]
        B[TestScriptEngine]
    end
    
    subgraph "Bridge Layer"
        C[SeleniumBridge]
        D[PlaywrightBridge]
        E[AppiumBridge]
    end
    
    subgraph "Driver Layer"
        F[WebDriver]
        G[Playwright Engine]
        H[Appium Server]
    end
    
    subgraph "Target Applications"
        I[Web Applications]
        J[Mobile Apps - iOS]
        K[Mobile Apps - Android]
        L[Desktop Applications]
    end
    
    A --> B
    B --> C
    B --> D
    B --> E
    
    C --> F
    D --> G
    E --> H
    
    F --> I
    G --> I
    G --> L
    H --> J
    H --> K
```

## Configuration and Deployment Architecture

### Configuration Management

```mermaid
graph TD
    subgraph "Configuration Sources"
        A[Environment Variables]
        B[Property Files]
        C[Command Line Args]
        D[Default Values]
    end
    
    subgraph "Configuration Processing"
        E[PropertyReader]
        F[Configuration Validation]
        G[Secret Management]
    end
    
    subgraph "Runtime Configuration"
        H[AI Provider Config]
        I[Integration Config]
        J[Job Parameters]
        K[Security Settings]
    end
    
    A --> E
    B --> E
    C --> E
    D --> E
    
    E --> F
    F --> G
    
    G --> H
    G --> I
    G --> J
    G --> K
```

### Deployment Options

```mermaid
graph TB
    subgraph "Deployment Modes"
        A[Standalone JAR]
        B[Spring Boot Web App]
        C[Library Integration]
        D[Chrome Extension]
    end
    
    subgraph "Runtime Environments"
        E[Local Development]
        F[CI/CD Pipelines]
        G[Cloud Platforms]
        H[Enterprise Servers]
    end
    
    subgraph "Distribution Channels"
        I[GitHub Releases]
        J[Maven Repository]
        K[Docker Images]
        L[Chrome Web Store]
    end
    
    A --> E
    A --> F
    B --> G
    B --> H
    C --> E
    C --> F
    D --> E
    
    A --> I
    B --> J
    C --> J
    B --> K
    D --> L
```

## Security Architecture

```mermaid
graph TD
    subgraph "Authentication & Authorization"
        A[API Token Management]
        B[OAuth Integration]
        C[Role-Based Access]
    end
    
    subgraph "Data Protection"
        D[Secret Encryption]
        E[Sensitive Data Masking]
        F[Secure Communication]
    end
    
    subgraph "Security Controls"
        G[Input Validation]
        H[Output Sanitization]
        I[Audit Logging]
        J[Rate Limiting]
    end
    
    A --> D
    B --> E
    C --> F
    
    D --> G
    E --> H
    F --> I
    
    G --> J
    H --> J
    I --> J
```

## Key Architectural Principles

### 1. **Modularity**
- Clear separation of concerns
- Pluggable architecture
- Independent deployable modules

### 2. **Extensibility**
- Interface-based design
- Plugin architecture
- Configuration-driven behavior

### 3. **Scalability**
- Asynchronous processing
- Caching strategies
- Resource optimization

### 4. **Reliability**
- Error handling and recovery
- Logging and monitoring
- Graceful degradation

### 5. **Security**
- Secure credential management
- Data encryption
- Audit trails

### 6. **Maintainability**
- Clean code principles
- Comprehensive testing
- Documentation standards

This architecture enables DMTools to serve as a comprehensive platform for software development lifecycle automation while maintaining flexibility and extensibility for future enhancements. 