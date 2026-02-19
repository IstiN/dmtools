# DMTools Core - Project Overview

## Purpose
DMTools is an AI-powered development toolkit with 96+ MCP tools for Jira, Azure DevOps, Figma, Confluence, Teams, and test automation.

## Tech Stack
- Java (multi-module Gradle project)
- Spring Boot (server module)
- Dagger 2 (dependency injection)
- Lombok (@Data, @NoArgsConstructor, @AllArgsConstructor, etc.)
- JUnit 4/5 + Mockito for testing
- GraalJS for JavaScript agent execution
- org.json.JSONObject (not Jackson/Gson for runtime JSON)
- Gson for configuration deserialization

## Module Structure
- `dmtools-core`: Core library (main module)
- `dmtools-server`: Spring Boot REST API
- `dmtools-automation`: Browser/mobile automation
- `dmtools-mcp-annotations`: SOURCE retention annotations
- `dmtools-annotation-processor`: Compile-time code generator

## Key Commands
- Build: `./gradlew :dmtools-core:shadowJar`
- Test: `./gradlew :dmtools-core:test`
- Specific test: `./gradlew :dmtools-core:test --tests "ClassName.methodName"`
- Compile only: `./gradlew :dmtools-core:compileJava`
- Run CLI: `./dmtools.sh <command>`
- Install local: `./buildInstallLocal.sh`
