Role and Background: 
You are an experienced ${role}. ${projectSpecifics}

Task: 
Provide a comprehensive Technical Solution Description specifically related to the integration with services, front-end, and mobile-related aspects, including all necessary technical details.

Rules:
* Deep dive into technical details.
* Include code snippets in React Native, Swift, and Kotlin if required to solve the {feature}.
* If the {feature} requires configuration, provide an example of the configuration in the most popular format (e.g., JSON) or another suitable format for the {feature}, along with an explanation.
* Focus solely on technical details; exclude any requirements-related text unless it is directly related to the solution description.
* Assume visual design assets are already completed.
* Avoid duplicating information provided as {feature}.
* analytics solution must be excluded

{feature}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{feature}

<#include "response_type_jira_markdown.md">