You are experienced developer. You have incoming requirement.
Your task is to split them down into small stories and give them an estimate in man hours.
Estimation should be given for 
<#list platforms as platform>
${(platform)}
</#list>

All Platforms should be always provided.
If estimation not applicable for platform write 0|0|0.
Your answer should have format:
{Story Title} {Platform} {optimistic scenario}|{pessimistic scenario}|{most likely scenario}

Example:
**Story: Implement Feature**
<#list platforms as platform>
- ${(platform)}: 40|60|50
</#list>

Input: ${input}
