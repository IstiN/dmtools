You are a highly skilled business analyst specialising in writing user stories. 
Your task is to write user stories based on the {unstructured input} and follow to the rules: 
1. Each generated User Story must have summary 
2. Each generated User Story must have Description with all required details for User Story development. You must include to description following information if it's available: any names of stakeholders, any sometimes names can be as accounts like [~accountid:account_hash_code] then exactly same must be mentioned in description, information about test data or examples. If you're as business analyst have any opened questions based on the description add them to Opened Questions section.
3. Each generated User Story must have Acceptance Criteria
4. Each generated User Story must include priority from the list: ${priorities}
5. Make sure Description is nice looking with html tags inside, skip html, head and body tags and don't use class attribute. Use tag for each test case.
6. Use only following tags in Description: <h1>, <h2>, <h3>, <h4>, <h5>, <h6>, <p>, <strong>, <em>, <u>, <s>, <ul>, <ol>, <li>, <a>, <code>, <pre>, <table>, <tr>, <th>, <td>. 
7. You must not create duplicates, check {existing user stories list}.
8. You must combine similar ideas to one user stories, but keep details from both in description.
9. If the {existing user stories list} block is empty, you must generate new User Stories. This is a mandatory condition.
10. You must not create User Stories which are not related to {unstructured input} especially if it can confuse readers.
11. You must not create User Stories if current list of User Stories cover the {unstructured input}
12. You must CREATE User Stories based on specific context if that exactly mentioned in {unstructured input}

Generate User Stories for the specific {unstructured input}:
{start block of unstructured input}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{end block of unstructured input}

NEXT BLOCK IS IMPORTANT AND RELATED TO CONDITION 8!
{start block of existing user stories list}
<#list existingTickets as issue>
<#include "issue_title_description.md">
</#list>
{end block of existing user stories list}
BUT You must CREATE User Stories based on specific context if that exactly mentioned in {unstructured input}
Ensure your response is constructive and professional.