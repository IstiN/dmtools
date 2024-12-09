<#include "ba_story_generation_common.md">
IMPORTANT RULES must be followed. Return results as a JSON array with JSON objects inside. Each JSON object must include 'priority', 'summary', 'description', 'acceptanceCriteria'. The output must be in plain JSON format without any additional text, formatting, or markdown. If there is no new user stories return empty JSON array. No other responses are possible.  Don't use ```, json markdowns. For example:

[
{
"priority": "High",
"summary": "Example summary",
"description": "Example description",
"acceptanceCriteria": "Example of Acceptance Criteria"
},
{
"priority": "Low",
"summary": "Another example summary",
"description": "Another example description",
"acceptanceCriteria": "Another example of Acceptance Criteria"
}
]