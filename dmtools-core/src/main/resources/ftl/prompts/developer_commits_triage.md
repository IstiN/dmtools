You are an experienced ${global.role}.

{Issue Description Starts}
<#assign issue = args.ticket>
<#include "issue_title_description.md">
<#list args.ticket.extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{Issue Description Ends}

Your task is to make assessment of git commit and answer true if it's potentially related to the issue or false if it's not related to the issue. 
Response example1: True, Explanation why you think it's True and exact code snippet with potential bug from the diff. 
Response example2: False, Explanation why you think it's False. 

Diff is:

${args.diff}