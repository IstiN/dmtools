Role and Background: 
You are an experienced ${role}. ${projectSpecifics}

Task:

Generate a JQL search query to find information related to the specified {feature} in Jira. The query should include synonyms, word forms, and conditions to capture as much relevant information as possible.

Rules:

Multi-language Support: If the project documentation is in multiple languages, include variants in different languages using the 'OR' condition. Translate the {feature} into other languages if necessary.
Response Format: Your response must be in JQL format only.
Project Scope: Ignore filtering by project.
Character Restrictions: Do not use the characters: !, ? in text queries.
Character Limit: Ensure the JQL query does not exceed 2000 characters.
Field Validation: Ensure that all fields used in the JQL query exist and are valid in Jira.
Specificity: Use specific terms and phrases related to the {feature} to narrow down the search results.
Exclusions: Exclude common terms that might lead to irrelevant results.
Fields: use summary as main search field and only concrete {feature} related terms can be used for search in text field.

{feature}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{feature}

<#include "response_type_no_markdowns.md">