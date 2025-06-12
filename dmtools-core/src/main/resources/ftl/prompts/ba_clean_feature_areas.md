You're experienced documentation writer. You must check and clean up feature areas list which will be used in documentation structure.
You must follow the rules:
1. duplicates must be removed
2. synonyms must be removed
3. if areas both refer to the same concept, they must be combined to one, examples:
*  Plural and singular forms must be combined into the plural form. Example: 'Education Filter', 'Education Filters' - must be replaced with 'Education Filters'.
* 'UI', 'User Interface', 'UI Design', 'User Interface Design' - must be replaced as 'User Interface' and it's must be considered as same area name
4. 'Enhancements' must be skipped if it's already related to some existing feature area.
5. similar feature areas must be combined to one feature area if possible, try to do that as much as possible
7. Areas containing overlapping terms must be merged into a single area that covers the broader concept. Example: "Optimization" and "Refactoring and Optimization" - must be "Refactoring and Optimization"
8. Ensure the consolidated area names are consistent and comprehensive.
9. All area names must be unique
10. Acronyms and Full Forms: If an area name is an acronym and its full form is also listed, they should be combined. For example, ‘UI’ and ‘User Interface’ should be combined into ‘User Interface’.
11. Standardization of Terminology: Ensure that the terminology used is consistent. For example, if ‘User Interface’ is used in one area, avoid using ‘UI’ in another. Stick to one term for clarity.
12. Avoid Jargon: If possible, avoid using technical jargon in the area names.
14. Use of Active Voice: Try to use active voice when naming the areas. It’s more direct and easier to understand.
15. Capitalization Consistency: Ensure that the capitalization of all area names is consistent. For example, if you’re using title case, stick to it throughout.
16. Refrain from using code formatting or HTML markdowns in your response.

Your response must be:
* JSONArray. Example of AI Response: ["area1", "area2", ... , "areaN"]
* without html, body, head, title tags
* without wrapping ```
* exclude markdowns from response


Input feature areas: ${input}