# DMTools AI Agents

AI agents for automated test case generation and processing.

## 📁 Available Agents

### 1. Manual Test Cases Generator
**File**: `agents/xray_test_cases_generator.json`

Generates manual Test issues with step-by-step instructions.

**Usage**:
```bash
./dmtools.sh run agents/xray_test_cases_generator.json
```

**Features**:
- Manual test steps in Xray JSON format
- Precondition management
- Related test detection

### 2. Cucumber Test Cases Generator
**File**: `agents/xray_cucumber_test_generator.json`

Generates Cucumber tests with Gherkin scenarios and datasets for data-driven testing.

**Usage**:
```bash
./dmtools.sh run agents/xray_cucumber_test_generator.json
```

**Features**:
- Gherkin Scenario Outline generation
- Dataset with test data variations
- Parameter validation (Gherkin ↔ Dataset)
- Precondition management

**Documentation**: [docs/README_CUCUMBER_GENERATOR.md](docs/README_CUCUMBER_GENERATOR.md)

## 🚀 How to Run

### Basic Usage

Run an agent with its config file:

```bash
./dmtools.sh run agents/<agent-config>.json
```

Example:
```bash
./dmtools.sh run agents/xray_cucumber_test_generator.json
```

### Configuration

Each agent config contains:

```json
{
  "name": "TestCasesGenerator",  // Job name (same for all agents)
  "params": {
    "inputJql": "key in (TP-1309)",  // Stories to process
    "testCaseIssueType": "Test",
    "testCasesCustomFields": [...],   // Custom fields to generate
    "preprocessJSAction": "...",      // Optional JS preprocessor
    "examples": "jql(...)"            // AI learning examples
  }
}
```

## 📝 Agent Configuration Files

### Required Parameters

- `inputJql` - JQL query to select user stories/requirements to process
- `testCaseIssueType` - Issue type for created tests (usually "Test")
- `testCasesCustomFields` - Array of custom fields to generate

### Optional Parameters

- `preprocessJSAction` - Path to JavaScript preprocessor
- `examples` - JQL query to find example tests for AI learning
- `confluencePages` - Array of Confluence page URLs for context
- `relatedTestCasesRules` - URL to related test cases rules

### Custom Fields

**Manual Tests**:
- `xrayTestSteps` - JSON array of test steps
- `xrayPreconditions` - Array of precondition keys

**Cucumber Tests**:
- `xrayGherkin` - Gherkin scenario text
- `xrayDataset` - JSON object with parameters and rows
- `xrayPreconditions` - Array of precondition keys

## 🔧 Preprocessors

Preprocessors run before test creation to:
- Validate data structures
- Create precondition tickets
- Replace temporary IDs with real keys
- Transform data formats

### Available Preprocessors

- `agents/js/preprocessXrayTestCases.js` - For manual tests
- `agents/js/preprocessXrayCucumberTests.js` - For Cucumber tests

## 📚 Documentation

- **Cucumber Generator Guide**: [docs/CUCUMBER_TEST_GENERATOR_GUIDE.md](docs/CUCUMBER_TEST_GENERATOR_GUIDE.md)
- **Cucumber Quick Start**: [docs/README_CUCUMBER_GENERATOR.md](docs/README_CUCUMBER_GENERATOR.md)
- **Dataset Implementation**: [../dmtools-core/XRAY_DATASET_IMPLEMENTATION.md](../dmtools-core/XRAY_DATASET_IMPLEMENTATION.md)

## 💡 Tips

1. **AI Examples**: Create test cases with specific labels (e.g., `ai_example`, `ai_cucumber_example`) to improve AI generation quality
2. **Incremental Testing**: Start with a small JQL query (e.g., `key = TP-123`) before processing many stories
3. **Review First**: Check generated tests in Jira before executing them
4. **Custom Fields**: Adjust `testCasesCustomFields` based on your Xray setup

## ⚠️ Requirements

- dmtools 1.7.105+
- Xray for Jira Cloud
- Configured credentials in `dmtools.env`:
  - Jira credentials
  - Xray API credentials
  - AI provider credentials (OpenAI, Anthropic, etc.)

## 🎯 Workflow

1. **Configure**: Edit agent config file (`inputJql`, custom fields, examples)
2. **Run**: Execute `./dmtools.sh run agents/<agent-config>.json`
3. **Review**: Check generated tests in Jira
4. **Iterate**: Adjust config and re-run if needed

---

**Need help?** Check the documentation in [docs/](docs/) folder or open an issue.
