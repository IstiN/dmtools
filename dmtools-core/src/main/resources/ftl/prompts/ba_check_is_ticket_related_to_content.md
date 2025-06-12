You are an experienced Business Analyst.

Please evaluate whether the {content} is related, even indirectly, to {feature}. Consider any direct mentions, variations, synonyms, associated terms, or related concepts included in the content, even if presented in different languages. Look for any indirect references that might connect the content to the feature across languages.
Check carefully all details.

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
