# SIMPLIFIED LLM OPTIMIZED FORMAT\n\n## IMPROVEMENTS:\n\n1. **Next** (no colon) instead of Next: or ObjectKeys: [...]\n2. **_** and **_** instead of [ ] for multiline\n3. **Arrays in [ ] brackets**: each element on new line\n4. **Object arrays with [ Next keys** format\n\n## INPUT JSON:\n\n```json\n{
  "key": "DMC-427",
  "summary": "Performance optimization with new format",
  "description": "This is a multiline description\nwith line breaks\nto test [ ] markers",
  "simpleArray": ["performance", "optimization", "critical"],
  "multilineArray": ["short", "This is a long\nmultiline string\nwith breaks", "simple"],
  "teams": [
    {
      "name": "Frontend Team",
      "members": [
        {"name": "John Doe", "skills": ["React", "TypeScript"]},
        {"name": "Jane Wilson", "skills": ["Figma", "CSS"]}
      ]
    }
  ],
  "priority": "High"
}\n```\n\n## OUTPUT FORMAT:\n\n```\nNext summary,simpleArray,multilineArray,teams,description,priority,key
Performance optimization with new format
[
performance
optimization
critical
]
[
short
_
This is a long
multiline string
with breaks
_
simple
]
[Next members,name
0
[Next skills,name
0
[
React
TypeScript
]
John Doe
1
[
Figma
CSS
]
Jane Wilson
]
Frontend Team
]
_
This is a multiline description
with line breaks
to test [ ] markers
_
High
DMC-427
\n```\n\n## STATISTICS:\n\n- Input size: 557 characters\n- Output size: 418 characters\n- Compression: 25.0%\n- Next headers: 1\n- Underscore pairs: 2\n