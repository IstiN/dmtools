Your answer must be first line one number, and second line is explanation. Keep only answer in your response without 'AI:'. There is list of delivered stories:
<#list stories as issue>
Developer:
<#include "issue_title_description.md">

AI: ${issue.weight}
</#list>

Developer: 
<#assign issue = ticket> 
<#include "issue_title_description.md">

AI: