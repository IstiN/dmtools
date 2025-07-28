# JiraClient MCP Tools Integration Tests

This directory contains comprehensive integration tests for all MCP tools methods in the JiraClient class.

## Overview

The `JiraClientMcpToolsIntegrationTest` class tests all 29 MCP-annotated methods in the JiraClient, ensuring they work correctly with a real Jira instance.

## Test Coverage

The integration tests cover all MCP tools methods:

### Data Extraction
- `jira_get_ticket_browse_url` - Generate ticket browse URLs

### Search
- `jira_search_with_pagination` - Search with pagination support
- `jira_search_and_perform` - Search and perform actions

### User Management
- `jira_get_my_profile` - Get current user profile
- `jira_get_user_profile` - Get specific user profile

### Ticket Management
- `jira_get_ticket` - Get specific ticket
- `jira_get_subtasks` - Get subtasks
- `jira_create_ticket_basic` - Create basic ticket
- `jira_create_ticket_with_json` - Create ticket with JSON
- `jira_create_ticket_with_parent` - Create ticket with parent
- `jira_create_epic_or_find` - Create or find epic
- `jira_get_issues_in_epic_by_type` - Get issues in epic by type
- `jira_update_description` - Update ticket description
- `jira_update_field` - Update specific field
- `jira_update_ticket_parent` - Update ticket parent
- `jira_get_transitions` - Get available transitions
- `jira_move_to_status` - Move to specific status
- `jira_move_to_status_with_resolution` - Move with resolution
- `jira_clear_field` - Clear field value
- `jira_set_fix_version` - Set fix version
- `jira_add_fix_version` - Add fix version
- `jira_set_priority` - Set priority
- `jira_remove_fix_version` - Remove fix version

### Comment Management
- `jira_post_comment` - Post comment
- `jira_post_comment_if_not_exists` - Post comment if not exists
- `jira_get_comments` - Get comments

### Project Management
- `jira_get_fix_versions` - Get fix versions
- `jira_get_components` - Get components
- `jira_get_project_statuses` - Get project statuses
- `jira_get_fields` - Get available fields
- `jira_get_field_custom_code` - Get field custom code

### Issue Linking
- `jira_get_issue_link_types` - Get link types
- `jira_link_issues` - Link issues

### API Operations
- `jira_execute_request` - Execute custom request

## Prerequisites

1. **Jira Instance**: Access to a Jira instance (Cloud or Server)
2. **Authentication**: Valid authentication token
3. **Project Access**: Access to a test project
4. **Permissions**: Sufficient permissions to create/update tickets

## Configuration

### Environment Variables
```bash
export JIRA_AUTH_TOKEN="your-auth-token-here"
```

### System Properties
```bash
-Djira.base.url=https://your-instance.atlassian.net
-Djira.auth.token=your-auth-token-here
-Djira.test.project=YOUR_PROJECT_KEY
```

### Properties File
The test uses `jira-integration-test.properties` for configuration.

## Running the Tests

### Run All Tests
```bash
./gradlew :dmtools-core:integrationTest
```

### Run Specific Test
```bash
./gradlew :dmtools-core:integrationTest --tests JiraClientMcpToolsIntegrationTest.testCreateTicketBasic
```

### Run with Custom Configuration
```bash
./gradlew :dmtools-core:integrationTest \
  -Djira.base.url=https://your-instance.atlassian.net \
  -Djira.auth.token=your-token \
  -Djira.test.project=YOUR_PROJECT
```

## Test Execution Order

Tests are executed in a specific order using `@Order` annotations:

1. Setup and basic functionality tests
2. Ticket creation tests
3. Ticket update tests
4. Complex operations tests
5. Cleanup

## Test Data Management

- Tests create temporary tickets for testing
- Test data is cleaned up automatically where possible
- Each test is independent and can be run individually

## Expected Behavior

- All tests should pass with valid Jira credentials
- Some tests may be skipped if certain features are not available (e.g., no fix versions)
- Tests handle both success and failure scenarios gracefully

## Troubleshooting

### Common Issues

1. **Authentication Errors**: Ensure your auth token is valid and has sufficient permissions
2. **Project Access**: Verify you have access to the test project
3. **Rate Limiting**: Tests may fail if Jira rate limits are exceeded
4. **Network Issues**: Ensure stable network connection to Jira

### Debug Mode

Enable debug logging:
```bash
-Dlogging.level.com.github.istin.dmtools.atlassian.jira=DEBUG
```

## Contributing

When adding new MCP tools to JiraClient:

1. Add corresponding test method to this class
2. Follow the existing naming convention: `test[MethodName]`
3. Use appropriate `@Order` annotation
4. Add proper assertions and error handling
5. Update this README with new test coverage

## Notes

- Tests are designed to be non-destructive where possible
- Some tests may create test data that persists in Jira
- Consider running tests in a dedicated test project
- Tests may take several minutes to complete due to Jira API calls 