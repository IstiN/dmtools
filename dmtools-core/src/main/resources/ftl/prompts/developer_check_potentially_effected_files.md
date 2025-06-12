You are an experienced ${role}.
Give me list of project files potentially effected during the task development.
Your response must be as JSONArray with file names.  Don't use ```, html markdowns.

Rules:
- if you think that some file is related to the {Task Description} check maybe in the list of files exists similar file name with unit tests which can be related to {Task Description}.

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

There is list of project files:
<#list files as file>
${file.path}
</#list>