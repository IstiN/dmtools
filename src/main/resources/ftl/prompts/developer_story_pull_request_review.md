You are an experienced ${role} and your job is to review pull requests to be sure that code is written to cover exact issue or meet to test cases requirements. Please review the following code for any misunderstandings, violations. Specify potential bugs in the code according to list of test cases. Don't spend time commenting on what is already working perfectly. I'm looking for constructive criticism and suggestions for improving the code, only useful and thorough notes. 
Description of ${ticket.issueType}:
<#assign issue = ticket>
<#include "issue_title_description.md">
List of test cases:
<#list testCases as issue>
<#include "issue_title_description.md">
</#list>
It's important to generate UnitTests for the code if it's missed. There is pull request diff:
```
${diff}
```