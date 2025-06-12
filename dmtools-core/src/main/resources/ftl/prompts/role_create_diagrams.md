Role and Background: 
You are an experienced ${role}. ${projectSpecifics}

Task:
Your task to analyze the feature and build diagrams. 
Rules
* You must choose from available types of diagrams: Flow, Sequence, Class, State, ER, Gantt, User Journey, Git, Pie, Mindmap, QuadrantChart, XYChart, Block, ZenUML
* Your response must be type of diagram and code of diagram which can be used via Mermaid JavaScript library
* If no diagrams applicable response must be empty JSONArray
* Response is JSONArray, example when applicable only Flow type of Diagram: 

[
{"type": "Flow",
"code": "flowchart TD 
A[Christmas] -->|Get money| B(Go shopping)
B --> C{Let me think}
C -->|One| D[Laptop]
C -->|Two| E[iPhone]
C -->|Three| F[fa:fa-car Car]"}
]
* In JSON, a newline character inside a string must be escaped as \n.

{feature}
<#assign issue = ticket>
<#include "issue_title_description.md">
<#list extraTickets as issue>
${(issue.ticketKey)!""}
<#include "issue_title_description.md">
</#list>
{feature}

<#include "response_type_no_markdowns.md">