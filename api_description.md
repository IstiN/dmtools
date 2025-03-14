# JSON API Documentation for `DevProductivityReportParams`

This document provides an example of how to use the `DevProductivityReportParams` class to configure and execute a developer productivity report. It also includes an example of calling the job using JSON parameters in the `JobRunner`.

---

## Example Usage of `DevProductivityReportParams`

```java
DevProductivityReportParams devProductivityReportParams = new DevProductivityReportParams();

// Set the start date for the report
devProductivityReportParams.set(DevProductivityReportParams.START_DATE, "01.09.2024");

// Set the calculation weight type (e.g., STORY_POINTS or TIME_SPENT)
devProductivityReportParams.set(DevProductivityReportParams.CALC_WEIGHT_TYPE, DevProductivityReportParams.CalcWeightType.STORY_POINTS.name());

// Set the JQL query for filtering tickets
devProductivityReportParams.set(DevProductivityReportParams.INPUT_JQL, JQL);

// Set the name of the report
devProductivityReportParams.set(DevProductivityReportParams.REPORT_NAME, "project_name");

// Enable or disable weighted calculations
devProductivityReportParams.set(DevProductivityReportParams.IS_WEIGHT, true);

// Define the sources for the report (e.g., Bitbucket, GitHub)
devProductivityReportParams.set(DevProductivityReportParams.SOURCES, new JSONArray()
        .put("bitbucket")
        .put("github")
);

// Define the statuses considered "ready for testing"
devProductivityReportParams.set(DevProductivityReportParams.STATUSES_READY_FOR_TESTING, new JSONArray()
        .put("ready for testing")
        .put("release")
        .put("review")
        .put("testing")
        .put("accepted")
        .put("completed")
);

// Set the regex pattern for identifying responsible users in comments
devProductivityReportParams.setCommentsRegexResponsible("Merge request by \\*([\\w\\s-]+)\\*");

// Set the initial status of tickets
devProductivityReportParams.set(DevProductivityReportParams.INITIAL_STATUS, "backlog");

// Define prefixes of tickets to ignore
devProductivityReportParams.set(DevProductivityReportParams.IGNORE_TICKET_PREFIXES, new JSONArray()
        .put("[VD]")
        .put("[QA]")
        .put("[PO]")
        .put("[BA]")
        .put("[SA]")
);

// Define statuses considered "in testing"
devProductivityReportParams.set(DevProductivityReportParams.STATUSES_IN_TESTING, new JSONArray()
        .put("testing")
);

// Define statuses considered "in development"
devProductivityReportParams.set(DevProductivityReportParams.STATUSES_IN_DEVELOPMENT, new JSONArray()
        .put("in progress")
        .put("ready for review")
);

// Set the formula file for calculating productivity
devProductivityReportParams.set(DevProductivityReportParams.FORMULA, "/formula/project_dev_productivity.js");

// Set the employee configuration file
devProductivityReportParams.set(QAProductivityReportParams.EMPLOYEES, "employees.json");
```

---

## Example JSON Configuration for `JobRunner`

The following JSON configuration can be used to execute the `DevProductivityReport` job via the `JobRunner`.

### JSON Example

```json
{
  "name": "DevProductivityReport",
  "params": {
    "start_date": "01.09.2024",
    "calc_weight_type": "STORY_POINTS",
    "input_jql": "project = PROJECT_NAME AND status != Closed",
    "report_name": "project_name",
    "is_weight": true,
    "sources": ["bitbucket", "github"],
    "statuses_ready_for_testing": ["ready for testing", "release", "review", "testing", "accepted", "completed"],
    "comment_regex_responsible": "Merge request by \\*([\\w\\s-]+)\\*",
    "initial_status": "backlog",
    "ignore_ticket_prefixes": ["[VD]", "[QA]", "[PO]", "[BA]", "[SA]"],
    "statuses_in_testing": ["testing"],
    "statuses_in_development": ["in progress", "ready for review"],
    "formula": "/formula/project_dev_productivity.js",
    "employees": "employees.json"
  }
}
```

---

## Example Employee Configuration File (`employees.json`)

This file defines the employees involved in the project, their roles, and their levels.

```json
[
  {
    "Employee": "John Doe",
    "Role": "Developer",
    "Level": "A3"
  },
  {
    "Employee": "Jane Smith",
    "Role": "Tester",
    "Level": "B2"
  },
  {
    "Employee": "Alice Johnson",
    "Role": "Business Analyst",
    "Level": "A4"
  }
]
```

---

## Example Employee Aliases File (`employees_aliases.json`)

This file maps employee names to their aliases used in various systems.

```json
{
  "John Doe": ["john-doe", "jdoe", "john.doe"],
  "Jane Smith": ["jane-smith", "jsmith", "jane.smith"],
  "Alice Johnson": ["alice-johnson", "ajohnson", "alice.j"]
}
```

---

## Example Formula File (`project_dev_productivity.js`)

This file contains the formula for calculating developer productivity.

```javascript
var storiesPointsFTR = ${.vars["Stories Moved To Testing FTR"]} * 1;
var bugsPointsFTR = ${.vars["Bugs Moved To Testing FTR"]} * 0.5;

// Calculate points for stories moved to testing
var storiesPoints = ${.vars["Stories Moved To Testing"]} * 1;
storiesPoints = storiesPoints - (storiesPoints - storiesPointsFTR) * 0.5;

// Calculate points for bugs moved to testing
var bugsPoints = ${.vars["Bugs Moved To Testing"]} * 0.5;
bugsPoints = bugsPoints - (bugsPoints - bugsPointsFTR) * 0.25;

// Calculate points for bugfixing time
var bugfixTimePoints = ${.vars["Bugfixing Time In Days"]} * 0.1;

// Calculate points for story development time
var storyDevTimePoints = ${.vars["Story Development Time In Days"]} * 0.2;

// Calculate points for vacation days
var vacationDaysPoints = ${.vars["Vacation days"]} * 1;

// Calculate points for pull requests
var pullRequestsPoints = ${.vars["Pull Requests"]?default(0)} * 0.2;
var pullRequestsLinesChanges = ${.vars["Lines Changes in Pull Requests"]?default(0)} * 0.2;

// Calculate points for pull requests comments
var pullRequestsCommentsPointsPositive = ${.vars["Pull Requests Comments Written"]?default(0)} * 0.1;
var pullRequestsCommentsPointsNegative = ${.vars["Pull Requests Comments Gotten"]?default(0)} * 0.15;

// Calculate points for pull requests approvals
var pullRequestsApprovalsPoints = ${.vars["Pull Requests Approvals"]?default(0)} * 0.3;

// Sum up all the calculated points
var totalPoints = storiesPoints + bugsPoints + bugfixTimePoints + storyDevTimePoints
    + vacationDaysPoints + pullRequestsPoints + pullRequestsCommentsPointsPositive - pullRequestsCommentsPointsNegative + pullRequestsApprovalsPoints + pullRequestsLinesChanges;

totalPoints;
```

---

## Running the Job with `JobRunner`

To execute the job, encode the JSON configuration into Base64 and pass it as an argument to the `JobRunner`.

### Example Command

```bash
java -cp your-jar-file.jar com.github.istin.dmtools.job.JobRunner $(echo -n '{"name":"DevProductivityReport","params":{...}}' | base64)
```

Replace `{...}` with the JSON configuration for the job.

---
