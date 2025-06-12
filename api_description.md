# DMTools Job Parameters API

## Overview

This document describes the job parameter format used by DMTools for executing various reports and automation tasks. For complete API documentation and interactive testing, use the Swagger UI.

## Quick Access

**For complete API documentation, schemas, and interactive testing:**

👉 **Visit Swagger UI: `http://localhost:8080/swagger-ui.html`**

## Job Execution Format

DMTools jobs are executed using JSON parameters in the following format:

```json
{
  "name": "JobName",
  "params": {
    "parameter1": "value1",
    "parameter2": "value2",
    ...
  }
}
```

## Common Job Types

### Developer Productivity Report
Generate comprehensive developer productivity analytics and reports.

### QA Productivity Report
Analyze quality assurance team performance and metrics.

### Presentation Generation
Create automated presentations from data sources.

### Business Analysis Reports
Generate business intelligence and analytical reports.

## Execution Methods

### 1. Direct Job Execution
Use the `/api/executeJob` endpoint with JSON parameters.

### 2. Base64 Encoded Parameters
For command-line execution, encode JSON parameters as Base64:

```bash
java -cp dmtools.jar com.github.istin.dmtools.job.UrlEncodedJobTrigger "$BASE64_ENCODED_PARAMS"
```

### 3. Web Interface
Use the DMTools web interface for interactive job configuration and execution.

## Parameter Configuration

### Common Parameters
- `start_date` - Report start date (format: dd.mm.yyyy)
- `report_name` - Name of the generated report
- `input_jql` - JIRA JQL query for data filtering
- `sources` - Data sources (e.g., "bitbucket", "github", "jira")

### Configuration Files
- `employees.json` - Employee information and roles
- `employees_aliases.json` - Employee alias mappings
- Formula files (`.js`) - Custom calculation formulas

## Getting Started

1. **Start the server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Access API documentation**:
   Visit `http://localhost:8080/swagger-ui.html`

3. **Test job execution**:
   Use the interactive Swagger interface to test job parameters

## Example Configuration Files

### Employee Configuration (`employees.json`)
```json
[
  {
    "Employee": "John Doe",
    "Role": "Developer",
    "Level": "A3"
  }
]
```

### Employee Aliases (`employees_aliases.json`)
```json
{
  "John Doe": ["john-doe", "jdoe", "john.doe"]
}
```

## Support

For detailed parameter schemas, validation rules, and interactive testing:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **GitHub Issues**: [DMTools Issues](https://github.com/IstiN/dmtools/issues)
- **Main Documentation**: [README.md](README.md) - Setup and configuration
