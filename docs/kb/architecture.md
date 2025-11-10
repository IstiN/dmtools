# Knowledge Base Architecture

## Overview

The DMTools Knowledge Base (KB) system transforms unstructured data (chat messages, documents, transcripts) into a structured, searchable knowledge repository. It uses AI to extract semantic information and organize it into interconnected entities.

## High-Level Architecture

```mermaid
graph TB
    subgraph "Input Sources"
        A[Chat Messages]
        B[Documents]
        C[Transcripts]
        D[Other Text Data]
    end
    
    subgraph "KB Processing Pipeline"
        E[KBOrchestrator]
        F[AI Analysis]
        G[Structure Building]
        H[AI Aggregation]
        I[Statistics Generation]
    end
    
    subgraph "Output Structure"
        J[Questions]
        K[Answers]
        L[Notes]
        M[Topics]
        N[People Profiles]
        O[Areas]
    end
    
    A --> E
    B --> E
    C --> E
    D --> E
    
    E --> F
    F --> G
    G --> H
    H --> I
    
    I --> J
    I --> K
    I --> L
    I --> M
    I --> N
    I --> O
    
    style E fill:#4CAF50,color:#fff
    style F fill:#2196F3,color:#fff
    style G fill:#FF9800,color:#fff
    style H fill:#2196F3,color:#fff
    style I fill:#FF9800,color:#fff
```

## Processing Modes

The KB system supports three processing modes for different use cases:

```mermaid
graph LR
    subgraph "FULL Mode"
        A1[Input] --> A2[AI Analysis]
        A2 --> A3[Structure Building]
        A3 --> A4[AI Aggregation]
        A4 --> A5[Statistics]
    end
    
    subgraph "PROCESS_ONLY Mode"
        B1[Input] --> B2[AI Analysis]
        B2 --> B3[Structure Building]
        B3 --> B4[Statistics]
        B4 -.Skip.-> B5[AI Aggregation]
    end
    
    subgraph "AGGREGATE_ONLY Mode"
        C1[Existing KB] --> C2[AI Aggregation]
        C2 --> C3[Statistics]
        C3 -.Skip.-> C4[Analysis]
    end
    
    style A2 fill:#2196F3,color:#fff
    style A4 fill:#2196F3,color:#fff
    style B2 fill:#2196F3,color:#fff
    style B5 fill:#ccc,color:#666
    style C2 fill:#2196F3,color:#fff
    style C4 fill:#ccc,color:#666
```

### Mode Descriptions

- **FULL**: Complete processing with AI analysis and aggregation (default)
- **PROCESS_ONLY**: Fast mode - structure only, no AI descriptions (for bulk data)
- **AGGREGATE_ONLY**: Generate AI descriptions for existing KB structure

## Core Components

```mermaid
graph TB
    subgraph "Orchestration Layer"
        A[KBOrchestrator]
    end
    
    subgraph "AI Agents"
        B[KBAnalysisAgent]
        C[KBAggregationAgent]
        D[KBQuestionAnswerMappingAgent]
    end
    
    subgraph "Utility Services"
        E[KBStructureManager]
        F[KBContextLoader]
        G[KBFileParser]
        H[PersonStatsCollector]
        I[KBSourceCleaner]
    end
    
    subgraph "Core Services"
        J[KBStructureBuilder]
        K[KBStatistics]
        L[SourceConfigManager]
    end
    
    A --> B
    A --> C
    A --> D
    A --> E
    A --> F
    
    E --> G
    E --> H
    E --> J
    
    F --> G
    A --> I
    I --> E
    
    E --> K
    A --> L
    
    style A fill:#4CAF50,color:#fff,stroke:#2E7D32,stroke-width:3px
    style B fill:#2196F3,color:#fff
    style C fill:#2196F3,color:#fff
    style D fill:#2196F3,color:#fff
```

## Data Flow

### 1. Input Processing

```mermaid
sequenceDiagram
    participant User
    participant Orchestrator
    participant FileReader
    participant ChunkPrep
    participant AnalysisAgent
    
    User->>Orchestrator: Submit Input File
    Orchestrator->>FileReader: Read & Normalize
    FileReader-->>Orchestrator: Normalized Content
    Orchestrator->>ChunkPrep: Prepare Chunks
    ChunkPrep-->>Orchestrator: Text Chunks
    
    loop For Each Chunk
        Orchestrator->>AnalysisAgent: Analyze Chunk
        AnalysisAgent-->>Orchestrator: Questions, Answers, Notes
    end
    
    Orchestrator->>Orchestrator: Merge Results
```

### 2. Structure Building

```mermaid
sequenceDiagram
    participant Orchestrator
    participant StructureManager
    participant IdMapper
    participant StructureBuilder
    participant FileSystem
    
    Orchestrator->>StructureManager: Build Structure
    StructureManager->>IdMapper: Map Temporary IDs
    IdMapper-->>StructureManager: Permanent IDs
    
    StructureManager->>StructureBuilder: Create Answer Files
    StructureBuilder->>FileSystem: Write answers/*.md
    
    StructureManager->>StructureBuilder: Create Question Files
    StructureBuilder->>FileSystem: Write questions/*.md
    
    StructureManager->>StructureBuilder: Create Note Files
    StructureBuilder->>FileSystem: Write notes/*.md
    
    StructureManager->>StructureBuilder: Create Topic Files
    StructureBuilder->>FileSystem: Write topics/*.md
    
    StructureManager->>StructureBuilder: Create People Profiles
    StructureBuilder->>FileSystem: Write people/*.md
```

### 3. AI Aggregation

```mermaid
sequenceDiagram
    participant Orchestrator
    participant BatchHelper
    participant AggHelper
    participant AggAgent
    participant FileSystem
    
    Orchestrator->>BatchHelper: Aggregate Batch
    
    loop For Each Person
        BatchHelper->>AggHelper: Aggregate Person
        AggHelper->>FileSystem: Read Contributions
        AggHelper->>AggAgent: Generate Description
        AggAgent-->>AggHelper: AI Description
        AggHelper->>FileSystem: Update Profile
    end
    
    loop For Each Topic
        BatchHelper->>AggHelper: Aggregate Topic
        AggHelper->>FileSystem: Read Q/A/N
        AggHelper->>AggAgent: Generate Description
        AggAgent-->>AggHelper: AI Description
        AggHelper->>FileSystem: Update Topic
    end
```

## Source Cleanup Architecture

```mermaid
graph TB
    subgraph "Source Cleanup Flow"
        A[cleanSourceBeforeProcessing=true]
        B[KBSourceCleaner]
        C[Scan Q/A/N Files]
        D{Match Source?}
        E[Delete File]
        F[Keep File]
        G[Rebuild Person Profiles]
        H[Regenerate Statistics]
    end
    
    A --> B
    B --> C
    C --> D
    D -->|Yes| E
    D -->|No| F
    E --> G
    F --> G
    G --> H
    
    style A fill:#FF5722,color:#fff
    style B fill:#FF9800,color:#fff
    style E fill:#F44336,color:#fff
    style F fill:#4CAF50,color:#fff
    style G fill:#2196F3,color:#fff
    style H fill:#2196F3,color:#fff
```

### Multi-Source Safety

```mermaid
graph LR
    subgraph "Before Cleanup"
        A1[Source A: Q1, A1, N1]
        B1[Source B: Q2, A2, N2]
        C1[Source C: Q3, A3, N3]
    end
    
    subgraph "Clean Source A"
        A2[Source A: DELETE]
        B2[Source B: KEEP]
        C2[Source C: KEEP]
    end
    
    subgraph "After Cleanup"
        A3[Source A: Q4, A4, N4]
        B3[Source B: Q2, A2, N2]
        C3[Source C: Q3, A3, N3]
    end
    
    A1 --> A2
    B1 --> B2
    C1 --> C2
    
    A2 --> A3
    B2 --> B3
    C2 --> C3
    
    style A2 fill:#F44336,color:#fff
    style B2 fill:#4CAF50,color:#fff
    style C2 fill:#4CAF50,color:#fff
```

## Output Structure

```mermaid
graph TB
    subgraph "KB Directory Structure"
        A[kb_output/]
        
        B[questions/]
        C[answers/]
        D[notes/]
        E[topics/]
        F[people/]
        G[areas/]
        H[stats/]
        I[inbox/]
        
        B1[q_0001.md]
        B2[q_0002.md]
        
        C1[a_0001.md]
        C2[a_0002.md]
        
        D1[n_0001.md]
        
        E1[deployment.md]
        E2[deployment-desc.md]
        
        F1[Alice_Brown.md]
        F2[Bob_Smith.md]
        
        G1[technology.md]
        
        H1[kb_statistics.json]
        
        I1[raw/]
        I2[analyzed/]
    end
    
    A --> B
    A --> C
    A --> D
    A --> E
    A --> F
    A --> G
    A --> H
    A --> I
    
    B --> B1
    B --> B2
    C --> C1
    C --> C2
    D --> D1
    E --> E1
    E --> E2
    F --> F1
    F --> F2
    G --> G1
    H --> H1
    I --> I1
    I --> I2
    
    style A fill:#4CAF50,color:#fff,stroke:#2E7D32,stroke-width:3px
```

## Entity Relationships

```mermaid
erDiagram
    QUESTION ||--o{ ANSWER : "answered-by"
    QUESTION }o--|| PERSON : "asked-by"
    ANSWER }o--|| PERSON : "answered-by"
    NOTE }o--|| PERSON : "authored-by"
    
    QUESTION }o--o{ TOPIC : "tagged-with"
    ANSWER }o--o{ TOPIC : "tagged-with"
    NOTE }o--o{ TOPIC : "tagged-with"
    
    TOPIC }o--|| AREA : "belongs-to"
    
    PERSON ||--o{ CONTRIBUTION : "has"
    CONTRIBUTION }o--|| TOPIC : "in"
    
    QUESTION {
        string id
        string text
        string author
        string area
        list topics
        list tags
        string date
        string answeredBy
    }
    
    ANSWER {
        string id
        string text
        string author
        string area
        list topics
        list tags
        string date
        double quality
        string answersQuestion
    }
    
    NOTE {
        string id
        string text
        string author
        string area
        list topics
        list tags
        string date
    }
    
    TOPIC {
        string id
        string title
        int questions
        int answers
        int notes
        list contributors
    }
    
    PERSON {
        string name
        int questionsAsked
        int answersProvided
        int notesCreated
        map contributions
    }
```

## Incremental Updates

```mermaid
graph TB
    subgraph "First Build"
        A1[Input Batch 1]
        B1[Process]
        C1[KB State 1]
    end
    
    subgraph "Second Build"
        A2[Input Batch 2]
        B2[Load Context]
        C2[Process]
        D2[Merge]
        E2[KB State 2]
    end
    
    subgraph "Third Build"
        A3[Input Batch 3]
        B3[Load Context]
        C3[Process]
        D3[Merge]
        E3[KB State 3]
    end
    
    A1 --> B1
    B1 --> C1
    
    C1 --> B2
    A2 --> C2
    B2 --> C2
    C2 --> D2
    D2 --> E2
    
    E2 --> B3
    A3 --> C3
    B3 --> C3
    C3 --> D3
    D3 --> E3
    
    style C1 fill:#4CAF50,color:#fff
    style E2 fill:#4CAF50,color:#fff
    style E3 fill:#4CAF50,color:#fff
```

## Statistics & Indexing

```mermaid
graph TB
    subgraph "Statistics Generation"
        A[Scan All Files]
        B[Count by Type]
        C[Count by Topic]
        D[Count by Person]
        E[Generate JSON]
        F[Create Indexes]
    end
    
    A --> B
    A --> C
    A --> D
    B --> E
    C --> E
    D --> E
    E --> F
    
    F --> G[kb_statistics.json]
    F --> H[people/index.md]
    F --> I[topics/index.md]
    F --> J[areas/index.md]
    
    style E fill:#2196F3,color:#fff
    style F fill:#FF9800,color:#fff
```

## Key Design Principles

### 1. **Separation of Concerns**
- **Orchestration**: `KBOrchestrator` coordinates workflow
- **AI Processing**: Agents handle AI interactions
- **Structure Management**: Utilities handle file operations
- **Statistics**: Separate service for metrics

### 2. **Incremental Processing**
- Load existing context before processing
- Merge new data with existing KB
- Preserve historical data
- Support partial updates

### 3. **Source Isolation**
- Each source tracked independently
- Clean individual sources without affecting others
- Automatic statistics recalculation
- Safe multi-source environments

### 4. **Error Recovery**
- Rollback on failures
- Track created files
- Clean up partial results
- Preserve KB integrity

### 5. **Scalability**
- Chunk large inputs
- Parallel processing where possible
- Efficient file scanning
- Optimized ID mapping

## Performance Considerations

```mermaid
graph LR
    subgraph "Optimization Strategies"
        A[Pre-compiled Regex]
        B[HashMap Lookups]
        C[Parallel Streams]
        D[Lazy Loading]
        E[Batch Processing]
    end
    
    A --> F[Fast Parsing]
    B --> G[Fast ID Mapping]
    C --> H[Fast File Scanning]
    D --> I[Memory Efficiency]
    E --> J[Reduced AI Calls]
    
    style F fill:#4CAF50,color:#fff
    style G fill:#4CAF50,color:#fff
    style H fill:#4CAF50,color:#fff
    style I fill:#4CAF50,color:#fff
    style J fill:#4CAF50,color:#fff
```

## Extension Points

The architecture supports extension through:

1. **Custom AI Agents**: Implement new analysis or aggregation logic
2. **Additional Entity Types**: Extend beyond Q/A/N
3. **Custom Statistics**: Add new metrics and reports
4. **Integration Hooks**: Connect to external systems
5. **Custom Validators**: Add domain-specific validation rules

## Technology Stack

- **Language**: Java 17+
- **AI Integration**: Dial (Claude), Gemini
- **Dependency Injection**: Dagger 2
- **File Format**: Markdown with YAML frontmatter
- **Serialization**: Gson
- **Testing**: JUnit 5, Mockito

