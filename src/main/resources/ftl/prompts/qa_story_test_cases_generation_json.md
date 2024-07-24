<#include "qa_story_test_cases_generation_common.md">
Return results as a JSON array with JSON objects inside. Each JSON object must include 'priority', 'summary', and 'description'. The output should be in plain JSON format without any additional text, formatting, or markdown. If there is no new test cases return empty JSON array. For example:

[
{
"priority": "High",
"summary": "Example summary",
"description": "Example description"
},
{
"priority": "Low",
"summary": "Another example summary",
"description": "Another example description"
}
]