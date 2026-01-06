# Xray Cucumber Test Generator - Guide

## Overview

The Cucumber Test Generator (`xray_cucumber_test_generator.json`) is an AI agent that automatically generates data-driven Cucumber tests with Gherkin scenarios and datasets for Xray.

## Configuration Files

- **Agent Config**: `agents/xray_cucumber_test_generator.json`
- **Preprocessor**: `agents/js/preprocessXrayCucumberTests.js`

## How It Works

1. **Input**: Provide a JQL query to select user stories/requirements
2. **AI Generation**: AI generates Cucumber scenarios with:
   - Gherkin syntax (Scenario Outline with Examples)
   - Dataset JSON (parameters + test data rows)
   - Preconditions (if needed)
3. **Preprocessing**: 
   - Validates Gherkin/Dataset consistency
   - Creates precondition tickets
   - Replaces temporary IDs
4. **Creation**: Creates Test issues in Jira with Xray custom fields

## Usage

```bash
./dmtools.sh run agents/xray_cucumber_test_generator.json
```

The JQL query is configured in the `inputJql` parameter of the agent config file.

## Generated Fields

### 1. Gherkin (xrayGherkin)

Example:
```gherkin
Scenario Outline: User login with different credentials
  Given user "<username>" is registered in the system
  When user enters username "<username>" and password "<password>"
  And user clicks the login button
  Then login result should be "<result>"
  And user should see message "<message>"

  Examples:
    | username | password  | result  | message           |
    | admin    | admin123  | success | Welcome back!     |
    | user     | wrongpass | failed  | Invalid password  |
    | guest    | guest456  | success | Welcome, Guest!   |
```

### 2. Dataset (xrayDataset)

JSON structure:
```json
{
  "parameters": [
    {"name": "username", "type": "text"},
    {"name": "password", "type": "text"},
    {"name": "result", "type": "text"},
    {"name": "message", "type": "text"}
  ],
  "rows": [
    {
      "order": 0,
      "Values": ["admin", "admin123", "success", "Welcome back!"]
    },
    {
      "order": 1,
      "Values": ["user", "wrongpass", "failed", "Invalid password"]
    },
    {
      "order": 2,
      "Values": ["guest", "guest456", "success", "Welcome, Guest!"]
    }
  ]
}
```

### 3. Preconditions (xrayPreconditions)

Array of precondition keys:
```json
["TP-910", "TP-911"]
```

Or with temporary IDs for new preconditions:
```json
[
  "TP-910",
  {
    "key": "@precondition-1",
    "summary": "Test database is initialized",
    "description": "System has test data",
    "steps": [
      {
        "action": "Start database",
        "data": "PostgreSQL",
        "result": "DB is running"
      }
    ]
  }
]
```

## Example Scenarios

### Example 1: API Testing

**User Story**: "As a developer, I want to test REST API endpoints with different HTTP methods"

**Generated Test**:

Gherkin:
```gherkin
Scenario Outline: REST API endpoint testing
  Given API endpoint "<endpoint>" is available
  When client sends "<method>" request with body "<request_body>"
  Then response status code should be "<status_code>"
  And response body should contain "<expected_field>"

  Examples:
    | endpoint      | method | request_body        | status_code | expected_field |
    | /api/users    | GET    |                     | 200         | users          |
    | /api/users    | POST   | {"name":"John"}     | 201         | id             |
    | /api/users/1  | PUT    | {"name":"Jane"}     | 200         | updated        |
    | /api/users/1  | DELETE |                     | 204         |                |
    | /api/invalid  | GET    |                     | 404         | error          |
```

Dataset:
```json
{
  "parameters": [
    {"name": "endpoint", "type": "text"},
    {"name": "method", "type": "text"},
    {"name": "request_body", "type": "text"},
    {"name": "status_code", "type": "text"},
    {"name": "expected_field", "type": "text"}
  ],
  "rows": [
    {"order": 0, "Values": ["/api/users", "GET", "", "200", "users"]},
    {"order": 1, "Values": ["/api/users", "POST", "{\"name\":\"John\"}", "201", "id"]},
    {"order": 2, "Values": ["/api/users/1", "PUT", "{\"name\":\"Jane\"}", "200", "updated"]},
    {"order": 3, "Values": ["/api/users/1", "DELETE", "", "204", ""]},
    {"order": 4, "Values": ["/api/invalid", "GET", "", "404", "error"]}
  ]
}
```

### Example 2: Form Validation

**User Story**: "As a user, I want form validation to work with different input types"

**Generated Test**:

Gherkin:
```gherkin
Scenario Outline: Registration form validation
  Given user is on registration page
  When user enters email "<email>"
  And user enters password "<password>"
  And user enters age "<age>"
  And user submits the form
  Then validation result should be "<result>"
  And error message should be "<error_message>"

  Examples:
    | email              | password   | age | result  | error_message              |
    | valid@example.com  | Pass123!   | 25  | success |                            |
    | invalid-email      | Pass123!   | 25  | failed  | Invalid email format       |
    | valid@example.com  | short      | 25  | failed  | Password too short         |
    | valid@example.com  | Pass123!   | 15  | failed  | Age must be 18 or older    |
    | valid@example.com  | Pass123!   | abc | failed  | Age must be a number       |
```

Dataset:
```json
{
  "parameters": [
    {"name": "email", "type": "text"},
    {"name": "password", "type": "text"},
    {"name": "age", "type": "text"},
    {"name": "result", "type": "text"},
    {"name": "error_message", "type": "text"}
  ],
  "rows": [
    {"order": 0, "Values": ["valid@example.com", "Pass123!", "25", "success", ""]},
    {"order": 1, "Values": ["invalid-email", "Pass123!", "25", "failed", "Invalid email format"]},
    {"order": 2, "Values": ["valid@example.com", "short", "25", "failed", "Password too short"]},
    {"order": 3, "Values": ["valid@example.com", "Pass123!", "15", "failed", "Age must be 18 or older"]},
    {"order": 4, "Values": ["valid@example.com", "Pass123!", "abc", "failed", "Age must be a number"]}
  ]
}
```

### Example 3: Payment Processing

**User Story**: "As a user, I want to process payments with different payment methods"

**Generated Test**:

Gherkin:
```gherkin
Scenario Outline: Payment processing with various methods
  Given user has cart with total amount "$<amount>"
  When user selects payment method "<payment_method>"
  And user enters card number "<card_number>"
  And user confirms payment
  Then payment status should be "<status>"
  And transaction fee should be "$<fee>"

  Examples:
    | amount | payment_method | card_number      | status   | fee  |
    | 100.00 | credit_card    | 4111111111111111 | approved | 3.00 |
    | 50.00  | debit_card     | 5555555555554444 | approved | 1.50 |
    | 200.00 | paypal         | -                | approved | 6.00 |
    | 10.00  | credit_card    | 0000000000000000 | declined | 0.00 |
    | 1000.00| credit_card    | 4111111111111111 | pending  | 30.00|
```

Dataset:
```json
{
  "parameters": [
    {"name": "amount", "type": "text"},
    {"name": "payment_method", "type": "text"},
    {"name": "card_number", "type": "text"},
    {"name": "status", "type": "text"},
    {"name": "fee", "type": "text"}
  ],
  "rows": [
    {"order": 0, "Values": ["100.00", "credit_card", "4111111111111111", "approved", "3.00"]},
    {"order": 1, "Values": ["50.00", "debit_card", "5555555555554444", "approved", "1.50"]},
    {"order": 2, "Values": ["200.00", "paypal", "-", "approved", "6.00"]},
    {"order": 3, "Values": ["10.00", "credit_card", "0000000000000000", "declined", "0.00"]},
    {"order": 4, "Values": ["1000.00", "credit_card", "4111111111111111", "pending", "30.00"]}
  ]
}
```

## Best Practices

### 1. Parameter Naming
- Use snake_case: `user_name`, `expected_result`
- Be descriptive: `login_status` not `status`
- Match business terminology

### 2. Test Data Coverage
- **Happy Path**: Valid data, successful flow
- **Negative Cases**: Invalid data, error handling
- **Edge Cases**: Boundary values, special characters
- **Combinations**: Different feature combinations

### 3. Gherkin Steps
- **Given**: Set up preconditions
- **When**: Perform actions
- **Then**: Assert expectations
- **And/But**: Additional steps

### 4. Dataset Size
- Minimum: 2-3 rows (one positive, one negative)
- Recommended: 5-7 rows (comprehensive coverage)
- Maximum: 10-15 rows (avoid too many variations)

### 5. Parameter Types
- `text`: Default, for strings
- `list`: Dropdown values
- `number`: Numeric values

## Validation

The preprocessor validates:

1. **Parameter Consistency**: All Gherkin `<parameters>` must exist in Dataset
2. **Row Structure**: Each row must have `order` and `Values` array
3. **Value Count**: `Values` length must match parameters count
4. **JSON Format**: Dataset must be valid JSON

## Troubleshooting

### Issue: "Gherkin parameter not found in dataset"
**Solution**: Ensure all `<param>` in Gherkin exist in `dataset.parameters[].name`

### Issue: "Row has X values but Y parameters defined"
**Solution**: Check that each row's `Values` array has exactly the same number of elements as `parameters`

### Issue: "Failed to parse dataset JSON"
**Solution**: Validate JSON syntax with a JSON validator

### Issue: "Precondition creation failed"
**Solution**: Check project permissions and Xray configuration

## CLI Commands

### Create Example Test Manually
```bash
./dmtools.sh jira_xray_create_test \
  --project TP \
  --summary "Login Test - Cucumber" \
  --description "Data-driven login testing" \
  --gherkin "Scenario Outline: Login\n  Given user \"<username>\"\n  When password \"<password>\"\n  Then result \"<result>\"\n\n  Examples:\n    | username | password | result |\n    | admin | pass | success |" \
  --dataset '{"parameters":[{"name":"username","type":"text"},{"name":"password","type":"text"},{"name":"result","type":"text"}],"rows":[{"order":0,"Values":["admin","pass","success"]}]}'
```

### Get Test with Dataset
```bash
./dmtools.sh jira_xray_get_test_details --key TP-1436
```

### List Cucumber Tests
```bash
./dmtools.sh jira_search \
  --jql "project = TP AND issuetype = Test AND labels = cucumber"
```

## Integration with CI/CD

Execute generated tests in your pipeline:

```yaml
# .github/workflows/cucumber-tests.yml
- name: Get Xray Tests
  run: |
    ./dmtools.sh jira_xray_get_tests \
      --jql "project = TP AND labels = automated" \
      --output tests.json

- name: Run Cucumber
  run: |
    cucumber --format json --out results.json

- name: Report to Xray
  run: |
    ./dmtools.sh jira_xray_import_results \
      --results results.json \
      --test-plan TP-100
```

---

**Version**: dmtools 1.7.105  
**Date**: January 6, 2026  
**Status**: ✅ Ready for Production
