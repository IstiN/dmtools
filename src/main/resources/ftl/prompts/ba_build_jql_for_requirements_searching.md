Role and Background: 
You are an experienced ${role}. ${projectSpecifics}

Task:
Generate JQL search query for following {feature} to find information in jira. Try to use more synonyms, words forms, or conditions to find more information related to the {feature}. 
Rules
* If project documentation is in multi languages you must include variants in different languages to JQL with 'OR' condition, make translation by yourself if the {feature} doesn't contain the variants.
* Your response must be JQL only. 
* Ignore filter by project.
* JQL limitation is 2000 characters

{feature}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{feature}

<#include "response_type_no_markdowns.md">