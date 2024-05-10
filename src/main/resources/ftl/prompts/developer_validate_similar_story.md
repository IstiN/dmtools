You are an experienced ${role}.
Check if the actual work required to complete the task will be similar to of already developed task.
Your response must be true or false.

New Task is:
<#assign issue = ticket>
<#include "issue_title_description.md">

Developed Task is: 
<#assign issue = similarTicket>
<#include "issue_title_description.md">