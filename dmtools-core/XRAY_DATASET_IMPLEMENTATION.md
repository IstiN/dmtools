# Xray Dataset Support Implementation

## 🎉 Summary

Successfully implemented full support for Xray Cucumber tests with datasets in `dmtools`.

### Implemented Features

#### 1. **Dataset Reading** ✅
- GraphQL API returns dataset with parameters and rows
- `jira_xray_get_test_details` now includes `xrayDataset` field
- Works for all existing Cucumber tests with datasets

#### 2. **Dataset Creation/Update** ✅
- Automatic dataset creation when creating Cucumber tests
- Uses Xray Internal API with proper authentication
- Transforms GraphQL format to Internal API format

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     XrayClient                              │
│  createTicketInProject() → setDataset()                     │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│                  XrayRestClient                             │
│  updateDatasetInternalAPI()                                 │
│    ├─ getXacptTokenFromJiraPage()                          │
│    ├─ getTestVersionId()                                   │
│    ├─ GET /api/internal/paramDataset (existing structure)  │
│    ├─ transformDatasetForInternalAPI()                     │
│    └─ PUT /api/internal/paramDataset (update)              │
└─────────────────────────────────────────────────────────────┘
```

### API Flow

#### Authentication
1. **X-acpt Token** - JWT extracted from Jira page HTML
   - Location: `contextJwt` in SSR JavaScript block
   - Regex: `"contextJwt"\s*:\s*"([^"]+)"`
   - Valid for ~15 minutes

2. **testVersionId** - Xray MongoDB ObjectId
   - Endpoint: `POST /api/internal/tests/versions`
   - Body: `{"issueIds":["<jiraIssueId>"], "includeArchived":true, "includeTestType":true}`
   - Returns: `{"<jiraIssueId>": [{"testVersionId": "<mongoId>", ...}]}`

#### Dataset Operations

**GET Existing Dataset:**
```
GET /api/internal/paramDataset?testIssueId=<jiraIssueId>
Headers: X-acpt: <jwt>, X-addon-key: com.xpandit.plugins.xray
```

**PUT Dataset:**
```
PUT /api/internal/paramDataset?testIssueId=<jiraIssueId>&testVersionId=<mongoId>
Headers: X-acpt: <jwt>, X-addon-key: com.xpandit.plugins.xray
Body: {
  "dataset": {
    "parameters": [
      {
        "_id": "<uuid>",  // UUID for new, ObjectId for existing
        "name": "username",
        "type": "text",
        "combinations": false,
        "listValues": [],
        "isNew": true  // Only for new parameters
      }
    ],
    "callTestIssueId": ""
  },
  "datasetRows": [
    {
      "values": {
        "<param1._id>": "value1",
        "<param2._id>": "value2"
      },
      "order": 0,
      "combinatorialParameterId": null
    }
  ],
  "iterationsCount": 2
}
```

### Data Transformation

**GraphQL Format → Internal API Format:**

| GraphQL | Internal API |
|---------|-------------|
| `parameters[].name` | `parameters[].name` ✓ |
| `parameters[].type` | `parameters[].type` ✓ |
| - | `parameters[]._id` (UUID or existing) |
| - | `parameters[].isNew: true` (for new) |
| - | `parameters[].combinations: false` |
| - | `parameters[].listValues: []` |
| `rows[].Values` (array) | `datasetRows[].values` (object) |
| `rows[].order` | `datasetRows[].order` ✓ |

### Usage Examples

#### CLI
```bash
# Get test with dataset
./dmtools.sh jira_xray_get_test_details --key TP-1436

# Create Cucumber test with dataset
./dmtools.sh jira_xray_create_test \
  --project TP \
  --summary "Login Test" \
  --description "Data-driven login test" \
  --gherkin "Scenario Outline: Login\n  Given user \"<username>\"\n  When password \"<password>\"\n  Then result \"<result>\"\n\n  Examples:\n    | username | password | result |\n    | admin | pass | success |" \
  --dataset '{"parameters":[{"name":"username","type":"text"},{"name":"password","type":"text"},{"name":"result","type":"text"}],"rows":[{"order":0,"Values":["admin","pass","success"]}]}'
```

#### Java API
```java
// Create Cucumber test with dataset
xrayClient.createTicketInProject(
    "TP",
    "Test",
    "Login Test",
    "Data-driven login test",
    fields -> {
        fields.set("gherkin", gherkinScenario);
        fields.set("dataset", datasetJson);
    }
);

// Read test with dataset
JSONObject testDetails = xrayClient.getTestDetailsGraphQL("TP-1436");
JSONObject dataset = testDetails.getJSONObject("dataset");
```

### Integration Tests

✅ **XrayClientIntegrationTest.testCreateTestWithGherkinAndDataset**
- Creates Cucumber test with 3 parameters, 3 rows
- Verifies dataset via GraphQL after creation

✅ **XrayDatasetCreateTest**
- Creates Cucumber test from scratch with dataset
- Validates parameter count and structure

✅ **XrayDatasetGetTest**
- Tests GET request to Internal API
- Verifies response structure with _id fields

### Known Limitations

1. **Xray GraphQL Limitation**: Cannot update datasets via GraphQL mutations
   - Solution: Use Internal API

2. **Authentication Requirement**: Requires Jira session + X-acpt token
   - Solution: Extract contextJwt from HTML SSR data

3. **Dataset IDs**: Internal API requires MongoDB ObjectIds
   - Solution: Generate UUIDs for new parameters, preserve existing IDs

### Files Modified

- `XrayRestClient.java`:
  - `getXacptTokenFromJiraPage()` - Extract JWT from HTML
  - `getTestVersionId()` - Get Xray MongoDB ObjectId
  - `updateDatasetInternalAPI()` - Main dataset update logic
  - `transformDatasetForInternalAPI()` - Format transformation
  - Updated GraphQL queries to include `dataset` field

- `XrayClient.java`:
  - `setDataset()` - High-level dataset setter
  - Updated `createTicketInProject()` to handle dataset field
  - Updated `enrichTicketsWithXrayData()` to include `xrayDataset`

- Integration Tests:
  - `XrayClientIntegrationTest.testCreateTestWithGherkinAndDataset()`
  - `XrayDatasetCreateTest`
  - `XrayDatasetGetTest`
  - `XrayDatasetPreparationTest` (diagnostic)
  - `XrayDatasetFullFlowTest` (diagnostic)

### Performance Notes

- **X-acpt token extraction**: ~1 second (HTML fetch + regex)
- **testVersionId lookup**: ~300ms (Internal API call)
- **Dataset PUT request**: ~500ms (Internal API call)
- **Total overhead**: ~2 seconds per dataset operation

### Future Improvements

1. Cache X-acpt tokens (valid ~15 minutes)
2. Batch dataset operations for multiple tests
3. Support for combinatorial parameters
4. Dataset templates/presets

---

**Status**: ✅ Production Ready  
**Version**: dmtools 1.7.105  
**Date**: January 6, 2026

