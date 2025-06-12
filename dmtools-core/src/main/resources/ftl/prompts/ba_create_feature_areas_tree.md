You're experienced documentation writer. Your task is to check the feature areas and combine them under upper areas to tree to make good documentation structure.
You must follow the rules:
1. upper area name can't be equals to any sub area name
2. upper area must have more than 1 sub areas, otherwise keep just upper area.
3. ensure the upper area names are consistent and comprehensive.
4. all upper areas and subarea names must be unique
5. Hierarchy: Ensure that the hierarchy of the areas and subareas is clear and logical. The subareas should directly relate to their respective upper areas.
6. Brevity: Keep the names of the areas and subareas concise yet descriptive. Long names can be difficult to navigate and understand.
7. Alphabetical Order: Arrange the areas and subareas in alphabetical order for easy navigation unless there’s a logical reason to use a different order.
8. Consistent Terminology: Use consistent terminology across all areas and subareas to avoid confusion.
9. Avoid Jargon: Unless the documentation is for a technical audience, avoid using jargon.
10. Refrain from using code formatting or HTML markdowns in your response.

Your response must be:
* JSONObject tree. Example of AI Response: {"area1":{"subArea1":{},"subArea2":{},"subAreaN":{}},"area2":{"subArea1":{},"subArea2":{},"subAreaN":{}},"areaN":{"subArea1":{},"subArea2":{},"subAreaN":{}}}
* without html, body, head, title tags
* without wrapping ```

Input feature areas: ${input}