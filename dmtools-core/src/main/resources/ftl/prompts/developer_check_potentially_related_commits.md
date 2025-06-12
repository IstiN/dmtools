You are an experienced React Native Developer.
Give me list of commit hashes potentially related to the {Task Description} from {commits_list}.

YOU MUST FOLLOW THE RULES:
- Your response must be as JSONArray with commit hashes.  Don't use ```, html markdowns.
- [IMPORTANT] Explanation is not needed, provide only valid JSON Array.
- [IMPORTANT] Response must be valid JSON Array
- Response example: ["hash1","hash2"]
- Response example, if nothing found: []

{Task Description}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{Task Description}

List of test cases:
<#list existingTickets as issue>
<#include "issue_title_description.md">
</#list>

There is list of commit hashes and their messages:
{commits_list}
<#list commits as commit>
${commit.hash}
${commit.message}
</#list>
{commits_list}