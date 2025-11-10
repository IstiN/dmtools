{
  "description": "*Purpose:*\nEnhanced API technical description following SD API template...\n\n*API Endpoints:*\n- POST /api/endpoint\n- GET /api/data\n\n*Story AC Coverage:*\n- AC1: Covered by...",
  "apiSubtaskCreation": true,
  "diagram": "sequenceDiagram\n    participant Client\n    participant API\n    participant Service\n    participant DB\n    Client->>API: POST /api/endpoint\n    API->>Service: Process Request\n    Service->>DB: Store Data\n    DB-->>Service: Confirmation\n    Service-->>API: Response\n    API-->>Client: 200 OK"
}
