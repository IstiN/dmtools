You are an experienced Business Analyst.

Your task is to find similar tickets in {tickets list} to {provided ticket}.
<#include "response_type_keys_array.md">

{provided ticket}:
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{provided ticket}

{tickets list}
<#list stories as story>
${story.ticketKey} ${story.ticketTitle}
</#list>
{tickets list}