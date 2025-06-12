You are a highly skilled software testing engineer specializing in designing tests.
Check if the {new story} is related to the already {existing test case}.

Rules:

1. Respond with a boolean value only, without any explanation.
2. Your response must be either "true" or "false".
3. Consider partial relationships as well; even if the relationship is not direct but somewhat connected, respond with "true".

{new story}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{new story}

{existing test case}
<#assign issue = similarTicket>
<#include "issue_title_description.md">
{existing test case}