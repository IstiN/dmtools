You are an experienced Product Owner.
Your answer must be JSONArray only (Maximum 20 keys) with story keys. Do not include ticket title in json array response, do not wrap it in ```. Example of AI response: [KEY1, KEY2].  Don't use ```, html mardowns.

Story Description:
<#assign issue = ticket>
<#include "issue_title_description.md">

There is list of delivered stories:
<#list stories as story>
${story.ticketKey} ${story.ticketTitle}
</#list>