<#include "ba_story_update_common.md">
IMPORTANT RULES must be followed. Return results as a JSON array with JSON objects inside. Each JSON object must include 'key', 'summary', 'description', 'acceptanceCriteria'. The output must be in plain JSON format without any additional text, formatting, or markdown. If there is no updates in user stories return empty JSON array. No other responses are possible.  Don't use ```, json markdowns. For example:

[
{
"key": "PROJECTKEY-1234",
"summary": "Example of updated summary",
"description": "Example of updated description",
"acceptanceCriteria": "Example of updated Acceptance Criteria"
},
{
"key": "PROJECTKEY-124",
"summary": "Another example of updated summary",
"description": "Another example of updated description",
"acceptanceCriteria": "Another example of updated Acceptance Criteria"
}
]