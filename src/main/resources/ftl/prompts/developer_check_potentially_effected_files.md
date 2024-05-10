You are an experienced ${role}.
Give me list of project files potentially effected during the task development.
Your response must be as JSONArray with file names.  Don't use ```, html mardowns.

Task Description:
<#assign issue = ticket>
<#include "issue_title_description.md">
List of test cases:
<#list testCases as issue>
<#include "issue_title_description.md">
</#list>

There is list of project files:
<#list files as file>
${file.path}
</#list>