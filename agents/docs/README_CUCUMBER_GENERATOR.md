# Xray Cucumber Test Generator - Quick Start

## 🚀 Quick Start

Generate Cucumber tests with datasets from user stories:

```bash
./dmtools.sh run agents/xray_cucumber_test_generator.json
```

The agent will process stories defined in the config's `inputJql` parameter.

## 📋 What Gets Generated

For each user story, the AI creates Test issues with:

1. **Gherkin Scenario** - BDD-style test scenario with parameters
2. **Dataset** - Test data with multiple variations
3. **Preconditions** - Optional setup requirements

## ✨ Example Output

**Input**: User story "Login functionality"

**Output**: Test issue with:

**Gherkin**:
```gherkin
Scenario Outline: User login
  Given user "<username>" exists
  When password "<password>" is entered
  Then result is "<result>"

  Examples:
    | username | password | result  |
    | admin    | pass123  | success |
    | user     | wrong    | failed  |
```

**Dataset**:
```json
{
  "parameters": [
    {"name": "username", "type": "text"},
    {"name": "password", "type": "text"},
    {"name": "result", "type": "text"}
  ],
  "rows": [
    {"order": 0, "Values": ["admin", "pass123", "success"]},
    {"order": 1, "Values": ["user", "wrong", "failed"]}
  ]
}
```

## 🎯 Key Features

- ✅ Automatic Gherkin scenario generation
- ✅ Dataset with test data variations
- ✅ Parameter validation (Gherkin ↔ Dataset)
- ✅ Precondition handling (create and link)
- ✅ Positive, negative, and edge cases

## 📁 Files

- **Config**: `agents/xray_cucumber_test_generator.json`
- **Preprocessor**: `agents/js/preprocessXrayCucumberTests.js`
- **Guide**: `agents/docs/CUCUMBER_TEST_GENERATOR_GUIDE.md`

## 🔧 Configuration

Edit `agents/xray_cucumber_test_generator.json`:

```json
{
  "name": "TestCasesGenerator",
  "params": {
    "inputJql": "key in (TP-1309)",
    "testCaseIssueType": "Test",
    "existingTestCasesJql": "project = TP",
    "examples": "jql(labels = ai_cucumber_example)"
  }
}
```

## 🎓 Examples

See full examples in [CUCUMBER_TEST_GENERATOR_GUIDE.md](./CUCUMBER_TEST_GENERATOR_GUIDE.md):
- API Testing
- Form Validation  
- Payment Processing
- And more...

## 🧪 Manual Test Creation

Create a single Cucumber test:

```bash
./dmtools.sh jira_xray_create_test \
  --project TP \
  --summary "Login Test" \
  --gherkin "Scenario Outline: Login..." \
  --dataset '{"parameters":[...], "rows":[...]}'
```

Get test with dataset:

```bash
./dmtools.sh jira_xray_get_test_details --key TP-1436
```

## 📚 Documentation

- **Full Guide**: [CUCUMBER_TEST_GENERATOR_GUIDE.md](./CUCUMBER_TEST_GENERATOR_GUIDE.md)
- **Implementation**: [../dmtools-core/XRAY_DATASET_IMPLEMENTATION.md](../dmtools-core/XRAY_DATASET_IMPLEMENTATION.md)

## 💡 Tips

1. **AI Examples**: Create tests with label `ai_cucumber_example` to improve AI generation
2. **Validation**: Preprocessor validates Gherkin/Dataset consistency
3. **Test Data**: Include 3-7 data rows for good coverage
4. **Parameters**: Use descriptive names (not param1, param2)
5. **Job Name**: The agent uses `"name": "TestCasesGenerator"` as it's processing with an agent

## ⚠️ Requirements

- dmtools 1.7.105+
- Xray for Jira Cloud
- Configured Xray credentials in `dmtools.env`

---

**Need help?** Check the [full guide](./CUCUMBER_TEST_GENERATOR_GUIDE.md) or [implementation docs](../dmtools-core/XRAY_DATASET_IMPLEMENTATION.md)
