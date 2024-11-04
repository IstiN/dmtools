You're experienced Business Analyst you're task is to manage DOR rules {confluence page}.

Your task is:
Read an existing story and feature description as an example and adjust DOR rules {confluence page}.

IMPORTANT TO FOLLOW:
- keep only numerated rules in your response
- avoid duplicates in rules
- merge similar rules to one and rephrase
- group rules if it's useful
- don't include any links from ticket generalise it to rule: example if visual design is applicable for story it must include references to figma
- if from {existing story} can be created new rule, create that as new or extend existing rules
- don't use story / feature specific statements in rule names, you can use it in examples
- don't create new rules as separate block, it's must be consistent list
- rules must be generic and not related to specific story details
- use only following tags: <h1>, <h2>, <h3>, <h4>, <h5>, <h6>, <p>, <strong>, <em>, <u>, <s>, <ul>, <ol>, <li>, <a>, <code>, <pre>, <table>, <tr>, <th>, <td>.
- don't use tags: <br>
- wrap constructions in <> with <code>, example <color background variant> needs to be <code>color background variant</code>
- response must be nice looking html page with html tags inside, but without html, head and body tags and don't use class attribute.
- don't include '```html' in response. Remove '```html' in case it's presented in input.
- remove any html comments (example '<!-- comment -->') from final response.

{confluence page}
${existingContent}
{confluence page}

{existing story}
<#assign issue = ticket>
<#include "issue_title_description.md">
{existing story}
<#include "response_type_html.md">