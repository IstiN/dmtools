# DiagramCreator Setup

This document provides instructions on how to set up and use the DiagramCreator job.

## Prerequisites

- Java 23
- Gradle
- Access to Jira and an AI service

## Configuration

1. **Job Configuration**: The DiagramCreator job is configured via `dmtools-server/src/main/resources/jobs/diagramcreator.json`.
2. **Parameters**: 
   - `storiesJql`: JQL query to select tickets for diagram generation (required).
   - `labelNameToMarkAsReviewed`: Label to add after diagram generation (optional).
   - `roleSpecific`: Role-specific context for diagram generation (optional).
   - `projectSpecific`: Project-specific context for diagrams (optional).

## Usage

1. **API Access**: The DiagramCreator job can be accessed via the following API endpoints:
   - `GET /api/v1/jobs/types`: Lists available job types, including "AI Diagram Generator".
   - `POST /api/v1/jobs`: Create a new job configuration.
   - `GET /api/v1/jobs/{id}`: Retrieve a specific job configuration.
   - `PUT /api/v1/jobs/{id}`: Update a job configuration.
   - `DELETE /api/v1/jobs/{id}`: Delete a job configuration.
   - `POST /api/v1/jobs/execute`: Execute a job.

2. **Execution**: 
   - The job executes asynchronously in `SERVER_MANAGED` mode. 
   - Upon execution, an immediate response with an execution ID is returned.
   - Parameter overrides can be provided during execution.

## Example

To generate a diagram for stories in the 'DMC' project:

curl -X POST https://your-dmtools-url/api/v1/jobs/execute \
     -H "Content-Type: application/json" \
     -d '{
           "jobType": "DiagramCreator", 
           "configParams": {
             "storiesJql": "project = DMC AND type = Story",
             "labelNameToMarkAsReviewed": "diagrams-generated"
           }
         }'
         }
```
