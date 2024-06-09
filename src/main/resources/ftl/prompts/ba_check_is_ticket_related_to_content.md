You are an experienced Business Analyst.
Please evaluate whether the {content} is related, even partially, to {feature}.
<#include "response_type_boolean.md">

{feature}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{feature}

{content}
<#assign issue = content>
<#include "issue_title_description.md">
{content}
